package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "support-help", layout = BuildHelpView.class)
@PageTitle("Support | Help")
public class SupportHelpView extends VerticalLayout {

    public SupportHelpView () {
        refresh ();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Definition";
        String summary2 = "Type";
        String summary3 = "Input";
        String summary4 = "Select";
        String summary5 = "Edit";
        String summary6 = "Delete";

        String[] text1 = {"The support component is a structural component that resists internal force and restrains joint displacement in the structure.",
                "• The support component is represented visually by simple shapes: square (fixed), arrow (pinned), triangle (roller), in green.",
                "• Hovering the mouse over the support will display its type and coordinates."
        };
        String[] text2 = {"The tool offers three types of support: fixed, pinned, and roller.",
                "• A fixed support restrains a joint from displacement in the X-Y plane and rotation about the Z-axis.",
                "• A pinned support restrains a joint from displacement in the X-Y plane.",
                "• A roller support restrains a joint from displacement in the axis perpendicular to the plane that it is resting on. By default, this plane is parallel to the X-axis and can be rotated counter-clockwise."
        };
        String[] text3 = {"To input a support:",
                "• Select a joint from the \"Joint Coordinates\" field.",
                "• Select a support type from the \"Support Type\" field.",
                "• If a roller is selected as a support type, an additional \"Incline Angle\" field can be filled to rotate the resting plane of the roller support.",
                "• Press \"Add\"."
        };
        String text4 = "To select a support, click on one of the rows in the grid below the input form. This action will highlight the support selected in orange.";
        String[] text5 = {"To edit a support:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the support or \"Close\" to cancel the operation.",
                "This action will replace the selected support with the input support as well as remove the support settlement component from the support."
        };
        String text6 = "To delete a support, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected support and the support settlement component from the support.";

        String[] image1 = {"sp1"};
        String[] image2 = {"sp2"};
        String[] image3 = {"sp3"};
        String[] image4 = {"sp4"};
        String[] image5 = {"sp5"};
        String[] image6 = {"sp6"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);
        HelpTools.createAccordionPanel(summary6, A, text6, image6);

        add(A);
    }
}
