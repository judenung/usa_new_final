package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "fabrication-error-help", layout = BuildHelpView.class)
@PageTitle("Fabrication Error | Help")
public class FabricationErrorHelpView extends VerticalLayout {

    public FabricationErrorHelpView () {
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

        String[] text1 = {"The fabrication error component represents an error in the length of the frame-member due to fabrication, causing tensile stresses to develop in the frame-member.",
                "• The fabrication error component is represented visually by two inward (in tension) or outward (in compression) arrow heads in grey.",
                "• Hovering the mouse over the tip or edges of the arrow head (fabrication error) will display its error length."
        };
        String[] text2 = {"To input a fabrication error:",
                "• Select a frame-member from the \"Frame Member ID\" field.",
                "• Fill in the \"Error Length\" field with non-zero value.",
                "• Press \"Add\"."
        };
        String text3 = "To select a fabrication error, click on one of the rows in the grid below the input form. This action will highlight the fabrication error selected in orange.";
        String[] text4 = {"To edit a fabrication error:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the fabrication error or \"Close\" to cancel the operation.",
                "This action will replace the fabrication error with the input fabrication error."
        };
        String text5 = "To delete a fabrication error, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected fabrication error.";

        String[] image1 = {"fe1"};
        String[] image2 = {"fe2"};
        String[] image3 = {"fe3"};
        String[] image4 = {"fe4"};
        String[] image5 = {"fe5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
