package com.example.application.frontends;

import com.example.application.backends.FabricationError;
import com.example.application.tools.ErrorNotification;
import com.example.application.tools.InputFrame;
import com.example.application.tools.MyUI;
import com.example.application.tools.Unit;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Route(value = "fabrication-error", layout = BuildView.class)
@PageTitle("Fabrication Error | PFSAT")
public class FabricationErrorView extends VerticalLayout {

    private VerticalLayout fabricationErrorForm;
    private Grid<FabricationError> fabricationErrorGrid;
    private GraphView graphView;

    private ComboBox<Integer> MID;
    private BigDecimalField err;
    private Button ADD;

    private ComboBox<Integer> MID_e;
    private BigDecimalField err_e;

    private InputFrame IF;
    private Unit U;

    public FabricationErrorView (@Autowired MyUI UI) {
        addClassName("fabrication-error-view");
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

        SplitLayout S1 = new SplitLayout(fabricationErrorForm, fabricationErrorGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        fabricationErrorForm = new VerticalLayout();
        fabricationErrorForm.addClassName("fabrication-error-form");

        MID = new ComboBox<>("Frame Member ID");
        err = new BigDecimalField("Error Length");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID.setItems(range);
        MID.addValueChangeListener(event -> {
            IF.markedFrameMembers.clear();
            if (MID.getValue() != null) {
                IF.markedFrameMembers.add(MID.getValue() - 1);
            }
            graphView.refreshGraph(UI);
        });

        InputFrame.trimMinMax(err, -1000000, 1000000, 2);

        MID.setWidthFull();
        err.setWidthFull();

        err.setHelperText("Positive When the Frame Member is Fabricated Longer than the Designed Length");
        Div p1 = new Div();
        p1.setText(U.mm);
        err.setSuffixComponent(p1);
        err.setPlaceholder("0.00");

        fabricationErrorForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(MID, err);
        v.setSpacing(false);
        fabricationErrorForm.add(v, ADD);
        fabricationErrorForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal err_ = err.getValue() == null ? BigDecimal.ZERO : U.convertSL1(err.getValue());

        if (IF.fabricationErrors.size() == InputFrame.fabricationErrorMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.fabricationErrorMaxItem + ".");
        }
        else if (MID.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(err_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of error length cannot be 0.");
        }
        else {
            boolean add = true;
            for (FabricationError fe : IF.fabricationErrors) {
                if (fe.MN == MID.getValue() - 1) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The fabrication error overlaps with existing fabrication error.");
            }
            else {
                try {
                    IF.addFabricationError(MID.getValue() - 1, err_);

                    err.clear();
                    fabricationErrorGrid.getDataProvider().refreshAll();
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
        fabricationErrorGrid = new Grid<>(FabricationError.class, false);
        List<FabricationError> fabricationErrorList = IF.fabricationErrors;

        fabricationErrorGrid.addClassName("fabrication-error-grid");
        fabricationErrorGrid.addColumn(PointLoad -> fabricationErrorList.indexOf(PointLoad) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        fabricationErrorGrid.addColumn(fabricationError -> fabricationError.MN + 1).setHeader("Member ID");
        fabricationErrorGrid.addColumn(fabricationError -> U.round(U.convertSL2(fabricationError.e)).toPlainString()).setHeader("e (" + U.mm + ")");

        fabricationErrorGrid.addColumn(new ComponentRenderer<>(Button::new, (button, FabricationError) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(FabricationError, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        fabricationErrorGrid.addColumn(new ComponentRenderer<>(Button::new, (button, FabricationError) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(FabricationError, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        fabricationErrorGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        fabricationErrorGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        fabricationErrorGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        fabricationErrorGrid.setItems(fabricationErrorList);

        fabricationErrorGrid.addSelectionListener(selection -> {
            Optional<FabricationError> optionalFE = selection.getFirstSelectedItem();
            if (optionalFE.isPresent()) {
                IF.markedFabricationErrors.clear();
                IF.markedFabricationErrors.add(IF.fabricationErrors.indexOf(optionalFE.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedFabricationErrors.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (FabricationError FE, MyUI UI) {
        IF.removeFabricationError(FE);

        fabricationErrorGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (FabricationError FE, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Fabrication Error");

        dialog.add(configureEditForm (FE, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, FE, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (FabricationError FE, MyUI UI) {
        MID_e = new ComboBox<>("Frame Member ID");
        err_e = new BigDecimalField("Error Length");

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID_e.setItems(range);

        InputFrame.trimMinMax(err_e, -1000000, 1000000, 2);

        MID_e.setWidthFull();
        err_e.setWidthFull();

        err_e.setHelperText("Positive When the Frame Member is Fabricated Longer than the Designed Length");
        Div p1 = new Div();
        p1.setText(U.mm);
        err_e.setSuffixComponent(p1);
        err_e.setPlaceholder("0.00");

        MID_e.setValue(range.get(FE.MN));
        err_e.setValue(U.round(U.convertSL2(FE.e)).stripTrailingZeros());

        VerticalLayout fabricationErrorForm_e = new VerticalLayout();
        fabricationErrorForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        fabricationErrorForm_e.add(MID_e, err_e);
        fabricationErrorForm_e.setSpacing(false);

        return fabricationErrorForm_e;
    }

    private void validateAndEdit (Dialog dialog, FabricationError FE, MyUI UI) {
        BigDecimal err_ = err_e.getValue() == null ? BigDecimal.ZERO : U.convertSL1(err_e.getValue());

        if (MID_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(err_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of error length cannot be 0.");
        }
        else {
            boolean add = true;
            for (FabricationError fe : IF.fabricationErrors) {
                if (fe.MN != FE.MN) {
                    if (fe.MN == MID_e.getValue() - 1) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The fabrication error overlaps with existing fabrication error.");
            }
            else {
                try {
                    int i = IF.fabricationErrors.indexOf(FE);
                    IF.addFabricationError(i,MID_e.getValue() - 1, err_);
                    IF.removeFabricationError(FE);

                    fabricationErrorGrid.getDataProvider().refreshAll();
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
