# 🚀 StockDual — Sistema Empresarial de Control de Stock e Integración con WooCommerce & IA

<p align="center">
  <img src="docs/images/stockdual_banner.jpg" alt="StockDual Banner" width="100%" />
</p>

[![Java](https://img.shields.io/badge/Java%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.4.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin%2024.6.4-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white)](https://vaadin.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2016-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gemini](https://img.shields.io/badge/Gemini%202.0%20Flash-8E75C2?style=for-the-badge&logo=googlegemini&logoColor=white)](https://deepmind.google/technologies/gemini/)

**StockDual** es una plataforma backend híbrida de nivel empresarial diseñada para optimizar la gestión de inventario y facturación multi-ubicación (*Local, Depósito y Galpón*). El sistema cuenta con una interfaz web reactiva de alto rendimiento, un motor de sincronización bidireccional avanzado con **WooCommerce** y un creador inteligente de productos asistido por la API multimodal de **Google Gemini**.

---

## 🌟 Descripción Premium del Sistema

Diseñado bajo rigurosos patrones de desarrollo corporativo, **StockDual** actúa como el núcleo unificado para comercios con presencia física (POS) y tiendas online (WooCommerce). Resuelve de forma nativa la discrepancia de inventarios, optimiza las tareas de auditoría rápida y acelera drásticamente la catalogación de nuevos artículos mediante visión por computadora (IA).

### Pilares Fundamentales
*   **Gestión Multi-Ubicación Inteligente**: Control granular y segmentado en tiempo real de existencias en el local principal, depósitos secundarios y almacenes a gran escala, calculando y consolidando métricas totales automáticamente.
*   **Edición Dinámica de Alta Densidad**: Grilla interactiva estilo "Excel" para auditorías veloces, con capacidad de edición directa (*inline editing*) y persistencia asíncrona segura.
*   **Fusión de Catálogos e Integración Woo**: Motor que mapea y sincroniza marcas, proveedores, códigos de barra (GTIN/EAN), precios y stocks, gestionando de forma proactiva las discrepancias.
*   **Creador de Productos Inteligente (Multimodal IA)**: Flujo móvil y de escritorio que recibe imágenes capturadas directamente con la cámara del dispositivo, las limpia algorítmicamente y consulta a **Gemini 2.0** para extraer y estructurar automáticamente títulos comerciales, descripciones optimizadas para SEO, viñetas de especificaciones y etiquetas de venta.

---

## ⚙️ Stack Tecnológico Detallado

*   **Lenguaje & JDK**: **Java 25 (Amazon Corretto)**. Aprovechamiento de características modernas del lenguaje (virtual threads para tareas de fondo, pattern matching y records optimizados).
*   **Framework Core**: **Spring Boot 3.4.2**
    *   *Spring Data JPA*: Para la capa de acceso a datos, implementando `@EntityGraph` para evitar el problema de consultas N+1 y optimizar las transacciones.
    *   *Spring Security*: Autenticación robusta basada en Spring Security para control de accesos de administradores y operarios.
    *   *Spring Web / RestTemplate*: Consumo asíncrono y estructurado de la API de WooCommerce y de la API REST de Google Gemini.
*   **Frontend**: **Vaadin Flow 24.6.4**
    *   Arquitectura reactiva 100% Java con renderizado en el servidor (SSR).
    *   Binding estructurado y validación inmediata mediante la API `Binder`.
    *   Integración nativa con cámara del dispositivo mediante HTML5 y empaquetado optimizado en producción mediante Vite.
*   **Base de Datos**: **PostgreSQL 16 (Alpine-based)**
    *   *Integridad de datos*: Constraints a nivel de base de datos (`CHECK` para evitar stocks o precios negativos).
    *   *Evolución controlada*: Control de versiones y migraciones automáticas mediante **Flyway**.
    *   *Rendimiento*: Indexación estratégica en campos de alta frecuencia de búsqueda (`sku`, `gtin`, `wc_id`, claves foráneas).
*   **Procesamiento Gráfico & Compresión**:
    *   Algoritmos de redimensión proporcional (canvas 650x650px).
    *   *Background Un-saturation*: Filtro algorítmico personalizado en Java que analiza canales RGB para eliminar el ruido de fondo (tonos grises/blancos sucios) y forzar un fondo blanco puro (`#FFFFFF`) adecuado para e-commerce.
    *   *WebP compression*: Integración del plugin `webp-imageio-sejda` para salida de imagen ultra-liviana.

---

## 📊 Arquitectura & Flujos de Datos

### 1. Diagrama de Flujo de Integración (StockDual ↔️ WooCommerce ↔️ Gemini API)

Este diagrama ilustra el flujo de creación de un producto desde la captura de la foto con un dispositivo móvil, el enriquecimiento con IA y el posterior despliegue bidireccional en WooCommerce.

```mermaid
graph TD
    %% Estilos de los nodos
    classDef principal fill:#e3f2fd,stroke:#0d47a1,stroke-width:2px,color:#0d47a1;
    classDef secundario fill:#f1f8e9,stroke:#33691e,stroke-width:2px,color:#33691e;
    classDef externo fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#e65100;
    
    subgraph Local [Entorno StockDual (Backend & UI)]
        PCV["ProductCreatorView (Frontend Vaadin)"]:::principal
        IPS["ImageProcessingService (Procesador WebP)"]:::principal
        DB[("PostgreSQL 16 (Flyway V13 Schema)")]:::principal
        WCS["WooCommerceSyncService (Sincronizador)"]:::principal
    end

    subgraph Gemini [Servicio Cognitivo Externo]
        GAPI["Gemini API (gemini-2.0-flash)"]:::externo
    end

    subgraph Tienda [WooCommerce Online Store]
        WCAPI["WooCommerce REST API"]:::externo
        POS["POS / Venta Física"]:::secundario
    end

    %% Relaciones y flujo
    PCV -->|1. Captura nativa / Carga foto| IPS
    IPS -->|2. Escala 650x650 & Desatura fondo a blanco puro| PCV
    PCV -->|3. POST Image Bytes & Prompt| GAPI
    GAPI -->|4. Retorna JSON estructurado (Copywriting)| PCV
    PCV -->|5. Confirma datos y guarda registro local| DB
    
    WCS -->|6. Lee discrepancias y nuevos productos| DB
    WCS -->|7. POST Extended Draft (SEO, Medidas, Foto WebP)| WCAPI
    WCAPI -->|8. Actualiza stock_wc local tras ventas web| WCS
    POS -->|9. Descuenta stock local físico| DB
    WCS -->|10. Pushes batch de stock consolidado| WCAPI
```

---

### 2. Diagrama Entidad-Relación de la Base de Datos (Versión V13)

Esquema relacional implementado en PostgreSQL. Contiene las tablas de dominio, la configuración de parámetros y la tabla de cálculo del Formador de Precios.

```mermaid
erDiagram
    app_user {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password_hash
        VARCHAR display_name
        VARCHAR role
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    app_setting {
        BIGINT id PK
        VARCHAR setting_key UK
        TEXT setting_value
        VARCHAR description
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    category {
        BIGINT id PK
        VARCHAR name UK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    brand {
        BIGINT id PK
        VARCHAR name UK
    }

    supplier {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR contact_info
    }

    product {
        BIGINT id PK
        VARCHAR sku UK
        VARCHAR name
        VARCHAR description
        DECIMAL price "CHECK >= 0"
        DECIMAL cost "CHECK >= 0"
        BIGINT category_id FK
        BIGINT brand_id FK
        BIGINT supplier_id FK
        VARCHAR gtin "EAN / Código barra"
        INTEGER stock_local "CHECK >= 0"
        INTEGER stock_deposito "CHECK >= 0"
        INTEGER stock_galpon "CHECK >= 0"
        INTEGER stock_total "CHECK >= 0"
        INTEGER stock_wc "CHECK >= 0"
        BIGINT wc_id UK "ID WooCommerce asignado"
        VARCHAR wc_category
        VARCHAR image_url
        NUMERIC weight "Medidas físicas"
        NUMERIC length
        NUMERIC width
        NUMERIC height
        TEXT short_description "Copy resumido en viñetas"
        TEXT google_metadata
        TEXT facebook_metadata
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    user_settings {
        BIGINT id PK
        VARCHAR user_id UK "Identificador del Admin"
        BOOLEAN show_gtin
        BOOLEAN show_category
        BOOLEAN show_price
        BOOLEAN show_cost
        BOOLEAN show_tags
        BOOLEAN show_brand
        BOOLEAN show_supplier
        INTEGER image_size "Alto de foto en grilla (px)"
        VARCHAR name_column_width
        VARCHAR wc_api_url
        VARCHAR wc_consumer_key
        VARCHAR wc_consumer_secret
        INTEGER critical_stock_threshold "Alerta de quiebre"
        VARCHAR gemini_api_key "API Key para visión"
        TEXT image_enhance_prompt
        VARCHAR app_base_url "Base URL para hostear fotos localmente"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    supplier_price_rule {
        BIGINT id PK
        BIGINT supplier_id FK
        DECIMAL base_cost
        DECIMAL discount_1
        DECIMAL discount_2
        DECIMAL discount_3
        DECIMAL perc_expense_1
        DECIMAL perc_expense_2
        DECIMAL perc_expense_3
        DECIMAL iva_percentage
        DECIMAL fixed_expense_1
        DECIMAL fixed_expense_2
        DECIMAL markup_percentage "Margen de ganancia"
        DECIMAL card_percentage "Márgen por pago tarjeta"
        DECIMAL calculated_final_price "Precio venta final"
        DECIMAL calculated_list_price "Precio de lista"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    product }|--|| category : "pertenece a"
    product }|--|o brand : "marca de"
    product }|--|o supplier : "provisto por"
    supplier_price_rule }|--|| supplier : "aplica a"
```

> **Nota sobre el historial:** el commit inicial incluye el bundle generado del frontend
> (Vaadin / `frontend/generated/`), que explica el volumen de líneas. El código de negocio está
> en `src/main/java/com/stockdual/`.

---

## 🖥️ Showcase & Capturas de la Interfaz

> [!NOTE]
> Las vistas principales están descritas abajo. **Las capturas todavía no están en el repositorio**: cada vista indica dónde iría la suya.

### 1. Panel de Control de Stock (Excel-Like Grid)
Grilla compacta optimizada para operaciones rápidas. Permite auditar SKU, código GTIN, marca, stock total y distribuciones físicas locales. Los registros con inconsistencias de inventario con respecto a WooCommerce se destacan visualmente de forma inmediata.

> *📷 Captura pendiente: esta vista todavía no tiene imagen en el repositorio.*

---

### 2. Creador Inteligente de Productos (IA Gemini)
Módulo adaptativo optimizado para dispositivos móviles y de escritorio. Utiliza la cámara del celular para capturar la foto del producto, procesarla a blanco puro, y enviar los bytes a la API de Gemini para autocompletar el formulario de WooCommerce.

> *📷 Captura pendiente: esta vista todavía no tiene imagen en el repositorio.*

---

### 3. Formador de Precios (Planilla de Costos)
Módulo de cálculo reactivo donde los costos se procesan en cascada dinámica (descuentos sucesivos, impuestos e IVA, logística y márgenes de utilidad) para fijar precios comerciales de venta final y lista.

> *📷 Captura pendiente: esta vista todavía no tiene imagen en el repositorio.*

---

### 4. Ajustes del Sistema y Credenciales
Panel centralizado de configuración donde se administran las API keys de WooCommerce, Gemini AI, umbrales de stock crítico e interfaces de visualización personalizadas.

> *📷 Captura pendiente: esta vista todavía no tiene imagen en el repositorio.*

---

## 🛠️ Configuración e Instalación del Entorno

### Requisitos Previos
*   **Java**: JDK 21 o Java 25 (Recomendado Amazon Corretto 25).
*   **Docker**: Docker Desktop con soporte para Docker Compose.
*   **Maven**: Opcional (se incluye el envoltorio `./mvnw` en el proyecto).

### 1. Iniciar la Base de Datos (Docker)
El proyecto incluye un archivo `docker-compose.yml` preconfigurado para levantar PostgreSQL en el puerto **5433**:

```bash
docker-compose up -d
```

### 2. Configurar Variables de Entorno
La aplicación lee la configuración desde `src/main/resources/application.yml`. Estas son **todas** las variables que el sistema consulta:

| Variable de Entorno | Descripción | Obligatoria | Valor por Defecto | Ejemplo |
| :--- | :--- | :--- | :--- | :--- |
| `DB_URL` | Dirección de conexión JDBC para PostgreSQL | No | `jdbc:postgresql://localhost:5433/stockdual` | `jdbc:postgresql://postgres_host:5432/db_prod` |
| `DB_USER` | Usuario de la base de datos | No | `stockdual` | `admin_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | No | `localdev` | `MiClaveSegura123` |
| `SERVER_PORT` | Puerto de escucha del servidor web | No | `8080` | `9000` |
| `STOCKDUAL_ENCRYPTION_KEY` | Clave AES/GCM con la que se cifran las credenciales de terceros en la base | **Sí** | — (el arranque aborta sin ella) | una cadena de 32 caracteres |
| `STOCKDUAL_ADMIN_PASSWORD` | Contraseña inicial del usuario `admin` que crea `DataInitializer` | **Sí** | — (el arranque aborta sin ella) | `cambiar-en-produccion` |

> [!IMPORTANT]
> Las dos últimas son **obligatorias**: si no están definidas, la aplicación no arranca y lanza `IllegalStateException`. `STOCKDUAL_ENCRYPTION_KEY` se normaliza a 32 bytes, pero conviene usar exactamente 32 caracteres.

**Cómo se cargan** — no es lo mismo para Docker que para la aplicación:

*   **Docker Compose** lee el archivo `.env` de la raíz para interpolar `POSTGRES_PASSWORD` del contenedor. El repositorio trae `.env.example` como plantilla con los valores de desarrollo.
*   **La aplicación Java no lee `.env`**: Spring resuelve variables de entorno reales. Definilas en la terminal antes de arrancar o en la *Run Configuration* del IDE. El paso siguiente muestra cómo.

### 3. Ejecutar la Aplicación en Desarrollo
Exporte las dos variables obligatorias y arranque con el wrapper. En Windows / PowerShell:

```powershell
$env:STOCKDUAL_ENCRYPTION_KEY = "clave-de-desarrollo-de-32-caracteres"
$env:STOCKDUAL_ADMIN_PASSWORD = "cambiar-en-produccion"
./mvnw spring-boot:run
```

En Linux / macOS:

```bash
export STOCKDUAL_ENCRYPTION_KEY="clave-de-desarrollo-de-32-caracteres"
export STOCKDUAL_ADMIN_PASSWORD="cambiar-en-produccion"
./mvnw spring-boot:run
```

*La base de datos se migrará automáticamente a la versión V13 en el primer inicio mediante Flyway.*

> Si prefiere no exportar las variables en cada terminal, configúrelas en la *Run Configuration* del IDE. La aplicación **no** lee el archivo `.env`: eso lo hace Docker Compose.

Si experimenta problemas con la carga de assets de Vaadin o desea forzar una limpieza profunda del motor de compilación frontend (Vite/Node), ejecute nuestro script de automatización:
```powershell
.\dev-refresh.ps1
```

### 4. Ejecución de Pruebas Unitarias
El núcleo financiero y los cálculos de márgenes se verifican mediante suites de pruebas unitarias:
```bash
./mvnw test
```

---

## 📜 Historia del Proyecto

Este sistema evolucionó desde una aplicación Vaadin más pequeña de gestión de catálogo
(`ProductsApp`, ya retirada) que resolvía el ABM de productos, clientes y coeficientes de precio,
pero sin control de stock multi-depósito ni integración con WooCommerce. Este repositorio es la
versión que quedó en producción y la que se mantiene.

---

## ⚖️ Decisiones y Trade-offs

### Vaadin en lugar de un SPA separado

**Decisión:** el frontend vive en el mismo artefacto Java, no como SPA independiente.
**Por qué:** un solo deploy, un solo lenguaje, y el equipo es de backend.
**Costo:** el bundle generado pesa en el repo y en el arranque; para UI muy custom Vaadin
impone su modelo de componentes. Si el frontend creciera, habría que separarlo.

### Stock multi-depósito en columnas, no en un libro de movimientos

**Decisión:** cada producto guarda su stock por ubicación en columnas (`stock_local`,
`stock_deposito`, `stock_galpon`) y el total se materializa en `stock_total`, recalculado en cada
guardado.
**Por qué:** la grilla tipo Excel, el POS y el dashboard necesitan el stock por depósito y el
total ya resueltos; con un libro de movimientos cada lectura sería una agregación y el grid
dejaría de ser instantáneo sobre miles de productos.
**Costo:** no hay trazabilidad por movimiento (no se reconstruye quién descontó qué ni cuándo) y
la última escritura gana si dos sesiones editan el mismo producto, porque no hay bloqueo
optimista. Si el negocio necesitara auditoría de stock, habría que introducir un ledger y migrar
los totales.

---

## 👨‍💻 Autor & Contacto
**Gerardo Maidana** — *Backend Developer | Java & Spring Boot Specialist*
*   [LinkedIn](https://linkedin.com/in/gerardomaidana)
*   [GitHub](https://github.com/CharlyZeta/)
