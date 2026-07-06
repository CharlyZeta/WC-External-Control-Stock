package com.stockdual.ui.layout;

import com.stockdual.config.AppVersionProvider;
import com.stockdual.ui.view.dashboard.DashboardView;
import com.stockdual.ui.view.pos.PosView;
import com.stockdual.ui.view.purchase.PurchaseView;
import com.stockdual.ui.view.settings.SettingsView;
import com.stockdual.ui.view.stock.PriceFormulatorView;
import com.stockdual.ui.view.stock.StockView;
import com.stockdual.ui.view.stock.ProductCreatorView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;
    private final AppVersionProvider versionProvider;

    public MainLayout(AuthenticationContext authContext, AppVersionProvider versionProvider) {
        this.authContext = authContext;
        this.versionProvider = versionProvider;
        setPrimarySection(Section.DRAWER);
        addClassName("main-layout");

        createHeader();
        createDrawer();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const theme = localStorage.getItem('theme');" +
                        "if (theme === 'dark' || (!theme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {"
                        +
                        "  document.documentElement.setAttribute('theme', 'dark');" +
                        "} else {" +
                        "  document.documentElement.removeAttribute('theme');" +
                        "}"));
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("drawer-toggle");
        toggle.setAriaLabel("Menú");

        H1 appName = new H1("StockDual");
        appName.addClassNames("app-name");

        Button logoutBtn = new Button("Salir", VaadinIcon.SIGN_OUT.create(), e -> authContext.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        logoutBtn.addClassName("logout-btn");

        String username = "Usuario";
        try {
            username = authContext.getPrincipalName().orElse("Usuario");
        } catch (Exception e) {}
        
        Span userSpan = new Span(username);
        userSpan.addClassName("user-display");

        Button themeToggle = new Button(VaadinIcon.ADJUST.create());
        themeToggle.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                    "const doc = document.documentElement;" +
                            "let newTheme = 'none';" +
                            "if (doc.getAttribute('theme') === 'dark') {" +
                            "  doc.removeAttribute('theme');" +
                            "  newTheme = 'light';" +
                            "} else {" +
                            "  doc.setAttribute('theme', 'dark');" +
                            "  newTheme = 'dark';" +
                            "}" +
                            "localStorage.setItem('theme', newTheme);");
        });
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        themeToggle.addClassName("theme-toggle-btn");

        HorizontalLayout rightSection = new HorizontalLayout(themeToggle, userSpan, logoutBtn);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.addClassName("header-right");

        HorizontalLayout header = new HorizontalLayout(toggle, appName, rightSection);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(appName);
        header.setWidthFull();
        header.addClassNames("header", LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        Div logoSection = new Div();
        logoSection.addClassName("drawer-logo-section");
        H2 logoText = new H2("StockDual");
        logoText.addClassName("drawer-logo");
        logoSection.add(logoText);

        SideNav nav = new SideNav();
        nav.addClassName("side-nav");

        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Control de Stock", StockView.class, VaadinIcon.STORAGE.create()));
        nav.addItem(new SideNavItem("Creador Productos", ProductCreatorView.class, VaadinIcon.PLUS_CIRCLE.create()));
        nav.addItem(new SideNavItem("Formador Precios", PriceFormulatorView.class, VaadinIcon.CALC_BOOK.create()));
        nav.addItem(new SideNavItem("Compras", PurchaseView.class, VaadinIcon.CART.create()));
        nav.addItem(new SideNavItem("Terminal POS", PosView.class, VaadinIcon.SHOP.create()));

        Hr separator = new Hr();
        separator.addClassName("nav-separator");

        SideNavItem settingsItem = new SideNavItem("Configuración", SettingsView.class, VaadinIcon.COG.create());
        SideNav settingsNav = new SideNav();
        settingsNav.addItem(settingsItem);

        Scroller scroller = new Scroller(new Div(nav, separator, settingsNav));
        scroller.addClassName("drawer-scroller");

        Div footerSection = new Div();
        footerSection.addClassName("drawer-footer");
        footerSection.getStyle().set("margin-top", "auto");
        footerSection.getStyle().set("padding", "var(--lumo-space-m)");
        footerSection.getStyle().set("text-align", "center");

        Paragraph version = new Paragraph("v" + versionProvider.getVersion());
        version.addClassName("drawer-version");
        version.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        version.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        version.getStyle().set("margin", "0");
        footerSection.add(version);

        addToDrawer(logoSection, scroller, footerSection);
    }
}
