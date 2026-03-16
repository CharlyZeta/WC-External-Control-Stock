package com.stockdual.ui.view.stock;

import com.stockdual.domain.entity.Category;
import com.stockdual.domain.entity.Product;
import com.stockdual.domain.repository.CategoryRepository;
import com.stockdual.domain.service.ProductService;
import com.stockdual.domain.service.WooCommerceSyncService;
import com.stockdual.domain.entity.Brand;
import com.stockdual.domain.entity.Supplier;
import com.stockdual.domain.entity.ProductFilterDTO;
import com.stockdual.domain.repository.BrandRepository;
import com.stockdual.domain.repository.SupplierRepository;
import com.stockdual.domain.entity.UserSettings;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.stockdual.domain.repository.ProductSpecification;
import java.util.List;
import java.util.Optional;

@Route(value = "stock", layout = MainLayout.class)
@PageTitle("Control de Stock — StockDual")
@PermitAll
public class StockView extends VerticalLayout {

    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private final TextField searchField = new TextField();
    private final ProductForm form;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final WooCommerceSyncService wcSyncService;
    private UserSettings currentSettings;

    private final ComboBox<Category> categoryFilter = new ComboBox<>("Categoría");
    private final ComboBox<Brand> brandFilter = new ComboBox<>("Marca");
    private final ComboBox<Supplier> supplierFilter = new ComboBox<>("Proveedor");
    private final Select<String> stockStatusFilter = new Select<>();
    private final Select<String> priceOrderFilter = new Select<>();

    private final VerticalLayout toolbar = new VerticalLayout();

    public StockView(ProductService productService, CategoryRepository categoryRepository,
            BrandRepository brandRepository, SupplierRepository supplierRepository,
            UserSettingsRepository userSettingsRepository, WooCommerceSyncService wcSyncService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.wcSyncService = wcSyncService;

        addClassName("stock-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        loadUserSettings();

        configureGrid();

        form = new ProductForm(categoryRepository.findAll());
        form.setWidth("25em");
        form.addSaveListener(this::saveProduct);
        form.addDeleteListener(this::deleteProduct);
        form.addCloseListener(e -> closeEditor());

        Div content = new Div(grid, form);
        content.addClassName("content");
        content.setSizeFull();

        add(getToolbar(), content);

        // Initial state
        updateList();
        closeEditor();
    }

    private void loadUserSettings() {
        currentSettings = userSettingsRepository.findByUserId("admin").orElseGet(() -> {
            UserSettings defaultSettings = new UserSettings();
            defaultSettings.setUserId("admin");
            return defaultSettings;
        });
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();

        // Highlight rows with stock issues
        grid.setClassNameGenerator(product -> {
            if (product.getStockTotal() == null || product.getStockWc() == null) {
                return null;
            }
            if (!product.getStockTotal().equals(product.getStockWc())) {
                return "stock-mismatch-row";
            }
            return null;
        });

        // Editor setup
        Binder<Product> binder = new Binder<>(Product.class);
        Editor<Product> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        // Image Column (Configured Size)
        grid.addComponentColumn(product -> {
            int size = currentSettings.getImageSize() != null ? currentSettings.getImageSize() : 50;
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), "Product Image");
                image.getStyle().set("width", size + "px").set("height", size + "px").set("object-fit", "cover").set("border-radius", "4px");
                return image;
            } else {
                Icon icon = VaadinIcon.PICTURE.create();
                icon.setColor("var(--lumo-tertiary-text-color)");
                icon.getStyle().set("width", size + "px").set("height", size + "px");
                return icon;
            }
        }).setHeader("Img").setAutoWidth(true).setFlexGrow(0);

        // SKU Column (Read-only)
        grid.addColumn(Product::getSku).setHeader("SKU")
                .setAutoWidth(true);

        // Name Column (Editable, Sortable, Configurable Width)
        TextField nameField = new TextField();
        nameField.setWidthFull();
        binder.forField(nameField).asRequired("Nombre es requerido").bind(Product::getName, Product::setName);
        grid.addColumn(Product::getName).setHeader("Nombre")
                .setEditorComponent(nameField)
                .setWidth(currentSettings.getNameColumnWidth() != null ? currentSettings.getNameColumnWidth() : "300px")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);

        // Stock Local (Editable, Red if mismatched)
        IntegerField stockLocalField = new IntegerField();
        stockLocalField.setWidthFull();
        stockLocalField.setMin(0);
        binder.forField(stockLocalField).bind(Product::getStockLocal, Product::setStockLocal);
        grid.addColumn(Product::getStockLocal).setHeader("Local")
                .setEditorComponent(stockLocalField)
                .setAutoWidth(true)
                .setClassNameGenerator(p -> {
                    if (p.getStockTotal() != null && p.getStockWc() != null
                            && !p.getStockTotal().equals(p.getStockWc())) {
                        return "stock-mismatch-cell";
                    }
                    return null;
                })
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Stock Deposito (Editable, Red if mismatched)
        IntegerField stockDepositoField = new IntegerField();
        stockDepositoField.setWidthFull();
        stockDepositoField.setMin(0);
        binder.forField(stockDepositoField).bind(Product::getStockDeposito, Product::setStockDeposito);
        grid.addColumn(Product::getStockDeposito).setHeader("Depósito")
                .setEditorComponent(stockDepositoField)
                .setAutoWidth(true)
                .setClassNameGenerator(p -> {
                    if (p.getStockTotal() != null && p.getStockWc() != null
                            && !p.getStockTotal().equals(p.getStockWc())) {
                        return "stock-mismatch-cell";
                    }
                    return null;
                })
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Stock Galpon (Editable, Red if mismatched)
        IntegerField stockGalponField = new IntegerField();
        stockGalponField.setWidthFull();
        stockGalponField.setMin(0);
        binder.forField(stockGalponField).bind(Product::getStockGalpon, Product::setStockGalpon);
        grid.addColumn(Product::getStockGalpon).setHeader("Galpón")
                .setEditorComponent(stockGalponField)
                .setAutoWidth(true)
                .setClassNameGenerator(p -> {
                    if (p.getStockTotal() != null && p.getStockWc() != null
                            && !p.getStockTotal().equals(p.getStockWc())) {
                        return "stock-mismatch-cell";
                    }
                    return null;
                })
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        com.vaadin.flow.component.HasValue.ValueChangeListener<com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<IntegerField, Integer>> stockChangeListener = e -> {
            Product editedProduct = editor.getItem();
            if (editedProduct != null) {
                int l = stockLocalField.getValue() != null ? stockLocalField.getValue() : 0;
                int d = stockDepositoField.getValue() != null ? stockDepositoField.getValue() : 0;
                int g = stockGalponField.getValue() != null ? stockGalponField.getValue() : 0;
                int wc = editedProduct.getStockWc() != null ? editedProduct.getStockWc() : 0;
                boolean mismatch = (l + d + g) != wc;
                
                stockLocalField.setInvalid(mismatch);
                stockDepositoField.setInvalid(mismatch);
                stockGalponField.setInvalid(mismatch);
            }
        };

        stockLocalField.setValueChangeMode(ValueChangeMode.EAGER);
        stockLocalField.addValueChangeListener(stockChangeListener);
        
        stockDepositoField.setValueChangeMode(ValueChangeMode.EAGER);
        stockDepositoField.addValueChangeListener(stockChangeListener);
        
        stockGalponField.setValueChangeMode(ValueChangeMode.EAGER);
        stockGalponField.addValueChangeListener(stockChangeListener);

        // Stock WC (Read-only, reported by WooCommerce)
        grid.addColumn(Product::getStockWc).setHeader("WC")
                .setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Dynamic Columns based on Settings
        if (currentSettings.isShowPrice()) {
            grid.addColumn(Product::getPrice).setHeader("Precio").setAutoWidth(true);
        }

        // GTIN Column (Editable optionally shown)
        if (currentSettings.isShowGtin()) {
            TextField gtinField = new TextField();
            gtinField.setWidthFull();
            binder.forField(gtinField).bind(Product::getGtin, Product::setGtin);
            grid.addColumn(Product::getGtin).setHeader("GTIN")
                    .setEditorComponent(gtinField)
                    .setAutoWidth(true);
        }

        // Category Column (Read-only optionally shown, Sortable)
        if (currentSettings.isShowCategory()) {
            grid.addColumn(product -> {
                    if (product.getCategory() != null) return product.getCategory().getName();
                    if (product.getWcCategory() != null && !product.getWcCategory().isEmpty()) return product.getWcCategory() + " (WC)";
                    return "-";
            })
                    .setHeader("Categoría")
                    .setAutoWidth(true)
                    .setSortable(true);
        }

        if (currentSettings.isShowBrand()) {
            grid.addColumn(product -> product.getBrand() != null ? product.getBrand().getName() : "-")
                    .setHeader("Marca").setAutoWidth(true);
        }
        if (currentSettings.isShowSupplier()) {
            grid.addColumn(product -> product.getSupplier() != null ? product.getSupplier().getName() : "-")
                    .setHeader("Proveedor").setAutoWidth(true);
        }

        // Action Buttons
        grid.addComponentColumn(product -> {
            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                grid.getEditor().editItem(product);
            });

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> {
                productService.deleteProduct(product);
                updateList();
            });

            HorizontalLayout actions = new HorizontalLayout(editButton, deleteBtn);
            actions.setPadding(false);
            return actions;
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Grid Events
        grid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable<?>) editorComponent).focus();
            }
        });

        editor.addSaveListener(e -> {
            productService.saveProduct(e.getItem());
            updateList();
        });

        editor.addCloseListener(e -> {
            if (e.getItem() != null) {
                // handle cancel
            }
        });

        Shortcuts.addShortcutListener(grid, () -> {
            if (editor.isOpen()) {
                editor.save();
            }
        }, Key.ENTER).listenOn(grid);

        Shortcuts.addShortcutListener(grid, () -> {
            if (editor.isOpen()) {
                editor.cancel();
            }
        }, Key.ESCAPE).listenOn(grid);
    }

    private HorizontalLayout getToolbar() {
        toolbar.addClassName("toolbar");
        toolbar.setPadding(false);
        toolbar.setSpacing(true);

        // --- Search ---
        searchField.setPlaceholder("Buscar (SKU o Nombre)...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateList());

        // --- Filters ---
        categoryFilter.setPlaceholder("Categoría");
        categoryFilter.setItems(categoryRepository.findAll());
        categoryFilter.setItemLabelGenerator(Category::getName);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> updateList());

        brandFilter.setPlaceholder("Marca");
        brandFilter.setItems(brandRepository.findAll());
        brandFilter.setItemLabelGenerator(Brand::getName);
        brandFilter.setClearButtonVisible(true);
        brandFilter.addValueChangeListener(e -> updateList());

        supplierFilter.setPlaceholder("Proveedor");
        supplierFilter.setItems(supplierRepository.findAll());
        supplierFilter.setItemLabelGenerator(Supplier::getName);
        supplierFilter.setClearButtonVisible(true);
        supplierFilter.addValueChangeListener(e -> updateList());

        stockStatusFilter.setItems("Todos", "Con Stock", "Sin Stock");
        stockStatusFilter.setValue("Todos");
        stockStatusFilter.addValueChangeListener(e -> updateList());

        priceOrderFilter.setItems("Cualquiera", "Mayor Precio", "Menor Precio");
        priceOrderFilter.setValue("Cualquiera");
        priceOrderFilter.addValueChangeListener(e -> updateList());

        // --- Layout ---
        HorizontalLayout filterRow1 = new HorizontalLayout(searchField, categoryFilter, brandFilter);
        filterRow1.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        HorizontalLayout filterRow2 = new HorizontalLayout(supplierFilter, stockStatusFilter, priceOrderFilter);
        filterRow2.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        Button addProductBtn = new Button("Nuevo Producto");
        addProductBtn.addClickListener(e -> addProduct());

        Button testDataBtn = new Button("Add Test Data");
        testDataBtn.addClickListener(e -> {
            productService.generateTestData();
            // Refresh filters data
            categoryFilter.setItems(categoryRepository.findAll());
            brandFilter.setItems(brandRepository.findAll());
            supplierFilter.setItems(supplierRepository.findAll());
            updateList();
        });

        Button syncBtn = new Button("Sincronizar", VaadinIcon.REFRESH.create());
        syncBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        syncBtn.addClickListener(e -> syncWooCommerce());

        Button restoreWcBtn = new Button("Restaurar desde WC", VaadinIcon.DOWNLOAD.create());
        restoreWcBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        restoreWcBtn.addClickListener(e -> {
            boolean hasProducts = productService.countProducts() > 0;
            ConfirmDialog dialog = new ConfirmDialog();
            
            if (hasProducts) {
                dialog.setHeader("Restaurar Stock desde WooCommerce");
                dialog.setText("Esto primero descargará los datos actuales desde WooCommerce y luego sobrescribirá tu stock Local reemplazándolo por esos valores, poniendo en 0 Depósito y Galpón. ¿Deseas continuar?");
                dialog.setConfirmText("Sobrescribir");
                dialog.setConfirmButtonTheme("error primary");
            } else {
                dialog.setHeader("Descargar Catálogo desde WooCommerce");
                dialog.setText("El catálogo local está vacío. Esto descargará todos los productos desde WooCommerce inicializando tu inventario con esas cantidades reales. ¿Deseas continuar?");
                dialog.setConfirmText("Descargar");
                dialog.setConfirmButtonTheme("primary");
            }
            dialog.setCancelable(true);
            dialog.addConfirmListener(event -> {
                try {
                    wcSyncService.syncFromWooCommerce();
                    int count = productService.overwriteLocalStockWithWc();
                    updateList();
                    if (hasProducts) {
                        Notification.show("Se descargaron los datos y se actualizaron las cantidades de " + count + " productos.", 5000, Notification.Position.MIDDLE);
                    } else {
                        Notification.show("El catálogo se descargó exitosamente desde cero.", 5000, Notification.Position.MIDDLE);
                    }
                } catch (Exception ex) {
                    Notification.show("Error al restaurar: " + ex.getMessage(), 7000, Notification.Position.MIDDLE);
                }
            });
            dialog.open();
        });

        Button purgeBtn = new Button("Vaciar Catálogo", VaadinIcon.TRASH.create());
        purgeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        purgeBtn.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Vaciar Catálogo Completo");
            dialog.setText("Esto eliminará TODOS los productos de la base de datos local. Al volver a sincronizar, se descargarán de nuevo desde cero. ¿Estás absolutamente seguro?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Vaciar");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> {
                productService.deleteAllProducts();
                updateList();
                Notification.show("Catálogo vaciado.");
            });
            dialog.open();
        });

        // Current grid item count display
        Span itemCountLabel = new Span();
        itemCountLabel.getStyle().set("margin-left", "auto").set("align-self", "center").set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)");
        itemCountLabel.setId("item-count-label");

        HorizontalLayout actionButtons = new HorizontalLayout(addProductBtn, syncBtn, restoreWcBtn, purgeBtn, testDataBtn, itemCountLabel);
        actionButtons.setWidthFull();
        actionButtons.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        toolbar.add(filterRow1, filterRow2, actionButtons);

        HorizontalLayout wrapper = new HorizontalLayout(toolbar);
        wrapper.setWidthFull();
        return wrapper;
    }

    public void editProduct(Product product) {
        if (product == null) {
            closeEditor();
        } else {
            form.setProduct(product);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setProduct(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addProduct() {
        grid.asSingleSelect().clear();
        editProduct(new Product());
    }

    private void saveProduct(ProductForm.SaveEvent event) {
        productService.saveProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void deleteProduct(ProductForm.DeleteEvent event) {
        productService.deleteProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void updateList() {
        // --- 1. Gather Filter Values ---
        String searchTerm = searchField.getValue();
        Category selectedCategory = categoryFilter.getValue();
        Brand selectedBrand = brandFilter.getValue();
        Supplier selectedSupplier = supplierFilter.getValue();
        String stockStatus = stockStatusFilter.getValue();
        String priceOrder = priceOrderFilter.getValue();

        // --- 2. Build Specification ---
        Specification<Product> spec = Specification.where(null);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasSkuOrNameLike(searchTerm));
        }
        if (selectedCategory != null) {
            spec = spec.and(ProductSpecification.hasCategory(selectedCategory));
        }
        if (selectedBrand != null) {
            spec = spec.and(ProductSpecification.hasBrand(selectedBrand));
        }
        if (selectedSupplier != null) {
            spec = spec.and(ProductSpecification.hasSupplier(selectedSupplier));
        }

        // Stock Status Filter
        if ("Con Stock".equals(stockStatus)) {
            spec = spec.and(ProductSpecification.hasStockTotalGreaterThan(0));
        } else if ("Sin Stock".equals(stockStatus)) {
            spec = spec.and(ProductSpecification.hasStockTotalEquals(0));
        }

        // --- 3. Execute Query with Sort ---
        List<Product> products;
        if (searchTerm != null || selectedCategory != null || selectedBrand != null || selectedSupplier != null
                || !"Todos".equals(stockStatus)) {
            ProductFilterDTO filterDTO = new ProductFilterDTO();
            filterDTO.setSearchText(searchTerm);
            filterDTO.setCategory(selectedCategory);
            filterDTO.setBrand(selectedBrand);
            filterDTO.setSupplier(selectedSupplier);
            filterDTO.setStockStatus(stockStatus);
            filterDTO.setPriceOrder(priceOrder);
            products = productService.findProductsBasedOnFilters(filterDTO);
        } else {
            products = productService.findAllProducts(null);
        }

        grid.setItems(products);

        // Update item count label
        java.util.Optional<com.vaadin.flow.component.Component> itemCountComp = toolbar.getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .flatMap(com.vaadin.flow.component.Component::getChildren)
                .filter(c -> c.getId().isPresent() && "item-count-label".equals(c.getId().get()))
                .findFirst();

        itemCountComp.ifPresent(c -> ((Span) c).setText("Total mostrados: " + products.size()));
    }

    private void syncWooCommerce() {
        try {
            Notification.show("Iniciando sincronización con WooCommerce...", 3000, Notification.Position.TOP_CENTER);

            wcSyncService.syncFromWooCommerce();
            wcSyncService.syncToWooCommerce();

            updateList(); // Refresh grid
            Notification.show("Sincronización completada exitosamente.", 5000, Notification.Position.BOTTOM_END);
        } catch (Exception ex) {
            Notification.show("Error en la sincronización: " + ex.getMessage(), 7000, Notification.Position.MIDDLE);
        }
    }
}
