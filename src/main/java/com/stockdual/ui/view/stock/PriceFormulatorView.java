package com.stockdual.ui.view.stock;

import com.stockdual.domain.entity.Supplier;
import com.stockdual.domain.entity.SupplierPriceRule;
import com.stockdual.domain.repository.SupplierRepository;
import com.stockdual.domain.repository.SupplierPriceRuleRepository;
import com.stockdual.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "price-formulator", layout = MainLayout.class)
@PageTitle("Formador de Precios — StockDual")
@PermitAll
public class PriceFormulatorView extends VerticalLayout {

    private final SupplierPriceRuleRepository priceRuleRepository;
    private final SupplierRepository supplierRepository;
    private final Grid<SupplierPriceRule> grid = new Grid<>(SupplierPriceRule.class, false);
    private final Binder<SupplierPriceRule> binder = new Binder<>(SupplierPriceRule.class);

    public PriceFormulatorView(SupplierPriceRuleRepository priceRuleRepository,
                               SupplierRepository supplierRepository) {
        this.priceRuleRepository = priceRuleRepository;
        this.supplierRepository = supplierRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("price-formulator-view");

        add(new H2("Formador de Precios por Proveedor"));
        add(new Span("Planilla de cálculo reactiva. Pulsa Enter para calcular y guardar los cambios."));

        configureGrid();
        add(getToolbar(), grid);

        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, 
                             GridVariant.LUMO_ROW_STRIPES,
                             GridVariant.LUMO_COMPACT);

        Editor<SupplierPriceRule> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        // --- ATADOS DE TECLADO ---
        Shortcuts.addShortcutListener(grid, () -> {
            if (editor.isOpen()) editor.save();
        }, Key.ENTER).listenOn(grid);

        Shortcuts.addShortcutListener(grid, () -> {
            if (editor.isOpen()) editor.cancel();
        }, Key.ESCAPE).listenOn(grid);

        // --- DEFINICIÓN DE COLUMNAS ---

        var colProv = grid.addColumn(rule -> rule.getSupplier() != null ? rule.getSupplier().getName() : "Sin Proveedor")
                .setHeader("Proveedor").setResizable(true).setWidth("180px").setFrozen(true);

        // Costo Base
        BigDecimalField baseCostField = createEditorField();
        binder.forField(baseCostField).bind(SupplierPriceRule::getBaseCost, SupplierPriceRule::setBaseCost);
        var colCost = grid.addColumn(rule -> formatCurrency(rule.getBaseCost()))
                .setHeader("Costo Base").setEditorComponent(baseCostField)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> {
                    String classes = "group-separator-column";
                    if (isZero(rule.getBaseCost())) classes += " dimmed-cell";
                    return classes;
                });

        // Descuentos (Cascada)
        BigDecimalField d1f = createEditorField();
        binder.forField(d1f).bind(SupplierPriceRule::getDiscount1, SupplierPriceRule::setDiscount1);
        var colD1 = grid.addColumn(rule -> formatPercent(rule.getDiscount1())).setHeader("Dto 1").setEditorComponent(d1f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getDiscount1()) ? "dimmed-cell" : null);

        BigDecimalField d2f = createEditorField();
        binder.forField(d2f).bind(SupplierPriceRule::getDiscount2, SupplierPriceRule::setDiscount2);
        var colD2 = grid.addColumn(rule -> formatPercent(rule.getDiscount2())).setHeader("Dto 2").setEditorComponent(d2f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getDiscount2()) ? "dimmed-cell" : null);

        BigDecimalField d3f = createEditorField();
        binder.forField(d3f).bind(SupplierPriceRule::getDiscount3, SupplierPriceRule::setDiscount3);
        var colD3 = grid.addColumn(rule -> formatPercent(rule.getDiscount3())).setHeader("Dto 3").setEditorComponent(d3f)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> {
                    String classes = "group-separator-column";
                    if (isZero(rule.getDiscount3())) classes += " dimmed-cell";
                    return classes;
                });

        // Gastos %
        BigDecimalField g1f = createEditorField();
        binder.forField(g1f).bind(SupplierPriceRule::getPercExpense1, SupplierPriceRule::setPercExpense1);
        var colG1 = grid.addColumn(rule -> formatPercent(rule.getPercExpense1())).setHeader("G1 %").setEditorComponent(g1f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getPercExpense1()) ? "dimmed-cell" : null);

        BigDecimalField g2f = createEditorField();
        binder.forField(g2f).bind(SupplierPriceRule::getPercExpense2, SupplierPriceRule::setPercExpense2);
        var colG2 = grid.addColumn(rule -> formatPercent(rule.getPercExpense2())).setHeader("G2 %").setEditorComponent(g2f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getPercExpense2()) ? "dimmed-cell" : null);

        BigDecimalField g3f = createEditorField();
        binder.forField(g3f).bind(SupplierPriceRule::getPercExpense3, SupplierPriceRule::setPercExpense3);
        var colG3 = grid.addColumn(rule -> formatPercent(rule.getPercExpense3())).setHeader("G3 %").setEditorComponent(g3f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getPercExpense3()) ? "dimmed-cell" : null);

        // IVA
        BigDecimalField ivaF = createEditorField();
        binder.forField(ivaF).bind(SupplierPriceRule::getIvaPercentage, SupplierPriceRule::setIvaPercentage);
        var colIVA = grid.addColumn(rule -> formatPercent(rule.getIvaPercentage())).setHeader("IVA %").setEditorComponent(ivaF)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> {
                    String classes = "group-separator-column";
                    if (isZero(rule.getIvaPercentage())) classes += " dimmed-cell";
                    return classes;
                });

        // Gastos Fijos (Montos)
        BigDecimalField f1f = createEditorField();
        binder.forField(f1f).bind(SupplierPriceRule::getFixedExpense1, SupplierPriceRule::setFixedExpense1);
        var colF1 = grid.addColumn(rule -> formatCurrency(rule.getFixedExpense1())).setHeader("Fijo 1").setEditorComponent(f1f).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> isZero(rule.getFixedExpense1()) ? "dimmed-cell" : null);

        BigDecimalField f2f = createEditorField();
        binder.forField(f2f).bind(SupplierPriceRule::getFixedExpense2, SupplierPriceRule::setFixedExpense2);
        var colF2 = grid.addColumn(rule -> formatCurrency(rule.getFixedExpense2())).setHeader("Fijo 2").setEditorComponent(f2f)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> {
                    String classes = "group-separator-column";
                    if (isZero(rule.getFixedExpense2())) classes += " dimmed-cell";
                    return classes;
                });

        // Markup
        BigDecimalField mupF = createEditorField();
        binder.forField(mupF).bind(SupplierPriceRule::getMarkupPercentage, SupplierPriceRule::setMarkupPercentage);
        var colMarkup = grid.addColumn(rule -> formatPercent(rule.getMarkupPercentage())).setHeader("Markup").setEditorComponent(mupF)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setClassNameGenerator(rule -> {
                    String classes = "group-separator-column";
                    if (isZero(rule.getMarkupPercentage())) classes += " dimmed-cell";
                    return classes;
                });

        // --- RESULTADOS ---
        var colFinal = grid.addColumn(rule -> formatCurrency(rule.getCalculatedFinalPrice()))
                .setHeader("FINAL")
                .setClassNameGenerator(rule -> "final-price-cell")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setFrozenToEnd(true);

        BigDecimalField crdF = createEditorField();
        binder.forField(crdF).bind(SupplierPriceRule::getCardPercentage, SupplierPriceRule::setCardPercentage);
        var colCard = grid.addColumn(rule -> formatPercent(rule.getCardPercentage()))
                .setHeader("% Tarjeta").setEditorComponent(crdF)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setFrozenToEnd(true)
                .setClassNameGenerator(rule -> isZero(rule.getCardPercentage()) ? "dimmed-cell" : null);

        var colList = grid.addColumn(rule -> formatCurrency(rule.getCalculatedListPrice()))
                .setHeader("LISTA")
                .setClassNameGenerator(rule -> "list-price-cell")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
                .setFrozenToEnd(true);

        // Acciones
        grid.addComponentColumn(rule -> {
            Button saveRowBtn = new Button(VaadinIcon.CHECK.create(), e -> editor.save());
            saveRowBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            saveRowBtn.setVisible(editor.isOpen() && editor.getItem().equals(rule));

            Button delBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                priceRuleRepository.delete(rule);
                updateList();
            });
            delBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            
            return new HorizontalLayout(saveRowBtn, delBtn);
        }).setHeader("Acciones").setWidth("100px").setFrozenToEnd(true);

        // --- AGRUPACIÓN DE ENCABEZADOS (Header Groups) ---
        HeaderRow groupHeader = grid.prependHeaderRow();
        
        groupHeader.join(colProv, colCost).setComponent(createGroupLabel("IDENTIFICACIÓN", "var(--lumo-primary-color-10pct)"));
        groupHeader.join(colD1, colD2, colD3).setComponent(createGroupLabel("DESCUENTOS", "rgba(239, 68, 68, 0.1)"));
        groupHeader.join(colG1, colG2, colG3, colIVA).setComponent(createGroupLabel("CARGOS %", "rgba(245, 158, 11, 0.1)"));
        groupHeader.join(colF1, colF2).setComponent(createGroupLabel("FIJOS $", "rgba(16, 185, 129, 0.1)"));
        groupHeader.getCell(colMarkup).setComponent(createGroupLabel("MARGEN", "rgba(139, 92, 246, 0.1)"));
        groupHeader.join(colFinal, colCard, colList).setComponent(createGroupLabel("PRECIOS DE VENTA", "var(--lumo-contrast-5pct)"));

        // Eventos del Editor
        editor.addSaveListener(e -> {
            SupplierPriceRule rule = e.getItem();
            rule.calculate(); 
            priceRuleRepository.save(rule);
            grid.getDataProvider().refreshItem(rule);
            Notification.show("Cálculo actualizado");
        });

        grid.addItemDoubleClickListener(e -> {
            if (editor.isOpen()) editor.cancel();
            grid.getEditor().editItem(e.getItem());
        });
    }

    private Component createGroupLabel(String text, String bgColor) {
        Span label = new Span(text);
        label.getStyle()
            .set("font-weight", "800")
            .set("font-size", "0.7rem")
            .set("letter-spacing", "0.1em")
            .set("width", "100%")
            .set("display", "block")
            .set("text-align", "center")
            .set("padding", "4px 0")
            .set("background-color", bgColor)
            .set("color", "var(--lumo-secondary-text-color)")
            .set("border-radius", "4px");
        return label;
    }

    private BigDecimalField createEditorField() {
        BigDecimalField field = new BigDecimalField();
        field.setWidthFull();
        field.setAutoselect(true);
        field.setLocale(new Locale("es", "AR"));
        field.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_ALIGN_CENTER);
        return field;
    }

    private HorizontalLayout getToolbar() {
        ComboBox<Supplier> supplierCombo = new ComboBox<>("Seleccionar Proveedor");
        supplierCombo.setItems(supplierRepository.findAll());
        supplierCombo.setItemLabelGenerator(Supplier::getName);
        supplierCombo.setPlaceholder("Elegir para añadir...");
        supplierCombo.setClearButtonVisible(true);
        supplierCombo.setWidth("250px");

        Button addBtn = new Button("Añadir a Planilla", VaadinIcon.PLUS.create(), e -> {
            if (supplierCombo.getValue() != null) {
                SupplierPriceRule newRule = SupplierPriceRule.builder()
                        .supplier(supplierCombo.getValue())
                        .baseCost(BigDecimal.ZERO)
                        .discount1(BigDecimal.ZERO).discount2(BigDecimal.ZERO).discount3(BigDecimal.ZERO)
                        .percExpense1(BigDecimal.ZERO).percExpense2(BigDecimal.ZERO).percExpense3(BigDecimal.ZERO)
                        .fixedExpense1(BigDecimal.ZERO).fixedExpense2(BigDecimal.ZERO)
                        .ivaPercentage(BigDecimal.valueOf(21))
                        .markupPercentage(BigDecimal.ZERO)
                        .cardPercentage(BigDecimal.ZERO)
                        .build();
                newRule.calculate();
                priceRuleRepository.save(newRule);
                updateList();
                grid.getEditor().editItem(newRule);
                supplierCombo.clear();
            } else {
                Notification.show("Selecciona un proveedor primero.");
            }
        });
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(supplierCombo, addBtn);
        toolbar.setAlignItems(Alignment.BASELINE);
        return toolbar;
    }

    private void updateList() {
        grid.setItems(priceRuleRepository.findAll());
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        return formatter.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatPercent(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        return amount.setScale(2, RoundingMode.HALF_UP).toString() + "%";
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
