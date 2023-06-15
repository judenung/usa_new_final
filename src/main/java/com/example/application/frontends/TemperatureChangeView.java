package com.example.application.frontends;

import com.example.application.backends.TemperatureChange;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Route(value = "temperature-change", layout = BuildView.class)
@PageTitle("Temperature Change | PFSAT")
public class TemperatureChangeView extends VerticalLayout{

    private VerticalLayout temperatureChangeForm;
    private Grid<TemperatureChange> temperatureChangeGrid;
    private GraphView graphView;

    private ComboBox<Integer> MID;
    private BigDecimalField T;
    private BigDecimalField alpha;
    private Button ADD;

    private ComboBox<Integer> MID_e;
    private BigDecimalField T_e;
    private BigDecimalField alpha_e;

    private InputFrame IF;
    private Unit U;

    public TemperatureChangeView (@Autowired MyUI UI) {
        addClassName("temperature-change-view");
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

        SplitLayout S1 = new SplitLayout(temperatureChangeForm, temperatureChangeGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        temperatureChangeForm = new VerticalLayout();
        temperatureChangeForm.addClassName("temperature-change-form");

        MID = new ComboBox<>("Frame Member ID");
        T = new BigDecimalField("Change in Temperature");
        alpha = new BigDecimalField("Coefficient of Thermal Expansion");
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

        InputFrame.trimMinMax(T, -1000000, 1000000, 2);
        InputFrame.trimMinMax(alpha, -1000000, 1000000, 2);

        MID.setWidthFull();
        T.setWidthFull();
        alpha.setWidthFull();

        T.setHelperText("Positive When the Temperature Increase");

        Div p1 = new Div();
        Div p2 = new Div();

        p1.setText(U.C);
        p2.setText("×10\u207B\u2076/" + U.C);

        T.setSuffixComponent(p1);
        alpha.setSuffixComponent(p2);

        T.setPlaceholder("0.00");
        alpha.setPlaceholder("0.00");

        temperatureChangeForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(MID, T, alpha);
        v.setSpacing(false);
        temperatureChangeForm.add(v, ADD);
        temperatureChangeForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal T_ = T.getValue() == null ? BigDecimal.ZERO : T.getValue();
        BigDecimal alpha_ = alpha.getValue() == null ? BigDecimal.ZERO : U.convertOC1(alpha.getValue());

        if (IF.temperatureChanges.size() == InputFrame.temperatureChangeMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.temperatureChangeMaxItem + ".");
        }
        else if (MID.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (T_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of change in temperature cannot be 0.");
        }
        else if (alpha_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of coefficient of thermal expansion cannot be 0.");
        }
        else {
            boolean add = true;
            for (TemperatureChange tc : IF.temperatureChanges) {
                if (tc.MN == MID.getValue() - 1) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The temperature change overlaps with existing temperature change.");
            }
            else {
                try {
                    IF.addTemperatureChange(MID.getValue() - 1, T_, alpha_);

                    T.clear();
                    alpha.clear();
                    temperatureChangeGrid.getDataProvider().refreshAll();
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
        temperatureChangeGrid = new Grid<>(TemperatureChange.class, false);
        List<TemperatureChange> temperatureChangeList = IF.temperatureChanges;

        temperatureChangeGrid.addClassName("temperature-change-grid");
        temperatureChangeGrid.addColumn(PointLoad -> temperatureChangeList.indexOf(PointLoad) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        temperatureChangeGrid.addColumn(TemperatureChange -> TemperatureChange.MN + 1).setHeader("Member ID");
        temperatureChangeGrid.addColumn(TemperatureChange -> TemperatureChange.T.toPlainString()).setHeader("ΔT (" + U.C + ")");
        temperatureChangeGrid.addColumn(TemperatureChange -> U.round(U.convertOC2(TemperatureChange.alpha)).toPlainString()).setHeader("α (×10\u207B\u2076/" + U.C + ")");

        temperatureChangeGrid.addColumn(new ComponentRenderer<>(Button::new, (button, TemperatureChange) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(TemperatureChange, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        temperatureChangeGrid.addColumn(new ComponentRenderer<>(Button::new, (button, TemperatureChange) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(TemperatureChange, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        temperatureChangeGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        temperatureChangeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        temperatureChangeGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        temperatureChangeGrid.setItems(temperatureChangeList);

        temperatureChangeGrid.addSelectionListener(selection -> {
            Optional<TemperatureChange> optionalTC = selection.getFirstSelectedItem();
            if (optionalTC.isPresent()) {
                IF.markedTemperatureChanges.clear();
                IF.markedTemperatureChanges.add(IF.temperatureChanges.indexOf(optionalTC.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedTemperatureChanges.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (TemperatureChange TC, MyUI UI) {
        IF.removeTemperatureChange(TC);

        temperatureChangeGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (TemperatureChange TC, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Temperature Change");

        dialog.add(configureEditForm (TC, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, TC, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (TemperatureChange TC, MyUI UI) {
        MID_e = new ComboBox<>("Frame Member ID");
        T_e = new BigDecimalField("Change in Temperature");
        alpha_e = new BigDecimalField("Coefficient of Thermal Expansion");

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID_e.setItems(range);

        InputFrame.trimMinMax(T_e, -1000000, 1000000, 2);
        InputFrame.trimMinMax(alpha_e, -1000000, 1000000, 2);

        MID_e.setWidthFull();
        T_e.setWidthFull();
        alpha_e.setWidthFull();

        T_e.setHelperText("Positive When the Temperature Increase");

        Div p1 = new Div();
        Div p2 = new Div();

        p1.setText(U.C);
        p2.setText("×10\u207B\u2076/" + U.C);

        T_e.setSuffixComponent(p1);
        alpha_e.setSuffixComponent(p2);

        T_e.setPlaceholder("0.00");
        alpha_e.setPlaceholder("0.00");

        MID_e.setValue(range.get(TC.MN));
        T_e.setValue(TC.T);
        alpha_e.setValue(U.round(U.convertOC2(TC.alpha)).stripTrailingZeros());

        VerticalLayout temperatureChangeForm_e = new VerticalLayout();
        temperatureChangeForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        temperatureChangeForm_e.add(MID_e, T_e, alpha_e);
        temperatureChangeForm_e.setSpacing(false);

        return temperatureChangeForm_e;
    }

    private void validateAndEdit (Dialog dialog, TemperatureChange TC, MyUI UI) {
        BigDecimal T_ = T_e.getValue() == null ? BigDecimal.ZERO : T_e.getValue();
        BigDecimal alpha_ = alpha_e.getValue() == null ? BigDecimal.ZERO : U.convertOC1(alpha_e.getValue());

        if (MID_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (T_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of change in temperature cannot be 0.");
        }
        else if (alpha_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of coefficient of thermal expansion cannot be 0.");
        }
        else {
            boolean add = true;
            for (TemperatureChange tc : IF.temperatureChanges) {
                if (tc.MN != TC.MN) {
                    if (tc.MN == MID_e.getValue() - 1) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The temperature change overlaps with existing temperature change.");
            }
            else {
                try {
                    int i = IF.temperatureChanges.indexOf(TC);
                    IF.addTemperatureChange(i,MID_e.getValue() - 1, T_, alpha_);
                    IF.removeTemperatureChange(TC);

                    temperatureChangeGrid.getDataProvider().refreshAll();
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
