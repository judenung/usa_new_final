package com.example.application.frontends;

import com.example.application.backends.*;
import com.example.application.tools.*;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jsoup.helper.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "support-settlement", layout = BuildView.class)
@PageTitle("Support Settlement | PFSAT")
public class SupportSettlementView extends VerticalLayout {

    private VerticalLayout supportSettlementForm;
    private Grid<SupportSettlement> supportSettlementGrid;
    private GraphView graphView;

    private ComboBox<String> SP;
    private BigDecimalField Sx;
    private BigDecimalField Sy;
    private BigDecimalField Sz;
    private Button ADD;

    private ComboBox<String> SP_e;
    private BigDecimalField Sx_e;
    private BigDecimalField Sy_e;
    private BigDecimalField Sz_e;

    private InputFrame IF;
    private Unit U;

    private ArrayList<String> SPs;

    public SupportSettlementView (@Autowired MyUI UI) {
        addClassName("support-settlement-view");
        setSizeFull();

        IF = UI.getInputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        U = UI.getUnit();

        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();

        configureForm(UI);
        configureGrid(UI);
        configureGraph(UI);

        SplitLayout S1 = new SplitLayout(supportSettlementForm, supportSettlementGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        supportSettlementForm = new VerticalLayout();
        supportSettlementForm.addClassNames("support-settlement-form");

        SP = new ComboBox<>("Support Coordinates");
        Sx = new BigDecimalField("Horizontal Settlement");
        Sy = new BigDecimalField("Vertical Settlement");
        Sz = new BigDecimalField("Rotational Settlement");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        SPs = new ArrayList<>();
        for (Support sp : IF.supports) {
            SPs.add("(" + sp.Cx.toPlainString() + " , " + sp.Cy.toPlainString() + ")");
        }
        SP.setItems(SPs);
        SP.addValueChangeListener(event -> {
            IF.markedSupports.clear();
            IF.markedJoints.clear();
            IF.markedSupportsSettlements.clear();
            if (SP.getValue() != null) {
                IF.markedSupports.add(SPs.indexOf(SP.getValue()));
                Support sp = IF.supports.get(SPs.indexOf(SP.getValue()));
                for (Joint j : IF.joints) {
                    if (sp.Cx.compareTo(j.Cx) == 0 && sp.Cy.compareTo(j.Cy) == 0) {
                        IF.markedJoints.add(IF.joints.indexOf(j));
                        break;
                    }
                }
            }
            graphView.refreshGraph(UI);
        });
        SP.addValueChangeListener(event -> {
            if (SP.getValue() != null) {
                Support sp = IF.supports.get(SPs.indexOf(SP.getValue()));
                if (sp.ST == 1 || sp.ST == 2 || sp.ST == 5) {
                    Sx.setEnabled(true);
                }
                else {
                    Sx.setEnabled(false);
                    Sx.clear();
                }
                if (sp.ST == 1 || sp.ST == 2 || sp.ST == 4) {
                    Sy.setEnabled(true);
                }
                else {
                    Sy.setEnabled(false);
                    Sy.clear();
                }
                if (sp.ST == 1) {
                    Sz.setEnabled(true);
                }
                else {
                    Sz.setEnabled(false);
                    Sz.clear();
                }
            }
            else {
                Sx.setEnabled(false);
                Sy.setEnabled(false);
                Sz.setEnabled(false);
                Sx.clear();
                Sy.clear();
                Sz.clear();
            }
        });

        Sx.setEnabled(false);
        Sy.setEnabled(false);
        Sz.setEnabled(false);

        InputFrame.trimMinMax(Sx, -1000000, 1000000, 2);
        InputFrame.trimMinMax(Sy, -1000000, 1000000, 2);
        InputFrame.trimAngle(Sz, 360, 2);

        SP.setWidthFull();
        Sx.setWidthFull();
        Sy.setWidthFull();
        Sz.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();
        Div h3 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h3.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h3.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        SP.setHelperText("(X , Y)");
        Sx.setHelperComponent(h1);
        Sy.setHelperComponent(h2);
        Sz.setHelperComponent(h3);

        Div s1 = new Div();
        Div s2 = new Div();
        Div s3 = new Div();

        s1.setText(U.mm);
        s2.setText(U.mm);
        s3.setText(U.deg);

        Sx.setSuffixComponent(s1);
        Sy.setSuffixComponent(s2);
        Sz.setSuffixComponent(s3);

        Sx.setPlaceholder("0.00");
        Sy.setPlaceholder("0.00");
        Sz.setPlaceholder("0.00");

        supportSettlementForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(SP, Sx, Sy, Sz);
        v.setSpacing(false);
        supportSettlementForm.add(v, ADD);
        supportSettlementForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal Sx_ = Sx.getValue() == null ? BigDecimal.ZERO : U.convertSL1(Sx.getValue());
        BigDecimal Sy_ = Sy.getValue() == null ? BigDecimal.ZERO : U.convertSL1(Sy.getValue());
        BigDecimal Sz_ = Sz.getValue() == null ? BigDecimal.ZERO : U.convertRad1(Sz.getValue());

        if (IF.supportSettlements.size() == InputFrame.supportSettlementMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.supportSettlementMaxItem + ".");
        }
        else if (SP.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Support Coordinates\n\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Sx_) == 0 && BigDecimal.ZERO.compareTo(Sy_) == 0 && BigDecimal.ZERO.compareTo(Sz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No support settlement was input.");
        }
        else {
            BigDecimal Cx = IF.supports.get(SPs.indexOf(SP.getValue())).Cx;
            BigDecimal Cy = IF.supports.get(SPs.indexOf(SP.getValue())).Cy;

            boolean add = true;
            for (SupportSettlement ss : IF.supportSettlements) {
                if (ss.Cx.compareTo(Cx) == 0 && ss.Cy.compareTo(Cy) == 0) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The support settlement overlaps with existing support settlement.");
            }
            else {
                try {
                    IF.addSupportSettlement(Cx, Cy, Sx_, Sy_, Sz_);

                    Sx.clear();
                    Sy.clear();
                    Sz.clear();
                    supportSettlementGrid.getDataProvider().refreshAll();
                    graphView.refreshGraph(UI);
                }
                catch (ValidationException e) {
                    ErrorNotification.displayNotification("System Error");
                }
            }
        }
    }

    private void configureGrid (MyUI UI) {
        supportSettlementGrid = new Grid<>(SupportSettlement.class, false);
        List<SupportSettlement> supportSettlementList = IF.supportSettlements;

        supportSettlementGrid.addClassName("support-settlement-grid");
        supportSettlementGrid.addColumn(JointLoad -> supportSettlementList.indexOf(JointLoad) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        supportSettlementGrid.addColumn(SupportSettlement -> SupportSettlement.Cx.toPlainString()).setHeader("X (" + U.m + ")");
        supportSettlementGrid.addColumn(SupportSettlement -> SupportSettlement.Cy.toPlainString()).setHeader("Y (" + U.m + ")");
        supportSettlementGrid.addColumn(SupportSettlement -> U.round(U.convertSL2(SupportSettlement.settlement[0])).toPlainString()).setHeader("Sx (" + U.mm + ")");
        supportSettlementGrid.addColumn(SupportSettlement -> U.round(U.convertSL2(SupportSettlement.settlement[1])).toPlainString()).setHeader("Sy (" + U.mm + ")");
        supportSettlementGrid.addColumn(SupportSettlement -> U.round(U.convertRad2(SupportSettlement.settlement[2])).toPlainString()).setHeader("Sz (" + U.deg + ")");

        supportSettlementGrid.addColumn(new ComponentRenderer<>(Button::new, (button, SupportSettlement) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(SupportSettlement, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        supportSettlementGrid.addColumn(new ComponentRenderer<>(Button::new, (button, SupportSettlement) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(SupportSettlement, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        supportSettlementGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        supportSettlementGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        supportSettlementGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        supportSettlementGrid.setItems(supportSettlementList);

        supportSettlementGrid.addSelectionListener(selection -> {
            Optional<SupportSettlement> optionalSS = selection.getFirstSelectedItem();
            if (optionalSS.isPresent()) {
                IF.markedSupportsSettlements.clear();
                IF.markedSupportsSettlements.add(IF.supportSettlements.indexOf(optionalSS.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedSupportsSettlements.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (SupportSettlement SS, MyUI UI) {
        IF.removeSupportSettlement(SS);

        supportSettlementGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
    }

    private void edit (SupportSettlement SS, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Support Settlement");

        dialog.add(configureEditForm (SS, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, SS, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (SupportSettlement SS, MyUI UI) {
        SP_e = new ComboBox<>("Support Coordinates");
        Sx_e = new BigDecimalField("Horizontal Settlement");
        Sy_e = new BigDecimalField("Vertical Settlement");
        Sz_e = new BigDecimalField("Rotational Settlement");

        SPs = new ArrayList<>();
        for (Support sp : IF.supports) {
            SPs.add("(" + sp.Cx.toPlainString() + " , " + sp.Cy.toPlainString() + ")");
        }
        SP_e.setItems(SPs);
        SP_e.addValueChangeListener(event -> {
            if (SP_e.getValue() != null) {
                Support sp = IF.supports.get(SPs.indexOf(SP_e.getValue()));
                if (sp.ST == 1 || sp.ST == 2 || sp.ST == 5) {
                    Sx_e.setEnabled(true);
                }
                else {
                    Sx_e.setEnabled(false);
                    Sx_e.clear();
                }
                if (sp.ST == 1 || sp.ST == 2 || sp.ST == 4) {
                    Sy_e.setEnabled(true);
                }
                else {
                    Sy_e.setEnabled(false);
                    Sy_e.clear();
                }
                if (sp.ST == 1) {
                    Sz_e.setEnabled(true);
                }
                else {
                    Sz_e.setEnabled(false);
                    Sz_e.clear();
                }
            }
            else {
                Sx_e.setEnabled(false);
                Sy_e.setEnabled(false);
                Sz_e.setEnabled(false);
                Sx_e.clear();
                Sy_e.clear();
                Sz_e.clear();
            }
        });

        Sx_e.setEnabled(false);
        Sy_e.setEnabled(false);
        Sz_e.setEnabled(false);

        InputFrame.trimMinMax(Sx_e, -1000000, 1000000, 2);
        InputFrame.trimMinMax(Sy_e, -1000000, 1000000, 2);
        InputFrame.trimAngle(Sz_e, 360, 2);

        SP_e.setWidthFull();
        Sx_e.setWidthFull();
        Sy_e.setWidthFull();
        Sz_e.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();
        Div h3 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h3.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h3.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        SP_e.setHelperText("(X , Y)");
        Sx_e.setHelperComponent(h1);
        Sy_e.setHelperComponent(h2);
        Sz_e.setHelperComponent(h3);

        Div s1 = new Div();
        Div s2 = new Div();
        Div s3 = new Div();

        s1.setText(U.mm);
        s2.setText(U.mm);
        s3.setText(U.deg);

        Sx_e.setSuffixComponent(s1);
        Sy_e.setSuffixComponent(s2);
        Sz_e.setSuffixComponent(s3);

        Sx_e.setPlaceholder("0.00");
        Sy_e.setPlaceholder("0.00");
        Sz_e.setPlaceholder("0.00");

        int i = 0;
        for (Support sp : IF.supports) {
            if (sp.Cx.compareTo(SS.Cx) == 0 && sp.Cy.compareTo(SS.Cy) == 0) {
                break;
            }
            i++;
        }
        SP_e.setValue(SPs.get(i));
        Sx_e.setValue(U.round(U.convertSL2(SS.settlement[0])));
        Sy_e.setValue(U.round(U.convertSL2(SS.settlement[1])));
        Sz_e.setValue(U.round(U.convertRad2(SS.settlement[2])));

        VerticalLayout supportSettlementForm_e = new VerticalLayout();
        supportSettlementForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        supportSettlementForm_e.add(SP_e, Sx_e, Sy_e, Sz_e);
        supportSettlementForm_e.setSpacing(false);

        return supportSettlementForm_e;
    }

    private void validateAndEdit (Dialog dialog, SupportSettlement SS, MyUI UI) {
        BigDecimal Sx_ = Sx_e.getValue() == null ? BigDecimal.ZERO : U.convertSL1(Sx_e.getValue());
        BigDecimal Sy_ = Sy_e.getValue() == null ? BigDecimal.ZERO : U.convertSL1(Sy_e.getValue());
        BigDecimal Sz_ = Sz_e.getValue() == null ? BigDecimal.ZERO : U.convertRad1(Sz_e.getValue());

        if (SP_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Support Coordinates\n\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Sx_) == 0 && BigDecimal.ZERO.compareTo(Sy_) == 0 && BigDecimal.ZERO.compareTo(Sz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No support settlement was input.");
        }
        else {
            BigDecimal Cx = IF.supports.get(SPs.indexOf(SP_e.getValue())).Cx;
            BigDecimal Cy = IF.supports.get(SPs.indexOf(SP_e.getValue())).Cy;

            boolean add = true;
            for (SupportSettlement ss : IF.supportSettlements) {
                if (ss.Cx.compareTo(SS.Cx) != 0 && ss.Cy.compareTo(SS.Cy) != 0) {
                    if (ss.Cx.compareTo(Cx) == 0 && ss.Cy.compareTo(Cy) == 0) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The support settlement overlaps with existing support settlement.");
            }
            else {
                try {
                    int i = IF.supportSettlements.indexOf(SS);
                    IF.addSupportSettlement(i, Cx, Cy, Sx_, Sy_, Sz_);
                    IF.removeSupportSettlement(SS);

                    supportSettlementGrid.getDataProvider().refreshAll();
                    graphView.refreshGraph(UI);
                    dialog.close();
                }
                catch (ValidationException e) {
                    ErrorNotification.displayNotification("System Error");
                }
            }
        }
    }

    private void configureGraph (MyUI UI) {
        graphView = new GraphView(UI);
    }
}
