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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "joint-load", layout = BuildView.class)
@PageTitle("Joint Load | PFSAT")
public class JointLoadView extends VerticalLayout {

    private VerticalLayout jointLoadForm;
    private Grid<JointLoad> jointLoadGrid;
    private GraphView graphView;

    private ComboBox<String> J;
    private BigDecimalField Fx;
    private BigDecimalField Fy;
    private BigDecimalField Mz;
    private Button ADD;

    private ComboBox<String> J_e;
    private BigDecimalField Fx_e;
    private BigDecimalField Fy_e;
    private BigDecimalField Mz_e;

    private InputFrame IF;
    private Unit U;

    private ArrayList<String> Js;

    public JointLoadView (@Autowired MyUI UI) {
        addClassName("joint-load-view");
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

        SplitLayout S1 = new SplitLayout(jointLoadForm, jointLoadGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        jointLoadForm = new VerticalLayout();
        jointLoadForm.addClassNames("joint-load-form");

        J = new ComboBox<>("Joint Coordinates");
        Fx = new BigDecimalField("Horizontal Force");
        Fy = new BigDecimalField("Vertical Force");
        Mz = new BigDecimalField("Couple Moment");
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

        InputFrame.trimMinMax(Fx, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Fy, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Mz, -1000000, 1000000, 4);

        J.setWidthFull();
        Fx.setWidthFull();
        Fy.setWidthFull();
        Mz.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();
        Div h3 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h3.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h3.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        J.setHelperText("(X , Y)");
        Fx.setHelperComponent(h1);
        Fy.setHelperComponent(h2);
        Mz.setHelperComponent(h3);

        Div s1 = new Div();
        Div s2 = new Div();
        Div s3 = new Div();

        s1.setText(U.kN);
        s2.setText(U.kN);
        s3.setText(U.kNm);

        Fx.setSuffixComponent(s1);
        Fy.setSuffixComponent(s2);
        Mz.setSuffixComponent(s3);

        Fx.setPlaceholder("0.0000");
        Fy.setPlaceholder("0.0000");
        Mz.setPlaceholder("0.0000");

        jointLoadForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(J, Fx, Fy, Mz);
        v.setSpacing(false);
        jointLoadForm.add(v, ADD);
        jointLoadForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal Fx_ = Fx.getValue() == null ? BigDecimal.ZERO : Fx.getValue();
        BigDecimal Fy_ = Fy.getValue() == null ? BigDecimal.ZERO : Fy.getValue();
        BigDecimal Mz_ = Mz.getValue() == null ? BigDecimal.ZERO : Mz.getValue();

        if (IF.jointLoads.size() == InputFrame.jointLoadMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of joint loads is limited to " + InputFrame.jointLoadMaxItem + ".");
        }
        else if (J.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Fx_) == 0 && BigDecimal.ZERO.compareTo(Fy_) == 0 && BigDecimal.ZERO.compareTo(Mz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J.getValue())).Cy;

            try {
                IF.addJointLoad(Cx, Cy, Fx_, Fy_, Mz_);

                Fx.clear();
                Fy.clear();
                Mz.clear();
                jointLoadGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                UI.refreshTabs();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void configureGrid (MyUI UI) {
        jointLoadGrid = new Grid<>(JointLoad.class, false);
        List<JointLoad> jointLoadList = IF.jointLoads;

        jointLoadGrid.addClassName("joint-load-grid");
        jointLoadGrid.addColumn(JointLoad -> jointLoadList.indexOf(JointLoad) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        jointLoadGrid.addColumn(JointLoad -> JointLoad.Cx.toPlainString()).setHeader("X (" + U.m + ")");
        jointLoadGrid.addColumn(JointLoad -> JointLoad.Cy.toPlainString()).setHeader("Y (" + U.m + ")");
        jointLoadGrid.addColumn(JointLoad -> JointLoad.load[0].toPlainString()).setHeader("Fx (" + U.kN + ")");
        jointLoadGrid.addColumn(JointLoad -> JointLoad.load[1].toPlainString()).setHeader("Fy (" + U.kN + ")");
        jointLoadGrid.addColumn(JointLoad -> JointLoad.load[2].toPlainString()).setHeader("Mz (" + U.kNm + ")");

        jointLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, JointLoad) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(JointLoad, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        jointLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, JointLoad) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(JointLoad, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        jointLoadGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        jointLoadGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        jointLoadGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        jointLoadGrid.setItems(jointLoadList);

        jointLoadGrid.addSelectionListener(selection -> {
            Optional<JointLoad> optionalJL = selection.getFirstSelectedItem();
            if (optionalJL.isPresent()) {
                IF.markedJointLoads.clear();
                IF.markedJointLoads.add(IF.jointLoads.indexOf(optionalJL.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedJointLoads.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (JointLoad JL, MyUI UI) {
        IF.removeJointLoad(JL);

        jointLoadGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (JointLoad JL, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Joint Load");

        dialog.add(configureEditForm (JL, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, JL, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (JointLoad JL, MyUI UI) {
        J_e = new ComboBox<>("Joint Coordinates");
        Fx_e = new BigDecimalField("Horizontal Force");
        Fy_e = new BigDecimalField("Vertical Force");
        Mz_e = new BigDecimalField("Couple Moment");

        Js = new ArrayList<>();
        for (Joint j : IF.joints) {
            Js.add("(" + j.Cx.toPlainString() + " , " + j.Cy.toPlainString() + ")");
        }
        J_e.setItems(Js);

        InputFrame.trimMinMax(Fx_e, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Fy_e, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Mz_e, -1000000, 1000000, 4);

        J_e.setWidthFull();
        Fx_e.setWidthFull();
        Fy_e.setWidthFull();
        Mz_e.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();
        Div h3 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h3.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h3.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        J_e.setHelperText("(X , Y)");
        Fx_e.setHelperComponent(h1);
        Fy_e.setHelperComponent(h2);
        Mz_e.setHelperComponent(h3);

        Div s1 = new Div();
        Div s2 = new Div();
        Div s3 = new Div();

        s1.setText(U.kN);
        s2.setText(U.kN);
        s3.setText(U.kNm);

        Fx_e.setSuffixComponent(s1);
        Fy_e.setSuffixComponent(s2);
        Mz_e.setSuffixComponent(s3);

        Fx_e.setPlaceholder("0.0000");
        Fy_e.setPlaceholder("0.0000");
        Mz_e.setPlaceholder("0.0000");

        int i = 0;
        for (Joint j : IF.joints) {
            if (j.Cx.compareTo(JL.Cx) == 0 && j.Cy.compareTo(JL.Cy) == 0) {
                break;
            }
            i++;
        }
        J_e.setValue(Js.get(i));
        Fx_e.setValue(JL.load[0]);
        Fy_e.setValue(JL.load[1]);
        Mz_e.setValue(JL.load[2]);

        VerticalLayout jointLoadForm_e = new VerticalLayout();
        jointLoadForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        jointLoadForm_e.add(J_e, Fx_e, Fy_e, Mz_e);
        jointLoadForm_e.setSpacing(false);

        return jointLoadForm_e;
    }

    private void validateAndEdit (Dialog dialog, JointLoad JL, MyUI UI) {
        BigDecimal Fx_ = Fx_e.getValue() == null ? BigDecimal.ZERO : Fx_e.getValue();
        BigDecimal Fy_ = Fy_e.getValue() == null ? BigDecimal.ZERO : Fy_e.getValue();
        BigDecimal Mz_ = Mz_e.getValue() == null ? BigDecimal.ZERO : Mz_e.getValue();

        if (J_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Fx_) == 0 && BigDecimal.ZERO.compareTo(Fy_) == 0 && BigDecimal.ZERO.compareTo(Mz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J_e.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J_e.getValue())).Cy;

            try {
                int i = IF.jointLoads.indexOf(JL);
                IF.addJointLoad(i, Cx, Cy, Fx_, Fy_, Mz_);
                IF.removeJointLoad(JL);

                jointLoadGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                dialog.close();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void configureGraph (MyUI UI) {
        graphView = new GraphView(UI);
    }
}
