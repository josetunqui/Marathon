# Sistema de Conteo Cíclico de Inventario Fraccionado
## Caso de Estudio: Marathon Sports — Sede Atocongo (Retail Deportivo)

![Marathon Sports Banner](https://img.shields.io/badge/Marathon%20Sports-Sede%20Atocongo-E30613?style=for-the-badge&logo=target)
![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot 3.2.4](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=for-the-badge&logo=springboot)
![Spring Security 6](https://img.shields.io/badge/Spring%20Security-6.2.3-6DB33F?style=for-the-badge&logo=springsecurity)
![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-D22128?style=for-the-badge&logo=apache)
![OpenPDF](https://img.shields.io/badge/OpenPDF-1.3.39-0052CC?style=for-the-badge)
![Tests Passing](https://img.shields.io/badge/Tests-17%2F17%20Passing-brightgreen?style=for-the-badge)

---

## 📌 Información Académica

* **Institución:** Universidad Tecnológica del Perú (UTP)
* **Curso:** Curso Integrador I: Sistemas - Software
* **Docente:** Mg. Anita Condo López
* **Integrantes / Autores:**
  * **Joseph Aaron Churampi Quispe** — Código: `U21212777`
  * **Jose Armando Tunqui Puelles** — Código: `U23331844`
* **Año Académico:** 2026

---

## 🎯 Caso de Estudio y Problemática

En tiendas retail deportivas de alta rotación como **Marathon Sports Sede Atocongo (Open Plaza Atocongo)**, las auditorías tradicionales a inventario cerrado (una vez al año o semestrales) generan pérdidas operativas masivas por parálisis de venta, no detectan a tiempo mermas por hurto o despachos traspapelados, y presentan desincronización entre el stock teórico de **SAP (transacción MB52)** y el stock real en piso de venta, almacén y vitrinas.

### Solución Implementada: Conteo Cíclico Fraccionado
El sistema divide el catálogo de tienda en 3 ciclos mensuales fijos:
1. **Día 9:** Línea Calzado Deportivo y Urbano (Nike, Adidas, Puma, Marathon).
2. **Día 19:** Línea Ropa y Textil Oficial (Camisetas Selección Peruana, Universitario, Alianza, Cristal).
3. **Día 29:** Línea Accesorios y Balonería (Balones LaLiga/FIFA, canilleras, mochilas, guantes).

Métricas Clave:
* **IRA (Exactitud de Registro de Inventario):** Meta corporativa $\ge 95.0\%$.
* **Valorización en Tiempo Real:** Detección automática de impacto económico en Soles ($S/.$).
* **Trazabilidad de Discrepancias:** Justificación con causales estandarizadas (*Hurto en Piso de Venta*, *Picking Traspapelado*, *Error de Recepción en Guía*).

---

## 🏛️ Arquitectura del Sistema

```
Integrador/
├── index.html                   # SPA Frontend con las 5 vistas de negocio corporativo
├── database_marathon_atocongo.sql # Script DDL + DML completo para MySQL 8.0
├── database_marathon_oracle_sqlserver.sql # DDL de producción para Oracle 19c & SQL Server 2022
├── README.md                    # Documentación principal del repositorio
├── DOCUMENTACION_ENTREGA.md     # Memoria técnica de ingeniería de software
├── css/
│   └── styles.css               # Sistema de diseño retail, dark/light, glassmorphism y print
├── js/
│   ├── api-client.js            # Cliente REST fetch con fallback offline garantizado
│   ├── app.js                   # Orquestador UI, audio beeper y modales
│   ├── audio.js                 # Web Audio API para simulación de escáner RF
│   ├── data.js                  # Catálogo de datos locales y constantes
│   ├── reconciliation.js        # Motor de conciliación matemática e IRA %
│   ├── scanner.js               # Emulador de escáner de códigos de barra EAN-13
│   └── storage.js               # Persistencia LocalStorage con versionado
└── backend/                     # Proyecto Maven Spring Boot 3
    ├── pom.xml                  # Dependencias y configuración de plugins
    ├── mvnw / mvnw.cmd          # Maven Wrapper
    └── src/
        ├── main/
        │   ├── java/pe/marathon/inventario/
        │   │   ├── config/      # DataLoader, OpenAPI/Swagger Config
        │   │   ├── controller/  # 9 REST Controllers (Auth, Inventario, Reportes...)
        │   │   ├── dao/         # Capa de persistencia JPA
        │   │   ├── dto/         # Data Transfer Objects
        │   │   ├── exception/   # GlobalExceptionHandler y excepciones de negocio
        │   │   ├── model/       # Entidades JPA (Producto, Conteo, Auditoria...)
        │   │   ├── scheduler/   # Tarea programada @Scheduled para ciclos
        │   │   ├── security/    # Spring Security 6 RBAC & BCrypt
        │   │   └── service/     # Lógica de negocio y servicios de reporte
        │   │       └── reporte/ # ExcelReporteService (POI) & PdfReporteService (OpenPDF)
        │   └── resources/
        │       └── application.properties # Configuración MySQL, Hibernate y Scheduler
        └── test/java/pe/marathon/inventario/
            ├── ReporteExportTest.java      # Test de generación Apache POI y OpenPDF
            ├── SecurityIntegrationTest.java# Test de Spring Security y roles BCrypt
            ├── ReconciliationServiceTest.java # Test de cálculo de fórmula IRA %
            └── ... (17 Tests Unitarios y de Integración con JUnit 5 y Mockito)
```

---

## 📚 Las 3 Librerías Externas Implementadas (Requisito del Curso)

### 1. Spring Security 6 (`spring-boot-starter-security 3.2.4`)
* **Ubicación:** [`SecurityConfig.java`](file:///backend/src/main/java/pe/marathon/inventario/security/SecurityConfig.java) y [`AuthController.java`](file:///backend/src/main/java/pe/marathon/inventario/controller/AuthController.java).
* **Propósito:** Autenticación de operarios, auditores y supervisores con contraseñas encriptadas con **BCrypt** (`BCryptPasswordEncoder`).
* **Roles RBAC:**
  * `ROLE_OPERARIO`: Acceso a captura de escáner en piso de venta y consulta pre-venta.
  * `ROLE_AUDITOR`: Acceso a conciliación, exportación de reportes Excel y actas PDF.
  * `ROLE_SUPERVISOR`: Aprobación definitiva de mermas y ajustes hacia SAP.
* **Interfaz de Cambio de Rol:** Modal interactivo en la barra superior que permite conmutar en vivo entre los usuarios demo.

### 2. Apache POI 5.2.5 (`org.apache.poi:poi` y `poi-ooxml`)
* **Ubicación:** [`ExcelReporteService.java`](file:///backend/src/main/java/pe/marathon/inventario/service/reporte/ExcelReporteService.java) y [`ReporteController.java`](file:///backend/src/main/java/pe/marathon/inventario/controller/ReporteController.java).
* **Propósito:** Generación en memoria (`XSSFWorkbook`) de reportes ejecutivos en formato `.xlsx`.
* **Características:**
  * Paleta visual corporativa Marathon Sports (Rojo `#E30613`, fuentes blancas y grises).
  * 12 columnas de conciliación: SKU, EAN, Línea, Marca, Descripción, Ubicación, Stock SAP, Físico, Diferencia, Estado, Costo Unitario e Impacto Económico ($S/.$).
  * Fórmulas automáticas de Excel (`SUM`, `AVERAGE`).
  * Hoja de cálculo complementaria con resumen de discrepancias y métricas IRA.
  * Endpoint directo: `GET /api/v1/reportes/excel/reconciliacion`.

### 3. OpenPDF 1.3.39 (`com.github.librepdf:openpdf`)
* **Ubicación:** [`PdfReporteService.java`](file:///backend/src/main/java/pe/marathon/inventario/service/reporte/PdfReporteService.java) y [`ReporteController.java`](file:///backend/src/main/java/pe/marathon/inventario/controller/ReporteController.java).
* **Propósito:** Generación formal del **Acta Oficial de Auditoría y Regularización de Mermas** (`ACT-2026-ATC-001`).
* **Características:**
  * Encabezado institucional con metadatos de la Sede Atocongo y fecha exacta.
  * Tarjeta ejecutiva con indicador IRA % y dictamen de exactitud.
  * Tabla formateada con detalle de faltantes, sobrantes y causales investigadas.
  * Bloque de firmas formales para el Auditor de Inventario y la Jefatura de Tienda.
  * Endpoint directo: `GET /api/v1/reportes/pdf/acta-auditoria`.

---

## 🚀 Guía de Instalación y Ejecución

### Prerrequisitos
* **Java Development Kit (JDK):** Versión 17 o superior.
* **MySQL Server:** 8.0 o superior (Puerto default `3306`).
* **Navegador Web:** Chrome, Edge, Firefox o Brave.

### 1. Configurar la Base de Datos
Ejecuta el script SQL en tu cliente MySQL (MySQL Workbench, HeidiSQL, DBeaver o consola):
```sql
SOURCE database_marathon_atocongo.sql;
```
*Base de datos:* `marathon_inventario` con usuario `root` y contraseña vacía (ajustable en `backend/src/main/resources/application.properties`).

### 2. Compilar y Ejecutar el Backend (Spring Boot)
Abre una terminal en la carpeta `backend/` y ejecuta:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# O ejecutar el JAR precompilado
java -jar target/inventario-atocongo-1.0.0.jar
```
El servidor iniciará en: **`http://localhost:8080`**.

### 3. Iniciar el Frontend
Simplemente haz doble clic o abre con tu navegador el archivo:
```
index.html
```
No requiere Node.js ni servidor frontend adicional; se conecta automáticamente al backend Spring Boot en `http://localhost:8080` y conmuta a modo local si el backend no estuviese activo.

---

## 🧪 Pruebas Unitarias y de Integración

El proyecto cuenta con **17 pruebas automatizadas** que validan la lógica de negocio, reportes y seguridad.
Para ejecutarlas:
```bash
.\mvnw.cmd test
```
**Resultado de Ejecución:**
```
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📡 Catálogo de Endpoints REST Principales

| Método | Endpoint | Descripción | Rol Mínimo |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Autenticación con Spring Security (BCrypt) | Anónimo |
| `GET` | `/api/v1/auth/usuarios-demo` | Listado de usuarios y perfiles demo | Anónimo |
| `GET` | `/api/v1/inventario/kpis` | Resumen ejecutivo e índice IRA % | Anónimo / Todos |
| `GET` | `/api/v1/inventario/reconciliacion` | Matriz completa SAP vs Físico | Anónimo / Todos |
| `POST` | `/api/v1/inventario/reseed-demo` | Restaurar datos demo a valores iniciales | Todos |
| `POST` | `/api/v1/conteo/escanear` | Registro de captura de código de barras | `ROLE_OPERARIO` |
| `GET` | `/api/v1/preventa/consulta` | Consulta de disponibilidad multizona | `ROLE_OPERARIO` |
| `GET` | `/api/v1/auditorias/pendientes` | Discrepancias abiertas para auditoría | `ROLE_AUDITOR` |
| `POST` | `/api/v1/auditorias/{id}/aprobar` | Aprobación de ajuste de merma | `ROLE_SUPERVISOR` |
| `GET` | `/api/v1/reportes/excel/reconciliacion`| Descarga de reporte Excel (.xlsx) con POI | `ROLE_AUDITOR` |
| `GET` | `/api/v1/reportes/pdf/acta-auditoria` | Generación de Acta Oficial PDF con OpenPDF | `ROLE_AUDITOR` |
| `GET` | `/swagger-ui/index.html` | Documentación interactiva Swagger / OpenAPI | Todos |

---

## 👥 Autores y Contacto
* **Joseph Aaron Churampi Quispe** (`U21212777`) — Jefe de Proyecto & Desarrollador Backend
* **Jose Armando Tunqui Puelles** (`U23331844`) — Especialista de Procesos & Desarrollador Fullstack
* **Docente Tutor:** Mg. Anita Condo López — *Curso Integrador I: Sistemas - Software (UTP 2026)*
