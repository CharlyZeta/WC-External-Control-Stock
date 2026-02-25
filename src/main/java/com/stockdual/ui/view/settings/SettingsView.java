package com.stockdual.ui.view.settings;

import com.stockdual.domain.entity.UserSettings;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.domain.service.ProductService;
import com.stockdual.infrastructure.client.WooCommerceClient;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.transaction.annotation.Transactional;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Configuraciones — StockDual")
@PermitAll
public class SettingsView extends VerticalLayout {

        private final UserSettingsRepository settingsRepository;
        private final WooCommerceClient wooCommerceClient;
        private final ProductService productService;
        private UserSettings currentSettings;

        private final Checkbox showGtin = new Checkbox("Mostrar GTIN");
        private final Checkbox showCategory = new Checkbox("Mostrar Categoría");
        private final Checkbox showPrice = new Checkbox("Mostrar Precio");
        private final Checkbox showCost = new Checkbox("Mostrar Costo");
        private final Checkbox showTags = new Checkbox("Mostrar Etiquetas (Tags)");
        private final Checkbox showBrand = new Checkbox("Mostrar Marca");
        private final Checkbox showSupplier = new Checkbox("Mostrar Proveedor");

        // Width configurations
        private final com.vaadin.flow.component.textfield.IntegerField imageSizeField = new com.vaadin.flow.component.textfield.IntegerField(
                        "Tamaño de Imagen (px)");
        private final com.vaadin.flow.component.textfield.TextField nameColumnWidthField = new com.vaadin.flow.component.textfield.TextField(
                        "Ancho de Columna 'Nombre'");

        // WooCommerce Configurations
        private final com.vaadin.flow.component.textfield.TextField wcApiUrl = new com.vaadin.flow.component.textfield.TextField(
                        "WooCommerce API URL");
        private final com.vaadin.flow.component.textfield.PasswordField wcConsumerKey = new com.vaadin.flow.component.textfield.PasswordField(
                        "Consumer Key");
        private final com.vaadin.flow.component.textfield.PasswordField wcConsumerSecret = new com.vaadin.flow.component.textfield.PasswordField(
                        "Consumer Secret");

        public SettingsView(UserSettingsRepository settingsRepository, WooCommerceClient wooCommerceClient,
                        ProductService productService) {
                this.settingsRepository = settingsRepository;
                this.wooCommerceClient = wooCommerceClient;
                this.productService = productService;

                addClassName("settings-view");
                setPadding(true);
                setSpacing(true);

                add(new H2("Preferencia de Columnas - Grilla de Stock"));
                add(new Span("Selecciona qué columnas adicionales quieres visualizar por defecto en el Control de Stock."));

                loadSettings();

                imageSizeField.setMin(10);
                imageSizeField.setMax(200);
                imageSizeField.setHelperText("Se recomienda 50px - 80px");

                nameColumnWidthField.setPlaceholder("Ej: 300px o 25rem");

                Button saveButton = new Button("Guardar Preferencias");
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                saveButton.addClickListener(e -> saveSettings());

                com.vaadin.flow.component.orderedlayout.HorizontalLayout columnsSizes = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                                imageSizeField, nameColumnWidthField);
                columnsSizes.setAlignItems(Alignment.BASELINE);

                add(new H2("Visibilidad de Columnas"));
                add(showGtin, showCategory, showPrice, showCost, showTags, showBrand, showSupplier);

                add(new Hr());
                add(new H2("Dimensión de Elementos"));
                add(columnsSizes);

                // WC Config layout
                add(new Hr());
                add(new H2("Sincronización WooCommerce"));
                add(new Span(
                                "Configura las credenciales de la API REST de WooCommerce para habilitar la importación/exportación de stock y productos."));
                wcApiUrl.setWidthFull();
                wcApiUrl.setPlaceholder("https://mi-tienda.com");
                wcConsumerKey.setWidthFull();
                wcConsumerSecret.setWidthFull();

                com.vaadin.flow.component.orderedlayout.HorizontalLayout wcKeysLayout = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                                wcConsumerKey, wcConsumerSecret);
                wcKeysLayout.setWidthFull();

                Button testConnectionBtn = new Button("Probar Conexión", VaadinIcon.PLUG.create());
                testConnectionBtn.addClickListener(e -> testWooCommerceConnection());
                testConnectionBtn.getStyle().set("margin-top", "var(--lumo-space-m)");

                Button purgeDataBtn = new Button("Purgar Datos Locales de Prueba", VaadinIcon.TRASH.create());
                purgeDataBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                purgeDataBtn.addClickListener(e -> showPurgeConfirmationDialog());
                purgeDataBtn.getStyle().set("margin-top", "var(--lumo-space-m)");

                com.vaadin.flow.component.orderedlayout.HorizontalLayout actionButtons = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                                testConnectionBtn, purgeDataBtn);

                add(wcApiUrl, wcKeysLayout, actionButtons);

                saveButton.getStyle().set("margin-top", "var(--lumo-space-l)");
                add(saveButton);
        }

        private void loadSettings() {
                // "admin" is used as a hardcoded user base since there's no full auth yet
                currentSettings = settingsRepository.findByUserId("admin").orElseGet(() -> {
                        UserSettings defaultSettings = new UserSettings();
                        defaultSettings.setUserId("admin");
                        return defaultSettings;
                });

                showGtin.setValue(currentSettings.isShowGtin());
                showCategory.setValue(currentSettings.isShowCategory());
                showPrice.setValue(currentSettings.isShowPrice());
                showCost.setValue(currentSettings.isShowCost());
                showTags.setValue(currentSettings.isShowTags());
                showBrand.setValue(currentSettings.isShowBrand());
                showSupplier.setValue(currentSettings.isShowSupplier());

                imageSizeField.setValue(currentSettings.getImageSize() != null ? currentSettings.getImageSize() : 50);
                nameColumnWidthField.setValue(
                                currentSettings.getNameColumnWidth() != null ? currentSettings.getNameColumnWidth()
                                                : "300px");

                wcApiUrl.setValue(currentSettings.getWcApiUrl() != null ? currentSettings.getWcApiUrl() : "");
                wcConsumerKey.setValue(
                                currentSettings.getWcConsumerKey() != null ? currentSettings.getWcConsumerKey() : "");
                wcConsumerSecret
                                .setValue(currentSettings.getWcConsumerSecret() != null
                                                ? currentSettings.getWcConsumerSecret()
                                                : "");
        }

        @Transactional
        protected void saveSettings() {
                currentSettings.setShowGtin(showGtin.getValue());
                currentSettings.setShowCategory(showCategory.getValue());
                currentSettings.setShowPrice(showPrice.getValue());
                currentSettings.setShowCost(showCost.getValue());
                currentSettings.setShowTags(showTags.getValue());
                currentSettings.setShowBrand(showBrand.getValue());
                currentSettings.setShowSupplier(showSupplier.getValue());

                if (imageSizeField.getValue() != null) {
                        currentSettings.setImageSize(imageSizeField.getValue());
                }
                if (nameColumnWidthField.getValue() != null && !nameColumnWidthField.getValue().trim().isEmpty()) {
                        currentSettings.setNameColumnWidth(nameColumnWidthField.getValue());
                }

                currentSettings.setWcApiUrl(wcApiUrl.getValue());
                currentSettings.setWcConsumerKey(wcConsumerKey.getValue());
                currentSettings.setWcConsumerSecret(wcConsumerSecret.getValue());

                settingsRepository.save(currentSettings);
                Notification.show("Configuraciones guardadas exitosamente", 3000, Notification.Position.BOTTOM_END);
        }

        private void testWooCommerceConnection() {
                String url = wcApiUrl.getValue();
                String key = wcConsumerKey.getValue();
                String secret = wcConsumerSecret.getValue();

                if (url == null || url.isEmpty() || key == null || key.isEmpty() || secret == null
                                || secret.isEmpty()) {
                        Notification.show("Completa todos los campos de WooCommerce antes de probar.", 4000,
                                        Notification.Position.MIDDLE);
                        return;
                }

                boolean isConnected = wooCommerceClient.testConnection(url, key, secret);

                if (isConnected) {
                        Notification.show("¡Conexión Exitosa con WooCommerce!", 4000, Notification.Position.BOTTOM_END);
                } else {
                        Notification.show("Error de conexión. Verifica la URL y tus credenciales.", 5000,
                                        Notification.Position.MIDDLE);
                }
        }

        private void showPurgeConfirmationDialog() {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Purgar Datos Locales");
                dialog.setText("¿Estás seguro de que deseas eliminar permanentemente todos los productos locales que NO están enlazados a WooCommerce (IDs nulos)? Esta acción borrará principalmente los catálogos de prueba.");

                dialog.setCancelable(true);
                dialog.setCancelText("Cancelar");

                dialog.setConfirmText("Eliminar Productos");
                dialog.setConfirmButtonTheme("error primary");
                dialog.addConfirmListener(event -> {
                        try {
                                productService.deleteAllUnlinkedProducts();
                                Notification.show("Productos no enlazados eliminados correctamente.", 5000,
                                                Notification.Position.BOTTOM_END);
                        } catch (Exception e) {
                                Notification.show("Error al eliminar productos: " + e.getMessage(), 7000,
                                                Notification.Position.MIDDLE);
                        }
                });

                dialog.open();
        }
}
