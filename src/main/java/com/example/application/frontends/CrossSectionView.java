package com.example.application.frontends;

import com.example.application.backends.*;
import com.example.application.tools.InputFrame;
import com.example.application.tools.ErrorNotification;
import com.example.application.tools.MyUI;
import com.example.application.tools.Unit;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "cross-section", layout = BuildView.class)
@PageTitle("Cross Section | PFSAT")
public class CrossSectionView extends VerticalLayout {

    private VerticalLayout crossSectionForm;
    private Grid<CrossSection> crossSectionGrid;
    private GraphView graphView;

    private BigDecimalField A;
    private BigDecimalField E;
    private BigDecimalField I;
    private Button ADD;

    private BigDecimalField A_e;
    private BigDecimalField E_e;
    private BigDecimalField I_e;

    private InputFrame IF;
    private Unit U;

    public CrossSectionView (@Autowired MyUI UI) {
        addClassName("cross-section-view");
        setSizeFull();

        IF = UI.getInputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        U = UI.getUnit();
        UI.crossSectionView = this;

        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();

        configureForm(UI);
        configureGrid(UI);
        configureGraph(UI);

        SplitLayout S1 = new SplitLayout(crossSectionForm, crossSectionGrid);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(70);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm (MyUI UI) {
        crossSectionForm = new VerticalLayout();
        crossSectionForm.addClassNames("cross-section-form");

        A = new BigDecimalField("Cross Sectional Area");
        E = new BigDecimalField("Modulus of Elasticity");
        I = new BigDecimalField("Moment of Inertia");
        ADD = new Button("Add");

        ADD.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ADD.addClickListener(event -> validateAndSave(UI));
        ADD.addClickShortcut(Key.ENTER);

        InputFrame.trimMinMax(A, 0, 1000000, 2);
        InputFrame.trimMinMax(E, 0, 1000000, 2);
        InputFrame.trimMinMax(I, 0, 1000000, 2);

        A.setWidthFull();
        E.setWidthFull();
        I.setWidthFull();

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();

        p1.setText(U.unitIndex == 0 ? "×10\u00B3 " + U.mm2 : U.mm2);
        p2.setText(U.GPa);
        p3.setText(U.unitIndex == 0 ? "×10\u2076 " + U.mm4 : U.mm4);

        A.setSuffixComponent(p1);
        E.setSuffixComponent(p2);
        I.setSuffixComponent(p3);

        A.setPlaceholder("0.00");
        E.setPlaceholder("0.00");
        I.setPlaceholder("0.00");

        crossSectionForm.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.add(A, E, I);
        v.setSpacing(false);
        crossSectionForm.add(v, ADD);
        crossSectionForm.setSpacing(false);
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal A_ = A.getValue() == null ? BigDecimal.ZERO : U.convertA1(A.getValue());
        BigDecimal E_ = E.getValue() == null ? BigDecimal.ZERO : U.convertE1(E.getValue());
        BigDecimal I_ = I.getValue() == null ? BigDecimal.ZERO : U.convertI1(I.getValue());

        if (IF.crossSections.size() == InputFrame.crossSectionMaxItem) {
            ErrorNotification.displayNotification("Maximum Items Error : The maximum number of point loads is limited to " + InputFrame.crossSectionMaxItem + ".");
        }
        else if (A_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of cross-sectional area cannot be zero.");
        }
        else if (E_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of modulus of elasticity cannot be zero.");
        }
        else if (I_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of moment of inertia cannot be zero.");
        }
        else {
            boolean add = true;
            for (CrossSection cs : IF.crossSections) {
                if (cs.A.compareTo(A_) == 0 && cs.E.compareTo(E_) == 0 && cs.I.compareTo(I_) == 0) {
                    add = false;
                    break;
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The cross-section with these values already exists.");
            }
            else {
                try {
                    IF.addCrossSection(A_, E_, I_);

                    A.clear();
                    E.clear();
                    I.clear();
                    crossSectionGrid.getDataProvider().refreshAll();
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
        crossSectionGrid = new Grid<>(CrossSection.class, false);
        List<CrossSection> crossSectionList = IF.crossSections;

        crossSectionGrid.addClassNames("cross-section-grid");
        crossSectionGrid.addColumn(CrossSection -> crossSectionList.indexOf(CrossSection) + 1).setHeader(" ID ")
                .setFrozen(true).setFlexGrow(0);

        String text1 = U.unitIndex == 0 ? "A (×10\u00B3 " + U.mm2 + ")" : "A (" + U.mm2 + ")";
        String text2 = U.unitIndex == 0 ? "I (×10\u2076 " + U.mm4 + ")" : "I (" + U.mm4 + ")";

        crossSectionGrid.addColumn(CrossSection -> U.round(U.convertA2(CrossSection.A)).toPlainString()).setHeader(text1);
        crossSectionGrid.addColumn(CrossSection -> U.round(U.convertE2(CrossSection.E)).toPlainString()).setHeader("E (" + U.GPa + ")");
        crossSectionGrid.addColumn(CrossSection -> U.round(U.convertI2(CrossSection.I)).toPlainString()).setHeader(text2);

        crossSectionGrid.addColumn(new ComponentRenderer<>(Button::new, (button, CrossSection) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> edit(CrossSection, UI));
            button.setIcon(new Icon("lumo", "edit"));
        })).setHeader("Edit").setFrozenToEnd(true).setFlexGrow(0);

        crossSectionGrid.addColumn(new ComponentRenderer<>(Button::new, (button, CrossSection) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> delete(CrossSection, UI));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("Remove").setFrozenToEnd(true).setFlexGrow(0);

        crossSectionGrid.getColumns().forEach(col -> col.setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER));

        crossSectionGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        crossSectionGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        crossSectionGrid.setItems(crossSectionList);

        crossSectionGrid.addSelectionListener(selection -> {
            Optional<CrossSection> optionalCS = selection.getFirstSelectedItem();
            if (optionalCS.isPresent()) {
                IF.markedFrameMembers.clear();
                for (FrameMember fm : IF.frameMembers) {
                    if (fm.A.compareTo(optionalCS.get().A) == 0 && fm.E.compareTo(optionalCS.get().E) == 0 && fm.I.compareTo(optionalCS.get().I) == 0) {
                        IF.markedFrameMembers.add(IF.frameMembers.indexOf(fm));
                    }
                }
                graphView.refreshGraph(UI);
            }
            else {
                IF.markedFrameMembers.clear();
                graphView.refreshGraph(UI);
            }
        });
    }

    private void delete (CrossSection CS, MyUI UI) {
        IF.removeCrossSection(CS);

        crossSectionGrid.getDataProvider().refreshAll();
        graphView.refreshGraph(UI);
        UI.refreshTabs();
    }

    private void edit (CrossSection CS, MyUI UI) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Edit Cross Section");

        dialog.add(configureEditForm (CS, UI));

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            graphView.refreshGraph(UI);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(CLOSE);

        Button EDIT = new Button("Edit", e -> validateAndEdit(dialog, CS, UI));
        dialog.getFooter().add(EDIT);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private VerticalLayout configureEditForm (CrossSection CS, MyUI UI) {
        A_e = new BigDecimalField("Cross Sectional Area");
        E_e = new BigDecimalField("Modulus of Elasticity");
        I_e = new BigDecimalField("Moment of Inertia");

        InputFrame.trimMinMax(A_e, 0, 1000000, 2);
        InputFrame.trimMinMax(E_e, 0, 1000000, 2);
        InputFrame.trimMinMax(I_e, 0, 1000000, 2);

        A_e.setWidthFull();
        E_e.setWidthFull();
        I_e.setWidthFull();

        Div p1 = new Div();
        Div p2 = new Div();
        Div p3 = new Div();

        p1.setText(U.unitIndex == 0 ? "×10\u00B3 " + U.mm2 : U.mm2);
        p2.setText(U.GPa);
        p3.setText(U.unitIndex == 0 ? "×10\u2076 " + U.mm4 : U.mm4);

        A_e.setSuffixComponent(p1);
        E_e.setSuffixComponent(p2);
        I_e.setSuffixComponent(p3);

        A_e.setPlaceholder("0.00");
        E_e.setPlaceholder("0.00");
        I_e.setPlaceholder("0.00");

        A_e.setValue(U.round(U.convertA2(CS.A)));
        E_e.setValue(U.round(U.convertE2(CS.E)));
        I_e.setValue(U.round(U.convertI2(CS.I)));

        VerticalLayout crossSectionForm_e = new VerticalLayout();
        crossSectionForm_e.setAlignItems(FlexComponent.Alignment.CENTER);
        crossSectionForm_e.add(A_e, E_e, I_e);
        crossSectionForm_e.setSpacing(false);

        return crossSectionForm_e;
    }

    private void validateAndEdit (Dialog dialog, CrossSection CS, MyUI UI) {
        BigDecimal A_ = A_e.getValue() == null ? BigDecimal.ZERO : U.convertA1(A_e.getValue());
        BigDecimal E_ = E_e.getValue() == null ? BigDecimal.ZERO : U.convertE1(E_e.getValue());
        BigDecimal I_ = I_e.getValue() == null ? BigDecimal.ZERO : U.convertI1(I_e.getValue());

        if (A_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of cross-sectional cannot be zero.");
        }
        else if (E_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of modulus of elasticity cannot be zero.");
        }
        else if (I_.compareTo(BigDecimal.ZERO) == 0) {
            ErrorNotification.displayNotification("Zero Value Error : The value of moment of inertia cannot be zero.");
        }
        else {
            boolean add = true;
            for (CrossSection cs : IF.crossSections) {
                if (cs.A.compareTo(CS.A) != 0 && cs.E.compareTo(CS.E) != 0 && cs.I.compareTo(CS.I) != 0) {
                    if (cs.A.compareTo(A_) == 0 && cs.E.compareTo(E_) == 0 && cs.I.compareTo(I_) == 0) {
                        add = false;
                        break;
                    }
                }
            }
            if (!add) {
                ErrorNotification.displayNotification("Overlap Error : The cross-section with these values already exists.");
            }
            else {
                try {
                    int i = IF.crossSections.indexOf(CS);
                    IF.addCrossSection(i, A_, E_, I_);
                    for (FrameMember fm : IF.frameMembers) {
                        if (fm.A.compareTo(CS.A) == 0 && fm.E.compareTo(CS.E) == 0 && fm.I.compareTo(CS.I) == 0) {
                            fm.A = A_;
                            fm.E = E_;
                            fm.I = I_;
                        }
                    }
                    IF.crossSections.remove(CS);

                    crossSectionGrid.getDataProvider().refreshAll();
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
