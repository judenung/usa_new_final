package com.example.application.helps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class HelpTools {

    public static void createAccordionPanel (String Summary, Accordion A, String Content) {
        Span s = new Span(Content);
        VerticalLayout v = new VerticalLayout(s);
        v.setWidthFull();
        v.setSpacing(false);
        v.setPadding(true);
        AccordionPanel AP = A.add(Summary, v);
        AP.addThemeVariants(DetailsVariant.FILLED);
    }

    public static void createAccordionPanel (String Summary, Accordion A, String Content, String[] images) {
        Span s = new Span(Content);
        VerticalLayout v = new VerticalLayout(s);
        v.setWidthFull();
        v.setSpacing(false);
        v.setPadding(true);
        AccordionPanel AP = A.add(Summary, v);
        AP.addThemeVariants(DetailsVariant.FILLED);

        for (String img : images) {
            String url = "images/" + img + ".PNG";
            Image i = new Image(url, "Error");
            i.setSizeFull();
            v.add(i);
        }
    }

    public static void createAccordionPanel (String Summary, Accordion A, String[] Content) {
        VerticalLayout v = new VerticalLayout();
        for (String content : Content) {
            Span s = new Span(content);
            v.add(s);
        }
        v.setWidthFull();
        v.setSpacing(false);
        v.setPadding(true);
        AccordionPanel AP = A.add(Summary, v);
        AP.addThemeVariants(DetailsVariant.FILLED);
    }

    public static void createAccordionPanel (String Summary, Accordion A, String[] Content, String[] images) {
        VerticalLayout v = new VerticalLayout();
        for (String content : Content) {
            Span s = new Span(content);
            v.add(s);
        }
        v.setWidthFull();
        v.setSpacing(false);
        v.setPadding(true);
        AccordionPanel AP = A.add(Summary, v);
        AP.addThemeVariants(DetailsVariant.FILLED);

        for (String img : images) {
            String url = "images/" + img + ".PNG";
            Image i = new Image(url, "Error");
            i.setSizeFull();
            v.add(i);
        }
    }
}
