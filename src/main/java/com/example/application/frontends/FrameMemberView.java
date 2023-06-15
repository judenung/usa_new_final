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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Route(value = "frame-member", layout = BuildView.class)
@PageTitle("Frame Member | PFSAT")
public class FrameMemberView extends VerticalLayout {

    private VerticalLayout frameMemberForm;
    private Grid<FrameMember> frameMemberGrid;
    private GraphView graphView;

    private BigDecimalField BCx;
    private BigDecimalField BCy;
    private BigDecimalField Px;
    private BigDecimalField Py;
    private ComboBox<Integer> CS;
    private Button ADD;

    private BigDecimalField BCx_e;
    private BigDecimalField BCy_e;
    private BigDecimalField Px_e;
    private BigDecimalField Py_e;
    private ComboBox<Integer> CS_e;

    private InputFrame IF;
    private Unit U;

    public FrameMemberView (@Autowired MyUI UI) {
        addClassName("frame-member-view");
        setSizeFull();

        IF = UI.getInputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        U = UI.getUnit();
        UI.frameMemberView = this;

        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();

        configureForm(UI);
        configureGrid(UI);
        configureGraph(UI);

        SplitLayout S1 = new SplitLayout(frameMemberForm, frameMemberGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        frameMemberForm = new VerticalLayout();
        frameMemberForm.addClassNames("frame-member-form");

        BCx = new BigDecimalField("Begin X Coordinate");
        BCy = new BigDecimalField("Begin Y Coordinate");
        Px = new BigDecimalField("Horizontal Distant from the Begin Point");
        Py = new BigDecimalField("Vertical Distant from the Begin Point");
        CS = new ComboBox<>("Cross Section ID");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        List<Integer> range = IntStream.rangeClosed(1, IF.crossSections.size()).boxed().toList();
        CS.setItems(range);

        InputFrame.trimMinMax(BCx, 0, 100, 2);
        InputFrame.trimMinMax(BCy, 0, 100, 2);
        InputFrame.trimMinMax(Px, -100, 100, 2);
        InputFrame.trimMinMax(Py, -100, 100, 2);

        BCx.setWidthFull();
        BCy.setWidthFull();
        Px.setWidthFull();
        Py.setWidthFull();
        CS.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        Px.setHelperComponent(h1);
        Py.setHelperComponent(h2);

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.m);
        p2.setText(U.m);
        p3.setText(U.m);
        p4.setText(U.m);

        BCx.setSuffixComponent(p1);
        BCy.setSuffixComponent(p2);
        Px.setSuffixComponent(p3);
        Py.setSuffixComponent(p4);

        BCx.setPlaceholder("0.00");
        BCy.setPlaceholder("0.00");
        Px.setPlaceholder("0.00");
        Py.setPlaceholder("0.00");

        frameMemberForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        if (IF.isDefaultCS) {
            v.add(BCx, BCy, Px, Py);
        }
        else {
            v.add(BCx, BCy, Px, Py, CS);
        }
        v.setSpacing(false);
        frameMemberForm.add(v, ADD);
        frameMemberForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal BCx_ = BCx.getValue() == null ? BigDecimal.ZERO : BCx.getValue();
        BigDecimal BCy_ = BCy.getValue() == null ? BigDecimal.ZERO : BCy.getValue();
        BigDecimal ECx_ = Px.getValue() == null ? BCx_ : BCx_.add(Px.getValue());
        BigDecimal ECy_ = Py.getValue() == null ? BCy_ : BCy_.add(Py.getValue());

        if (IF.frameMembers.size() == InputFrame.frameMemberMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.frameMemberMaxItem + ".");
        }
        else if (CS.isEmpty() && !IF.isDefaultCS) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Cross Section ID\n\" field is not selected.");
        }
        else if (BCx_.compareTo(IF.PW) > 0 || BCy_.compareTo(IF.PH) > 0 || BCx_.compareTo(BigDecimal.ZERO) < 0 || BCy_.compareTo(BigDecimal.ZERO) < 0 ||
                 ECx_.compareTo(IF.PW) > 0 || ECy_.compareTo(IF.PH) > 0 || ECx_.compareTo(BigDecimal.ZERO) < 0 || ECy_.compareTo(BigDecimal.ZERO) < 0 ) {
            ErrorNotification.displayNotification("Out of Bound Error : The frame-member is located outside of the grid.");
        }
        else if (BCx_.compareTo(ECx_) == 0 && BCy_.compareTo(ECy_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of frame-member length cannot be 0.");
        }
        else {
            try {
                if (IF.isDefaultCS) {
                    IF.addFrameMember(BCx_, BCy_, ECx_, ECy_, InputFrame.defaultCrossSection);
                }
                else {
                    IF.addFrameMember(BCx_, BCy_, ECx_, ECy_, CS.getValue() - 1);
                }
                IF.frameMembers.get(IF.frameMembers.size() - 1).ID = IF.frameMembers.size();

                BCx.clear();
                BCy.clear();
                Px.clear();
                Py.clear();
                frameMemberGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                UI.refreshTabs();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void configureGrid (MyUI UI) {
        frameMemberGrid = new Grid<>(FrameMember.class, false);
        List<FrameMember> frameMemberList = IF.frameMembers;

        frameMemberGrid.addClassNames("frame-member-grid");
        frameMemberGrid.addColumn(FrameMember -> frameMemberList.indexOf(FrameMember) + 1).setHeader("ID")
                .setFrozen(true).setFlexGrow(0);

        frameMemberGrid.addColumn(FrameMember -> FrameMember.BCx.toPlainString()).setHeader("X\u2081 (" + U.m + ")");
        frameMemberGrid.addColumn(FrameMember -> FrameMember.BCy.toPlainString()).setHeader("Y\u2081 (" + U.m + ")");
        frameMemberGrid.addColumn(FrameMember -> FrameMember.ECx.toPlainString()).setHeader("X\u2082 (" + U.m + ")");
        frameMemberGrid.addColumn(FrameMember -> FrameMember.ECy.toPlainString()).setHeader("Y\u2082 (" + U.m + ")");
        frameMemberGrid.addColumn(FrameMember -> U.round2(FrameMember.L).toPlainString()).setHeader("L (" + U.m + ")");
        if (!IF.isDefaultCS) {
            String text1 = U.unitIndex == 0 ? "A (×10\u00B3 " + U.mm2 + ")" : "A (" + U.mm2 + ")";
            String text2 = U.unitIndex == 0 ? "I (×10\u2076 " + U.mm4 + ")" : "I (" + U.mm4 + ")";
            frameMemberGrid.addColumn(FrameMember -> U.round(U.convertA2(FrameMember.A)).toPlainString()).setHeader(text1);
            frameMemberGrid.addColumn(FrameMember -> U.round(U.convertE2(FrameMember.E)).toPlainString()).setHeader("E (" + U.GPa + ")");
            frameMemberGrid.addColumn(FrameMember -> U.round(U.convertI2(FrameMember.I)).toPlainString()).setHeader(text2);
        }

        frameMemberGrid.addColumn(new ComponentRenderer<>(Button::new, (button, FrameMember) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(FrameMember, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        frameMemberGrid.addColumn(new ComponentRenderer<>(Button::new, (button, FrameMember) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(FrameMember, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        frameMemberGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        frameMemberGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        frameMemberGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        frameMemberGrid.setItems(frameMemberList);

        frameMemberGrid.addSelectionListener(selection -> {
            Optional<FrameMember> optionalFM = selection.getFirstSelectedItem();
            if (optionalFM.isPresent()) {
                IF.markedFrameMembers.clear();
                IF.markedFrameMembers.add(IF.frameMembers.indexOf(optionalFM.get()));
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedFrameMembers.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (FrameMember FM, MyUI UI) {
        IF.removeFrameMember(FM);

        frameMemberGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (FrameMember FM, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Frame Member");

        dialog.add(configureEditForm (FM, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, FM, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (FrameMember FM, MyUI UI) {
        BCx_e = new BigDecimalField("Begin X Coordinate");
        BCy_e = new BigDecimalField("Begin Y Coordinate");
        Px_e = new BigDecimalField("Horizontal Distant from the Begin Point");
        Py_e = new BigDecimalField("Vertical Distant from the Begin Point");
        CS_e = new ComboBox<>("Cross Section ID");

        List<Integer> range = IntStream.rangeClosed(1, IF.crossSections.size()).boxed().toList();
        CS_e.setItems(range);

        InputFrame.trimMinMax(BCx_e, 0, 100, 2);
        InputFrame.trimMinMax(BCy_e, 0, 100, 2);
        InputFrame.trimMinMax(Px_e, -100, 100, 2);
        InputFrame.trimMinMax(Py_e, -100, 100, 2);

        BCx_e.setWidthFull();
        BCy_e.setWidthFull();
        Px_e.setWidthFull();
        Py_e.setWidthFull();
        CS_e.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        Px_e.setHelperComponent(h1);
        Py_e.setHelperComponent(h2);

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();
        Div p4 = new Div();

        p1.setText(U.m);
        p2.setText(U.m);
        p3.setText(U.m);
        p4.setText(U.m);

        BCx_e.setSuffixComponent(p1);
        BCy_e.setSuffixComponent(p2);
        Px_e.setSuffixComponent(p3);
        Py_e.setSuffixComponent(p4);

        BCx_e.setPlaceholder("0.00");
        BCy_e.setPlaceholder("0.00");
        Px_e.setPlaceholder("0.00");
        Py_e.setPlaceholder("0.00");

        BCx_e.setValue(FM.BCx);
        BCy_e.setValue(FM.BCy);
        Px_e.setValue(FM.ECx.subtract(FM.BCx));
        Py_e.setValue(FM.ECy.subtract(FM.BCy));
        int i = 0;
        for (CrossSection cs : IF.crossSections) {
            if (cs.A.compareTo(FM.A) == 0 && cs.E.compareTo(FM.E) == 0 && cs.I.compareTo(FM.I) == 0) {
                break;
            }
            i++;
        }

        VerticalLayout frameMemberForm_e = new VerticalLayout();
        frameMemberForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        if (IF.isDefaultCS) {
            frameMemberForm_e.add(BCx_e, BCy_e, Px_e, Py_e);
        }
        else {
            CS_e.setValue(range.get(i));
            frameMemberForm_e.add(BCx_e, BCy_e, Px_e, Py_e, CS_e);
        }
        frameMemberForm_e.setSpacing(false);

        return frameMemberForm_e;
    }

    private void validateAndEdit (Dialog dialog, FrameMember FM, MyUI UI) {
        BigDecimal BCx_ = BCx_e.getValue() == null ? BigDecimal.ZERO : BCx_e.getValue();
        BigDecimal BCy_ = BCy_e.getValue() == null ? BigDecimal.ZERO : BCy_e.getValue();
        BigDecimal ECx_ = Px_e.getValue() == null ? BCx_ : BCx_.add(Px_e.getValue());
        BigDecimal ECy_ = Py_e.getValue() == null ? BCy_ : BCy_.add(Py_e.getValue());

        if (CS_e.isEmpty() && !IF.isDefaultCS) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Cross Section ID\n\" field is not selected.");
        }
        else if (BCx_.compareTo(IF.PW) > 0 || BCy_.compareTo(IF.PH) > 0 || BCx_.compareTo(BigDecimal.ZERO) < 0 || BCy_.compareTo(BigDecimal.ZERO) < 0 ||
                ECx_.compareTo(IF.PW) > 0 || ECy_.compareTo(IF.PH) > 0 || ECx_.compareTo(BigDecimal.ZERO) < 0 || ECy_.compareTo(BigDecimal.ZERO) < 0 ) {
            ErrorNotification.displayNotification("Out of Bound Error : The frame-member is located outside of the grid.");
        }
        else if (BCx_.compareTo(ECx_) == 0 && BCy_.compareTo(ECy_) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of frame-member length cannot be 0.");
        }
        else {
            try {
                int i = IF.frameMembers.indexOf(FM);
                if (IF.isDefaultCS) {
                    IF.addFrameMember(i, BCx_, BCy_, ECx_, ECy_, InputFrame.defaultCrossSection);
                }
                else {
                    IF.addFrameMember(i, BCx_, BCy_, ECx_, ECy_, CS_e.getValue() - 1);
                }
                IF.removeFrameMember(FM);
                IF.frameMembers.get(i).ID = i + 1;

                frameMemberGrid.getDataProvider().refreshAll();
                graphView.refreshGraph(UI);
                UI.refreshTabs();
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
