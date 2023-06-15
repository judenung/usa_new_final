package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "solution-help", layout = BuildHelpView.class)
@PageTitle("Solution | Help")
public class SolutionHelpView extends VerticalLayout {

    public SolutionHelpView () {
        refresh();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Solution";
        String summary2 = "Toggle Components Visibility";
        String summary3 = "Reaction Forces";
        String summary4 = "Internal Forces";
        String summary5 = "Deformations";
        String summary6 = "Mesh Size";
        String summary7 = "Unstable Structure";

        String[] text1 = {"To enable the solution tab:",
                "• At least one frame-member component must be inputted.",
                "• At least one support component must be inputted.",
                "• At least one of the following components must be inputted: joint load, point load, linearly distributed load, temperature change, or fabrication error.",
                "Upon entering the solution tab, the structural analysis process will begin immediately."
        };
        String[] text2 = {"Inside the solution tab, the visibility of the structural component can be toggled off and on from the \"Display Options\" field and the \"Display\" button. Note that:",
                "• Supports include supports and hinges.",
                "• Loads include joint loads, point loads and linearly distributed loads.",
                "• External effects include temperature changes, fabrication errors and support settlements.",
                "Additionally, the visibility of the frame-member components and their internal forces and deformations can also be toggled off and on from the \"Frame Members\" field and the \"Display\" button."
        };
        String[] text3 = {"The reaction force component is an internal force that develops at the support of the structure when it is subjected to loads.",
                "• The reaction force component is represented visually by straight (horizontal and vertical force) and round (bending moment) arrows in red.",
                "• Hovering the mouse over the tip or the end of the arrow (reaction force) will display its force vector and coordinates."
        };
        String[] text4 = {"The internal force components include normal force, shear force, and bending moment that develop along the span of the frame-member of the structure when it is subjected to loads.",
                "• The internal force components are represented visually by line graphs parallel to the local coordinate system in pink (normal force), brown (shear force), and purple (bending moment).",
                "• Hovering the mouse over the node of the line graph will display its force vector and coordinates."
        };
        String[] text5 = {"The deformation component is the displaced shape of the span of the frame-member of the structure when it is subjected to loads.",
                "• The deformation component is represented visually by line graphs parallel to the local coordinate system in cyan.",
                "• Hovering the mouse over the node of the line graph will display its displacements and coordinates."
        };
        String[] text6 = {"The mesh size of the structure can be changed by editing the value in the \"Mesh Size\" field and then pressing \"Save Option\". This action will rerun the structural analysis algorithm with the new mesh size."};
        String[] text7 = {"If the structure constructed from the inputted structural components is statically unstable, a pop-up warning message will appear upon entering the solution tab."};

        String[] image1 = {"so1"};
        String[] image2 = {"so2"};
        String[] image3 = {"so3"};
        String[] image4 = {"so4.1", "so4.2", "so4.3", "so4.4"};
        String[] image5 = {"so5"};
        String[] image6 = {"so6.1", "so6.2"};
        String[] image7 = {"so7"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);
        HelpTools.createAccordionPanel(summary6, A, text6, image6);
        HelpTools.createAccordionPanel(summary7, A, text7, image7);

        add(A);
    }
}
