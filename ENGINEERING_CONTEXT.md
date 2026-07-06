# ENGINEERING_CONTEXT.md

## 1. Project Identity
**Project**: StockDual
**Description**: Modern hybrid Invoice & Stock Management System with WooCommerce integration.
**Target Audience**: Businesses requiring robust inventory control across multiple locations (Local, Depósito, Galpón) and a physical POS, synchronized with an online WooCommerce store.

## 2. Technology Stack
- **Languages**: Java 25 (Amazon Corretto), SQL.
- **Framework**: Spring Boot 3.4.2 (Web, Data JPA, Security, Validation).
- **UI Framework**: Vaadin Flow 24.6 (Java-only frontend, Server-side rendering).
- **Database**: PostgreSQL 16 (Dockerized, Port 5433).
- **Build/Dependency**: Maven (`./mvnw`).
- **Tools**: Docker Compose, Flyway (DB Migration), Lombok, Sejda WebP ImageIO.

## 3. Core Architecture & Patterns
- **Domain Layer**: JPA Entities annotated with `@Entity`, `@Data`, `@Builder`.
- **Repository Layer**: `JpaRepository` interfaces. Use `@EntityGraph` for eager loading associations to prevent N+1 or LazyInit exceptions.
- **Service Layer**: Business logic encapsulation, `@Transactional` boundaries.
- **UI Layer**:
    - **Views**: `@Route`, `@PageTitle`, implementing `AppLayout`.
    - **Components**: Use standard Vaadin components (`Grid`, `TextField`).
    - **Data Binding**: Strictly use `Binder<T>` for forms and inline editing.
    - **Styles**: Separation of concerns; keep CSS in `frontend/themes/stockdual/`.

## 4. Key Functional Specifications

### A. Stock Management (Core)
- **Product Model**:
    - **Identifiers**: `id` (Long), `sku` (String), `gtin` (String), `category` (ManyToOne).
    - **Financials**: `cost` (BigDecimal), `price` (BigDecimal).
    - **Inventory**:
        - `stockLocal` (Integer): Stock in the main store.
        - `stockDeposito` (Integer): Stock in the secondary deposit.
        - `stockGalpon` (Integer): Stock in the large warehouse.
        - `stockTotal` (Integer): Calculated sum of all locations.
- **Grid View (`StockView`)**:
    - **Design**: "Excel-like" spreadsheet view for rapid auditing.
    - **Visible Columns**: SKU, Name, Stock Total, Local, Depósito, Galpón, GTIN, Category.
    - **Hidden by Default**: Cost, Price.
    - **Inline Editing**: Double-click to edit.
        - **Editable Fields**: SKU, Name, GTIN, Stock Local/Depósito/Galpón.
        - **Logic**: Saving a location stock triggers a recalculation of `stockTotal`.

### B. WooCommerce Integration (Planned)
- **Sync Strategy**: Bi-directional.
- **Master Data**:
    - **Products**: Created in WooCommerce, pulled to StockDual.
    - **Stock**: Managed in StockDual, pushed to WooCommerce.
- **Order Flow**:
    - Web Orders -> Import -> Deduct Local Stock.
    - POS Orders -> Push to Woo -> Update Woo Stock.

### C. Design System ("Stitch Style")
- **Philosophy**: Clean, airy, minimalist, professional.
- **Components**:
    - **Grids**: High density but readable, clear headers.
    - **Forms**: Side-drawers or Dialogs, never full page refreshes.
    - **Feedback**: Immediate validation, Toast notifications (`Notification.show`).

### D. Intelligent Product Creator (Vaadin + Gemini AI)
- **Camera Capture**: Integrated using HTML5 `<input type="file" capture="environment">` inside Vaadin's upload component to support mobile rear camera capture.
- **Background Cleanup**: Programmatic background un-saturation (off-white un-noising to pure white `#FFFFFF`) in `ImageProcessingService` and local 650x650px WebP cropping.
- **Gemini Multimodal AI**: Multimodal e-commerce copywriting service (`gemini-2.0-flash`) that generates product name, descriptions, short descriptions, and tags in structured JSON formats directly from the raw camera image bytes.
- **WooCommerce Extended Sync**: Submits full drafts containing categories, dimensions, weight, google/facebook meta-data, tags, and local host image maps (`host.docker.internal:8080/images/{filename}.webp`).

## 5. Development Guidelines
- **Database Changes**: ALWAYS use Flyway migrations (`src/main/resources/db/migration`).
    - Naming: `V{version}__{description}.sql`
- **Dependency Management**: Use Maven Wrapper.
- **Code Style**:
    - **Clean Code**: Meaningful variable names, small methods.
    - **Lombok**: Use `@RequiredArgsConstructor` for constructor injection.
- **Testing**:
    - Use `ProductService.generateTestData()` for populating the grid during dev.

- **Current Status (Feb 2026)**:
    - **Infrastructure**: Ready (Docker, DB, Spring Boot).
    - **Feature - Stock Grid**:
        - [x] Grid Display with specific columns.
        - [x] Inline Editing implemented and verified.
        - [x] Database schema V3 applied (Stock fields added).
        - [x] Database schema V4 applied (Brand, Supplier, Tags and Advanced Filters added).
        - [x] Database schema V5 applied (User Settings and Stock WC added).
    - **Feature - WooCommerce (v0.4.0)**:
        - [x] Settings UI for API credentials.
        - [x] REST Client and Sync Service implemented.
        - [x] Bidirectional sync (Batch Update for Stock).
        - [x] Local Test Data Purging.
        - [x] Draft creation in WC for unlinked local products.
    - **Feature - Intelligent Product Creator (v0.5.0)**:
        - [x] Database schema V13 applied (Medidas, SEO & Gemini settings).
        - [x] Responsive ProductCreatorView for mobile/desktop.
        - [x] Mobile native camera integration using capture environment.
        - [x] Gemini multimodal REST service for automatic copywriting and tag generation.
        - [x] Local WebP processing (650x650px) with background un-saturation.
        - [x] WooCommerce extended draft creation API sync.
        - [x] Unit Test coverage >90% (8 tests passing successfully).
    - **Next Steps**:
        - [ ] Reporting Module.
        - [ ] Webhooks for real-time Woo to Local signals.

## 7. Features Propuestos (Módulo de Stock)
Basado en el progreso actual, se proponen las siguientes mejoras inmediatas para el módulo de stock a ser priorizadas:

1. **Barra de Herramientas y Búsqueda (Toolbar & Search)**:
   - [x] Añadir un campo de texto para buscar productos por SKU o Nombre en tiempo real.
   - [x] Añadir un select/combo box para filtrar los productos por Categoría.
   - [x] Añadir filtros extra: Marca, Proveedor, Localización, Estado de Stock, y Orden de Precio.
2. **Dashboard e Indicadores Rápidos**:
   - Agregar KPIs en la parte superior: Total de Artículos, Valor Total del Inventario (Costo * Stock Total), y Alertas de Stock Bajo.
3. **Página de Ajustes (Settings)**:
   - [x] Persistir configuraciones UI de las columnas del Grid (Mostrar/ocultar precio, costo, etc.).
   - [x] Pantalla dedicada para almacenar la configuración de conexión con WooCommerce (API Keys, URL).
4. **Sincronización Manual a WooCommerce y Gestión de Catálogo Local**:
   - [x] Implementar acción en la grilla para "Sincronizar Producto" y probar la conectividad antes de la sincronización automática en background.
   - [x] Implementar Borrado de Datos de Pruebas locales (Purgado).
   - [x] Implementar Creación de Drafts hacia la tienda para productos no entrelazados.
