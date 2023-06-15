package com.example.application.frontends;

import com.example.application.backends.Joint;
import com.example.application.backends.Support;
import com.example.application.backends.SupportSettlement;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Route(value = "support", layout = BuildView.class)
@PageTitle("Support | PFSAT")
public class SupportView extends VerticalLayout {

    private VerticalLayout supportForm;
    private Grid<Support> supportGrid;
    private GraphView graphView;

    private ComboBox<String> J;
    private ComboBox<String> ST;
    private BigDecimalField A;
    private Button ADD;

    private ComboBox<String> J_e;
    private ComboBox<String> ST_e;
    private BigDecimalField A_e;

    private InputFrame IF;
    private Unit U;

    private ArrayList<String> Js;

    private final static String[] STs = {"Fixed", "Pinned", "Roller"};
    private final static String[] STs2 = {"Fixed", "Pinned", "Incline Roller", "Roller", "Vertical Roller"};

    public SupportView (@Autowired MyUI UI) {
        addClassName("support-view");
        setSizeFull();

        IF = UI.getInputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        U = UI.getUnit();
        UI.supportView = this;

        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();

        configureForm(UI);
        configureGrid(UI);
        configureGraph(UI);

        SplitLayout S1 = new SplitLayout(supportForm, supportGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        supportForm = new VerticalLayout();
        supportForm.addClassNames("support-form");

        J = new ComboBox<>("Joint Coordinates");
        ST = new ComboBox<>("Support Type");
        A = new BigDecimalField("Incline Angle");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        Js = new ArrayList<>();
        for (Joint j : IF.joints) {
            Js.add("(" + j.Cx.toPlainString() + " , " + j.Cy.toPlainString() + ")");
        }
        J.setItems(Js);
        J.addValueChangeListener(event -> {
            IF.markedJoints.clear();
            IF.markedSupports.clear();
            IF.markedSupportsSettlements.clear();
            if (J.getValue() != null) {
                IF.markedJoints.add(Js.indexOf(J.getValue()));
                Joint j = IF.joints.get(Js.indexOf(J.getValue()));
                for (Support sp : IF.supports) {
                    if (sp.Cx.compareTo(j.Cx) == 0 && sp.Cy.compareTo(j.Cy) == 0) {
                        IF.markedSupports.add(IF.supports.indexOf(sp));
                        break;
                    }
                }
                for (SupportSettlement ss : IF.supportSettlements) {
                    if (ss.Cx.compareTo(j.Cx) == 0 && ss.Cy.compareTo(j.Cy) == 0) {
                        IF.markedSupportsSettlements.add(IF.supportSettlements.indexOf(ss));
                        break;
                    }
                }
            }
            graphView.refreshGraph(UI);
        });
        ST.setItems(STs);
        ST.addValueChangeListener(event -> {
           if (ST.getValue() != null) {
               A.setEnabled(ST.getValue().contains(STs[2]));
               if (!ST.getValue().contains(STs[2])) {
                   A.clear();
               }
           }
        });

        A.setEnabled(false);
        InputFrame.trimAngle(A, 360, 2);

        J.setWidthFull();
        ST.setWidthFull();
        A.setWidthFull();

        Div h = new Div();
        Div s = new Div();

        h.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        s.setText(U.deg);

        A.setHelperComponent(h);
        A.setSuffixComponent(s);
        A.setPlaceholder("0.00");

        J.setHelperText("(X , Y)");

        supportForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(J, ST, A);
        v.setSpacing(false);
        supportForm.add(v, ADD);
        supportForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {

        BigDecimal A_ = A.getValue() == null ? BigDecimal.ZERO : A.getValue();

        if (IF.supports.size() == InputFrame.supportMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.supportMaxItem + ".");
        }
        else if (J.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\n\" field is not selected.");
        }
        else if (ST.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Support Type\n\" field is not selected.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J.getValue())).Cy;

            boolean add = true;
            for (Support s : IF.supports) {
                if (s.Cx.compareTo(Cx) == 0 && s.Cy.compareTo(Cy) == 0) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The support overlaps with existing support.");
            }
            else {
                try {
                    int supportType = Arrays.asList(STs).indexOf(ST.getValue()) + 1;
                    if (supportType != 3) {
                        IF.addSupport(Cx, Cy, supportType);
                    }
                    else {
                        if (A_.compareTo(BigDecimal.ZERO) == 0 || A_.compareTo(new BigDecimal("180")) == 0) {
                            IF.addSupport(Cx, Cy, supportType + 1);
                        }
                        else if (A_.compareTo(new BigDecimal("90")) == 0 || A_.compareTo(new BigDecimal("270")) == 0) {
                            IF.addSupport(Cx, Cy, supportType + 2);
                        }
                        else {
                            IF.addSupport(Cx, Cy, supportType);
                        }
                        IF.supports.get(IF.supports.size() - 1).angle = A_;
                    }
                    IF.markedSupports.add(IF.supports.size() - 1);

                    supportGrid.getDataProvider().refreshAll();
                    graphView.refreshGraph(UI);
                    UI.refreshTabs();
                }
                catch (ValidationException e) {
                    ErrorNotification.displayNotification("System Error");
                }
            }
        }
    }

    private void configureGrid (MyUI UI) {
        supportGrid = new Grid<>(Support.class, false);
        List<Support> supportList = IF.supports;

        supportGrid.addClassName("support-grid");
        supportGrid.addColumn(Support -> supportList.indexOf(Support) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        supportGrid.addColumn(Support -> Support.Cx.toPlainString()).setHeader("X (" + U.m + ")");
        supportGrid.addColumn(Support -> Support.Cy.toPlainString()).setHeader("Y (" + U.m + ")");
        supportGrid.addColumn(Support -> STs2[Support.ST - 1]).setHeader("Support Type");
        supportGrid.addColumn(Support -> Support.ST >= 3 ? Support.angle.toPlainString() : "N/A").setHeader("Angle (" + U.deg + ")");

        supportGrid.addColumn(new ComponentRenderer<>(Button::new, (button, Support) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(Support, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        supportGrid.addColumn(new ComponentRenderer<>(Button::new, (button, Support) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(Support, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        supportGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        supportGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        supportGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        supportGrid.setItems(supportList);

        supportGrid.addSelectionListener(selection -> {
            Optional<Support> optionalSP = selection.getFirstSelectedItem();
            if (optionalSP.isPresent()) {
                IF.markedSupports.clear();
                IF.markedSupportsSettlements.clear();
                IF.markedSupports.add(IF.supports.indexOf(optionalSP.get()));
                for (SupportSettlement ss : IF.supportSettlements) {
                    if (ss.Cx.compareTo(optionalSP.get().Cx) == 0 && ss.Cy.compareTo(optionalSP.get().Cy) == 0) {
                        IF.markedSupportsSettlements.add(IF.supportSettlements.indexOf(ss));
                        break;
                    }
                }
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedSupports.clear();
                IF.markedSupportsSettlements.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (Support S, MyUI UI) {
        IF.removeSupport(S);

        supportGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (Support S, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Support");

        dialog.add(configureEditForm (S, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, S, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (Support S, MyUI UI) {
        J_e = new ComboBox<>("Joint Coordinates");
        ST_e = new ComboBox<>("Support Type");
        A_e = new BigDecimalField("Incline Angle");

        Js = new ArrayList<>();
        for (Joint j : IF.joints) {
            Js.add("(" + j.Cx.toPlainString() + " , " + j.Cy.toPlainString() + ")");
        }
        J_e.setItems(Js);
        ST_e.setItems(STs);
        ST_e.addValueChangeListener(event -> {
            if (ST_e.getValue() != null) {
                A_e.setEnabled(ST_e.getValue().contains(STs[2]));
                if (!ST_e.getValue().contains(STs[2])) {
                    A_e.clear();
                }
            }
        });

        A_e.setEnabled(false);
        InputFrame.trimAngle(A_e, 360, 2);

        J_e.setWidthFull();
        ST_e.setWidthFull();
        A_e.setWidthFull();

        Div h = new Div();
        Div s = new Div();

        h.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        s.setText(U.deg);

        A_e.setHelperComponent(h);
        A_e.setSuffixComponent(s);
        A_e.setPlaceholder("0.00");

        J_e.setHelperText("(X , Y)");

        int i = 0;
        for (Joint j : IF.joints) {
            if (j.Cx.compareTo(S.Cx) == 0 && j.Cy.compareTo(S.Cy) == 0) {
                break;
            }
            i++;
        }
        J_e.setValue(Js.get(i));
        int index = S.ST > 3 ? 2 : S.ST - 1;
        ST_e.setValue(STs[index]);
        if (S.ST >= 3) {
            A_e.setValue(S.angle);
        }

        VerticalLayout supportForm_e = new VerticalLayout();
        supportForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        supportForm_e.add(J_e, ST_e, A_e);
        supportForm_e.setSpacing(false);

        return supportForm_e;
    }

    private void validateAndEdit (Dialog dialog, Support S, MyUI UI) {

        BigDecimal A_ = A_e.getValue() == null ? BigDecimal.ZERO : A_e.getValue();

        if (J_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\n\" field is not selected.");
        }
        else if (ST_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Support Type\n\" field is not selected.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J_e.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J_e.getValue())).Cy;

            boolean add = true;
            for (Support s : IF.supports) {
                if (s.Cx.compareTo(S.Cx) != 0 || s.Cy.compareTo(S.Cy) != 0) {
                    if (s.Cx.compareTo(Cx) == 0 && s.Cy.compareTo(Cy) == 0) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The support overlaps with existing support.");
            }
            else {
                try {
                    int i = IF.supports.indexOf(S);
                    int supportType = Arrays.asList(STs).indexOf(ST_e.getValue()) + 1;
                    if (supportType != 3) {
                        IF.addSupport(i, Cx, Cy, supportType);
                    }
                    else {
                        if (A_.compareTo(BigDecimal.ZERO) == 0 || A_.compareTo(new BigDecimal("180")) == 0) {
                            IF.addSupport(i, Cx, Cy, supportType + 1);
                        }
                        else if (A_.compareTo(new BigDecimal("90")) == 0 || A_.compareTo(new BigDecimal("270")) == 0) {
                            IF.addSupport(i, Cx, Cy, supportType + 2);
                        }
                        else {
                            IF.addSupport(i, Cx, Cy, supportType);
                        }
                        IF.supports.get(i).angle = A_;
                    }
                    IF.removeSupport(S);

                    supportGrid.getDataProvider().refreshAll();
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
