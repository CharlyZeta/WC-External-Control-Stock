package com.stockdual.ui.view.purchase;

import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "compras", layout = MainLayout.class)
@PageTitle("Compras — StockDual")
@PermitAll
public class PurchaseView extends VerticalLayout {

    public PurchaseView() {
        addClassName("purchase-view");
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Ingreso de Mercadería");
        title.addClassName("view-title");

        Div placeholder = new Div();
        placeholder.addClassName("placeholder-card");

        Div iconDiv = new Div(VaadinIcon.CART.create());
        iconDiv.addClassName("placeholder-icon");

        Paragraph text = new Paragraph(
                "Ingreso de mercadería por compras a proveedores. " +
                        "Módulo en desarrollo — Fase 3.");
        text.addClassName("placeholder-text");

        placeholder.add(iconDiv, text);
        add(title, placeholder);
    }
}
