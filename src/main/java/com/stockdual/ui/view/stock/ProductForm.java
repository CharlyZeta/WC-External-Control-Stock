package com.stockdual.ui.view.stock;

import com.stockdual.domain.entity.Category;
import com.stockdual.domain.entity.Product;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class ProductForm extends FormLayout {

    TextField sku = new TextField("SKU");
    TextField name = new TextField("Nombre");
    TextArea description = new TextArea("Descripción");
    BigDecimalField price = new BigDecimalField("Precio");
    BigDecimalField cost = new BigDecimalField("Costo");
    ComboBox<Category> category = new ComboBox<>("Categoría");

    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button close = new Button("Cancelar");

    Binder<Product> binder = new BeanValidationBinder<>(Product.class);

    public ProductForm(List<Category> categories) {
        addClassName("product-form");

        // Define required fields rules before binding
        binder.forField(sku)
                .asRequired("El SKU es obligatorio.")
                .bind(Product::getSku, Product::setSku);

        binder.forField(name)
                .asRequired("El Nombre es obligatorio.")
                .bind(Product::getName, Product::setName);

        binder.forField(price)
                .asRequired("El Precio es obligatorio.")
                .bind(Product::getPrice, Product::setPrice);

        binder.bindInstanceFields(this);

        category.setItems(categories);
        category.setItemLabelGenerator(Category::getName);

        add(sku,
                name,
                category,
                price,
                cost,
                description,
                createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    public void setProduct(Product product) {
        binder.setBean(product);
    }

    // Events
    public static abstract class ProductFormEvent extends ComponentEvent<ProductForm> {
        private Product product;

        protected ProductFormEvent(ProductForm source, Product product) {
            super(source, false);
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }
    }

    public static class SaveEvent extends ProductFormEvent {
        SaveEvent(ProductForm source, Product product) {
            super(source, product);
        }
    }

    public static class DeleteEvent extends ProductFormEvent {
        DeleteEvent(ProductForm source, Product product) {
            super(source, product);
        }
    }

    public static class CloseEvent extends ProductFormEvent {
        CloseEvent(ProductForm source) {
            super(source, null);
        }
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
