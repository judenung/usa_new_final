package com.example.application.frontends;

import com.example.application.backends.*;
import com.example.application.tools.ErrorNotification;
import com.example.application.tools.InputFrame;
import com.example.application.tools.MyUI;
import com.example.application.tools.Unit;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.jsoup.helper.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "hinge", layout = BuildView.class)
@PageTitle("Hinge | PFSAT")
public class HingeView extends VerticalLayout {

    private VerticalLayout hingeForm;
    private Grid<Hinge> hingeGrid;
    private GraphView graphView;

    private ComboBox<String> J;
    private MultiSelectComboBox<String> MID;
    private Button ADD;

    private ComboBox<String> J_e;
    private MultiSelectComboBox<String> MID_e;

    private InputFrame IF;
    private Unit U;

    private ArrayList<String> Js;

    public HingeView (@Autowired MyUI UI) {
        addClassName("hinge-view");
        setSizeFull();

        IF = UI.getInputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        U = UI.getUnit();
        UI.hingeView = this;

        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();

        configureForm(UI);
        configureGrid(UI);
        configureGraph(UI);

        SplitLayout S1 = new SplitLayout(hingeForm, hingeGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        hingeForm = new VerticalLayout();
        hingeForm.addClassNames("hinge-form");

        J = new ComboBox<>("Joint Coordinates");
        MID = new MultiSelectComboBox<>("Frame Member ID");
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
            if (J.getValue() != null) {
                ArrayList<String> MIDs = calMIDs(J);
                MID.setItems(MIDs);
                MID.select(MIDs);
                MID.setEnabled(true);
            }
            else {
                MID.deselectAll();
                MID.setEnabled(false);
            }
        });
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

        MID.addValueChangeListener(event -> {
            IF.markedFrameMembers.clear();
            for (FrameMember fm : IF.frameMembers) {
                if (MID.getValue().contains(fm.ID + "")) {
                    IF.markedFrameMembers.add(fm.ID - 1);
                }
            }
            graphView.refreshGraph(UI);
        });
        MID.setEnabled(false);

        J.setWidthFull();
        MID.setWidthFull();

        J.setHelperText("(X , Y)");

        hingeForm.setAlignItems(Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(J, MID);
        v.setSpacing(false);
        hingeForm.add(v, ADD);
        hingeForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {

        if (IF.hinges.size() == InputFrame.hingeMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.hingeMaxItem + ".");
        }
        else if (J.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\n\" field is not selected.");
        }
        else if (MID.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\n\" field is not selected.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J.getValue())).Cy;

            boolean add = true;
            for (Hinge h : IF.hinges) {
                if (h.Cx.compareTo(Cx) == 0 && h.Cy.compareTo(Cy) == 0) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The hinge overlaps with existing hinge.");
            }
            else {
                try {
                    IF.addHinge(Cx, Cy);
                    ArrayList<String> MIDs = calMIDs(J);
                    for (String id : MIDs) {
                        if (MID.getValue().contains(id)) {
                            IF.hinges.get(IF.hinges.size() - 1).MN.add((short) (Short.parseShort(id) - 1));
                        }
                    }

                    hingeGrid.getDataProvider().refreshAll();
                    graphView.refreshGraph(UI);
                }
                catch (ValidationException e) {
                    ErrorNotification.displayNotification("System Error");
                }
            }
        }
    }

    private void configureGrid (MyUI UI) {
        hingeGrid = new Grid<>(Hinge.class, false);
        List<Hinge> hingeList = IF.hinges;

        hingeGrid.addClassName("hinge-grid");
        hingeGrid.addColumn(Support -> hingeList.indexOf(Support) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        hingeGrid.addColumn(Hinge -> Hinge.Cx.toPlainString()).setHeader("X (" + U.m + ")");
        hingeGrid.addColumn(Hinge -> Hinge.Cy.toPlainString()).setHeader("Y (" + U.m + ")");
        hingeGrid.addColumn(this::setMIDString).setHeader("Member ID");

        hingeGrid.addColumn(new ComponentRenderer<>(Button::new, (button, Hinge) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(Hinge, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        hingeGrid.addColumn(new ComponentRenderer<>(Button::new, (button, Hinge) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> delete(Hinge, UI));
            button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        hingeGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        hingeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        hingeGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        hingeGrid.setItems(hingeList);

        hingeGrid.addSelectionListener(selection -> {
            Optional<Hinge> optionalH = selection.getFirstSelectedItem();
            if (optionalH.isPresent()) {
                IF.markedHinges.clear();
                IF.markedHinges.add(IF.hinges.indexOf(optionalH.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedHinges.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (Hinge h, MyUI UI) {
        IF.removeHinge(h);

        hingeGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
    }

    private void edit (Hinge H, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Hinge");

        dialog.add(configureEditForm (H, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, H, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (Hinge H, MyUI UI) {
        J_e = new ComboBox<>("Joint Coordinates");
        MID_e = new MultiSelectComboBox<>("Frame Member ID");

        Js = new ArrayList<>();
        for (Joint j : IF.joints) {
            Js.add("(" + j.Cx.toPlainString() + " , " + j.Cy.toPlainString() + ")");
        }
        J_e.setItems(Js);
        J_e.addValueChangeListener(event -> {
            if (J_e.getValue() != null) {
                ArrayList<String> MIDs = calMIDs(J_e);
                MID_e.setItems(MIDs);
                MID_e.select(MIDs);
                MID_e.setEnabled(true);
            }
            else {
                MID_e.deselectAll();
                MID_e.setEnabled(false);
            }
        });

        MID_e.setEnabled(false);

        J_e.setWidthFull();
        MID_e.setWidthFull();

        J_e.setHelperText("(X , Y)");

        int i = 0;
        for (Joint j : IF.joints) {
            if (j.Cx.compareTo(H.Cx) == 0 && j.Cy.compareTo(H.Cy) == 0) {
                break;
            }
            i++;
        }
        J_e.setValue(Js.get(i));
        ArrayList<String> id = new ArrayList<>();
        for (int MID : H.MN) {
            id.add((MID + 1) + "");
        }
        MID_e.setValue(id);

        VerticalLayout hingeForm_e = new VerticalLayout();
        hingeForm_e.setAlignItems(Alignment.CENTER);
        hingeForm_e.add(J_e, MID_e);
        hingeForm_e.setSpacing(false);

        return hingeForm_e;
    }

    private void validateAndEdit (Dialog dialog, Hinge H, MyUI UI) {

        if (J_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Joint Coordinates\n\" field is not selected.");
        }
        else if (MID_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\n\" field is not selected.");
        }
        else {
            BigDecimal Cx = IF.joints.get(Js.indexOf(J_e.getValue())).Cx;
            BigDecimal Cy = IF.joints.get(Js.indexOf(J_e.getValue())).Cy;

            boolean add = true;
            for (Hinge h : IF.hinges) {
                if (h.Cx.compareTo(H.Cx) != 0 || h.Cy.compareTo(H.Cy) != 0) {
                    if (h.Cx.compareTo(Cx) == 0 && h.Cy.compareTo(Cy) == 0) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The hinge overlaps with existing hinge.");
            }
            else {
                try {
                    int i = IF.hinges.indexOf(H);
                    IF.addHinge(i, Cx, Cy);
                    ArrayList<String> MIDs = calMIDs(J_e);
                    for (String id : MIDs) {
                        if (MID_e.getValue().contains(id)) {
                            IF.hinges.get(i).MN.add((short) (Short.parseShort(id) - 1));
                        }
                    }
                    IF.removeHinge(H);

                    hingeGrid.getDataProvider().refreshAll();
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


    private String setMIDString (Hinge h) {
        StringBuilder IDs = new StringBuilder();
        for (int MN : h.MN) {
            IDs.append(MN + 1).append(", ");
        }
        return IDs.substring(0, IDs.length() - 2);
    }

    private ArrayList<String> calMIDs (ComboBox<String> J) {
        ArrayList<String> MIDs = new ArrayList<>();
        Joint j = IF.joints.get(Js.indexOf(J.getValue()));
        for (FrameMember fm : IF.frameMembers) {
            if ((fm.BCx.compareTo(j.Cx) == 0 && fm.BCy.compareTo(j.Cy) == 0) || (fm.ECx.compareTo(j.Cx) == 0 && fm.ECy.compareTo(j.Cy) == 0)) {
                MIDs.add(fm.ID + "");
            }
        }
        return MIDs;
    }

}
