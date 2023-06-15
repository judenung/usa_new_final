package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "point-load-help", layout = BuildHelpView.class)
@PageTitle("Point Load | Help")
public class PointLoadHelpView extends VerticalLayout {

    public PointLoadHelpView () {
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

        String[] text1 = {"The point load component is a load that is applied on the span of the frame-member (commonly found on beams).",
                "• The point load component is represented visually by straight (horizontal and vertical force) and round (couple moment) arrows in blue.",
                "• Hovering the mouse over the tip or the end of the arrow (point load) will display its load vector, orientation, and coordinates."
        };
        String[] text2 = {"To input a point load:",
                "• Select a frame-member from the \"Frame Member ID\" field.",
                "• Select the load orientation from the \"Orientation\" field.",
                "• Fill in the \"Distant from the Begin Joint\" field.",
                "• Fill in at least one of the three remaining fields: \"Horizontal Force\", \"Vertical Force\", and \"Couple Moment\" with non-zero values.",
                "• Press \"Add\"."
        };
        String text3 = "To select a point load, click on one of the rows in the grid below the input form. This action will highlight the point load selected in orange.";
        String[] text4 = {"To edit a point load:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the point load or \"Close\" to cancel the operation.",
                "This action will replace the selected point load with the input point load."
        };
        String text5 = "To delete a point load, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected point load.";

        String[] image1 = {"pl1"};
        String[] image2 = {"pl2"};
        String[] image3 = {"pl3"};
        String[] image4 = {"pl4"};
        String[] image5 = {"pl5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}

