-- ============================================================================
-- BASE DE DATOS: marathon_inventario
-- PROYECTO: Sistema de Conteo Cíclico de Inventario Fraccionado
-- CASO DE ESTUDIO: Marathon Sports - Sede Atocongo (Retail Deportivo)
-- CURSO: Curso Integrador I: Sistemas - Software
-- DOCENTE: Mg. Anita Condo López
-- INTEGRANTES:
--   - Joseph Aaron Churampi Quispe (U21212777)
--   - Jose Armando Tunqui Puelles (U23331844)
-- AÑO: 2026
-- ============================================================================

CREATE DATABASE IF NOT EXISTS marathon_inventario
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE marathon_inventario;

-- ----------------------------------------------------------------------------
-- 1. TABLA: productos (Catálogo Sincronizado SAP MB52)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS productos;
CREATE TABLE productos (
    sku VARCHAR(30) NOT NULL PRIMARY KEY,
    ean VARCHAR(30) NOT NULL UNIQUE,
    linea VARCHAR(50) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    talla VARCHAR(20),
    color VARCHAR(50),
    ubicacion VARCHAR(100),
    stock_sap INT NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_piso_venta INT DEFAULT 0,
    stock_almacen INT DEFAULT 0,
    stock_vitrina INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. TABLA: conteos_fisicos (Lecturas y Conteos Consolidados)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS conteos_fisicos;
CREATE TABLE conteos_fisicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL UNIQUE,
    conteo_fisico INT NOT NULL DEFAULT 0,
    fecha_conteo DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    escaneado_por VARCHAR(100),
    ubicacion VARCHAR(100),
    CONSTRAINT fk_conteo_producto FOREIGN KEY (sku) REFERENCES productos (sku) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. TABLA: ciclos_conteo (Programación de Ciclos Fraccionados: Días 9, 19, 28)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS ciclos_conteo;
CREATE TABLE ciclos_conteo (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    linea VARCHAR(50) NOT NULL,
    dia_programado INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    frecuencia VARCHAR(50),
    responsable VARCHAR(100),
    fecha_limite VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. TABLA: auditorias_merma (Discrepancias, Causales y Aprobaciones)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS auditorias_merma;
CREATE TABLE auditorias_merma (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL,
    descripcion_producto VARCHAR(200),
    linea VARCHAR(50),
    ubicacion VARCHAR(100),
    stock_sap INT DEFAULT 0,
    conteo_fisico INT DEFAULT 0,
    diferencia INT DEFAULT 0,
    tipo VARCHAR(50) NOT NULL,
    causal VARCHAR(200) NOT NULL,
    comentario VARCHAR(500),
    validado_por VARCHAR(100),
    fecha_validacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_aprobado BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. TABLA: usuarios (Personal de Auditoría y Roles RBAC)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id VARCHAR(30) NOT NULL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    sede VARCHAR(100),
    codigo VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. TABLA: movimientos_trazabilidad (Historial de Auditoría y Logs)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS movimientos_trazabilidad;
CREATE TABLE movimientos_trazabilidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL,
    tipo_badge VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(100),
    stock_sap INT,
    conteo_fisico INT,
    diferencia INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 7. TABLA: configuracion_ciclo (Reglas y Asignaciones Activas)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS configuracion_ciclo;
CREATE TABLE configuracion_ciclo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    frecuencia VARCHAR(120) NOT NULL,
    categorias_muestreo VARCHAR(250) NOT NULL,
    regla_muestreo VARCHAR(300) NOT NULL,
    responsable_calzado VARCHAR(100),
    rol_responsable_calzado VARCHAR(100),
    responsable_jefe VARCHAR(100),
    rol_responsable_jefe VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INSERCIÓN DE DATOS INICIALES (CATÁLOGO REAL SEDE ATOCONGO)
-- ============================================================================

-- 1. Insertar 22 Productos (Calzado, Ropa, Accesorios)
INSERT INTO productos (sku, ean, linea, marca, descripcion, talla, color, ubicacion, stock_sap, precio_unitario, costo_unitario, stock_piso_venta, stock_almacen, stock_vitrina) VALUES
-- Calzado
('CAL-NIK-001', '7751234001015', 'Calzado', 'Nike', 'Zapatillas Nike Mercurial Vapor 15 Club FG/MG', '41', 'Negro / Carmesí', 'Atocongo • Anaquel 04', 14, 269.90, 160.00, 6, 7, 1),
('CAL-NIK-002', '790123456712', 'Calzado', 'Nike', 'Zapatillas Nike Air Max Excee Running', '42', 'Blanco / Azul', 'Pasillo A - Estante 03', 10, 389.00, 210.00, 4, 5, 1),
('CAL-ADI-003', '7751234001039', 'Calzado', 'Adidas', 'Zapatillas Adidas Predator Accuracy.3 FG', '40.5', 'Negro / Blanco / Rosa', 'Atocongo • Anaquel 04', 12, 299.90, 180.00, 5, 6, 1),
('CAL-ADI-004', '7751234001046', 'Calzado', 'Adidas', 'Zapatillas Adidas Grand Court Base 2.0 Urbano', '42', 'Blanco Clásico', 'Pasillo B - Estante 01', 20, 199.90, 115.00, 8, 10, 2),
('CAL-PUM-005', '7751234001053', 'Calzado', 'Puma', 'Zapatillas Puma Future Match TT Fútbol Césped', '41', 'Azul Eléctrico / Naranja', 'Pasillo A - Estante 04', 8, 289.90, 170.00, 3, 4, 1),
('CAL-PUM-006', '7751234001060', 'Calzado', 'Puma', 'Zapatillas Puma Smash V2 L Casual', '39', 'Blanco / Negro', 'Pasillo B - Estante 02', 16, 189.90, 110.00, 6, 8, 2),
('CAL-UND-007', '7751234001077', 'Calzado', 'Under Armour', 'Zapatillas UA Charged Assert 10 Running', '43', 'Gris Grafito', 'Pasillo B - Estante 04', 7, 279.90, 165.00, 3, 3, 1),
('CAL-MAR-008', '7751234001084', 'Calzado', 'Marathon', 'Zapatillas Marathon Training Pro Flex', '40', 'Negro / Rojo', 'Pasillo C - Estante 01', 18, 159.90, 85.00, 8, 8, 2),
-- Ropa
('ROP-MAR-101', '7751234002012', 'Ropa', 'Marathon', 'Camiseta Oficial Selección Peruana Local 2026', 'M', 'Blanco / Rojo', 'Isla Central - Perchero 01', 35, 269.90, 140.00, 14, 18, 3),
('ROP-MAR-102', '7751234002029', 'Ropa', 'Marathon', 'Camiseta Oficial Selección Peruana Visitante', 'L', 'Rojo / Dorado', 'Isla Central - Perchero 02', 22, 269.90, 140.00, 9, 11, 2),
('ROP-MAR-103', '7751234002036', 'Ropa', 'Marathon', 'Camiseta Oficial Universitario 2026', 'M', 'Crema Tradicional', 'Atocongo • Anaquel 04', 20, 249.90, 135.00, 8, 10, 2),
('ROP-NIK-104', '7751234002043', 'Ropa', 'Nike', 'Camiseta Alianza Lima Oficial Stadium 2026', 'S', 'Azul / Blanco', 'Pared Textil - Sector Sur', 19, 279.90, 155.00, 8, 9, 2),
('ROP-ADI-105', '7751234002050', 'Ropa', 'Adidas', 'Camiseta Sporting Cristal Oficial 2026', 'L', 'Celeste / Blanco', 'Pared Textil - Sector Este', 15, 249.90, 135.00, 6, 8, 1),
('ROP-NIK-106', '7751234002067', 'Ropa', 'Nike', 'Pantalón Buzo Nike Dri-FIT Academy', 'M', 'Negro', 'Perchero Textil 04', 24, 179.90, 95.00, 10, 12, 2),
('ROP-ADI-107', '7751234002074', 'Ropa', 'Adidas', 'Casaca Cortaviento Adidas Tiro 24', 'L', 'Azul Marino / Blanco', 'Perchero Textil 05', 11, 229.90, 125.00, 4, 6, 1),
('ROP-MAR-108', '7751234002081', 'Ropa', 'Marathon', 'Short Deportivo Marathon Entrenamiento Pro', 'M', 'Negro / Rojo', 'Mesa Textil 01', 30, 79.90, 38.00, 12, 15, 3),
-- Accesorios
('ACC-ADI-201', '7751234003019', 'Accesorios', 'Adidas', 'Balón de Fútbol Adidas Club N°5 Oficial', 'N°5', 'Blanco / Negro / Neón', 'Módulo Central Accesorios', 25, 99.90, 52.00, 10, 12, 3),
('ACC-PUM-202', '7751234003026', 'Accesorios', 'Puma', 'Balón Puma Orbita LaLiga 1 Training', 'N°5', 'Amarillo / Multicolor', 'Módulo Central Accesorios', 14, 89.90, 46.00, 6, 7, 1),
('ACC-NIK-203', '7751234003033', 'Accesorios', 'Nike', 'Mochila Nike Brasilia 9.5 Training (24L)', 'Única', 'Negro / Logo Blanco', 'Pared Accesorios 01', 12, 149.90, 82.00, 5, 6, 1),
('ACC-MAR-204', '7751234003040', 'Accesorios', 'Marathon', 'Canilleras Marathon Pro Shield con Tobillera', 'M', 'Rojo / Negro', 'Góndola Accesorios 02', 40, 49.90, 22.00, 16, 20, 4),
('ACC-ADI-205', '7751234003057', 'Accesorios', 'Adidas', 'Guantes de Arquero Adidas Predator Training', '9', 'Negro / Rojo / Blanco', 'Góndola Accesorios 03', 8, 119.90, 65.00, 3, 4, 1),
('ACC-NIK-206', '7751234003064', 'Accesorios', 'Nike', 'Pack x3 Pares Medias Deportivas Nike Everyday', 'L (39-43)', 'Blanco', 'Mostrador de Caja', 45, 59.90, 28.00, 20, 20, 5);

-- 2. Insertar Conteos Físicos de Prueba
INSERT INTO conteos_fisicos (sku, conteo_fisico, fecha_conteo, escaneado_por, ubicacion) VALUES
('CAL-NIK-001', 12, DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Jose Armando Tunqui', 'Atocongo • Anaquel 04'),
('CAL-NIK-002', 11, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'Joseph Aaron Churampi', 'Pasillo A - Estante 03'),
('CAL-ADI-003', 11, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'Jose Armando Tunqui', 'Atocongo • Anaquel 04'),
('ROP-MAR-103', 22, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'Jose Armando Tunqui', 'Atocongo • Anaquel 04'),
('CAL-ADI-004', 20, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'Jose Armando Tunqui', 'Pasillo B - Estante 01'),
('CAL-PUM-005', 7, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'Joseph Aaron Churampi', 'Pasillo A - Estante 04'),
('CAL-PUM-006', 17, DATE_SUB(NOW(), INTERVAL 1 HOUR), 'Joseph Aaron Churampi', 'Pasillo B - Estante 02');

-- 3. Insertar Ciclos Programados
INSERT INTO ciclos_conteo (id, linea, dia_programado, nombre, descripcion, estado, frecuencia, responsable, fecha_limite) VALUES
('ciclo-calzado-09', 'Calzado', 9, 'Ciclo 1: Calzado Deportivo y Urbano', 'Auditoría mensual prioritaria de calzado de alta rotación (Zapatillas fútbol, running y urbanas).', 'En Proceso', '3 Conteos Fraccionados al Mes (Días 9, 19, 29)', 'Jose Armando Tunqui', '2026-10-09'),
('ciclo-ropa-19', 'Ropa', 19, 'Ciclo 2: Ropa y Textil Deportivo', 'Auditoría de camisetas oficiales de clubes/selecciones, polerones, casacas y shorts.', 'Pendiente', '3 Conteos Fraccionados al Mes (Días 9, 19, 29)', 'Joseph Aaron Churampi', '2026-10-19'),
('ciclo-accesorios-28', 'Accesorios', 29, 'Ciclo 3: Accesorios y Balonería', 'Auditoría de balones, canilleras, guantes, medias técnicas, mochilas y gorras.', 'Pendiente', '3 Conteos Fraccionados al Mes (Días 9, 19, 29)', 'Joseph Aaron Churampi', '2026-10-29');

-- 4. Insertar Discrepancias / Auditorías
INSERT INTO auditorias_merma (sku, descripcion_producto, linea, ubicacion, stock_sap, conteo_fisico, diferencia, tipo, causal, comentario, validado_por, fecha_validacion, estado_aprobado) VALUES
('CAL-NIK-001', 'Zapatillas Nike Mercurial Vapor 15 Club', 'CALZADO', 'Atocongo • Anaquel 04', 14, 12, -2, 'Faltante (-2)', 'Hurto en Piso de Venta', 'Caja vacía en góndola principal', 'Joseph Aaron Churampi', DATE_SUB(NOW(), INTERVAL 2 HOUR), FALSE),
('CAL-ADI-003', 'Zapatillas Adidas Predator Accuracy.3', 'CALZADO', 'Atocongo • Anaquel 04', 12, 11, -1, 'Faltante (-1)', 'Picking Traspapelado', 'Ubicado en pasillo secundario', 'Joseph Aaron Churampi', DATE_SUB(NOW(), INTERVAL 1 HOUR), FALSE),
('ROP-MAR-103', 'Camiseta Oficial Universitario 2026', 'ROPA', 'Atocongo • Anaquel 04', 20, 22, 2, 'Sobrante (+2)', 'Error de Recepción', 'Doble ingreso en guía de remisión', 'Joseph Aaron Churampi', DATE_SUB(NOW(), INTERVAL 40 MINUTE), FALSE);

-- 5. Insertar Usuarios
INSERT INTO usuarios (id, nombre, usuario, clave, rol, sede, codigo) VALUES
('user-01', 'Joseph Aaron Churampi Quispe', 'jchurampi', 'marathon2026', 'Jefe de Inventario', 'Marathon Atocongo', 'U21212777'),
('user-02', 'Jose Armando Tunqui Puelles', 'jtunqui', 'marathon2026', 'Auditor Principal Calzado', 'Marathon Atocongo', 'U23331844'),
('user-03', 'Anita Condo López', 'acondo', 'admin123', 'Supervisora / Docente', 'Marathon Atocongo', 'DOC-01'),
('user-04', 'Operario Escáner Piso', 'operario', 'operario123', 'Personal de Tienda', 'Marathon Atocongo', 'OPER-01');

-- 6. Insertar Movimientos de Trazabilidad
INSERT INTO movimientos_trazabilidad (sku, tipo_badge, titulo, descripcion, fecha_hora, usuario, stock_sap, conteo_fisico, diferencia) VALUES
('CAL-NIK-001', 'Ajustado', 'Ajuste de Auditoría Física de Inventario', 'Confirmado por Joseph Churampi - Ajuste de merma en sistema SAP', DATE_SUB(NOW(), INTERVAL 1 HOUR), 'Joseph Aaron Churampi', 14, 12, -2),
('CAL-NIK-001', 'Conteo Físico', 'Escaneo Físico en Terminal de Conteo', 'Registrado por Jose Armando Tunqui - Conteo total de 12 unidades en anaquel', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'Jose Armando Tunqui', 14, 12, -2),
('CAL-NIK-001', 'MB52 Sync', 'Carga Inicial de Stock Teórico SAP', 'Sincronizado vía MB52 Excel - Carga inicial de 14 unidades en Sede Atocongo', DATE_SUB(NOW(), INTERVAL 7 HOUR), 'Sistema Automático SAP', 14, 0, -14);

-- 7. Insertar Configuración Activa
INSERT INTO configuracion_ciclo (frecuencia, categorias_muestreo, regla_muestreo, responsable_calzado, rol_responsable_calzado, responsable_jefe, rol_responsable_jefe) VALUES
('3 Conteos Fraccionados al Mes (Días 9, 19, 29)', 'Calzado Deportivo,Ropa & Textil,Accesorios', 'Filtro activo: Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma).', 'Jose Armando Tunqui', 'Auditor Principal Calzado', 'Joseph Aaron Churampi', 'Jefe de Inventario');

-- ============================================================================
-- FIN DEL SCRIPT SQL
-- ============================================================================
