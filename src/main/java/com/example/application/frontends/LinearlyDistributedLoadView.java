package com.example.application.frontends;

import com.example.application.backends.LinearlyDistributedLoad;
import com.example.application.backends.PointLoad;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Route(value = "linearly-distributed-load", layout = BuildView.class)
@PageTitle("Point Load | PFSAT")
public class LinearlyDistributedLoadView extends VerticalLayout {

    private VerticalLayout linearlyDistributedLoadForm;
    private Grid<LinearlyDistributedLoad> linearlyDistributedLoadGrid;
    private GraphView graphView;

    private ComboBox<Integer> MID;
    private ComboBox<String> LDLT;
    private ComboBox<String> LDLO;
    private BigDecimalField W1;
    private BigDecimalField W2;
    private BigDecimalField a;
    private BigDecimalField b;
    private Button ADD;

    private ComboBox<Integer> MID_e;
    private ComboBox<String> LDLT_e;
    private ComboBox<String> LDLO_e;
    private BigDecimalField W1_e;
    private BigDecimalField W2_e;
    private BigDecimalField a_e;
    private BigDecimalField b_e;

    private InputFrame IF;
    private Unit U;

    private final static String[] LDLTs = {
            "Horizontal Loads",
            "Vertical Loads"};
    private final static String[] LDLTs2 = {
            "Horizontal",
            "Vertical",
            "Horizontal",
            "Vertical"};
    private final static String[] LDLOs = {
            "With Respect to the Local Coordinate System",
            "With Respect to the Global Coordinate System"};
    private final static String[] LDLOs2 = {
            "Local",
            "Local",
            "Global",
            "Global"};

    public LinearlyDistributedLoadView (@Autowired MyUI UI) {
        addClassName("linearly-distributed-load-view");
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

        SplitLayout S1 = new SplitLayout(linearlyDistributedLoadForm, linearlyDistributedLoadGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        linearlyDistributedLoadForm = new VerticalLayout();
        linearlyDistributedLoadForm.addClassName("linearly-distributed-load-form");

        MID = new ComboBox<>("Frame Member ID");
        LDLT = new ComboBox<>("Load Type");
        LDLO = new ComboBox<>("Orientation");
        W1 = new BigDecimalField("Begin Load");
        W2 = new BigDecimalField("End Load");
        a = new BigDecimalField("Distant from the Begin Joint");
        b = new BigDecimalField("Distant from the End Joint");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID.setItems(range);
        MID.addValueChangeListener(event -> {
            IF.markedFrameMembers.clear();
            if (MID.getValue() != null) {
                String L = U.round2(IF.frameMembers.get(MID.getValue() - 1).L).toPlainString();
                a.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                b.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                IF.markedFrameMembers.add(MID.getValue() - 1);
                if (a.getValue() != null) {
                    if (a.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                        a.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                    }
                }
                if (b.getValue() != null) {
                    if (b.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                        b.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                    }
                }
            }
            else {
                a.setHelperText("With Respect to the Local Coordinate System");
                b.setHelperText("With Respect to the Local Coordinate System");
            }
            graphView.refreshGraph(UI);
        });
        LDLT.setItems(LDLTs);
        LDLT.addValueChangeListener(event -> {
            Div h1 = new Div();
            Div h2 = new Div();
            if (LDLT.getValue() != null) {
                if (LDLT.getValue().equals(LDLTs[0])) {
                    h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                    h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                }
                else {
                    h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                    h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                }
            }
            else {
                h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
            }
            h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
            h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
            W1.setHelperComponent(h1);
            W2.setHelperComponent(h2);
        });
        LDLO.setItems(LDLOs);

        a.addValueChangeListener(event -> {
            if (MID.getValue() != null && a.getValue() != null) {
                if (a.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                    a.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                }
            }
        });
        b.addValueChangeListener(event -> {
            if (MID.getValue() != null && b.getValue() != null) {
                if (b.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                    b.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                }
            }
        });

        InputFrame.trimMinMax(W1, -1000000, 1000000, 4);
        InputFrame.trimMinMax(W2, -1000000, 1000000, 4);
        InputFrame.trimMin(a, 0, 2);
        InputFrame.trimMin(b, 0, 2);

        MID.setWidthFull();
        LDLT.setWidthFull();
        LDLO.setWidthFull();
        W1.setWidthFull();
        W2.setWidthFull();
        a.setWidthFull();
        b.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        W1.setHelperComponent(h1);
        W2.setHelperComponent(h2);
        a.setHelperText("With Respect to the Local Coordinate System");
        b.setHelperText("With Respect to the Local Coordinate System");

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.kN + "/" + U.m);
        p2.setText(U.kN + "/" + U.m);
        p3.setText(U.m);
        p4.setText(U.m);

        W1.setSuffixComponent(p1);
        W2.setSuffixComponent(p2);
        a.setSuffixComponent(p3);
        b.setSuffixComponent(p4);

        W1.setPlaceholder("0.0000");
        W2.setPlaceholder("0.0000");
        a.setPlaceholder("0.00");
        b.setPlaceholder("0.00");

        linearlyDistributedLoadForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(MID, LDLT, LDLO, a, b, W1, W2);
        v.setSpacing(false);
        linearlyDistributedLoadForm.add(v, ADD);
        linearlyDistributedLoadForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal W1_ = W1.getValue() == null ? BigDecimal.ZERO : W1.getValue();
        BigDecimal W2_ = W2.getValue() == null ? BigDecimal.ZERO : W2.getValue();
        BigDecimal a_ = a.getValue() == null ? BigDecimal.ZERO : a.getValue();
        BigDecimal b_ = b.getValue() == null ? BigDecimal.ZERO : b.getValue();

        if (IF.linearlyDistributedLoads.size() == InputFrame.linearlyDistributedLoadMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of linearly distributed loads is limited to " + InputFrame.linearlyDistributedLoadMaxItem + ".");
        }
        else if (MID.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (LDLT.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Load Type\" field is not selected.");
        }
        else if (LDLO.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Orientation\" field is not selected.");
        }
        else if (a_.add(b_).compareTo(IF.frameMembers.get(MID.getValue() - 1).L) >= 0) {
            ErrorNotification.displayNotification("Out of Bound Error : Linearly distributed load has non-positive span.");
        }
        else if (BigDecimal.ZERO.compareTo(W1_) == 0 && BigDecimal.ZERO.compareTo(W2_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            try {
                IF.addLinearlyDistributedLoad((MID.getValue() - 1), (Arrays.asList(LDLOs).indexOf(LDLO.getValue()) * 2 + Arrays.asList(LDLTs).indexOf(LDLT.getValue()) + 1), W1_, W2_, a_, b_);

                W1.clear();
                W2.clear();
                a.clear();
                b.clear();
                linearlyDistributedLoadGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                UI.refreshTabs();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void configureGrid (MyUI UI) {
        linearlyDistributedLoadGrid = new Grid<>(LinearlyDistributedLoad.class, false);
        List<LinearlyDistributedLoad> linearlyDistributedLoadList = IF.linearlyDistributedLoads;

        linearlyDistributedLoadGrid.addClassName("linearly-distributed-load-grid");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> linearlyDistributedLoadList.indexOf(LinearlyDistributedLoad ) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LinearlyDistributedLoad.MN + 1).setHeader("Member ID");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LDLTs2[LinearlyDistributedLoad.LT - 1]).setHeader("Load Type");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LDLOs2[LinearlyDistributedLoad.LT - 1]).setHeader("Orientation");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LinearlyDistributedLoad.load[2].toPlainString()).setHeader("d\u2081 (" + U.m + ")");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LinearlyDistributedLoad.load[3].toPlainString()).setHeader("d\u2082 (" + U.m + ")");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LinearlyDistributedLoad.load[0].toPlainString()).setHeader("W\u2081 (" + U.kN + "/" + U.m + ")");
        linearlyDistributedLoadGrid.addColumn(LinearlyDistributedLoad -> LinearlyDistributedLoad.load[1].toPlainString()).setHeader("W\u2082 (" + U.kN + "/" + U.m + ")");

        linearlyDistributedLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, LinearlyDistributedLoad) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(LinearlyDistributedLoad, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        linearlyDistributedLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, LinearlyDistributedLoad) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(LinearlyDistributedLoad, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        linearlyDistributedLoadGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        linearlyDistributedLoadGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        linearlyDistributedLoadGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        linearlyDistributedLoadGrid.setItems(linearlyDistributedLoadList);

        linearlyDistributedLoadGrid.addSelectionListener(selection -> {
            Optional<LinearlyDistributedLoad> optionalLDL = selection.getFirstSelectedItem();
            if (optionalLDL.isPresent()) {
                IF.markedLinearlyDistributedLoads.clear();
                IF.markedLinearlyDistributedLoads.add(IF.linearlyDistributedLoads.indexOf(optionalLDL.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedLinearlyDistributedLoads.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (LinearlyDistributedLoad LDL, MyUI UI) {
        IF.removeLinearlyDistributedLoad(LDL);

        linearlyDistributedLoadGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (LinearlyDistributedLoad LDL, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Linearly Distributed Load");

        dialog.add(configureEditForm (LDL, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, LDL, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (LinearlyDistributedLoad LDL, MyUI UI) {
        MID_e = new ComboBox<>("Frame Member ID");
        LDLT_e = new ComboBox<>("Load Type");
        LDLO_e = new ComboBox<>("Orientation");
        W1_e = new BigDecimalField("Begin Load");
        W2_e = new BigDecimalField("End Load");
        a_e = new BigDecimalField("Distant from the Begin Joint");
        b_e = new BigDecimalField("Distant from the End Joint");

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID_e.setItems(range);
        MID_e.addValueChangeListener(event -> {
            if (MID_e.getValue() != null) {
                String L = U.round2(IF.frameMembers.get(MID_e.getValue() - 1).L).toPlainString();
                a_e.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                b_e.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                if (a_e.getValue() != null) {
                    if (a_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                        a_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                    }
                }
                if (b_e.getValue() != null) {
                    if (b_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                        b_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                    }
                }
            }
            else {
                a_e.setHelperText("With Respect to the Local Coordinate System");
                b_e.setHelperText("With Respect to the Local Coordinate System");
            }
        });
        LDLT_e.setItems(LDLTs);
        LDLT_e.addValueChangeListener(event -> {
            Div h1 = new Div();
            Div h2 = new Div();
            if (LDLT_e.getValue() != null) {
                if (LDLT_e.getValue().equals(LDLTs[0])) {
                    h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                    h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                } else {
                    h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                    h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                }
            }
            else {
                h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
                h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
                h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
            }
            h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
            h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
            W1_e.setHelperComponent(h1);
            W2_e.setHelperComponent(h2);
        });
        LDLO_e.setItems(LDLOs);

        a_e.addValueChangeListener(event -> {
            if (MID_e.getValue() != null && a_e.getValue() != null) {
                if (a_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                    a_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                }
            }
        });
        b_e.addValueChangeListener(event -> {
            if (MID_e.getValue() != null && b_e.getValue() != null) {
                if (b_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                    b_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                }
            }
        });

        InputFrame.trimMinMax(W1_e, -1000000, 1000000, 4);
        InputFrame.trimMinMax(W2_e, -1000000, 1000000, 4);
        InputFrame.trimMin(a_e, 0, 2);
        InputFrame.trimMin(b_e, 0, 2);

        MID_e.setWidthFull();
        LDLT_e.setWidthFull();
        LDLO_e.setWidthFull();
        W1_e.setWidthFull();
        W2_e.setWidthFull();
        a_e.setWidthFull();
        b_e.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        W1_e.setHelperComponent(h1);
        W2_e.setHelperComponent(h2);
        a_e.setHelperText("With Respect to the Local Coordinate System");
        b_e.setHelperText("With Respect to the Local Coordinate System");

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.kN + "/" + U.m);
        p2.setText(U.kN + "/" + U.m);
        p3.setText(U.m);
        p4.setText(U.m);

        W1_e.setSuffixComponent(p1);
        W2_e.setSuffixComponent(p2);
        a_e.setSuffixComponent(p3);
        b_e.setSuffixComponent(p4);

        W1_e.setPlaceholder("0.0000");
        W2_e.setPlaceholder("0.0000");
        a_e.setPlaceholder("0.00");
        b_e.setPlaceholder("0.00");

        MID_e.setValue(range.get(LDL.MN));
        LDLT_e.setValue(LDL.LT == 1 || LDL.LT == 3 ? LDLTs[0] : LDLTs[1]);
        LDLO_e.setValue(LDL.LT == 1 || LDL.LT == 2 ? LDLOs[0] : LDLOs[1]);
        W1_e.setValue(LDL.load[0]);
        W2_e.setValue(LDL.load[1]);
        a_e.setValue(LDL.load[2]);
        b_e.setValue(LDL.load[3]);

        VerticalLayout linearlyDistributedLoadForm_e = new VerticalLayout();
        linearlyDistributedLoadForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        linearlyDistributedLoadForm_e.add(MID_e, LDLT_e, LDLO_e, a_e, b_e, W1_e, W2_e);
        linearlyDistributedLoadForm_e.setSpacing(false);

        return linearlyDistributedLoadForm_e;
    }

    private void validateAndEdit (Dialog dialog, LinearlyDistributedLoad LDL, MyUI UI) {
        BigDecimal W1_ = W1_e.getValue() == null ? BigDecimal.ZERO : W1_e.getValue();
        BigDecimal W2_ = W2_e.getValue() == null ? BigDecimal.ZERO : W2_e.getValue();
        BigDecimal a_ = a_e.getValue() == null ? BigDecimal.ZERO : a_e.getValue();
        BigDecimal b_ = b_e.getValue() == null ? BigDecimal.ZERO : b_e.getValue();

        if (MID_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (LDLT_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Load Type\" field is not selected.");
        }
        else if (LDLO_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Orientation\" field is not selected.");
        }
        else if (a_.add(b_).compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) >= 0) {
            ErrorNotification.displayNotification("Out of Bound Error : Linearly distributed load has non-positive span.");
        }
        else if (BigDecimal.ZERO.compareTo(W1_) == 0 && BigDecimal.ZERO.compareTo(W2_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            try {
                int i = IF.linearlyDistributedLoads.indexOf(LDL);
                IF.addLinearlyDistributedLoad(i, (MID_e.getValue() - 1), (Arrays.asList(LDLOs).indexOf(LDLO_e.getValue()) * 2 + Arrays.asList(LDLTs).indexOf(LDLT_e.getValue()) + 1), W1_, W2_, a_, b_);
                IF.removeLinearlyDistributedLoad(LDL);

                linearlyDistributedLoadGrid.getDataProvider().refreshAll();
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
