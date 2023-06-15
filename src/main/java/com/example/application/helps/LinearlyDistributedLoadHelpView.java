package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "linearly-distributed-load-help", layout = BuildHelpView.class)
@PageTitle("Linearly Distributed Load | Help")
public class LinearlyDistributedLoadHelpView extends VerticalLayout {

    public LinearlyDistributedLoadHelpView () {
        refresh ();
    }

    public void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Definition";
        String summary2 = "Input";
        String summary3 = "Select";
        String summary4 = "Edit";
        String summary5 = "Delete";

        String[] text1 = {"The linearly distributed load component is a load that is applied over the length of its span on the span of the frame-member (commonly found on beams).",
                "• The linearly distributed load component is represented visually by an array of parallel straight arrows in blue.",
                "• Hovering the mouse over the tip or the end of the arrow (linearly distributed load) will display its load vector, orientation and coordinates."
        };
        String[] text2 = {"To input a linearly distributed load:",
                "• Select a frame-member from the \"Frame Member ID\" field.",
                "• Select the load type from the \"Load Type\" field.",
                "• Select the load orientation from the \"Orientation\" field.",
                "• Fill in the \"Distant from the Begin Joint\" and \"Distant from the End Joint\" fields.",
                "• Fill in at least one of the two remaining fields: \"Begin Load\" and \"End Load\" with non-zero values.",
                "• Press \"Add\"."
        };
        String text3 = "To select a linearly distributed load, click on one of the rows in the grid below the input form. This action will highlight the linearly distributed load selected in orange.";
        String[] text4 = {"To edit a linearly distributed load:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the linearly distributed load or \"Close\" to cancel the operation.",
                "This action will replace the linearly distributed load load with the input linearly distributed load."
        };
        String text5 = "To delete a linearly distributed load, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected linearly distributed load.";

        String[] image1 = {"ldl1"};
        String[] image2 = {"ldl2"};
        String[] image3 = {"ldl3"};
        String[] image4 = {"ldl4"};
        String[] image5 = {"ldl5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
