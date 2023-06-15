package com.example.application.tools;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ErrorNotification {

    public static void displayNotification (String msg) {

        Notification notification = new Notification();
        Button closeButton = new Button(new Icon("lumo", "cross"));
        Icon icon = VaadinIcon.WARNING.create();

        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(event -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(icon, new Div(new Text(msg)), closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.setPosition(Notification.Position.MIDDLE);

        notification.add(layout);
        notification.open();
        notification.setDuration(5000);
    }
}
