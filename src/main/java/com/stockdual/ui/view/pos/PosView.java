package com.stockdual.ui.view.pos;

import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "pos", layout = MainLayout.class)
@PageTitle("Terminal POS — StockDual")
@PermitAll
public class PosView extends VerticalLayout {

    public PosView() {
        addClassName("pos-view");
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Terminal POS");
        title.addClassName("view-title");

        Div placeholder = new Div();
        placeholder.addClassName("placeholder-card");

        Div iconDiv = new Div(VaadinIcon.SHOP.create());
        iconDiv.addClassName("placeholder-icon");

        Paragraph text = new Paragraph(
                "Generación de pedidos para clientes WooCommerce. " +
                        "Módulo en desarrollo — Fase 4.");
        text.addClassName("placeholder-text");

        placeholder.add(iconDiv, text);
        add(title, placeholder);
    }
}
