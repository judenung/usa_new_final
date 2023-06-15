package com.example.application.frontends;

import com.example.application.tools.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jsoup.helper.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "solution", layout = BuildView.class)
@PageTitle("Solution | PFSAT")
public class SolutionView extends VerticalLayout {

    private VerticalLayout solutionForm;
    private VerticalLayout frameMemberForm;
    private GraphView graphView;

    private CheckboxGroup<String> displayOption1;
    private CheckboxGroup<String> displayOption2;
    private Button DISPLAY1;
    private Button DISPLAY2;

    private InputFrame IF;
    private OutputFrame OF;

    private static String[] DOs1 = {
            "Frame Members",
            "Supports",
            "Loads",
            "External Effects",
            "Reaction Forces",
            "Normal Forces",
            "Shear Forces",
            "Bending Moments",
            "Bending Moments (Reverse)",
            "Deformation"};

    private String[] DOs2;

    boolean switchMD;

    public SolutionView (@Autowired MyUI UI) {
        addClassName("solution-view");
        setSizeFull();

        IF = UI.getInputFrame();
        OF = UI.getOutputFrame();
        IF.showAll();
        UI.getOutputFrame().hideAll();
        IF.unmarkedAll();
        UI.solutionView = this;

        configureGraph(UI);
        configureForm(UI);
        refresh(UI);
    }

    public void refresh (MyUI UI) {
        this.removeAll();
        configureWarningDialog1(UI);
        configureWarningDialog2(UI);

        SplitLayout S1 = new SplitLayout(solutionForm, frameMemberForm);
        S1.setOrientation(SplitLayout.Orientation.VERTICAL);
        S1.setSplitterPosition(60);
        S1.setSizeFull();
        SplitLayout S2 = new SplitLayout(S1, graphView);
        S2.setSplitterPosition(30);
        S2.setSizeFull();
        add(S2);
    }

    private void configureForm(MyUI UI) {
        solutionForm = new VerticalLayout();
        solutionForm.addClassName("solution-form");

        frameMemberForm = new VerticalLayout();
        frameMemberForm.addClassName("frame-member-form");

        DISPLAY1 = new Button("Display");
        DISPLAY1.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        DISPLAY1.addClickListener(event -> display1(UI));
        DISPLAY1.setDisableOnClick(true);
        DISPLAY1.setEnabled(false);

        DISPLAY2 = new Button("Display");
        DISPLAY2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        DISPLAY2.addClickListener(event -> display2(UI));
        DISPLAY2.setDisableOnClick(true);
        DISPLAY2.setEnabled(false);

        displayOption1 = new CheckboxGroup<>();
        displayOption1.setLabel("Display Options");
        displayOption1.setItems(DOs1);
        displayOption1.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        displayOption1.select(DOs1[0], DOs1[1], DOs1[2], DOs1[3]);
        displayOption1.addValueChangeListener(event -> {
            boolean enable = false;
            if (IF.showFrameMember != displayOption1.getValue().contains(DOs1[0])) {
                enable = true;
            }
            if (IF.showSupport != displayOption1.getValue().contains(DOs1[1])) {
                enable = true;
            }
            if (IF.showLoad != displayOption1.getValue().contains(DOs1[2])) {
                enable = true;
            }
            if (IF.showExternalEffect != displayOption1.getValue().contains(DOs1[3])) {
                enable = true;
            }
            if (OF.showReaction != displayOption1.getValue().contains(DOs1[4])) {
                enable = true;
            }
            if (OF.showNormal != displayOption1.getValue().contains(DOs1[5])) {
                enable = true;
            }
            if (OF.showShear != displayOption1.getValue().contains(DOs1[6])) {
                enable = true;
            }
            if (OF.showMoment != displayOption1.getValue().contains(DOs1[7])) {
                enable = true;
            }
            if (OF.isReverseMoment != displayOption1.getValue().contains(DOs1[8])) {
                enable = true;
            }
            if (OF.showDisplacement != displayOption1.getValue().contains(DOs1[9])) {
                enable = true;
            }
            DISPLAY1.setEnabled(enable);
        });
        displayOption1.addValueChangeListener(event -> {
            if (displayOption1.getValue().contains(DOs1[7]) && displayOption1.getValue().contains(DOs1[8])) {
                if (switchMD) {
                    displayOption1.deselect(DOs1[7]);
                    switchMD = false;
                }
                else {
                    displayOption1.deselect(DOs1[8]);
                    switchMD = true;
                }
            }
            else if (displayOption1.getValue().contains(DOs1[7])) {
                switchMD = true;
            }
            else if (displayOption1.getValue().contains(DOs1[8])) {
                switchMD = false;
            }
        });

        displayOption2 = new CheckboxGroup<>();
        displayOption2.setLabel("Frame Members");
        DOs2 = new String[IF.frameMembers.size()];
        for (int i = 0; i < IF.frameMembers.size(); i++) {
            DOs2[i] = "Frame Member " + (i + 1) + " ";
        }
        displayOption2.setItems(DOs2);
        displayOption2.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        displayOption2.select(DOs2);
        displayOption2.addValueChangeListener(event -> {
            boolean enable = false;
            for (int i = 0; i < IF.frameMembers.size(); i++) {
                if (IF.undisplayedFrameMembers.contains(i) == displayOption2.getValue().contains(DOs2[i])) {
                    enable = true;
                }
            }
            DISPLAY2.setEnabled(enable);
        });

        solutionForm.setAlignItems(Alignment.CENTER);
        solutionForm.setSpacing(false);
        solutionForm.add(displayOption1, DISPLAY1);

        frameMemberForm.setAlignItems(Alignment.CENTER);
        frameMemberForm.setSpacing(false);
        frameMemberForm.add(displayOption2, DISPLAY2);
    }

    private void validateAndSolve() {
        try {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            Div progressBarLabel = new Div();
            progressBarLabel.setText("Calculating...");
            add(progressBarLabel, progressBar);

            OF.solve(IF);

            remove(progressBarLabel, progressBar);
        }
        catch (ValidationException e) {
            ErrorNotification.displayNotification("System Error");
        }
    }

    private void display1 (MyUI UI) {
        IF.showFrameMember = displayOption1.getValue().contains(DOs1[0]);
        IF.showSupport = displayOption1.getValue().contains(DOs1[1]);
        IF.showLoad = displayOption1.getValue().contains(DOs1[2]);
        IF.showExternalEffect = displayOption1.getValue().contains(DOs1[3]);
        OF.showReaction = displayOption1.getValue().contains(DOs1[4]);
        OF.showNormal = displayOption1.getValue().contains(DOs1[5]);
        OF.showShear = displayOption1.getValue().contains(DOs1[6]);
        OF.showMoment = displayOption1.getValue().contains(DOs1[7]);
        OF.isReverseMoment = displayOption1.getValue().contains(DOs1[8]);
        OF.showDisplacement = displayOption1.getValue().contains(DOs1[9]);

        graphView.refreshGraph(UI);
    }

    private void display2 (MyUI UI) {
        IF.undisplayedFrameMembers.clear();
        for (int i = 0; i < IF.frameMembers.size(); i++) {
            if (!displayOption2.getValue().contains(DOs2[i])) {
                IF.undisplayedFrameMembers.add(i);
            }
        }
        graphView.refreshGraph(UI);
    }

    private void configureWarningDialog1 (MyUI UI) {
        int finiteElement = UI.graphView.calNumberOfFiniteElement();
        if (finiteElement > 300) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Warning");
            dialog.setText("The number of finite elements exceeds the allowable limit (300). Consider adjusting the mesh size.");
            dialog.setConfirmText("Go Back");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> {
                DISPLAY1.getUI().ifPresent(ui -> ui.navigate("frame-member"));
                UI.buildView.tabs.setSelectedIndex(1);
            });

            dialog.open();
        }
        else {
            validateAndSolve();
        }
    }

    private void configureWarningDialog2 (MyUI UI) {
        if (!OF.isStable) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Warning");
            dialog.setText("The system of equilibrium equations cannot be solved because the structure is unstable. The results of the analysis are incorrect.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Proceed");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> graphView.refreshGraph(UI));
            dialog.addCancelListener(event -> {
                DISPLAY1.getUI().ifPresent(ui -> ui.navigate("support"));
                UI.buildView.tabs.setSelectedIndex(2);
            });

            dialog.open();
        }
        else {
            graphView.refreshGraph(UI);
        }
    }

    private void configureGraph (MyUI UI) {
        graphView = new GraphView(UI);
    }
}