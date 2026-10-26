# INFORME DE ENTREGA DE PROYECTO
## CURSO INTEGRADOR I: SISTEMAS - SOFTWARE

---

### CARÁTULA Y DATOS GENERALES

* **UNIVERSIDAD:** Universidad Tecnológica del Perú (UTP)
* **FACULTAD:** Facultad de Ingeniería de Sistemas e Informática
* **CURSO:** Curso Integrador I: Sistemas - Software
* **DOCENTE:** Mg. Anita Condo López
* **TÍTULO DEL PROYECTO:**  
  *Sistema Web de Conteo Cíclico de Inventario Fraccionado para la Reducción de Mermas y Conciliación con SAP MB52 en Tiendas Retail Deportivo*
* **CASO DE APLICACIÓN:** Marathon Sports — Sede Atocongo (Open Plaza Atocongo, Lima, Perú)
* **INTEGRANTES:**
  1. **Joseph Aaron Churampi Quispe** — Código: `U21212777`
  2. **Jose Armando Tunqui Puelles** — Código: `U23331844`
* **FECHA DE PRESENTACIÓN:** Octubre de 2026

---

## 1. INTRODUCCIÓN Y CONTEXTO EMPRESARIAL

Marathon Sports es una de las principales cadenas de retail deportivo en el Perú y Sudamérica, operando tiendas con miles de unidades en exhibición y almacén. La **Sede Atocongo** presenta un flujo continuo de clientes y un inventario dinámico compuesto por tres grandes líneas de negocio: **Calzado Deportivo y Urbano**, **Ropa / Textil Oficial** y **Accesorios / Balonería**.

Históricamente, las tiendas de este sector han dependido de inventarios físicos generales anuales o semestrales con cierre de tienda. Este método resulta ineficiente en el comercio minorista moderno:
1. Paraliza la facturación y la atención al cliente durante el conteo.
2. Descubre las mermas meses después de ocurridas, imposibilitando la identificación de la causa raíz.
3. Genera falsos quiebres de stock en el sistema ERP SAP (transacción MB52), provocando ventas perdidas por falta de información de stock en tiempo real.

El presente proyecto implementa un **Sistema de Conteo Cíclico Fraccionado**, permitiendo auditorías continuas y no disruptivas programadas los días **9, 19 y 28 de cada mes**.

---

## 2. DEFINICIÓN DEL PROBLEMA Y OBJETIVOS

### 2.1. Problema General
¿Cómo reducir las discrepancias entre el stock teórico de SAP y el stock físico real, minimizando las mermas no justificadas en la tienda Marathon Sports Sede Atocongo?

### 2.2. Objetivo General
Diseñar e implementar una solución integral de software web y backend empresarial basada en Java Spring Boot que automatice el conteo cíclico fraccionado, reconcilie en tiempo real el stock físico con SAP MB52 y formalice la auditoría de mermas mediante reportes oficiales en Excel y PDF.

### 2.3. Objetivos Específicos
1. Desarrollar una interfaz de captura rápida mediante simulación de escáner de códigos de barra (EAN-13) con retroalimentación sonora y visual.
2. Calcular en tiempo real el indicador de Exactitud de Registro de Inventario (**IRA %**) y valorizar el impacto económico de faltantes y sobrantes.
3. Implementar un módulo de seguridad basado en **Spring Security 6** con control de acceso basado en roles (**RBAC**) y contraseñas cifradas en **BCrypt**.
4. Integrar la librería **Apache POI 5.2.5** para generar matrices de conciliación ejecutivas en formato `.xlsx`.
5. Integrar la librería **OpenPDF 1.3.39** para emitir Actas Oficiales de Auditoría de Mermas con dictamen legal y firmas de jefatura.
6. Programar tareas automáticas de auditoría cíclica fraccionada mediante el scheduler `@Scheduled` de Spring Boot.

---

## 3. ESPECIFICACIÓN DE REQUERIMIENTOS

### Requerimientos Funcionales (RF)

| Código | Nombre | Descripción |
|---|---|---|
| **RF01** | Captura por Código de Barras | El sistema debe registrar lecturas de códigos EAN-13 o SKU mediante lector RF o entrada manual con alertas audibles y visuales. |
| **RF02** | Consulta Pre-Venta de Stock | El vendedor debe poder consultar en tiempo real el stock físico dividido por zonas (Piso de venta, Almacén, Vitrina). |
| **RF03** | Reserva Inmediata | El vendedor puede apartar unidades físicas en mostrador actualizando la disponibilidad antes de emitir comprobante. |
| **RF04** | Conciliación Automática | El sistema debe comparar el conteo físico consolidado contra el stock teórico SAP (MB52) y clasificar cada SKU en Conforme, Faltante o Sobrante. |
| **RF05** | Cálculo de IRA % | El sistema debe calcular el indicador: $\text{IRA} = \left(\frac{\text{Conformes}}{\text{Total SKUs}}\right) \times 100$. |
| **RF06** | Auditoría y Justificación | El auditor debe registrar la causal investigada (*Hurto*, *Picking Traspapelado*, *Error de Recepción*) y evidencia para cada discrepancia. |
| **RF07** | Aprobación de Ajuste | El Jefe de Tienda (`ROLE_SUPERVISOR`) debe poder aprobar formalmente el ajuste de merma para regularizar SAP. |
| **RF08** | Exportación Excel (Apache POI) | El sistema debe exportar la matriz de conciliación a archivo Microsoft Excel `.xlsx` con estilos corporativos y fórmulas. |
| **RF09** | Emisión de Acta PDF (OpenPDF) | El sistema debe generar el Acta Oficial de Regularización en PDF con formato legal y firma digitalizada. |
| **RF10** | Autenticación y RBAC | Acceso restringido según rol: Operario, Auditor y Supervisor utilizando Spring Security 6. |

### Requerimientos No Funcionales (RNF)

* **RNF01 (Seguridad):** Las contraseñas deben estar cifradas mediante algoritmo hash BCrypt con salt criptográfico.
* **RNF02 (Rendimiento):** El tiempo de respuesta de los endpoints REST no debe exceder los 250 ms en condiciones normales de red local.
* **RNF03 (Disponibilidad Offline):** El frontend debe contar con mecanismo de resiliencia y almacenamiento local (LocalStorage) en caso de caída temporal del enlace con el servidor central.
* **RNF04 (Usabilidad):** Diseño ergonómico de alto contraste apto para terminales industriales Honeywell / Zebra y dispositivos móviles de tienda.
* **RNF05 (Compatibilidad):** El frontend es 100% compatible con navegadores estándar sin requerir plugins propietarios ni frameworks pesados.
* **RNF06 (Trazabilidad):** Todo movimiento de inventario debe registrar fecha, hora precisa (DATETIME) y nombre del usuario responsable.

---

## 4. DETALLE DE LAS LIBRERÍAS EXTERNAS IMPLEMENTADAS

### 1. Spring Security 6.2.3
* **Dependencia:** `org.springframework.boot:spring-boot-starter-security`
* **Implementación:**
  * Clase `SecurityConfig.java`: Configura `SecurityFilterChain`, desactiva CSRF para APIs REST stateless, habilita CORS para el frontend, y define la cadena de filtros de autorización.
  * `BCryptPasswordEncoder`: Cifra las credenciales de los usuarios con factor de costo 10.
  * Jerarquía de roles: `ROLE_OPERARIO` < `ROLE_AUDITOR` < `ROLE_SUPERVISOR`.
  * Endpoint `POST /api/v1/auth/login`: Autentica credenciales mediante `AuthenticationManager` de Spring Security y retorna el token de sesión y roles.

### 2. Apache POI 5.2.5
* **Dependencias:** `org.apache.poi:poi` y `org.apache.poi:poi-ooxml`
* **Implementación:**
  * Servicio `ExcelReporteService.java`: Instancia un libro `XSSFWorkbook` en memoria.
  * Aplica estilos de celda personalizados: Cabeceras con relleno rojo corporativo Marathon (`#E30613`), texto blanco en negrita, bordes finos, alineación numérica y formato de moneda `S/ #,##0.00`.
  * Escribe filas de datos con 12 atributos por SKU y agrega fila de totales con fórmulas de Excel (`SUM(H2:H23)`, `SUM(L2:L23)`).
  * Retorna un `ByteArrayInputStream` que se transmite al cliente con MIME type `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

### 3. OpenPDF 1.3.39
* **Dependencia:** `com.github.librepdf:openpdf`
* **Implementación:**
  * Servicio `PdfReporteService.java`: Crea un documento PDF con márgenes A4 normalizados.
  * Incorpora tabla de metadatos institucionales con número de acta (`ACT-2026-ATC-001`).
  * Inserta recuadro de métrica ejecutiva con el valor IRA % y categorización cromática.
  * Genera tabla de discrepancias con causales investigadas y observaciones de auditoría.
  * Añade bloque formal de dos columnas para las firmas del Auditor de Inventario (`Joseph Aaron Churampi`) y la Jefa de Tienda / Supervisora (`Anita Condo López`).

### 4. Jakarta Validation (Complementaria)
* **Dependencia:** `org.springframework.boot:spring-boot-starter-validation`
* **Propósito:** Validaciones estrictas en DTOs con `@NotBlank`, `@Min`, `@Size` para evitar inyecciones y cargas de datos corruptos.

---

## 5. REPORTE DE PRUEBAS DE SOFTWARE (TESTING)

El backend cuenta con una batería de **17 pruebas automatizadas** ejecutadas con JUnit 5 y Mockito.

### Resumen de Ejecución:
* **Entorno:** OpenJDK 17 / Apache Maven 3.9
* **Comando:** `mvnw.cmd test`
* **Pruebas ejecutadas:** 17
* **Fallos:** 0
* **Errores:** 0
* **Omitidas:** 0
* **Estado:** **`BUILD SUCCESS`**

### Detalle de Suites de Prueba:
1. `ReporteExportTest`: Valida la generación no nula y tamaño de bytes de los reportes generados con Apache POI (`.xlsx`) y OpenPDF (`.pdf`).
2. `SecurityIntegrationTest`: Valida la autenticación con Spring Security, encriptación BCrypt y rechazo de contraseñas incorrectas (401).
3. `ReconciliationServiceTest`: Valida el cálculo de la fórmula IRA % y la correcta clasificación de conformidades.
4. `AuditoriaServiceTest`: Valida la aprobación de ajustes de merma y cambio de estado a aprobado.
5. `ConteoServiceTest`: Valida la sumatoria incremental de escaneos y persistencia en base de datos.
6. `PreVentaServiceTest`: Valida la consulta multizona y la reserva de stock.
7. `TrazabilidadServiceTest`: Valida el orden cronológico y consistencia del log de movimientos.
8. `CicloServiceTest`: Valida la programación de los 3 conteos fraccionados.

---

## 6. GUÍA DE DEMOSTRACIÓN PARA LA SUSTENTACIÓN

Para la sustentación ante el jurado o docente evaluador, se recomienda seguir el siguiente guion de prueba en vivo:

1. **Apertura y Métricas Generales:**
   * Abrir `index.html`. Observar las 4 tarjetas KPI en la vista de **Validación & Auditoría** (IRA %, Stock SAP, Físico e Impacto Económico).
2. **Prueba de Reporte Excel con Apache POI:**
   * Hacer clic en el botón verde **"Exportar Excel (.xlsx)"**.
   * Abrir el archivo descargado y mostrar las 12 columnas, los colores institucionales y las fórmulas calculadas por Apache POI.
3. **Prueba de Acta Oficial con OpenPDF:**
   * Hacer clic en el botón rojo **"Acta Oficial PDF"**.
   * Mostrar el acta con número correlativo, dictamen del IRA y los campos de firma formal.
4. **Prueba de Seguridad con Spring Security:**
   * Hacer clic en el botón morado **"Spring Security"**.
   * Cambiar de rol entre **Operario** (Jose Armando Tunqui), **Auditor** (Joseph Aaron Churampi) y **Supervisora** (Anita Condo López) observando cómo se actualiza el header y se valida el token.
5. **Prueba de Escáner y Marbete:**
   * Ir a la pestaña **"Conteo Escáner"**.
   * Usar los botones rápidos (+1, +5, +10) o los chips demo de calzado/ropa.
   * Hacer clic en **"Imprimir Marbete"** para mostrar la etiqueta de estantería con código de barras listo para impresión.
6. **Prueba de Consulta Pre-Venta:**
   * Ir a **"Consulta Pre-Venta"** y buscar "Nike Air Max" o hacer clic en los chips rápidos.
   * Mostrar la distribución exacta en Piso de Venta, Almacén y Vitrina y probar el botón **"Reservar para Pre-Venta"**.
7. **Prueba de Trazabilidad y Modelos:**
   * Ir a **"Trazabilidad SKU"** para revisar la línea de tiempo de auditorías.
   * Hacer clic en el enlace **"Modelos de Datos"** en el header para mostrar los diagramas conceptual y relacional normalizados.
8. **Prueba de Reset Demo:**
   * Hacer clic en **"Reset Demo"** en la esquina superior para demostrar la capacidad de restaurar los datos iniciales de forma idempotente.

---

## 7. CONCLUSIONES

1. Se cumplió satisfactoriamente con la totalidad de los requerimientos funcionales y las especificaciones académicas del curso, integrando **Spring Security 6**, **Apache POI 5.2.5** y **OpenPDF 1.3.39**.
2. La arquitectura basada en conteo cíclico fraccionado (Días 9, 19 y 28) reduce drásticamente las horas improductivas de inventario general y garantiza que la exactitud de registro (**IRA %**) se mantenga por encima de la meta corporativa del 95%.
3. El sistema cuenta con resiliencia total, código modular, base de datos completamente normalizada y una suite de 17 pruebas automatizadas que certifican su calidad y estabilidad para la entrega final del proyecto.
