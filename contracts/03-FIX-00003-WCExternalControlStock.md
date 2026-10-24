# CONTRACT: Higiene del repositorio público WC-External-Control-Stock (StockDual)
# ID: FIX-00003
# Status: RESOLVED
# Mode: LOOP
# Gate-Mode: EXPRESS
# HO-GATE: 2026-10-24 — aprobado por el humano (Status DRAFT→APPROVED, Mode GATE→LOOP)
# Repo GitHub: CharlyZeta/WC-External-Control-Stock · nombre interno: StockDual
# Local: D:\ProyectosDual\StockDual (rama main, HEAD d06aba7)
# Baseline de auditoría: 2026-10-23 · Re-auditado en Gate: 2026-10-24

## Intent

El repositorio público del sistema StockDual tiene tres frentes abiertos de cara a quien lo
evalúa: expone una contraseña de base de datos como valor por defecto, no es descubrible en la
búsqueda de GitHub (0 topics) y su historial —188.206 líneas en el commit inicial, 7 commits—
parece sospechoso porque nadie explica de dónde salen. Este work item cierra esos tres frentes
sin tocar el comportamiento funcional del sistema.

## Use Case

**Actor:** Gerardo Maidana, dueño del repositorio; lo evalúan reclutadores y clientes técnicos
desde el perfil de GitHub.

**Goal:** que quien abra el repo entienda qué es, por qué el historial es corto, qué decisiones
se tomaron y cuánto costaron; que no exista ninguna credencial real en el árbol de código; y que
el repo se encuentre buscando las tecnologías que usa.

### Main Flow

1. Aplicar los 8 topics (`java spring-boot vaadin woocommerce inventory-management ecommerce
   postgresql rest-api`) al repositorio en GitHub y verificar por API que quedaron aplicados.
2. Reemplazar el default sensible de la contraseña de BD por `localdev` en los 3 archivos
   trackeados (`src/main/resources/application.yml`, `docker-compose.yml`, tabla de variables
   del `Readme.md`) y crear `.env.example` trackeado con ese valor.
3. Verificar que el stack levanta con la credencial nueva: `docker-compose up -d` +
   `./mvnw spring-boot:run` sin `DB_PASSWORD` definido, y la app responde en
   `http://localhost:8080`.
4. Agregar al `Readme.md` la nota de historial dentro de la sección de arquitectura y la
   sección "Decisiones y trade-offs" con 2 decisiones reales, antes de "Autor & Contacto".

### Alternative Flows

- AF-01: El literal sensible aparece en un archivo trackeado que la auditoría no listó (tests,
  docs, scripts) → se corrige también; BR-001 aplica a todo el árbol fuera de `contracts/`.
- AF-02: El `Readme.md` ya tiene una sección de decisiones parcial o con otro título → se
  consolida en una única sección "Decisiones y trade-offs" con 2 decisiones, sin duplicar
  contenido ni dejar encabezados huérfanos.

### Riesgos y Contingencias (no normativo, no genera Completion Map)

- C-01: Los defaults de password quedan desalineados entre `application.yml` (`DB_PASSWORD`) y
  `docker-compose.yml` (`POSTGRES_PASSWORD`) → alinear ambos (BR-002).
- C-02: El volumen de PostgreSQL del compose ya fue inicializado con la contraseña anterior y el
  arranque falla por autenticación → escalar a Gate; el Loop no borra volúmenes ni datos.
- C-03: La API de GitHub rechaza la escritura de topics (403/404) → aplicar AF-04 del plan
  (instrucción manual) y escalar. Nunca se commitea ni se imprime el token.
- C-04: La aplicación no arranca por una causa ajena a este work item (build del frontend
  Vaadin, JVM, red) → registrar la salida exacta, marcar `❌` y escalar; una causa externa no se
  disfraza de éxito ni se arregla fuera de la spec.
- C-05: El contrato excluye `contracts/` de BR-001 porque este archivo necesita nombrar el
  literal para verificarlo. La exclusión es explícita y auditable, no una excepción oculta.

## Business Rules

- BR-001: Ningún archivo trackeado contiene el literal sensible auditado (la contraseña de
  desarrollo usada como default), excluyendo `contracts/`, que es el registro de esta misma
  corrección y necesita nombrarlo para poder verificarse. Verificable:
  `git grep -n "stockdual_dev_2024" -- . ':!contracts'` sin coincidencias.
- BR-002: El default de la contraseña de BD es el mismo valor no sensible (`localdev`) en
  `application.yml`, `docker-compose.yml` y la tabla del `Readme.md`, de modo que el stack
  levanta sin configurar variables de entorno.
- BR-003: `.env.example` está trackeado con `DB_PASSWORD=localdev`; `.env` sigue ignorado por
  `.gitignore` y no hay ningún `.env` real trackeado.
- BR-004: El `Readme.md` explica el volumen del commit inicial dentro de la sección de
  arquitectura (menciona `frontend/generated/` y `src/main/java/com/stockdual/`) y la nota
  aparece antes de "Autor & Contacto".
- BR-005: El `Readme.md` tiene una sección "Decisiones y trade-offs" con exactamente 2
  decisiones, cada una declarando **Decisión**, **Por qué** y **Costo**, ubicada antes de
  "Autor & Contacto".
- BR-006: Los topics del repositorio son exactamente los 8 del plan y no incluyen `pci-dss` ni
  ninguna certificación: implementar control de stock no es una certificación.
- BR-007: No hay regresión en lo ya entregado: licencia MIT en `LICENSE`, linaje documentado en
  "Historia del Proyecto" y wrapper Maven sin distribución vendoreada (`.mvn/maven` ausente del
  índice).
- BR-008: El work item no modifica código de negocio (`src/main/java/com/stockdual/**`),
  migraciones Flyway (`src/main/resources/db/migration/**`) ni tests existentes.

## Acceptance Criteria

- AC-001: GIVEN el árbol trackeado WHEN se ejecuta
  `git grep -n "stockdual_dev_2024" -- . ':!contracts'` THEN no hay coincidencias (exit code 1).
- AC-002: GIVEN `src/main/resources/application.yml` WHEN se lee la propiedad de password de
  datasource THEN vale `${DB_PASSWORD:localdev}`.
- AC-003: GIVEN `docker-compose.yml` WHEN se lee `POSTGRES_PASSWORD` THEN vale
  `${POSTGRES_PASSWORD:-localdev}`.
- AC-004: GIVEN la tabla de variables de entorno del `Readme.md` WHEN se lee la fila
  `DB_PASSWORD` THEN el valor por defecto documentado es `localdev`.
- AC-005: GIVEN el repositorio WHEN se ejecuta `git ls-files .env.example` THEN devuelve la ruta
  y el archivo contiene exactamente `DB_PASSWORD=localdev`.
- AC-006: GIVEN `.gitignore` WHEN se ejecuta `git ls-files .env` THEN no hay `.env` trackeado y
  `.gitignore` sigue conteniendo `.env`.
- AC-007: GIVEN PostgreSQL del compose arriba con el default nuevo y `DB_PASSWORD` sin definir
  WHEN se ejecuta `./mvnw spring-boot:run` THEN la aplicación conecta a la base y responde HTTP
  en `http://localhost:8080`.
- AC-008: GIVEN el `Readme.md` WHEN se busca "Nota sobre el historial" THEN existe dentro de la
  sección de arquitectura y menciona `frontend/generated/` y `src/main/java/com/stockdual/`.
- AC-009: GIVEN el orden de encabezados del `Readme.md` WHEN se listan los encabezados de nivel 2
  THEN existe uno que contiene "Decisiones y Trade-offs", aparece después de la sección de
  arquitectura y antes de `## 👨‍💻 Autor & Contacto`, y cuelga exactamente 2 subsecciones `###`.
- AC-010: GIVEN las 2 decisiones del README WHEN se leen THEN la primera es "Vaadin en lugar de
  un SPA separado" y la segunda es una decisión real del proyecto (stock multi-depósito,
  concurrencia de stock o Gemini server-side) y cada una declara su costo.
- AC-011: GIVEN la API de GitHub WHEN se ejecuta
  `GET /repos/CharlyZeta/WC-External-Control-Stock/topics` THEN devuelve exactamente los 8
  topics del plan y no incluye `pci-dss`.
- AC-012: GIVEN el índice de git WHEN se verifica `git ls-files LICENSE`, la presencia de
  "Historia del Proyecto" en el README y la ausencia de `.mvn/maven` en el índice THEN las tres
  condiciones se cumplen.
- AC-013: GIVEN el diff del work item WHEN se listan los archivos modificados THEN ninguno está
  bajo `src/main/java/com/stockdual/` ni `src/main/resources/db/migration/`.

## Entities Affected

- `src/main/resources/application.yml`: default de `DB_PASSWORD` (dato de configuración, no de
  negocio).
- `docker-compose.yml`: default de `POSTGRES_PASSWORD` del servicio `db`.
- `Readme.md`: fila `DB_PASSWORD` de la tabla de variables, nota de historial en arquitectura,
  sección "Decisiones y trade-offs".
- `.env.example`: archivo nuevo, trackeado, sin secretos reales.
- Configuración del repositorio en GitHub: topics.

## Inferred Assumptions

- [x] CONFIRMED — AS-01: el token guardado en Git Credential Manager (usuario `CharlyZeta`,
  scopes `gist, repo, workflow`) alcanza para escribir topics por API → si es falso, aplica C-03
  (instrucción manual) y el paso 4 queda pendiente de acción humana.
- [x] CONFIRMED — AS-02: `localdev` es un valor aceptable para el dueño (corto, sin requisito de
  longitud) y reemplaza al literal sensible en los 3 defaults juntos → si es falso, cambiar los 3
  a la vez (BR-002).
- [x] CONFIRMED — AS-03: la verificación de arranque (AC-007) corre contra el PostgreSQL del
  `docker-compose.yml` local, sin datos de negocio, y no requiere borrar volúmenes existentes →
  si es falso, aplica C-02.

## Ambiguity Log

- [x] Ninguna. Gate 2026-10-24: sin contradicciones BR↔AC; AS-01..AS-03 confirmadas por el
  humano en el HO-GATE (2026-10-24).

## Completion Map

# generated: 2026-10-24T09:30 — el Loop persiste el estado antes y después de cada ítem
# Topics aplicados en GitHub|functional|assertion:FIX-00003-ac011|verifier|✅
# Topics sin certificaciones|functional|unit-test:FIX-00003-br006|verifier|✅
# Sin literal sensible en el árbol|security|unit-test:FIX-00003-br001|verifier|✅
# grep del literal sin coincidencias|security|assertion:FIX-00003-ac001|verifier|✅
# .env.example trackeado|security|unit-test:FIX-00003-br003|verifier|✅
# Contenido de .env.example|security|assertion:FIX-00003-ac005|verifier|✅
# .env real fuera del índice|security|assertion:FIX-00003-ac006|verifier|✅
# Defaults alineados en los 3 archivos|functional|unit-test:FIX-00003-br002|verifier|✅
# application.yml con localdev|functional|assertion:FIX-00003-ac002|verifier|✅
# docker-compose.yml con localdev|functional|assertion:FIX-00003-ac003|verifier|✅
# Tabla del README con localdev|functional|assertion:FIX-00003-ac004|verifier|✅
# Literal en archivos no listados|security|unit-test:FIX-00003-af01|verifier|✅
# Arranque local con el default nuevo|functional|assertion:FIX-00003-ac007|tester|✅
# Nota de historial en arquitectura|functional|unit-test:FIX-00003-br004|verifier|✅
# Nota cita bundle y paquete|functional|assertion:FIX-00003-ac008|verifier|✅
# Sección de decisiones con costo|functional|unit-test:FIX-00003-br005|verifier|✅
# Orden de encabezados del README|functional|assertion:FIX-00003-ac009|verifier|✅
# Contenido de las 2 decisiones|functional|assertion:FIX-00003-ac010|verifier|✅
# Sección de decisiones única|functional|unit-test:FIX-00003-af02|verifier|✅
# Sin regresión en pasos 1–3|arch|unit-test:FIX-00003-br007|verifier|✅
# Licencia, linaje y wrapper vigentes|arch|assertion:FIX-00003-ac012|verifier|✅
# Código de negocio intacto|arch|unit-test:FIX-00003-br008|verifier|✅
# Diff acotado a config y docs|arch|assertion:FIX-00003-ac013|verifier|✅
# Main Flow completo (repo público higienizado)|functional|integration-test:FIX-00003-main|tester|✅

## Anexo: Plan original de auditoría (referencia)

Conservado del Contract original para trazabilidad. Pasos 1–3 ya entregados:

1. ✅ **Licencia MIT** — commit `1cd564e`.
2. ✅ **Linaje documentado** — commit `2ce2738`, sección "Historia del Proyecto" del README.
3. ✅ **Distribución de Maven vendoreada eliminada** — commit `d06aba7`, 41 archivos y 6.740
   líneas fuera del índice; wrapper verificado (`mvnw.cmd -v` → Maven 3.9.9).

Pendientes 4–6 (ver Main Flow). Contenido de referencia para el paso 6:

```markdown
> **Nota sobre el historial:** el commit inicial incluye el bundle generado del frontend
> (Vaadin / `frontend/generated/`), que explica el volumen de líneas. El código de negocio está
> en `src/main/java/com/stockdual/`.

### Vaadin en lugar de un SPA separado

**Decisión:** el frontend vive en el mismo artefacto Java, no como SPA independiente.
**Por qué:** un solo deploy, un solo lenguaje, y el equipo es de backend.
**Costo:** el bundle generado pesa en el repo y en el arranque; para UI muy custom Vaadin
impone su modelo de componentes. Si el frontend creciera, habría que separarlo.
```

Verificación de cierre original:

```powershell
git -C "D:\ProyectosDual\StockDual" grep -n "stockdual_dev_2024" -- . ':!contracts'
Select-String -Path "D:\ProyectosDual\StockDual\Readme.md" -Pattern "Decisiones|commit inicial"
```

- Sin coincidencias del literal (fuera de `contracts/`).
- README con la nota del bundle y 2 decisiones con costo declarado.

## Registro

| Paso | Fecha | Evidencia |
|---|---|---|
| 1 | 2026-10-23 | `1cd564e` |
| 2 | 2026-10-23 | `2ce2738` |
| 3 | 2026-10-23 | `d06aba7` |
| 4 | 2026-10-24 | topics por API `PUT`/`GET`: 8 aplicados |
| 5 | 2026-10-24 | `5dac321` — 3 defaults + `.env.example` + fila del README |
| 6 | 2026-10-24 | `5dac321` — nota de historial + 2 decisiones con costo |

## Completion Report

```
COMPLETION REPORT: FIX-00003
  Status:              RESOLVED
  Completion Map:      24/24 ✅
  Tests generados:     0 (higiene de repo: verificación por comando reproducible, ver
                       .sdd/runs/FIX-00003-2026-10-24T09-30.md)
  Criterios cubiertos: 24/24
  Ciclos Gate/Loop:    1 Gate + 1 Loop
  Ambigüedades:        0 escaladas, 0 resueltas
```

### Condiciones y observaciones de la verificación

- **AC-007** se verificó contra un volumen PostgreSQL **nuevo y efímero** (proyecto compose
  `stockdual-verify`, puerto 5434, volumen eliminado al terminar): 13 migraciones Flyway
  aplicadas y app sirviendo `HTTP 200` en `http://localhost:8080` sin `DB_PASSWORD` definido.
  Motivo: el volumen local `stockdual_pgdata` ya estaba inicializado con la contraseña anterior
  y PostgreSQL conserva la de la inicialización (riesgo C-02). No se borró ni se alteró ningún
  volumen del usuario.
- **Variables obligatorias ajenas a este work item**: el arranque local exige además
  `STOCKDUAL_ENCRYPTION_KEY` y `STOCKDUAL_ADMIN_PASSWORD` (obligatorias desde FIX-0001). Se
  usaron valores de prueba; ninguna es la contraseña de la BD, así que AC-007 se cumple con el
  alcance declarado.
- **Hallazgo fuera de alcance (candidato a work item nuevo)**: la tabla de variables del
  `Readme.md` no documenta `STOCKDUAL_ENCRYPTION_KEY` ni `STOCKDUAL_ADMIN_PASSWORD`, hoy
  obligatorias para arrancar. No se tocó por no estar en esta spec (BR-008 / AC-013).

## Estado verificado en la auditoría (2026-10-23) y re-verificado en Gate (2026-10-24)

- Licencia: **MIT** ✅ · Linaje: ✅ · Maven vendoreado: ✅ (`.mvn/maven` ausente del índice)
- Topics: **0** ❌ (confirmado por API en Gate)
- El literal sensible estaba en **3 archivos trackeados**: `application.yml:8`,
  `docker-compose.yml:11`, `Readme.md:288` (fila de tabla que documenta el default).

## Estado final (Loop, 2026-10-24)

- Topics: **8** ✅ (`java, spring-boot, vaadin, woocommerce, inventory-management, ecommerce,
  postgresql, rest-api`), sin certificaciones.
- El literal sensible: **0 coincidencias** en el árbol trackeado fuera de `contracts/`.
- `.env.example` trackeado con `DB_PASSWORD=localdev`; ningún `.env` trackeado.
- `Readme.md`: nota de historial en arquitectura + "Decisiones y Trade-offs" con 2 decisiones y
  su costo declarado.
