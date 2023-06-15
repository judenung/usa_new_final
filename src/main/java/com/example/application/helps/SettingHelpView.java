package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "setting-help", layout = BuildHelpView.class)
@PageTitle("Setting | Help")
public class SettingHelpView extends VerticalLayout {

    public SettingHelpView () {
        refresh();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Default Value";
        String summary2 = "Grid Size";
        String summary3 = "Unit System";
        String summary4 = "Mesh Size and Number of Finite Element";
        String summary5 = "Force Component Calculator";
        String summary6 = "Clear All";
        String summary7 = "Dark/Light Mode";
        String summary8 = "Navigation";
        String summary9 = "Zoom";

        String[] text1 = {"Before proceeding to construct a structure, a few settings can be adjusted:",
                "• Grid Size (default value: 10 m × 10 m).",
                "• Mesh Size (default value: 1 m).",
                "• Unit System (default value: SI).",
                "• Cross Section (default value: Default).",
                "To adjust the settings, edit or select the values from the fields and press \"Save Option\"."
        };
        String[] text2 = {"The grid size limits the size of the coordinate system and can be chosen from 10 possible values, from 10 × 10 to 100 × 100.",
                "In the case where adjusting the grid size results in the loss of structural components, a pop-up warning will appear."
        };
        String[] text3 = {"Two set of unit systems are supported:",
                "• SI (International System of Units): m, mm, kN, kN\u00B7m, GPa, deg, °C.",
                "• BG (British Gravitational System of Units): ft, in, kips, kips-ft, ksi, deg, °C."
        };
        String text4 = "The mesh size is the maximum span of the sub-frame-members (finite elements), a partition of the frame-member into many tiny elements. Reducing the mesh size increases the number of finite elements, making the internal forces and deformation diagram more smooth but requiring more computing power and time.";
        String text5 = "The \"Force Component Calculator\" button creates a pop-up and draggable calculator that converts an input diagonal force vector into horizontal and vertical components.";
        String text6 = "The \"Clear All\" button removes all input structural components.";
        String text7 = "To switch between dark and light theme, press the \"Light Mode\"/\"Dark Mode\" button.";
        String[] text8 = {"To input, edit, and remove different structural components, navigate between different tabs in the left drawer.",
                "Note that some tabs may be disabled until a specific condition is met:",
                "• The \"Cross Section\" tab is enabled when custom the cross-section is selected in the setting.",
                "• The \"Frame Member\" tab is enabled when at least one cross-section is inputted or when the default cross-section is selected in the setting.",
                "• The \"Support\", \"Hinge\", \"Joint Load\", \"Point Load\", \"Linearly Distributed Load\", \"Temperature Change\", and \"Fabrication Error\" tabs are enabled when at least one frame-member is inputted.",
                "• The \"Support Settlement\" tab is enabled when at least one support is inputted.",
                "• The \"Solution\" tab is enabled when at least one of the following components is inputted: frame-member, support, load (joint load, point load, linearly distributed load, temperature change, or fabrication error)."
        };
        String text9 = "The display in the grid can be vertically and horizontally zoomed using the zoom bars.";

        String[] image1 = {"se1"};
        String[] image2 = {"se2.1", "se2.2", "se2.3"};
        String[] image3 = {"se3.1", "se3.2"};
        String[] image4 = {"se4.1", "se4.2"};
        String[] image5 = {"se5"};
        String[] image6 = {"se6"};
        String[] image7 = {"se7.1", "se7.2"};
        String[] image8 = {"se8"};
        String[] image9 = {"se9.1", "se9.2"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);
        HelpTools.createAccordionPanel(summary6, A, text6, image6);
        HelpTools.createAccordionPanel(summary7, A, text7, image7);
        HelpTools.createAccordionPanel(summary8, A, text8, image8);
        HelpTools.createAccordionPanel(summary9, A, text9, image9);

        add(A);
    }
}
