package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "cross-section-help", layout = BuildHelpView.class)
@PageTitle("Cross Section | Help")
public class CrossSectionHelpView extends VerticalLayout {

    public CrossSectionHelpView () {
        refresh ();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Definition";
        String summary2 = "Default Value";
        String summary3 = "Custom Value";
        String summary4 = "Input";
        String summary5 = "Select";
        String summary6 = "Edit";
        String summary7 = "Delete";

        String[] text1 = {"The cross-section component defines the stiffness of the frame-member. It includes cross-sectional properties such as:",
                "• Cross-sectional area (mm\u00B2/in\u00B2).",
                "• Modulus of elasticity (GPa/ksi).",
                "• Second moment of inertia (mm\u2074/in\u2074)."
        };
        String text2 = "By default, the cross-section of the frame-member is configured with predefined values and can be defined with just its begin and end locations. However, with the default cross-section, the values of the structure’s joint displacement will not be shown in the solution.";
        String[] text3 = {"To enable custom cross-sections:",
                "• Select \"Custom\" in the \"Cross Section\" field in the right tab.",
                "• Press \"Save Option\".",
                "Note that the \"Cross Section\" field is only available when no frame-member or cross-section is inputted. In this mode, the frame-member tab is only made available after at least one cross-section is inputted."
        };
        String[] text4 = {"To input a cross-section:",
                "• Fill in the \"Cross Sectional Area\", \"Modulus of Elasticity\", and \"Moment of Inertia\" fields with a non-zero value.",
                "• Press \"Add\"."
        };
        String text5 = "To select a cross-section, click on one of the rows in the grid below the input form. This action will highlight any frame-members that are defined using the cross-section selected in orange.";
        String[] text6 = {"To edit a cross-section: ",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the cross-section or \"Close\" to cancel the operation.",
                "This action will replace the cross-section of the frame-members defined using the selected cross-section with the input cross-section."
        };
        String text7 = "To delete a cross-section, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected cross-section, the frame-members defined using this cross-section, and other components defined with reference to the removed frame-members.";

        String[] image2 = {"cs2"};
        String[] image3 = {"cs3"};
        String[] image4 = {"cs4"};
        String[] image5 = {"cs5"};
        String[] image6 = {"cs6"};
        String[] image7 = {"cs7"};

        HelpTools.createAccordionPanel(summary1, A, text1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);
        HelpTools.createAccordionPanel(summary6, A, text6, image6);
        HelpTools.createAccordionPanel(summary7, A, text7, image7);

        this.add(A);
    }

}
