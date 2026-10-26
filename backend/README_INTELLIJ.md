# 🚀 Guía de Inicio Rápido: Backend Spring Boot en IntelliJ IDEA
### Sistema de Conteo Cíclico de Inventario — Marathon Sports Sede Atocongo
**Curso:** Curso Integrador I: Sistemas - Software  
**Autores:** Joseph Aaron Churampi Quispe (U21212777), Jose Armando Tunqui Puelles (U23331844)  
**Docente:** Mg. Anita Condo López  

---

## 📁 1. ¿Cómo abrir el proyecto en IntelliJ IDEA?

1. Abre **IntelliJ IDEA** (Community o Ultimate).
2. Haz clic en **File > Open...** (o *Open* en la pantalla de bienvenida).
3. Selecciona la carpeta **`backend`**:
   ```
   Integrador/backend
   ```
4. Haz clic en **OK** y selecciona **"Open as Project"** (o *"Trust Project"* si te lo solicita).
5. Espera unos segundos a que IntelliJ detecte el archivo `pom.xml` y descargue las dependencias de Maven automáticamente.

---

## ▶️ 2. ¿Cómo ejecutar el proyecto?

1. En el panel izquierdo de archivos (*Project Explorer*), navega hasta:
   ```
   src/main/java/pe/marathon/inventario/MarathonInventarioApplication.java
   ```
2. Haz clic derecho sobre el archivo `MarathonInventarioApplication.java` y selecciona:
   **`Run 'MarathonInventarioApplication'`** (o presiona `Shift + F10`).
3. En la consola inferior verás los logs de arranque de Spring Boot y el mensaje:
   ```
   ==================================================================
   🚀 MARATHON INVENTARIO ATOCONGO - BACKEND INICIADO CON ÉXITO
   📡 Endpoints REST: http://localhost:8080/api/v1/inventario/reconciliacion
   📖 Swagger UI Docs: http://localhost:8080/swagger-ui.html
   🗄️ Consola H2 DB:  http://localhost:8080/h2-console
   ==================================================================
   ```

---

## 🌐 3. Servicios y Herramientas Listas

### A. Documentación Swagger / OpenAPI 3:
Abre en tu navegador:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**
Podrás probar todos los endpoints REST directamente desde una interfaz web interactiva.

### B. Consola de Base de Datos H2 (En memoria):
👉 **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
- **JDBC URL:** `jdbc:h2:mem:marathondb`
- **User Name:** `sa`
- **Password:** *(dejar vacío)*
- Haz clic en **Connect** para ver las tablas `PRODUCTOS`, `CONTEOS_FISICOS`, `CICLOS_CONTEO`, `AUDITORIAS_MERMA`.

### C. Conexión con tu Frontend HTML/JS:
1. Abre tu archivo `index.html` en el navegador.
2. Haz clic en el botón superior **"Test API Spring Boot"**.
3. Selecciona el modo **"Spring Boot (8080)"**.
4. ¡El frontend detectará automáticamente el backend corriendo en IntelliJ!

---

## 🗄️ 4. ¿Cómo cambiar a MySQL (Opcional)?

Por defecto el proyecto arranca con base de datos H2 en memoria para que no tengas que instalar nada y funcione de inmediato. Si para la sustentación deseas usar **MySQL Workbench**:

1. Abre `src/main/resources/application.properties`.
2. Comenta la sección de H2 y descomenta las 4 líneas de MySQL:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/marathon_inventario?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=TU_PASSWORD_DE_MYSQL
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   ```
3. Crea la base de datos vacía en MySQL Workbench:
   ```sql
   CREATE DATABASE marathon_inventario;
   ```
4. Reinicia Spring Boot y las tablas y datos iniciales se crearán automáticamente.

---

## 📦 5. Estructura de Paquetes en Java

```
pe.marathon.inventario/
├── MarathonInventarioApplication.java (Clase principal)
├── config/
│   ├── CorsConfig.java                (Permite peticiones desde el navegador sin bloqueo CORS)
│   └── DataLoader.java                (Carga los 22 productos, conteos y ciclos de Marathon Atocongo)
├── controller/
│   ├── InventarioController.java      (/api/v1/inventario: cruces, lecturas de escáner y ajustes)
│   ├── ProductoController.java        (/api/v1/productos: catálogo y stock SAP)
│   ├── CicloController.java           (/api/v1/ciclos: calendario días 9, 19 y 28)
│   └── AuditoriaController.java       (/api/v1/auditorias: mermas y justificaciones)
├── dto/
│   ├── ScanRequestDTO.java
│   ├── ScanResponseDTO.java
│   ├── AjusteManualDTO.java
│   ├── ItemReconciliacionDTO.java
│   ├── ResumenKpiDTO.java
│   ├── ReporteReconciliacionDTO.java
│   ├── AuditoriaRequestDTO.java
│   └── EstadoCicloDTO.java
├── model/
│   ├── Producto.java                  (Entidad JPA catálogo SAP)
│   ├── ConteoFisico.java              (Entidad JPA conteo en tienda)
│   ├── CicloConteo.java               (Entidad JPA ciclos fraccionados)
│   ├── AuditoriaMerma.java            (Entidad JPA mermas y causas)
│   ├── HistorialEscaneo.java          (Entidad JPA auditoría de pistola/escáner)
│   └── Usuario.java                   (Entidad JPA personal de tienda)
├── repository/                        (Interfaces Spring Data JPA con consultas automáticas)
├── security/                          (Spring Security 6: SecurityConfig, encriptación BCrypt y RBAC)
└── service/                           (Lógica de cruces, fórmula IRA % y valorizaciones en S/.)
    └── reporte/                       (Apache POI para Excel .xlsx y OpenPDF para Actas PDF)
```

---

## 📚 6. Las 3 Librerías Implementadas (Requisito Académico)

Para cumplir con el requerimiento de integración de librerías especializadas en arquitectura Spring Boot, se han implementado e interconectado 3 componentes:

### 1️⃣ Spring Security 6 (`org.springframework.boot:spring-boot-starter-security`)
* **Propósito:** Autenticación de usuarios, autorización basada en roles (RBAC) y encriptación robusta de contraseñas.
* **Componentes creados:**
  * [`SecurityConfig.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/security/SecurityConfig.java): Configuración de `SecurityFilterChain`, algoritmo `BCryptPasswordEncoder` (fuerza 10), protección de endpoints y compatibilidad con CORS.
  * [`AuthController.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/controller/AuthController.java): Endpoints `/api/v1/auth/login`, `/api/v1/auth/status` y `/api/v1/auth/usuarios-demo`.
* **Usuarios y Roles Configurados:**
  * `operario` / `operario123` -> Rol `OPERARIO` (Piso de venta y pistolas de escáner).
  * `auditor` / `auditor123` -> Rol `AUDITOR` (Conciliación, cálculo de IRA % y reportes).
  * `supervisor` / `admin123` -> Rol `SUPERVISOR` (Aprobación final de mermas y ajustes SAP).

### 2️⃣ Apache POI 5.2.5 (`org.apache.poi:poi`, `poi-ooxml`)
* **Propósito:** Generación programática de hojas de cálculo de Microsoft Excel (`.xlsx`) profesionales y estilizadas.
* **Componentes creados:**
  * [`ExcelReporteService.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/service/reporte/ExcelReporteService.java): Construcción de libros Excel con encabezados corporativos Marathon Crimson, tarjetas KPI de exactitud (IRA %), formatos de moneda nacional (`S/ #,##0.00`), bordes y anchos automáticos.
  * [`ReporteController.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/controller/ReporteController.java): Endpoint `GET /api/v1/reportes/excel/reconciliacion`.

### 3️⃣ OpenPDF 1.3.39 (`com.github.librepdf:openpdf`)
* **Propósito:** Generación formal de Actas Oficiales de Auditoría de Inventario Cíclico en formato PDF (`application/pdf`).
* **Componentes creados:**
  * [`PdfReporteService.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/service/reporte/PdfReporteService.java): Maquetación de documentos PDF con membrete institucional, tablas de conciliación SAP vs Físico, dictamen pericial de diferencias y recuadros formales para firmas del Auditor y Jefe de Tienda.
  * [`ReporteController.java`](file:///c:/Users/joset_/OneDrive/Documentos/Integrador/backend/src/main/java/pe/marathon/inventario/controller/ReporteController.java): Endpoint `GET /api/v1/reportes/pdf/acta-auditoria`.

