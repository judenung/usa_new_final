package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "hinge-help", layout = BuildHelpView.class)
@PageTitle("Hinge | Help")
public class HingeHelpView extends VerticalLayout {

    public HingeHelpView () {
        refresh();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Definition";
        String summary2 = "Input";
        String summary3 = "Select";
        String summary4 = "Edit";
        String summary5 = "Delete";

        String[] text1 = {"The hinge component is a structural component that restricts rotation and prevents the development of bending moment.",
                "• The hinge component is represented visually by green circles near the joints of the frame-member.",
                "• Hovering the mouse over the hinge will display its coordinate."
        };
        String[] text2 = {"To input a hinge:",
                "• Select a joint from the \"Joint Coordinates\" field.",
                "• Select one or more frame-members from the \"Frame Member ID\" field.",
                "• Press \"Add\"."
        };
        String text3 = "To select a hinge, click on one of the rows in the grid below the input form. This action will highlight the hinge selected in orange.";
        String[] text4 = {"To edit a hinge:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the value or \"Close\" to cancel the operation.",
                "This action will replace the selected hinge with the input hinge."
        };
        String text5 = "To delete a hinge, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected hinge.";

        String[] image1 = {"h1"};
        String[] image2 = {"h2"};
        String[] image3 = {"h3"};
        String[] image4 = {"h4"};
        String[] image5 = {"h5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
