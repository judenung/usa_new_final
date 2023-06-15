package com.example.application.tools;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class IconStyle {

    public static Icon setStyle1 (VaadinIcon iconName) {
        Icon icon = iconName.create();
        icon.getStyle()
                .set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");
        return icon;
    }

    public static Icon setStyle2 (VaadinIcon iconName) {
        Icon icon = iconName.create();
        icon.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "var(--lumo-space-xs)");
        return icon;
    }
}
