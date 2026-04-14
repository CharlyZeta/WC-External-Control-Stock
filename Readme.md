# 🚀 StockDual
[![Version](https://img.shields.io/badge/Version-0.5.0--beta-blue?style=for-the-badge)](./ChangeLog.txt)
[![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.4.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin%2024.6-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white)](https://vaadin.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red?style=for-the-badge)](./LICENSE)

Sistema híbrido avanzado para la gestión integral de inventario y facturación en múltiples ubicaciones (Local, Depósito y Galpón), con sincronización nativa bidireccional para WooCommerce.

## 🌟 Funcionalidades Principales

### 📊 Dashboard Inteligente (KPIs)
Visualización en tiempo real del estado de salud del negocio.
*   **Métricas Clave**: Total de productos, stock físico total, conteo de faltantes y stock crítico.
*   **Alertas Configurables**: Define tu propio umbral de "Stock Crítico" desde los ajustes de usuario.
*   **Distribución Geográfica**: Gráficos de barras que muestran la concentración de mercadería por ubicación.

> **[ CAPTURA SIMULADA: DASHBOARD ]**  
> ![Dashboard Screenshot](https://raw.githubusercontent.com/CharlyZeta/StockDual/main/docs/screenshots/dashboard_mock.png)  
> *Interfaz limpia con tarjetas de métricas en degradados azules, verdes y naranjas.*

### 📋 Control de Stock "Excel-Like"
Grilla de alta densidad optimizada para auditorías rápidas.
*   **Edición Inline**: Modifica stocks directamente en la celda sin ventanas emergentes.
*   **Filtros Unificados**: Busca por nombre, SKU o categorías (combinando categorías locales y de WooCommerce).
*   **Detección de Discrepancias**: Resaltado visual automático (en rojo suave) de productos cuyo stock local no coincide con el reportado en la tienda online.

> **[ CAPTURA SIMULADA: CONTROL DE STOCK ]**  
> ![Stock Grid Screenshot](https://raw.githubusercontent.com/CharlyZeta/StockDual/main/docs/screenshots/stock_grid_mock.png)  
> *Grilla compacta con columnas fijas y resaltado de errores de sincronización.*

### 🧮 Formador de Precios (Planilla de Costos)
Herramienta administrativa para la determinación precisa de precios de venta.
*   **Cálculo en Cascada**: Aplica descuentos sucesivos, gastos en porcentaje, IVA, gastos fijos y márgenes de ganancia.
*   **Reactividad Total**: Los resultados de "Precio Final" y "Precio Lista" se actualizan instantáneamente al presionar Enter.
*   **Persistencia por Proveedor**: Almacena las reglas de cálculo incluso para proveedores sin productos activos.

> **[ CAPTURA SIMULADA: FORMADOR DE PRECIOS ]**  
> ![Price Formulator Screenshot](https://raw.githubusercontent.com/CharlyZeta/StockDual/main/docs/screenshots/price_formulator_mock.png)  
> *Planilla técnica con valores centrados y columnas de resultados congeladas a la derecha.*

## 🛠 Tech Stack & Arquitectura
*   **Backend**: Java 21 + Spring Boot 3.4.
*   **Frontend**: Vaadin Flow 24 (React-based components) + Vite.
*   **Base de Datos**: PostgreSQL 16 con control de versiones vía **Flyway**.
*   **Seguridad**: Spring Security con autenticación robusta y control de acceso por roles.
*   **Integración**: Cliente REST reactivo para la API de WooCommerce.

## 🧪 Calidad y Testing
El proyecto sigue una arquitectura orientada a **TDD (Test Driven Development)** para sus capas de lógica financiera.
*   **Validación de Cálculos**: Tests unitarios que aseguran la precisión de la cascada de costos hasta el segundo decimal.
*   **Ejecución**: 
    ```bash
    ./mvnw test
    ```

## 🔧 Ejecución Local
1.  **Requisitos**: Docker y Java 21.
2.  **Base de Datos**: `docker-compose up -d`.
3.  **Ejecución**: `./mvnw spring-boot:run`.
4.  **Limpieza de Cache**: En caso de problemas con la UI, usar el script personalizado:
    ```powershell
    .\dev-refresh.ps1
    ```

## 🚀 Despliegue
- `Dockerfile` incluido para empaquetado de proyecto final estandarizado.
- Preparado para despliegue en contenedores apoyado por migraciones automáticas nativas vía `Flyway`.

## 📌 Estado del Proyecto
- **En mejora continua** (Catálogo manual estable, integración de stock base operativa, formador de precios funcional).

## 🛣 Roadmap
- Implementar el módulo completo de reportes.
- Agregar Webhooks para recibir actualizaciones de ventas/ordenes que modifican inventario de WooCommerce de manera directa y en tiempo real.

## 👨‍💻 Autor
**Gerardo Maidana**  
Backend Developer | Java & Spring Boot  
[LinkedIn](https://linkedin.com/in/gerardomaidana) | [GitHub](https://github.com/CharlyZeta/)
