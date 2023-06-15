package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "support-settlement-help", layout = BuildHelpView.class)
@PageTitle("Support Settlement | Help")
public class SupportSettlementHelpView extends VerticalLayout {

    public SupportSettlementHelpView () {
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

        String[] text1 = {"The support settlement component represents the settlement of the support in the direction of its restraint coordinate.",
                "• The support component is represented visually by simple shapes: square (fixed), arrow (pinned), triangle (roller), in grey.",
                "• Hovering the mouse over the support will display its type, settlements, and coordinates."
        };
        String[] text2 = {"To input a support settlement:",
                "• Select a support from the \"Support Coordinates\" field.",
                "• Fill in at least one of the three remaining fields: \"Horizontal Settlement\", \"Vertical Settlement\", and \"Rotational Settlement\" with non-zero values.",
                "• Press \"Add\".",
                "Note that support settlement cannot be added to a roller support with an incline angle different from 0, 90, 180, or 270 degrees because the settlement coordinate does not align with the global coordinate system."
        };
        String text3 = "To select a support settlement, click on one of the rows in the grid below the input form. This action will highlight the support settlement selected in orange.";
        String[] text4 = {"To edit a support settlement:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the support settlement or \"Close\" to cancel the operation.",
                "This action will replace the selected support settlement with the input support settlement."
        };
        String text5 = "To delete a support settlement, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected support settlement.";

        String[] image1 = {"ss1"};
        String[] image2 = {"ss2.1", "ss2.2", "ss2.3"};
        String[] image3 = {"ss3"};
        String[] image4 = {"ss4"};
        String[] image5 = {"ss5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
