-- ===================================================================
-- SCRIPT SQL: BASE DE DATOS MARATHON SPORTS - SEDE ATOCONGO
-- Caso de Estudio: Conteo Cíclico de Inventario Fraccionado
-- Autores: Joseph Aaron Churampi (U21212777), Jose Armando Tunqui (U23331844)
-- ===================================================================

-- 1. Tabla de Productos (Stock SAP)
CREATE TABLE IF NOT EXISTS productos (
    sku VARCHAR(30) PRIMARY KEY,
    ean VARCHAR(30) NOT NULL UNIQUE,
    linea VARCHAR(50) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    talla VARCHAR(20),
    color VARCHAR(50),
    ubicacion VARCHAR(100),
    stock_sap INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(10,2) NOT NULL
);

-- 2. Tabla de Conteos Físicos
CREATE TABLE IF NOT EXISTS conteos_fisicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL UNIQUE,
    conteo_fisico INT NOT NULL,
    fecha_conteo DATETIME,
    escaneado_por VARCHAR(100),
    ubicacion VARCHAR(100)
);

-- 3. Tabla de Ciclos de Conteo (Días 9, 19 y 28)
CREATE TABLE IF NOT EXISTS ciclos_conteo (
    id VARCHAR(50) PRIMARY KEY,
    linea VARCHAR(50) NOT NULL,
    dia_programado INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    estado VARCHAR(30) NOT NULL,
    frecuencia VARCHAR(50),
    responsable VARCHAR(100),
    fecha_limite VARCHAR(20)
);

-- 4. Tabla de Auditorías de Merma
CREATE TABLE IF NOT EXISTS auditorias_merma (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    causal VARCHAR(200) NOT NULL,
    comentario TEXT,
    validado_por VARCHAR(100) NOT NULL,
    fecha_validacion DATETIME,
    estado_aprobado BOOLEAN NOT NULL
);

-- 5. Tabla de Historial de Escaneo
CREATE TABLE IF NOT EXISTS historial_escaneos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    timestamp DATETIME,
    ubicacion VARCHAR(100),
    usuario VARCHAR(100)
);

-- 6. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(30) PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    sede VARCHAR(100),
    codigo VARCHAR(30)
);
