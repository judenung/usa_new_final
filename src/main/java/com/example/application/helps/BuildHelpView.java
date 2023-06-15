package com.example.application.helps;

import com.example.application.tools.IconStyle;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;

public class BuildHelpView extends AppLayout {

    public Tabs tabs;
    public boolean isDark;
    private Button mode;

    public BuildHelpView () {
        tabs = new Tabs();

        createHeader();
        refreshTabs();
    }

    private void createHeader () {
        H1 title = new H1("Help");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        isDark = true;
        mode = new Button("Light Mode", e -> {

            var js = "document.documentElement.setAttribute('theme', $0)";

            if (isDark) {
                getElement().executeJs(js, Lumo.LIGHT);
                mode.setText("Dark Mode");
            }
            else {
                getElement().executeJs(js, Lumo.DARK);
                mode.setText("Light Mode");
            }

            isDark = !isDark;
        });

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, mode);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private Tab createTab (VaadinIcon viewIcon, String viewName, Class c) {
        RouterLink link = new RouterLink();
        link.add(IconStyle.setStyle1(viewIcon), new Span(viewName));
        link.setRoute(c);

        return new Tab(link);
    }

    public void refreshTabs () {
        remove(tabs);

        tabs.removeAll();
//        Tab tab1 = createTab(VaadinIcon.PLAY_CIRCLE_O, "Demo", DemoHelpView.class);
        Tab tab2 = createTab(VaadinIcon.COG,"Setting", SettingHelpView.class);
        Tab tab3 = createTab(VaadinIcon.VIEWPORT, "Cross Section", CrossSectionHelpView.class);
        Tab tab4 = createTab(VaadinIcon.LINE_H,"Frame Member", FrameMemberHelpView.class);
        Tab tab5 = createTab(VaadinIcon.EJECT,"Support", SupportHelpView.class);
        Tab tab6 = createTab(VaadinIcon.SLIDER,"Hinge", HingeHelpView.class);
        Tab tab7 = createTab(VaadinIcon.ADD_DOCK,"Joint Load", JointLoadHelpView.class);
        Tab tab8 = createTab(VaadinIcon.DOWNLOAD_ALT,"Point Load", PointLoadHelpView.class);
        Tab tab9 = createTab(VaadinIcon.BAR_CHART,"Linearly Distributed Load", LinearlyDistributedLoadHelpView.class);
        Tab tab10 = createTab(VaadinIcon.FIRE, "Temperature Change", TemperatureChangeHelpView.class);
        Tab tab11 = createTab(VaadinIcon.WRENCH, "Fabrication Error", FabricationErrorHelpView.class);
        Tab tab12 = createTab(VaadinIcon.WARNING, "Support Settlement", SupportSettlementHelpView.class);
        Tab tab13 = createTab(VaadinIcon.PLAY, "Solution", SolutionHelpView.class);

        tabs.add(
//                tab1,
                tab2,
                tab3,
                tab4,
                tab5,
                tab6,
                tab7,
                tab8,
                tab9,
                tab10,
                tab11,
                tab12,
                tab13
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setSelectedIndex(0);

        addToDrawer(tabs);
    }
}
