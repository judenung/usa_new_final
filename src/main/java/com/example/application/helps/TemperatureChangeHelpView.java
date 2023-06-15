package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "temperature-change-help", layout = BuildHelpView.class)
@PageTitle("Temperature Change | Help")
public class TemperatureChangeHelpView extends VerticalLayout {

    public TemperatureChangeHelpView () {
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

        String[] text1 = {"The temperature change component represents a rise or drop in temperature in the frame-member, causing expansion and contraction.",
                "• The temperature change component is represented visually by two inward (in tension) or outward (in compression) arrow heads in red.",
                "• Hovering the mouse over the tip or edges of the arrow head (temperature change) will display its change in temperature and coefficient of thermal expansion."
        };
        String[] text2 = {"To input a temperature change:",
                "• Select a frame-member from the \"Frame Member ID\" field.",
                "• Fill in the \"Change in Temperature\" and \"Coefficient of Thermal Expansion\" fields with non-zero values.",
                "• Press \"Add\"."
        };
        String text3 = "To select a temperature change, click on one of the rows in the grid below the input form. This action will highlight the temperature change selected in orange.";
        String[] text4 = {"To edit a temperature change:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the temperature change or \"Close\" to cancel the operation.",
                "This action will replace the temperature change with the input temperature change."
        };
        String text5 = "To delete a temperature change, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected temperature change.";

        String[] image1 = {"tc1"};
        String[] image2 = {"tc2"};
        String[] image3 = {"tc3"};
        String[] image4 = {"tc4"};
        String[] image5 = {"tc5"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);

        add(A);
    }
}
