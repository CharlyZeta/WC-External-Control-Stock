package com.stockdual.ui.view.dashboard;

import com.stockdual.domain.service.DashboardService;
import com.stockdual.domain.entity.UserSettings;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard — StockDual")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final DashboardService dashboardService;
    private final UserSettingsRepository userSettingsRepository;
    private UserSettings currentSettings;

    public DashboardView(DashboardService dashboardService, UserSettingsRepository userSettingsRepository) {
        this.dashboardService = dashboardService;
        this.userSettingsRepository = userSettingsRepository;
        
        loadUserSettings();
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("dashboard-view");

        add(buildHeader());
        add(buildMainKPIs());
        add(buildStockDistribution());
        add(buildSecondaryMetrics());
    }

    private void loadUserSettings() {
        currentSettings = userSettingsRepository.findByUserId("admin").orElseGet(() -> {
            UserSettings defaultSettings = new UserSettings();
            defaultSettings.setUserId("admin");
            defaultSettings.setCriticalStockThreshold(5);
            return defaultSettings;
        });
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().getPage().executeJs(loadDashboardCSS());
    }

    // ─────────────────── HEADER ───────────────────
    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(false);

        Span greeting = new Span(getGreeting());
        greeting.addClassName("dash-greeting");

        Span date = new Span(formatDate(LocalDate.now()));
        date.addClassName("dash-date");

        left.add(greeting, date);

        Span badge = new Span("Resumen General");
        badge.addClassName("dash-badge");

        header.add(left, badge);
        return header;
    }

    // ─────────────────── KPIs ───────────────────
    private HorizontalLayout buildMainKPIs() {
        long outOfStock = dashboardService.getOutOfStockCount();
        int threshold = currentSettings.getCriticalStockThreshold() != null ? currentSettings.getCriticalStockThreshold() : 5;
        long criticalStock = dashboardService.getCriticalStockCount(threshold);

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("kpi-row");

        row.add(createKPICard(
                formatNumber(dashboardService.getTotalProducts()),
                "Total Productos",
                VaadinIcon.CUBE,
                "blue"));

        row.add(createKPICard(
                formatNumber(dashboardService.getTotalStock()) + " u.",
                "Stock Físico",
                VaadinIcon.STORAGE,
                "green"));

        row.add(createKPICard(
                formatNumber(outOfStock),
                "Sin Stock",
                VaadinIcon.CLOSE_CIRCLE,
                outOfStock > 0 ? "orange" : "blue"));

        row.add(createKPICard(
                formatNumber(criticalStock),
                "Stock Crítico (≤" + threshold + ")",
                VaadinIcon.WARNING,
                criticalStock > 0 ? "purple" : "green"));

        return row;
    }

    // ─────────────────── STOCK POR UBICACIÓN ───────────────────
    private VerticalLayout buildStockDistribution() {
        long local = dashboardService.getStockLocal();
        long deposito = dashboardService.getStockDeposito();
        long galpon = dashboardService.getStockGalpon();
        long total = local + deposito + galpon;

        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.addClassName("stock-section");

        H2 title = new H2("Stock por Ubicación");
        title.addClassName("section-title");

        HorizontalLayout cardsRow = new HorizontalLayout();
        cardsRow.setWidthFull();
        cardsRow.setSpacing(true);
        cardsRow.addClassName("stock-cards-row");

        double localPct = total > 0 ? (double) local / total : 0;
        double depPct = total > 0 ? (double) deposito / total : 0;
        double galPct = total > 0 ? (double) galpon / total : 0;

        cardsRow.add(createLocationCard("Local", formatNumber(local) + " u.", localPct, "blue"));
        cardsRow.add(createLocationCard("Depósito", formatNumber(deposito) + " u.", depPct, "green"));
        cardsRow.add(createLocationCard("Galpón", formatNumber(galpon) + " u.", galPct, "purple"));

        section.add(title, cardsRow);
        return section;
    }

    // ─────────────────── MÉTRICAS SECUNDARIAS ───────────────────
    private VerticalLayout buildSecondaryMetrics() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.addClassName("secondary-section");

        H2 title = new H2("Catálogo");
        title.addClassName("section-title");

        HorizontalLayout cardsRow = new HorizontalLayout();
        cardsRow.setWidthFull();
        cardsRow.setSpacing(true);
        cardsRow.addClassName("secondary-cards-row");

        cardsRow.add(createMiniCard(
                formatNumber(dashboardService.getBrandCount()),
                "Marcas",
                VaadinIcon.TAG));

        cardsRow.add(createMiniCard(
                formatNumber(dashboardService.getSupplierCount()),
                "Proveedores",
                VaadinIcon.TRUCK));

        cardsRow.add(createMiniCard(
                dashboardService.getImageCoveragePercent() + "%",
                "Imágenes",
                VaadinIcon.PICTURE));

        section.add(title, cardsRow);
        return section;
    }

    // ─────────────────── COMPONENTES ───────────────────

    private VerticalLayout createKPICard(String value, String label, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassName("kpi-card");
        card.addClassName("kpi-" + color);

        Icon ic = icon.create();
        ic.addClassName("kpi-icon");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("kpi-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("kpi-label");

        Div colorBar = new Div();
        colorBar.addClassName("kpi-color-bar");

        card.add(colorBar, ic, valueSpan, labelSpan);
        return card;
    }

    private VerticalLayout createLocationCard(String name, String stock, double percentage, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassName("loc-card");
        card.addClassName("loc-" + color);

        Span nameSpan = new Span(name);
        nameSpan.addClassName("loc-name");

        Span stockSpan = new Span(stock);
        stockSpan.addClassName("loc-stock");

        ProgressBar bar = new ProgressBar();
        bar.setValue(Math.max(0, Math.min(1, percentage)));
        bar.addClassName("loc-bar");
        bar.addClassName("loc-bar-" + color);
        bar.setWidthFull();

        card.add(nameSpan, stockSpan, bar);
        return card;
    }

    private VerticalLayout createMiniCard(String value, String label, VaadinIcon icon) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassName("mini-card");

        Icon ic = icon.create();
        ic.addClassName("mini-icon");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("mini-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("mini-label");

        card.add(ic, valueSpan, labelSpan);
        return card;
    }

    private String loadDashboardCSS() {
        return """
            if (!document.getElementById('dashboard-inline-css')) {
                const style = document.createElement('style');
                style.id = 'dashboard-inline-css';
                style.textContent = `
                    .dash-greeting { font-size: 1.75rem; font-weight: 700; color: var(--sd-text-primary, #e8ecf4); letter-spacing: -0.02em; }
                    .dash-date { font-size: 0.85rem; color: var(--sd-text-muted, #6b7fa3); margin-top: 4px; display: block; text-transform: capitalize; }
                    .dash-badge { font-size: 0.85rem; font-weight: 500; color: var(--sd-text-muted, #6b7fa3); background: var(--sd-bg-elevated, #1e2a3f); padding: 8px 16px; border-radius: 20px; border: 1px solid var(--sd-border, #232f48); }
                    .section-title { font-size: 1.1rem; font-weight: 700; color: var(--sd-text-primary, #e8ecf4); margin: 0; letter-spacing: -0.01em; }

                    .kpi-row { display: flex; gap: 20px; flex-wrap: wrap; }
                    .kpi-card { background: var(--sd-bg-surface, #192233); border: 1px solid var(--sd-border, #232f48); border-radius: 16px; padding: 24px !important; position: relative; overflow: hidden; min-width: 220px; flex: 1; transition: transform 0.2s ease, box-shadow 0.2s ease; }
                    .kpi-card:hover { transform: translateY(-2px); box-shadow: 0 8px 30px rgba(0,0,0,0.3); }
                    .kpi-color-bar { position: absolute; top: 0; left: 0; right: 0; height: 3px; border-radius: 16px 16px 0 0; }
                    .kpi-blue .kpi-color-bar { background: linear-gradient(90deg, #135bec, #4d8ef7); }
                    .kpi-green .kpi-color-bar { background: linear-gradient(90deg, #10b981, #34d399); }
                    .kpi-purple .kpi-color-bar { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
                    .kpi-orange .kpi-color-bar { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
                    .kpi-blue .kpi-icon { color: #4d8ef7; }
                    .kpi-green .kpi-icon { color: #10b981; }
                    .kpi-purple .kpi-icon { color: #a78bfa; }
                    .kpi-orange .kpi-icon { color: #f59e0b; }
                    .kpi-icon { width: 48px; height: 48px; margin-bottom: 12px; }
                    .kpi-value { font-size: 1.5rem; font-weight: 800; color: var(--sd-text-primary, #e8ecf4); letter-spacing: -0.03em; line-height: 1; }
                    .kpi-label { font-size: 0.75rem; font-weight: 600; color: var(--sd-text-muted, #6b7fa3); text-transform: uppercase; letter-spacing: 0.06em; margin-top: 4px; }

                    .stock-section { margin-bottom: 16px; }
                    .secondary-section { margin-bottom: 16px; }
                    .stock-cards-row { display: flex; gap: 16px; flex-wrap: wrap; }
                    .loc-card { background: var(--sd-bg-surface, #192233); border: 1px solid var(--sd-border, #232f48); border-radius: 14px; min-width: 200px; flex: 1; }
                    .loc-name { font-size: 0.8rem; font-weight: 600; color: var(--sd-text-muted, #6b7fa3); text-transform: uppercase; letter-spacing: 0.05em; }
                    .loc-stock { font-size: 1.8rem; font-weight: 800; color: var(--sd-text-primary, #e8ecf4); letter-spacing: -0.03em; line-height: 1; }
                    .loc-bar vaadin-progress-bar::part(value) { border-radius: 3px; }
                    .loc-bar vaadin-progress-bar::part(bar) { border-radius: 3px; }
                    .loc-bar-blue vaadin-progress-bar { --vaadin-progress-value-color: #135bec; --vaadin-progress-bar-color: rgba(19,91,236,0.15); }
                    .loc-bar-green vaadin-progress-bar { --vaadin-progress-value-color: #10b981; --vaadin-progress-bar-color: rgba(16,185,129,0.15); }
                    .loc-bar-purple vaadin-progress-bar { --vaadin-progress-value-color: #8b5cf6; --vaadin-progress-bar-color: rgba(139,92,246,0.15); }

                    .secondary-cards-row { display: flex; gap: 12px; flex-wrap: wrap; }
                    .mini-card { background: var(--sd-bg-surface, #192233); border: 1px solid var(--sd-border, #232f48); border-radius: 12px; min-width: 140px; flex: 1; transition: transform 0.2s ease; }
                    .mini-card:hover { transform: translateY(-2px); }
                    .mini-icon { width: 32px; height: 32px; color: var(--lumo-primary-color, #135bec); margin-bottom: 4px; }
                    .mini-value { font-size: 1.3rem; font-weight: 800; color: var(--sd-text-primary, #e8ecf4); letter-spacing: -0.02em; }
                    .mini-label { font-size: 0.7rem; font-weight: 600; color: var(--sd-text-muted, #6b7fa3); text-transform: uppercase; letter-spacing: 0.05em; }
                `;
                document.head.appendChild(style);
            }
            """;
    }

    // ─────────────────── UTILS ───────────────────
    private String formatNumber(long n) {
        return NumberFormat.getNumberInstance(new Locale("es", "AR")).format(n);
    }

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Buenos días";
        if (hour < 19) return "Buenas tardes";
        return "Buenas noches";
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'de' yyyy", new Locale("es", "AR")));
    }
}
