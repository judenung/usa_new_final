package com.example.application.frontends;

import com.example.application.tools.MyUI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Route(value = "")
@PageTitle("PFSAT")
public class MainView extends VerticalLayout {

    MainView (@Autowired MyUI UI) {
        addClassName("main-view");
        setSizeFull();

        H1 Title = new H1("Main Page To Be Designed...");
        Title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        Button START = new Button ();
        START.addClickListener(event -> START.getUI().ifPresent(ui -> ui.navigate("frame-member")));
        START.setIcon(new Icon(VaadinIcon.PLAY));
        START.setWidth("100px");

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(Title, START);
    }
}
