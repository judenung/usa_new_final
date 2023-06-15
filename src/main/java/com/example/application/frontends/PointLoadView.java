package com.example.application.frontends;

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

@Route(value = "point-load", layout = BuildView.class)
@PageTitle("Point Load | PFSAT")
public class PointLoadView extends VerticalLayout {

    private VerticalLayout pointLoadForm;
    private Grid<PointLoad> pointLoadGrid;
    private GraphView graphView;

    private ComboBox<Integer> MID;
    private ComboBox<String> PLT;
    private BigDecimalField d;
    private BigDecimalField Fx;
    private BigDecimalField Fy;
    private BigDecimalField Mz;
    private Button ADD;

    private ComboBox<Integer> MID_e;
    private ComboBox<String> PLT_e;
    private BigDecimalField d_e;
    private BigDecimalField Fx_e;
    private BigDecimalField Fy_e;
    private BigDecimalField Mz_e;

    private InputFrame IF;
    private Unit U;

    private final static String[] PLTs = {"With Respect to the Local Coordinate System", "With Respect to the Global Coordinate System"};
    private final static String[] PLTs2 = {"Local", "Global"};

    public PointLoadView (@Autowired MyUI UI) {
        addClassName("frame-member-view");
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

        SplitLayout S1 = new SplitLayout(pointLoadForm, pointLoadGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        pointLoadForm = new VerticalLayout();
        pointLoadForm.addClassName("point-load-form");

        MID = new ComboBox<>("Frame Member ID");
        PLT = new ComboBox<>("Orientation");
        d = new BigDecimalField("Distant from the Begin Joint");
        Fx = new BigDecimalField("Horizontal Force");
        Fy = new BigDecimalField("Vertical Force");
        Mz = new BigDecimalField("Couple Moment");
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
                d.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                IF.markedFrameMembers.add(MID.getValue() - 1);
                if (d.getValue() != null) {
                    if (d.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                        d.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                    }
                }
            }
            else {
                d.setHelperText("With Respect to the Local Coordinate System");
            }
            graphView.refreshGraph(UI);
        });
        PLT.setItems(PLTs);

        d.addValueChangeListener(event -> {
            if (MID.getValue() != null && d.getValue() != null) {
                if (d.getValue().compareTo(IF.frameMembers.get(MID.getValue() - 1).L) > 0) {
                    d.setValue(IF.frameMembers.get(MID.getValue() - 1).L);
                }
            }
        });

        InputFrame.trimMin(d, 0, 2);
        InputFrame.trimMinMax(Fx, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Fy, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Mz, -1000000, 1000000, 4);

        MID.setWidthFull();
        PLT.setWidthFull();
        d.setWidthFull();
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

        d.setHelperText("With Respect to the Local Coordinate System");
        Fx.setHelperComponent(h1);
        Fy.setHelperComponent(h2);
        Mz.setHelperComponent(h3);

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.m);
        p2.setText(U.kN);
        p3.setText(U.kN);
        p4.setText(U.kNm);

        d.setSuffixComponent(p1);
        Fx.setSuffixComponent(p2);
        Fy.setSuffixComponent(p3);
        Mz.setSuffixComponent(p4);

        d.setPlaceholder("0.00");
        Fx.setPlaceholder("0.0000");
        Fy.setPlaceholder("0.0000");
        Mz.setPlaceholder("0.0000");

        pointLoadForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(MID, PLT, d, Fx, Fy, Mz);
        v.setSpacing(false);
        pointLoadForm.add(v, ADD);
        pointLoadForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal d_ = d.getValue() == null ? BigDecimal.ZERO : d.getValue();
        BigDecimal Fx_ = Fx.getValue() == null ? BigDecimal.ZERO : Fx.getValue();
        BigDecimal Fy_ = Fy.getValue() == null ? BigDecimal.ZERO : Fy.getValue();
        BigDecimal Mz_ = Mz.getValue() == null ? BigDecimal.ZERO : Mz.getValue();

        if (IF.pointLoads.size() == InputFrame.pointLoadMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.pointLoadMaxItem + ".");
        }
        else if (MID.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (PLT.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Orientation\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Fx_) == 0 && BigDecimal.ZERO.compareTo(Fy_) == 0 && BigDecimal.ZERO.compareTo(Mz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            try {
                IF.addPointLoad(MID.getValue() - 1, Arrays.asList(PLTs).indexOf(PLT.getValue()) + 1, d_, Fx_, Fy_, Mz_);

                d.clear();
                Fx.clear();
                Fy.clear();
                Mz.clear();
                pointLoadGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                UI.refreshTabs();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void configureGrid (MyUI UI) {
        pointLoadGrid = new Grid<>(PointLoad.class, false);
        List<PointLoad> pointLoadList = IF.pointLoads;

        pointLoadGrid.addClassName("point-load-grid");
        pointLoadGrid.addColumn(PointLoad -> pointLoadList.indexOf(PointLoad) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        pointLoadGrid.addColumn(PointLoad -> PointLoad.MN + 1).setHeader("Member ID");
        pointLoadGrid.addColumn(PointLoad -> PLTs2[PointLoad.LT - 1]).setHeader("Orientation");
        pointLoadGrid.addColumn(PointLoad -> PointLoad.d.toPlainString()).setHeader("d (" + U.m + ")");
        pointLoadGrid.addColumn(PointLoad -> PointLoad.load[0].toPlainString()).setHeader("Fx (" + U.kN + ")");
        pointLoadGrid.addColumn(PointLoad -> PointLoad.load[1].toPlainString()).setHeader("Fy (" + U.kN + ")");
        pointLoadGrid.addColumn(PointLoad -> PointLoad.load[2].toPlainString()).setHeader("Mz (" + U.kNm + ")");

        pointLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, PointLoad) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> edit(PointLoad, UI));
                    button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        pointLoadGrid.addColumn(new ComponentRenderer<>(Button::new, (button, PointLoad) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(PointLoad, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        pointLoadGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        pointLoadGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        pointLoadGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        pointLoadGrid.setItems(pointLoadList);

        pointLoadGrid.addSelectionListener(selection -> {
            Optional<PointLoad> optionalPL = selection.getFirstSelectedItem();
            if (optionalPL.isPresent()) {
                IF.markedPointLoads.clear();
                IF.markedPointLoads.add(IF.pointLoads.indexOf(optionalPL.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedPointLoads.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (PointLoad PL, MyUI UI) {
        IF.removePointLoad(PL);

        pointLoadGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (PointLoad PL, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Point Load");

        dialog.add(configureEditForm (PL, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, PL, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (PointLoad PL, MyUI UI) {
        MID_e = new ComboBox<>("Frame Member ID");
        PLT_e = new ComboBox<>("Orientation");
        d_e = new BigDecimalField("Distant from the Begin Joint");
        Fx_e = new BigDecimalField("Horizontal Force");
        Fy_e = new BigDecimalField("Vertical Force");
        Mz_e = new BigDecimalField("Couple Moment");

        List<Integer> range = IntStream.rangeClosed(1, IF.frameMembers.size()).boxed().toList();
        MID_e.setItems(range);
        MID_e.addValueChangeListener(event -> {
            if (MID_e.getValue() != null) {
                String L = U.round2(IF.frameMembers.get(MID_e.getValue() - 1).L).toPlainString();
                d_e.setHelperText("With Respect to the Local Coordinate System (L = " + L + " " + U.m + ")");
                if (d_e.getValue() != null) {
                    if (d_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                        d_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                    }
                }
            }
            else {
                d_e.setHelperText("With Respect to the Local Coordinate System");
            }
        });
        PLT_e.setItems(PLTs);

        d_e.addValueChangeListener(event -> {
            if (MID_e.getValue() != null && d_e.getValue() != null) {
                if (d_e.getValue().compareTo(IF.frameMembers.get(MID_e.getValue() - 1).L) > 0) {
                    d_e.setValue(IF.frameMembers.get(MID_e.getValue() - 1).L);
                }
            }
        });

        InputFrame.trimMin(d_e, 0,2);
        InputFrame.trimMinMax(Fx_e, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Fy_e, -1000000, 1000000, 4);
        InputFrame.trimMinMax(Mz_e, -1000000, 1000000, 4);

        MID_e.setWidthFull();
        PLT_e.setWidthFull();
        d_e.setWidthFull();
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

        d_e.setHelperText("With Respect to the Local Coordinate System");
        Fx_e.setHelperComponent(h1);
        Fy_e.setHelperComponent(h2);
        Mz_e.setHelperComponent(h3);

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.m);
        p2.setText(U.kN);
        p3.setText(U.kN);
        p4.setText(U.kNm);

        d_e.setSuffixComponent(p1);
        Fx_e.setSuffixComponent(p2);
        Fy_e.setSuffixComponent(p3);
        Mz_e.setSuffixComponent(p4);

        d_e.setPlaceholder("0.00");
        Fx_e.setPlaceholder("0.0000");
        Fy_e.setPlaceholder("0.0000");
        Mz_e.setPlaceholder("0.0000");

        MID_e.setValue(range.get(PL.MN));
        PLT_e.setValue(PLTs[PL.LT - 1]);
        d_e.setValue(PL.d);
        Fx_e.setValue(PL.load[0]);
        Fy_e.setValue(PL.load[1]);
        Mz_e.setValue(PL.load[2]);

        VerticalLayout pointLoadForm_e = new VerticalLayout();
        pointLoadForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        pointLoadForm_e.add(MID_e, PLT_e, d_e, Fx_e, Fy_e, Mz_e);
        pointLoadForm_e.setSpacing(false);

        return  pointLoadForm_e;
    }

    private void validateAndEdit (Dialog dialog, PointLoad PL, MyUI UI) {
        BigDecimal d_ = d_e.getValue() == null ? BigDecimal.ZERO : d_e.getValue();
        BigDecimal Fx_ = Fx_e.getValue() == null ? BigDecimal.ZERO : Fx_e.getValue();
        BigDecimal Fy_ = Fy_e.getValue() == null ? BigDecimal.ZERO : Fy_e.getValue();
        BigDecimal Mz_ = Mz_e.getValue() == null ? BigDecimal.ZERO : Mz_e.getValue();

        if (MID_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Frame Member ID\" field is not selected.");
        }
        else if (PLT_e.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Orientation\" field is not selected.");
        }
        else if (BigDecimal.ZERO.compareTo(Fx_) == 0 && BigDecimal.ZERO.compareTo(Fy_) == 0 && BigDecimal.ZERO.compareTo(Mz_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : No load magnitude was input.");
        }
        else {
            try {
                int i = IF.pointLoads.indexOf(PL);
                IF.addPointLoad(i,MID_e.getValue() - 1, Arrays.asList(PLTs).indexOf(PLT_e.getValue()) + 1, d_, Fx_, Fy_, Mz_);
                IF.removePointLoad(PL);

                pointLoadGrid.getDataProvider().refreshAll();
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
