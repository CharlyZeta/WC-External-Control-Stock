package com.stockdual.ui.view.stock;
 
import com.stockdual.domain.entity.*;
import com.stockdual.domain.repository.BrandRepository;
import com.stockdual.domain.repository.CategoryRepository;
import com.stockdual.domain.repository.SupplierRepository;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.domain.service.GeminiService;
import com.stockdual.domain.service.ImageProcessingService;
import com.stockdual.domain.service.ProductService;
import com.stockdual.domain.service.WooCommerceSyncService;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
 
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.UUID;
 
@Route(value = "product-creator", layout = MainLayout.class)
@PageTitle("Creador de Productos — StockDual")
@PermitAll
public class ProductCreatorView extends VerticalLayout {
 
    private final ProductService productService;
    private final WooCommerceSyncService wcSyncService;
    private final GeminiService geminiService;
    private final ImageProcessingService imageProcessingService;
    private final UserSettingsRepository settingsRepository;
 
    private final Binder<Product> binder = new BeanValidationBinder<>(Product.class);
    private Product product = new Product();
 
    // Basic fields
    private final TextField sku = new TextField("SKU (Código único)");
    private final TextField name = new TextField("Nombre del Producto");
    private final ComboBox<Category> category = new ComboBox<>("Categoría");
    private final ComboBox<Brand> brand = new ComboBox<>("Marca");
    private final ComboBox<Supplier> supplier = new ComboBox<>("Proveedor");
    private final BigDecimalField price = new BigDecimalField("Precio de Venta");
    private final BigDecimalField cost = new BigDecimalField("Costo");
    private final IntegerField stockLocal = new IntegerField("Stock Local");
    private final IntegerField stockDeposito = new IntegerField("Stock Depósito");
    private final IntegerField stockGalpon = new IntegerField("Stock Galpón");
 
    // Advanced / SEO fields
    private final BigDecimalField weight = new BigDecimalField("Peso (kg)");
    private final BigDecimalField length = new BigDecimalField("Largo (cm)");
    private final BigDecimalField width = new BigDecimalField("Ancho (cm)");
    private final BigDecimalField height = new BigDecimalField("Alto (cm)");
    private final TextArea shortDescription = new TextArea("Descripción Corta (Resumen)");
    private final TextArea description = new TextArea("Descripción Completa");
    private final TextField tags = new TextField("Etiquetas (separadas por coma)");
    private final TextArea googleMetadata = new TextArea("Metadatos de Google (SEO)");
    private final TextArea facebookMetadata = new TextArea("Metadatos de Facebook (Pixel/Ads)");
 
    // Image / Capture variables
    private final Div imageContainer = new Div();
    private byte[] rawImageBytes;
    private String processedFilename;
    private UserSettings currentSettings;
 
    public ProductCreatorView(ProductService productService,
                              WooCommerceSyncService wcSyncService,
                              GeminiService geminiService,
                              ImageProcessingService imageProcessingService,
                              CategoryRepository categoryRepository,
                              BrandRepository brandRepository,
                              SupplierRepository supplierRepository,
                              UserSettingsRepository settingsRepository) {
        this.productService = productService;
        this.wcSyncService = wcSyncService;
        this.geminiService = geminiService;
        this.imageProcessingService = imageProcessingService;
        this.settingsRepository = settingsRepository;
 
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("product-creator-view");
 
        loadUserSettings();
 
        H2 title = new H2("Creador de Productos Inteligente");
        Span subtitle = new Span("Carga especificaciones del producto, toma una foto con el móvil para optimizarla mediante IA y súbelo a WooCommerce.");
        add(title, subtitle);
 
        // 1. Configuración de Pestañas
        Tab tabBasic = new Tab(VaadinIcon.INFO_CIRCLE.create(), new Span(" 1. Datos Básicos"));
        Tab tabSeo = new Tab(VaadinIcon.FLIGHT_TAKEOFF.create(), new Span(" 2. Medidas y SEO"));
        Tab tabImage = new Tab(VaadinIcon.CAMERA.create(), new Span(" 3. Foto & Asistente IA"));
        Tabs tabs = new Tabs(tabBasic, tabSeo, tabImage);
        tabs.setWidthFull();
        add(tabs);
 
        // 2. Definición de Contenedores de Pestañas
        Div contentContainer = new Div();
        contentContainer.setSizeFull();
 
        FormLayout basicForm = createBasicForm(categoryRepository, brandRepository, supplierRepository);
        FormLayout seoForm = createSeoForm();
        VerticalLayout imageForm = createImageForm();
 
        contentContainer.add(basicForm, seoForm, imageForm);
        add(contentContainer);
 
        // Lógica de navegación de pestañas
        basicForm.setVisible(true);
        seoForm.setVisible(false);
        imageForm.setVisible(false);
 
        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            basicForm.setVisible(selectedTab.equals(tabBasic));
            seoForm.setVisible(selectedTab.equals(tabSeo));
            imageForm.setVisible(selectedTab.equals(tabImage));
        });
 
        // 3. Botones de Acción (Pie de página)
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setPadding(true);
 
        Button saveLocalBtn = new Button("Guardar Localmente", VaadinIcon.DATABASE.create());
        saveLocalBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveLocalBtn.addClickListener(e -> saveProductLocally(true));
 
        Button syncWcBtn = new Button("Guardar y Enviar a WooCommerce", VaadinIcon.REFRESH.create());
        syncWcBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        syncWcBtn.addClickListener(e -> syncProductToWooCommerce());
 
        actions.add(saveLocalBtn, syncWcBtn);
        add(actions);
 
        // 4. Bind Fields
        configureBindings();
        resetForm();
    }
 
    private void loadUserSettings() {
        currentSettings = settingsRepository.findByUserId("admin").orElseGet(() -> {
            UserSettings defaultSettings = new UserSettings();
            defaultSettings.setUserId("admin");
            return defaultSettings;
        });
    }
 
    private void configureBindings() {
        binder.forField(sku)
                .asRequired("El SKU es obligatorio.")
                .bind(Product::getSku, Product::setSku);
 
        binder.forField(name)
                .asRequired("El nombre del producto es obligatorio.")
                .bind(Product::getName, Product::setName);
 
        binder.forField(price)
                .asRequired("El precio es obligatorio.")
                .bind(Product::getPrice, Product::setPrice);
 
        binder.bindInstanceFields(this);
    }
 
    private FormLayout createBasicForm(CategoryRepository categoryRepository,
                                       BrandRepository brandRepository,
                                       SupplierRepository supplierRepository) {
        FormLayout form = new FormLayout();
        form.setWidthFull();
 
        category.setItems(categoryRepository.findAll());
        category.setItemLabelGenerator(Category::getName);
        category.setClearButtonVisible(true);
 
        brand.setItems(brandRepository.findAll());
        brand.setItemLabelGenerator(Brand::getName);
        brand.setClearButtonVisible(true);
 
        supplier.setItems(supplierRepository.findAll());
        supplier.setItemLabelGenerator(Supplier::getName);
        supplier.setClearButtonVisible(true);
 
        form.add(sku, name, category, brand, supplier, price, cost, stockLocal, stockDeposito, stockGalpon);
 
        // Configuración de visualización responsiva (2 columnas por defecto)
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        return form;
    }
 
    private FormLayout createSeoForm() {
        FormLayout form = new FormLayout();
        form.setWidthFull();
 
        form.add(weight, length, width, height, tags, shortDescription, description, googleMetadata, facebookMetadata);
 
        form.setColspan(shortDescription, 2);
        form.setColspan(description, 2);
        form.setColspan(googleMetadata, 2);
        form.setColspan(facebookMetadata, 2);
 
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        return form;
    }
 
    private VerticalLayout createImageForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidthFull();
        form.setSpacing(true);
        form.setAlignItems(FlexComponent.Alignment.CENTER);
 
        // Contenedor de previsualización
        imageContainer.getStyle()
                .set("width", "300px")
                .set("height", "300px")
                .set("border", "2px dashed var(--lumo-contrast-30pct)")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden")
                .set("background-color", "var(--lumo-contrast-5pct)");
 
        showImagePlaceholder();
 
        // Componente de subida con captura de cámara del móvil
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        upload.setAutoUpload(true);
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Arrastra una foto aquí"));
        upload.setUploadButton(new Button("Tomar Foto / Elegir", VaadinIcon.CAMERA.create()));
 
        // capture="environment" abre la cámara trasera nativa en teléfonos móviles
        upload.getElement().setAttribute("capture", "environment");
 
        upload.addSucceededListener(event -> {
            try (InputStream in = buffer.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] temp = new byte[4096];
                int read;
                while ((read = in.read(temp)) != -1) {
                    out.write(temp, 0, read);
                }
                rawImageBytes = out.toByteArray();
                
                // Guardar una imagen temporal en la previsualización
                processedFilename = imageProcessingService.processAndSaveImage(rawImageBytes);
                updateImagePreview("/images/" + processedFilename);
                Notification.show("Foto cargada y redimensionada localmente en WebP (650x650px).", 3000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                showErrorNotification("Error al procesar la imagen cargada: " + ex.getMessage());
            }
        });
 
        // Botones de asistente con IA
        HorizontalLayout aiActions = new HorizontalLayout();
        aiActions.setSpacing(true);
 
        Button aiTextBtn = new Button("Generar Textos con IA", VaadinIcon.MAGIC.create());
        aiTextBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        aiTextBtn.addClickListener(e -> generateProductDetailsWithGemini());
 
        Button aiBgBtn = new Button("Limpiar Fondo de Foto", VaadinIcon.PAINT_ROLL.create());
        aiBgBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        aiBgBtn.addClickListener(e -> cleanBackgroundLocal());
 
        aiActions.add(aiTextBtn, aiBgBtn);
 
        form.add(imageContainer, upload, aiActions);
        return form;
    }
 
    private void showImagePlaceholder() {
        imageContainer.removeAll();
        Icon placeholder = VaadinIcon.PICTURE.create();
        placeholder.setSize("100px");
        placeholder.setColor("var(--lumo-contrast-30pct)");
        imageContainer.add(placeholder);
    }
 
    private void updateImagePreview(String relativeUrl) {
        imageContainer.removeAll();
        Image img = new Image(relativeUrl, "Vista previa");
        img.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");
        imageContainer.add(img);
    }
 
    private void generateProductDetailsWithGemini() {
        if (rawImageBytes == null) {
            showErrorNotification("Debes tomar o cargar una foto del producto primero.");
            return;
        }
        if (currentSettings.getGeminiApiKey() == null || currentSettings.getGeminiApiKey().trim().isEmpty()) {
            showErrorNotification("La API Key de Gemini no está configurada en la sección Ajustes.");
            return;
        }
 
        Notification.show("Gemini analizando la imagen y redactando información comercial...", 4000, Notification.Position.MIDDLE);
 
        try {
            GeminiService.ProductCopyResult result = geminiService.generateProductCopy(rawImageBytes, currentSettings.getGeminiApiKey());
            
            if (result != null) {
                if (result.getName() != null) name.setValue(result.getName());
                if (result.getDescription() != null) description.setValue(result.getDescription());
                if (result.getShortDescription() != null) shortDescription.setValue(result.getShortDescription());
                if (result.getTags() != null) tags.setValue(result.getTags());
                
                // Generar automáticamente algunos metadatos SEO
                googleMetadata.setValue("Title: " + result.getName() + " | Compra Online\nDescription: " + result.getShortDescription());
                facebookMetadata.setValue("Product Name: " + result.getName() + "\nPromo Code: PROMO-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
                
                Notification.show("¡Datos de e-commerce e IA completados exitosamente!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        } catch (Exception e) {
            showErrorNotification("Error al procesar IA con Gemini: " + e.getMessage());
        }
    }
 
    private void cleanBackgroundLocal() {
        if (rawImageBytes == null) {
            showErrorNotification("Sube o toma una foto primero.");
            return;
        }
 
        try {
            // Llama de nuevo a ImageProcessingService para forzar la higienización del fondo a blanco
            processedFilename = imageProcessingService.processAndSaveImage(rawImageBytes);
            updateImagePreview("/images/" + processedFilename);
            Notification.show("Fondo de imagen higienizado localmente.", 3000, Notification.Position.BOTTOM_START);
        } catch (Exception ex) {
            showErrorNotification("Error al limpiar el fondo: " + ex.getMessage());
        }
    }
 
    private boolean saveProductLocally(boolean showNotification) {
        if (!binder.isValid()) {
            showErrorNotification("Por favor, corrige los errores en el formulario antes de guardar.");
            return false;
        }
 
        // Asignar imagen
        if (processedFilename != null) {
            product.setImageUrl("/images/" + processedFilename);
        }
 
        // Calcular stock total
        int total = (stockLocal.getValue() != null ? stockLocal.getValue() : 0) +
                    (stockDeposito.getValue() != null ? stockDeposito.getValue() : 0) +
                    (stockGalpon.getValue() != null ? stockGalpon.getValue() : 0);
        product.setStockTotal(total);
 
        productService.saveProduct(product);
 
        if (showNotification) {
            Notification.show("Producto guardado exitosamente en la base de datos local.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        return true;
    }
 
    private void syncProductToWooCommerce() {
        if (!saveProductLocally(false)) {
            return;
        }
 
        Notification.show("Sincronizando producto con WooCommerce como Borrador...", 3000, Notification.Position.MIDDLE);
 
        try {
            String appBaseUrl = currentSettings.getAppBaseUrl() != null ? currentSettings.getAppBaseUrl() : "http://host.docker.internal:8080";
            Long wcId = wcSyncService.createDraftProductInWc(product, appBaseUrl);
            
            if (wcId != null) {
                Notification.show("¡Producto sincronizado con éxito como Borrador! ID WC: " + wcId, 6000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                resetForm();
            } else {
                showErrorNotification("La sincronización falló sin retornar un ID de producto.");
            }
        } catch (Exception ex) {
            showErrorNotification("Error en la sincronización con WooCommerce: " + ex.getMessage());
        }
    }
 
    private void resetForm() {
        product = new Product();
        binder.setBean(product);
        
        // Reset stocks manually as they are integer fields binded separately
        stockLocal.setValue(0);
        stockDeposito.setValue(0);
        stockGalpon.setValue(0);
        
        rawImageBytes = null;
        processedFilename = null;
        showImagePlaceholder();
    }
 
    private void showErrorNotification(String msg) {
        Notification.show(msg, 6000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
