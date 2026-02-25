package com.stockdual.ui.view.dashboard;

import com.stockdual.domain.service.DashboardService;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard — StockDual")
@PermitAll
public class DashboardView extends VerticalLayout {

        private final DashboardService dashboardService;

        public DashboardView(DashboardService dashboardService) {
                this.dashboardService = dashboardService;
                addClassName("dashboard-view");
                setPadding(true);
                setSpacing(true);

                // Page header
                H2 title = new H2("Dashboard");
                title.addClassName("view-title");

                // Stats cards row
                HorizontalLayout statsRow = new HorizontalLayout();
                statsRow.setWidthFull();
                statsRow.addClassName("stats-row");

                java.text.NumberFormat currencyFormat = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("es", "AR"));

                statsRow.add(
                                createStatCard("Productos", String.valueOf(dashboardService.getTotalProducts()),
                                                VaadinIcon.CUBE,
                                                "stat-card-blue"),
                                createStatCard("Stock Físico", String.valueOf(dashboardService.getTotalStock()),
                                                VaadinIcon.STORAGE,
                                                "stat-card-green"),
                                createStatCard("Valor Inventario",
                                                currencyFormat.format(dashboardService.getTotalInventoryValue()),
                                                VaadinIcon.CASH,
                                                "stat-card-blue"),
                                createStatCard("Alertas Mismatch", String.valueOf(dashboardService.getLowStockCount()),
                                                VaadinIcon.WARNING,
                                                "stat-card-orange"));

                // Placeholder content
                Div placeholder = new Div();
                placeholder.addClassName("placeholder-card");
                Paragraph placeholderText = new Paragraph(
                                "El dashboard se completará cuando se implemente la integración con WooCommerce y el módulo de stock.");
                placeholderText.addClassName("placeholder-text");
                placeholder.add(placeholderText);

                add(title, statsRow, placeholder);
        }

        private Div createStatCard(String label, String value, VaadinIcon icon, String cssClass) {
                Div card = new Div();
                card.addClassNames("stat-card", cssClass);

                Div iconWrapper = new Div(icon.create());
                iconWrapper.addClassName("stat-icon");

                Span valueSpan = new Span(value);
                valueSpan.addClassName("stat-value");

                Span labelSpan = new Span(label);
                labelSpan.addClassName("stat-label");

                Div textWrapper = new Div(valueSpan, labelSpan);
                textWrapper.addClassName("stat-text");

                card.add(iconWrapper, textWrapper);
                return card;
        }
}
