package com.example.application.frontends;

import com.example.application.backends.*;
import com.example.application.tools.*;
import com.storedobject.chart.Color;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import org.jsoup.helper.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

public class GraphView extends VerticalLayout {

    private VerticalLayout optionForm;
    private SplitLayout frameMemberOptionForm;
    private Graph G;

    private Button FORCE_COMPONENT_CALCULATOR;
    private IntegerField finiteElement;
    private ComboBox<String> gridSize;
    private BigDecimalField meshSize;
    private ComboBox<String> unitChange;
    private ComboBox<String> crossSection;
    private Button SAVE_OPTION;
    private Button CLEAR_ALL;

    private InputFrame IF;
    private OutputFrame OF;
    private Unit U;

    private String[] GSs;
    private String[] UCs = {"SI", "BG"};
    private String[] CSs = {"Default", "Custom"};

    public GraphView (@Autowired MyUI UI) {
        addClassName("graph-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        IF = UI.getInputFrame();
        OF = UI.getOutputFrame();
        U = UI.getUnit();
        UI.graphView = this;

        refreshGraph(UI);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (IF.isDefaultCS && IF.frameMembers.isEmpty()) {
            this.getUI().ifPresent(ui -> ui.navigate("frame-member"));
        }
        else if (!IF.isDefaultCS && IF.crossSections.isEmpty()){
            this.getUI().ifPresent(ui -> ui.navigate("cross-section"));
        }
    }

    public void refreshGraph (MyUI UI) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        Div progressBarLabel = new Div();
        progressBarLabel.setText("Loading Graph...");
        add(progressBarLabel, progressBar);

        this.removeAll();

        configureOptionForm(UI);
        configureGraph(UI);

        VerticalLayout v = new VerticalLayout();
        v.add(G);
        v.setPadding(true);
        v.setJustifyContentMode(JustifyContentMode.CENTER);
        v.setAlignItems(Alignment.CENTER);
        frameMemberOptionForm = new SplitLayout(v, optionForm);
        frameMemberOptionForm.setSplitterPosition(70);
        frameMemberOptionForm.setSizeFull();

        add(frameMemberOptionForm);

        remove(progressBarLabel, progressBar);
    }

    private void configureOptionForm (MyUI UI) {
        optionForm = new VerticalLayout();

        FORCE_COMPONENT_CALCULATOR = new Button("Force Component Calculator");
        finiteElement = new IntegerField("Number of Finite Element");
        gridSize = new ComboBox<>("Grid Size");
        meshSize = new BigDecimalField("Mesh Size");
        unitChange = new ComboBox<>("Unit System");
        crossSection = new ComboBox<>("Cross Section");
        SAVE_OPTION = new Button("Save Option");
        CLEAR_ALL = new Button("Clear All");

        FORCE_COMPONENT_CALCULATOR.addClickListener(event -> createCalculator());
        SAVE_OPTION.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        SAVE_OPTION.addClickListener(event -> saveOption(UI));
        CLEAR_ALL.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        CLEAR_ALL.addClickListener(event -> clearAll(UI));

        GSs = U.mxm;
        gridSize.setItems(GSs);
        unitChange.setItems(UCs);
        crossSection.setItems(CSs);

        finiteElement.setValue(calNumberOfFiniteElement());
        finiteElement.setReadOnly(true);

        gridSize.setValue(GSs[IF.PH.intValue() / 10 - 1]);
        unitChange.setValue(UCs[U.unitIndex]);
        unitChange.setEnabled((IF.frameMembers.isEmpty() && IF.isDefaultCS) || (IF.crossSections.isEmpty() && !IF.isDefaultCS));
        crossSection.setValue(IF.isDefaultCS ? CSs[0] : CSs[1]);
        crossSection.setEnabled((IF.frameMembers.isEmpty() && IF.isDefaultCS) || (IF.crossSections.isEmpty() && !IF.isDefaultCS));

        meshSize.setValue(IF.MS.stripTrailingZeros());
        InputFrame.trimMinMax(meshSize, 0, 1000,1);

        gridSize.setWidthFull();
        meshSize.setWidthFull();
        finiteElement.setWidthFull();
        unitChange.setWidthFull();
        crossSection.setWidthFull();

        finiteElement.setHelperText("Maximum Amount = 300");

        Div p = new Div();
        p.setText(U.m);
        meshSize.setSuffixComponent(p);
        meshSize.setHelperText("Minimum Value = " + IF.min_MS.stripTrailingZeros().toPlainString() + " " + U.m);
        meshSize.setPlaceholder(IF.min_MS.multiply(BigDecimal.TEN).stripTrailingZeros() + "");
        unitChange.setHelperText("Enable Only After Clear All");
        crossSection.setHelperText("Enable Only After Clear All");

        optionForm.setAlignItems(Alignment.CENTER);
        optionForm.setJustifyContentMode(JustifyContentMode.CENTER);
        VerticalLayout v = new VerticalLayout();
        v.setAlignItems(Alignment.CENTER);
        v.add(FORCE_COMPONENT_CALCULATOR, finiteElement, gridSize, meshSize, unitChange, crossSection);
        v.setSpacing(false);
        optionForm.add(v, SAVE_OPTION, CLEAR_ALL);
        optionForm.setWidth("70");
    }

    private void validateAndSave (MyUI UI) {
        BigDecimal meshSize_ = meshSize.getValue() == null ? IF.min_MS.multiply(BigDecimal.TEN).stripTrailingZeros() : meshSize.getValue();

        if (gridSize.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Grid Size\n\" field is not selected.");
        }
        if (unitChange.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Unit System\n\" field is not selected.");
        }
        if (crossSection.isEmpty()) {
            ErrorNotification.displayNotification("Unselected Fields Error : \"Cross Section\n\" field is not selected.");
        }
        else {
            try {
                IF.PH = new BigDecimal((Arrays.asList(GSs).indexOf(gridSize.getValue()) + 1) * 10 + "");
                IF.PW = new BigDecimal((Arrays.asList(GSs).indexOf(gridSize.getValue()) + 1) * 10 + "");
                IF.removeOutOfBoundFrameMember();

                IF.min_MS = new BigDecimal((Arrays.asList(GSs).indexOf(gridSize.getValue()) + 1) + "").divide(BigDecimal.TEN, 1, RoundingMode.FLOOR);
                if (meshSize_.compareTo(IF.min_MS) < 0) {
                    IF.MS = IF.min_MS;
                }
                else {
                    IF.MS = meshSize_;
                }

                U.setUnit(Arrays.asList(UCs).indexOf(unitChange.getValue()));

                IF.isDefaultCS = Arrays.asList(CSs).indexOf(crossSection.getValue()) == 0;
                if (crossSection.isEnabled()) {
                    if (IF.isDefaultCS) {
                        SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate("frame-member"));
                    }
                    else {
                        SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate("cross-section"));
                    }
                }

                refreshGraph(UI);
                UI.refreshAllGrid();
                UI.refreshTabs();
            }
            catch (ValidationException e) {
                ErrorNotification.displayNotification("System Error");
            }
        }
    }

    private void createCalculator () {
        FORCE_COMPONENT_CALCULATOR.setEnabled(false);

        Dialog dialog = new Dialog();
        dialog.setModal(false);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.setHeaderTitle("Force Component Calculator");

        BigDecimalField F = new BigDecimalField("Force Magnitude");
        BigDecimalField A = new BigDecimalField("Rotational Angle");
        BigDecimalField Fx = new BigDecimalField("Resulted Horizontal Load");
        BigDecimalField Fy = new BigDecimalField("Resulted Vertical Load");

        Fx.setReadOnly(true);
        Fy.setReadOnly(true);

        InputFrame.trimMinMax(F, -1000000, 1000000, 4);
        InputFrame.trimAngle(A, 360, 2);

        F.setWidthFull();
        A.setWidthFull();
        Fx.setWidthFull();
        Fy.setWidthFull();

        Div h1 = new Div();
        Div h2 = new Div();
        Div h3 = new Div();
        Div h4 = new Div();

        h1.add(IconStyle.setStyle2(VaadinIcon.ARROW_DOWN));
        h1.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h2.add(IconStyle.setStyle2(VaadinIcon.ROTATE_LEFT));
        h2.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h3.add(IconStyle.setStyle2(VaadinIcon.ARROW_RIGHT));
        h3.add(IconStyle.setStyle2(VaadinIcon.PLUS));
        h4.add(IconStyle.setStyle2(VaadinIcon.ARROW_UP));
        h4.add(IconStyle.setStyle2(VaadinIcon.PLUS));

        F.setHelperComponent(h1);
        A.setHelperComponent(h2);
        Fx.setHelperComponent(h3);
        Fy.setHelperComponent(h4);

        Div s1 = new Div();
        Div s2 = new Div();
        Div s3 = new Div();
        Div s4 = new Div();

        s1.setText(U.kN);
        s2.setText(U.deg);
        s3.setText(U.kN);
        s4.setText(U.kN);

        F.setSuffixComponent(s1);
        A.setSuffixComponent(s2);
        Fx.setSuffixComponent(s3);
        Fy.setSuffixComponent(s4);

        F.setPlaceholder("0.0000");
        A.setPlaceholder("0.00");
        Fx.setPlaceholder("0.0000");
        Fy.setPlaceholder("0.0000");

        VerticalLayout calculator = new VerticalLayout();
        calculator.setAlignItems(FlexComponent.Alignment.CENTER);
        calculator.add(F, A, Fx, Fy);
        calculator.setSpacing(false);

        dialog.add(calculator);

        Button CLOSE = new Button("Close", e -> {
            dialog.close();
            FORCE_COMPONENT_CALCULATOR.setEnabled(true);
        });
        CLOSE.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button CALCULATE = new Button("Calculate", e -> {
            BigDecimal F_ = F.getValue() == null ? BigDecimal.ZERO : F.getValue();
            BigDecimal A_ = A.getValue() == null ? BigDecimal.ZERO : A.getValue();
            double Fx_ = U.round2(F_.doubleValue() * Math.sin(Math.toRadians(A_.doubleValue())));
            double Fy_ = U.round2(- F_.doubleValue() * Math.cos(Math.toRadians(A_.doubleValue())));
            Fx.setValue(new BigDecimal(Fx_ + ""));
            Fy.setValue(new BigDecimal(Fy_ + ""));
        });
        CALCULATE.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(CLOSE);
        dialog.getFooter().add(CALCULATE);

        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.open();
    }

    private void saveOption (MyUI UI) {
        boolean warn = false;
        BigDecimal PH = new BigDecimal((Arrays.asList(GSs).indexOf(gridSize.getValue()) + 1) * 10 + "");
        BigDecimal PW = new BigDecimal((Arrays.asList(GSs).indexOf(gridSize.getValue()) + 1) * 10 + "");

        for (FrameMember fm : IF.frameMembers) {
            if (fm.BCx.compareTo(PW) > 0 || fm.ECx.compareTo(PW) > 0 || fm.BCy.compareTo(PH) > 0 || fm.ECy.compareTo(PH) > 0) {
                warn = true;
                break;
            }
        }

        if (warn) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Save Option");
            dialog.setText("Some items will be lost due to the shrinkage of the grid size.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Proceed");
            dialog.setConfirmButtonTheme("primary");
            dialog.addConfirmListener(event -> {
                validateAndSave(UI);
            });

            dialog.open();
        }
        else {
            validateAndSave(UI);
        }
    }

    private void clearAll (MyUI UI) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Clear All");
        dialog.setText("This action will clear all the items in the grid.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Proceed");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            IF.clearAll();
            OF.clearAll();
            refreshGraph(UI);
            UI.refreshAllGrid();
            if (IF.isDefaultCS) {
                SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate("frame-member"));
            }
            else {
                SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate("cross-section"));
            }
            UI.refreshTabs();
        });

        dialog.open();
    }

    private void configureGraph (MyUI UI) {
        G = new Graph(UI);
        G.setSizeFull();
        if (UI.buildView.isDark) {
            G.setDarkTheme();
            G.setDefaultBackground(new Color(UI.getGraphStyle().darkModeBackGroundColor));
        }
    }

    public int calNumberOfFiniteElement () {

        int finiteElements = 0;

        for (FrameMember fm : IF.frameMembers) {
            ArrayList<BigDecimal> intermediateJoints = new ArrayList<>();
            intermediateJoints.add(BigDecimal.ZERO);
            intermediateJoints.add(fm.L);

            for (PointLoad pl : IF.pointLoads) {
                BigDecimal d;
                if (pl.MN == IF.frameMembers.indexOf(fm)) {
                    d = pl.d;
                    boolean add = true;
                    for (BigDecimal ij : intermediateJoints) {
                        if (ij.compareTo(d) == 0) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        short i = 0;
                        for (; i < intermediateJoints.size(); i++) {
                            if (d.compareTo(intermediateJoints.get(i)) < 0) {
                                break;
                            }
                        }
                        intermediateJoints.add(i, d);
                    }
                }
            }

            for (LinearlyDistributedLoad ldl : IF.linearlyDistributedLoads) {
                BigDecimal d;
                if (ldl.MN == IF.frameMembers.indexOf(fm)) {
                    for (short i = 0; i < 3; i++) {
                        if (i == 0) {
                            d = ldl.load[2];
                        } else if (i == 1) {
                            d = fm.L.subtract(ldl.load[3]);
                        } else if (i == 2 && (ldl.load[0].multiply(ldl.load[1]).compareTo(BigDecimal.ZERO) < 0)) {
                            BigDecimal fmL = IF.frameMembers.get(ldl.MN).L;
                            d = ldl.load[0].multiply(fmL.subtract(ldl.load[2]).subtract((ldl.load[3]))).
                                    divide(ldl.load[0].subtract(ldl.load[1]), 15, RoundingMode.CEILING).add(ldl.load[2]);
                        } else {
                            break;
                        }
                        boolean add = true;
                        for (BigDecimal ij : intermediateJoints) {
                            if (ij.compareTo(d) == 0) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            short j = 0;
                            for (; j < intermediateJoints.size(); j++) {
                                if (d.compareTo(intermediateJoints.get(j)) < 0) {
                                    break;
                                }
                            }
                            intermediateJoints.add(j, d);
                        }
                    }
                }
            }

            for (int i = 0; i < intermediateJoints.size() - 1; i++) {
                BigDecimal L = intermediateJoints.get(i + 1).subtract(intermediateJoints.get(i));
                finiteElements += (int) Math.ceil(L.divide(IF.MS, 15, RoundingMode.CEILING).doubleValue());
            }
        }

        return finiteElements;
    }
}