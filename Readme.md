# 🚀 StockDual
Sistema híbrido moderno para la gestión de facturación e inventario en múltiples ubicaciones (Local, Depósito y Galpón), integrado de forma bidireccional con WooCommerce. 
Soluciona el problema de mantener el stock sincronizado entre tiendas físicas y virtuales de forma centralizada. 
Pensado para pymes con operaciones retail y eCommerce.

## 🎯 Objetivo
Brindar un control preciso y centralizado del inventario distribuido, optimizando los procesos de auditoría y ajuste de stock mediante una interfaz tipo hoja de cálculo ágil. Su valor principal radica en la sincronización con WooCommerce, evitando quiebres de stock y ventas de productos no disponibles.

## 🧠 Contexto
Desarrollado como una solución productiva real para unificar la gestión del Punto de Venta (POS) y los depósitos con la tienda online de WooCommerce. Fue pensado para escalar de manera sólida.

## 🛠 Tech Stack
![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.4.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2016-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Vaadin](https://img.shields.io/badge/Vaadin%20Flow%2024.6-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

## 🏗 Arquitectura
Arquitectura limpia en capas (Layered architecture):
- **Domain**: Entidades JPA puras (`@Entity`, `Product`, `Category`).
- **Repository**: Interfaces `JpaRepository`. Se utilizan cargas profundas dirigidas (`@EntityGraph`) para evitar problemas tipo N+1.
- **Service**: Encapsula las reglas de negocio y maneja los límites de transacción (`@Transactional`).
- **Client**: Integraciones REST con capas externas (como el cliente de la API de WooCommerce).
- **UI**: Vistas y componentes completamente en Java apoyados en el framework Vaadin, enlazando datos a través de `Binder<T>`.

## 🔐 Seguridad
- Spring Security para el manejo de sesiones estándar y blindaje de las rutas y vistas proporcionadas por Vaadin.
- Las credenciales sensibles (API Keys de WooCommerce) y accesos de BD se abstraen en configuraciones y variables de entorno del servidor.

## 🧪 Testing
Este proyecto contiene pruebas de componentes específicos:
- Pruebas unitarias de configuración de BD (ej. `DbTest.java`).
- Pruebas de integración para las API (ej. `WcTest.java`).

**Cómo ejecutarlos:**
```bash
./mvnw test
```

## 🐳 Ejecución Local
1. Clonar el repositorio
```bash
git clone https://github.com/CharlyZeta/StockDual.git
```

2. Configuración y levante de servicios:
Levantar la base de datos (PostgreSQL 16 mapeado al puerto 5433).
```bash
docker-compose up -d
```

3. Ejecutar la app:
```bash
./mvnw spring-boot:run
```

## 🚀 Despliegue
- `Dockerfile` incluido para empaquetado de proyecto final estandarizado.
- Preparado para despliegue en contenedores apoyado por migraciones automáticas nativas vía `Flyway`.

## 📌 Estado del Proyecto
- En mejora continua (Catálogo manual estable, integración de stock base operativa).

## 🛣 Roadmap
- Implementar el módulo completo de reportes.
- Agregar Webhooks para recibir actualizaciones de ventas/ordenes que modifican inventario de WooCommerce de manera directa y en tiempo real.
- Agregar KPIs en la parte superior del tablero: Total de artículos, valor del inventario e items bajos en stock.

## 👨‍💻 Autor
Gerardo Maidana
Backend Developer | Java & Spring Boot
LinkedIn https://linkedin.com/in/gerardomaidana
GitHub https://github.com/CharlyZeta/
