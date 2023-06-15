package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "frame-member-help", layout = BuildHelpView.class)
@PageTitle("Frame Member | Help")
public class FrameMemberHelpView extends VerticalLayout {

    public FrameMemberHelpView () {
        refresh();
    }

    private void refresh () {
        this.removeAll();

        Accordion A = new Accordion();
        A.setWidthFull();

        String summary1 = "Definition";
        String summary2 = "Custom Cross Section";
        String summary3 = "Input";
        String summary4 = "Select";
        String summary5 = "Edit";
        String summary6 = "Delete";

        String[] text1 = {"The frame-member component represents long and slender components in the structure (i.e., beams, columns, beam-columns, and trusses). It is defined using the beginning and end joints' locations (m/ft) in the X-Y coordinate system.",
                "• The frame-member component is represented visually by a black (white in dark mode) line connected to two green circles at both ends, which represent the joints of the frame-member.",
                "• Additionally, a thinner parallel dashed line of the same color as the main line is there to help with the orientation of the local coordinate system.",
                "• Hovering the mouse over the joint will display its coordinates."
        };
        String text2 = "When \"Custom\" cross-section is selected, the frame-member tab is only made available after at least one cross-section is inputted. The frame-member definition will also require the cross-sectional properties (i.e., area, modulus of elasticity, and second moment of inertia) as inputs.";
        String[] text3 = {"To input a frame-member: ",
                "• Fill in the \"Begin X Coordinate\" and \"Begin Y Coordinate\" fields with non-negative value.",
                "• Fill in either one or both of the \"Horizontal Distant from the Begin Point\" and \"Vertical Distant from the Begin Point\" fields.",
                "• In the case where \"Custom\" cross-section is selected, select a cross-section from the \"Cross Section ID\" field.",
                "• Press \"Add\". "
        };
        String text4 = "To select a frame-member, click on one of the rows in the grid below the input form. This action will highlight the frame-member selected in orange.";
        String[] text5 = {"To edit a frame-member:",
                "• Click on the edit icon on one of the rows in the grid below the input form.",
                "• Edit the values in the pop-up edit form.",
                "• Press \"Edit\" to edit the frame-member or \"Close\" to cancel the operation.",
                "This action will replace the selected frame-member with the input frame-member as well as remove other components defined with reference to the selected frame member."
        };
        String text6 = "To delete a frame-member, click on the delete icon on one of the rows in the grid below the input form. This action will remove the selected frame-members and other components defined with reference to this frame-member.";

        String[] image1 = {"fm1.1", "fm1.2"};
        String[] image2 = {"fm2"};
        String[] image3 = {"fm3"};
        String[] image4 = {"fm4"};
        String[] image5 = {"fm5"};
        String[] image6 = {"fm6"};

        HelpTools.createAccordionPanel(summary1, A, text1, image1);
        HelpTools.createAccordionPanel(summary2, A, text2, image2);
        HelpTools.createAccordionPanel(summary3, A, text3, image3);
        HelpTools.createAccordionPanel(summary4, A, text4, image4);
        HelpTools.createAccordionPanel(summary5, A, text5, image5);
        HelpTools.createAccordionPanel(summary6, A, text6, image6);

        add(A);
    }
}
