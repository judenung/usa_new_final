package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "joint-load-help", layout = BuildHelpView.class)
@PageTitle("Joint Load | Help")
public class JointLoadHelpView extends VerticalLayout {

    public JointLoadHelpView () {
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

        String[] text1 = {"The joint load component is a load directly applied at the joint of the structure (commonly found in truss structures).",
                "• The joint load component is represented visually by straight (horizontal and vertical force) and round (couple moment) arrows in blue.",
                "• Hovering the mouse over the tip or the end of the arrow (joint load) will display its load vector and coordinates."
        };
        String[] text2 = {"To input a joint load:",
                "• Select a joint from the \"Joint Coordinates\" field.",
                "• Fill in at least one of the three remaining fields: \"Horizontal Force\", \"Vertical Force\", and \"Couple Moment\" with non-zero values.",
                "• Press \"Add\"."
        };
        String text3 = "To select a joint load, click on one of the rows in the grid below the input form. This action will highlight the joint load selected in orange.";
        String[] text4 = {"To edit a joint load:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the joint load or \"Close\" to cancel the operation.",
                "This action will replace the selected joint load with the input joint load."
        };
        String text5 = "To delete a joint load, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected joint load.";

        String[] image1 = {"jl1"};
        String[] image2 = {"jl2"};
        String[] image3 = {"jl3"};
        String[] image4 = {"jl4"};
        String[] image5 = {"jl5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
