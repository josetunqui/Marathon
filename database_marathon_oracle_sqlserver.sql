-- ============================================================================
-- PROYECTO: Sistema de Conteo Cíclico Fraccionado de Inventario
-- CASO DE APLICACIÓN: Marathon Sports - Sede Atocongo (Retail Deportivo)
-- CURSO: Curso Integrador I: Sistemas - Software (UTP 2026)
-- DOCENTE: Mg. Anita Condo López
-- INTEGRANTES:
--   - Joseph Aaron Churampi Quispe (U21212777)
--   - Jose Armando Tunqui Puelles (U23331844)
-- ============================================================================
-- ARCHIVO: MODELO FÍSICO DE BASE DE DATOS DDL (ORACLE 19c/21c & SQL SERVER 2022)
-- ============================================================================

-- ############################################################################
-- SECCIÓN 1: DDL PARA ORACLE DATABASE 19c / 21c (PL/SQL)
-- ############################################################################

/*
   INSTRUCCIONES DE EJECUCIÓN EN ORACLE SQL DEVELOPER / SQL*PLUS:
   Conectarse como usuario esquema: MARATHON_ADMIN
*/

-- 1.1. LIMPIEZA DE TABLAS Y SECUENCIAS PREVIAS EN ORACLE
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE MOVIMIENTOS_TRAZABILIDAD CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE AUDITORIAS_MERMA CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE CONTEOS_FISICOS CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE CICLOS_CONTEO CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE CONFIGURACION_CICLO CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE PRODUCTOS CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE USUARIOS CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 1.2. TABLA: PRODUCTOS (Catálogo Maestro Sincronizado con SAP MB52)
CREATE TABLE PRODUCTOS (
    SKU                 VARCHAR2(30)   NOT NULL,
    EAN                 VARCHAR2(30)   NOT NULL,
    LINEA               VARCHAR2(50)   NOT NULL,
    MARCA               VARCHAR2(50)   NOT NULL,
    DESCRIPCION         VARCHAR2(200)  NOT NULL,
    TALLA               VARCHAR2(20),
    COLOR               VARCHAR2(50),
    UBICACION           VARCHAR2(100),
    STOCK_SAP           NUMBER(8)      DEFAULT 0 NOT NULL,
    PRECIO_UNITARIO     NUMBER(10,2)   DEFAULT 0.00 NOT NULL,
    COSTO_UNITARIO      NUMBER(10,2)   DEFAULT 0.00 NOT NULL,
    STOCK_PISO_VENTA    NUMBER(8)      DEFAULT 0,
    STOCK_ALMACEN       NUMBER(8)      DEFAULT 0,
    STOCK_VITRINA       NUMBER(8)      DEFAULT 0,
    CONSTRAINT PK_PRODUCTOS PRIMARY KEY (SKU),
    CONSTRAINT UK_PRODUCTOS_EAN UNIQUE (EAN),
    CONSTRAINT CHK_PROD_STOCK CHECK (STOCK_SAP >= 0),
    CONSTRAINT CHK_PROD_PRECIO CHECK (PRECIO_UNITARIO >= 0.00)
);

CREATE INDEX IDX_PROD_LINEA ON PRODUCTOS (LINEA);
CREATE INDEX IDX_PROD_MARCA ON PRODUCTOS (MARCA);

-- 1.3. TABLA: USUARIOS (Control de Acceso y Roles RBAC en Marathon)
CREATE TABLE USUARIOS (
    ID                  VARCHAR2(30)   NOT NULL,
    NOMBRE              VARCHAR2(120)  NOT NULL,
    USUARIO             VARCHAR2(50)   NOT NULL,
    CLAVE               VARCHAR2(100)  NOT NULL,
    ROL                 VARCHAR2(50)   NOT NULL,
    SEDE                VARCHAR2(100)  DEFAULT 'Marathon Atocongo',
    CODIGO              VARCHAR2(30),
    CONSTRAINT PK_USUARIOS PRIMARY KEY (ID),
    CONSTRAINT UK_USUARIOS_LOGIN UNIQUE (USUARIO),
    CONSTRAINT CHK_USUARIO_ROL CHECK (ROL IN ('Jefe de Inventario', 'Auditor Principal Calzado', 'Supervisora / Docente', 'Personal de Tienda', 'ROLE_SUPERVISOR', 'ROLE_AUDITOR', 'ROLE_OPERARIO'))
);

-- 1.4. TABLA: CICLOS_CONTEO (Programación Cíclica Fraccionada: Días 9, 19, 28)
CREATE TABLE CICLOS_CONTEO (
    ID                  VARCHAR2(50)   NOT NULL,
    LINEA               VARCHAR2(50)   NOT NULL,
    DIA_PROGRAMADO      NUMBER(2)      NOT NULL,
    NOMBRE              VARCHAR2(100)  NOT NULL,
    DESCRIPCION         VARCHAR2(300),
    ESTADO              VARCHAR2(30)   DEFAULT 'Pendiente' NOT NULL,
    FRECUENCIA          VARCHAR2(50),
    RESPONSABLE         VARCHAR2(100),
    FECHA_LIMITE        VARCHAR2(20),
    CONSTRAINT PK_CICLOS_CONTEO PRIMARY KEY (ID),
    CONSTRAINT CHK_CICLO_DIA CHECK (DIA_PROGRAMADO IN (9, 19, 28, 29)),
    CONSTRAINT CHK_CICLO_ESTADO CHECK (ESTADO IN ('Pendiente', 'En Proceso', 'Completado', 'Aprobado'))
);

-- 1.5. TABLA: CONTEOS_FISICOS (Captura Consolidada de Lecturas por SKU)
CREATE TABLE CONTEOS_FISICOS (
    ID                  NUMBER(19) GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    SKU                 VARCHAR2(30)   NOT NULL,
    CONTEO_FISICO       NUMBER(8)      DEFAULT 0 NOT NULL,
    FECHA_CONTEO        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ESCANEADO_POR       VARCHAR2(100),
    UBICACION           VARCHAR2(100),
    CONSTRAINT PK_CONTEOS_FISICOS PRIMARY KEY (ID),
    CONSTRAINT UK_CONTEOS_SKU UNIQUE (SKU),
    CONSTRAINT FK_CONTEO_PRODUCTO FOREIGN KEY (SKU) REFERENCES PRODUCTOS (SKU) ON DELETE CASCADE,
    CONSTRAINT CHK_CONTEO_VALOR CHECK (CONTEO_FISICO >= 0)
);

CREATE INDEX IDX_CONTEO_SKU ON CONTEOS_FISICOS (SKU);

-- 1.6. TABLA: AUDITORIAS_MERMA (Detección de Discrepancias y Aprobación de Mermas)
CREATE TABLE AUDITORIAS_MERMA (
    ID                  NUMBER(19) GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    SKU                 VARCHAR2(30)   NOT NULL,
    DESCRIPCION_PROD    VARCHAR2(200),
    LINEA               VARCHAR2(50),
    UBICACION           VARCHAR2(100),
    STOCK_SAP           NUMBER(8)      DEFAULT 0,
    CONTEO_FISICO       NUMBER(8)      DEFAULT 0,
    DIFERENCIA          NUMBER(8)      DEFAULT 0,
    TIPO                VARCHAR2(50)   NOT NULL,
    CAUSAL              VARCHAR2(200)  NOT NULL,
    COMENTARIO          VARCHAR2(500),
    VALIDADO_POR        VARCHAR2(100),
    FECHA_VALIDACION    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ESTADO_APROBADO     NUMBER(1)      DEFAULT 0 NOT NULL, -- 0: Pendiente, 1: Aprobado
    CONSTRAINT PK_AUDITORIAS_MERMA PRIMARY KEY (ID),
    CONSTRAINT FK_AUDIT_PROD FOREIGN KEY (SKU) REFERENCES PRODUCTOS (SKU) ON DELETE CASCADE,
    CONSTRAINT CHK_AUDIT_APROBADO CHECK (ESTADO_APROBADO IN (0, 1))
);

CREATE INDEX IDX_AUDIT_SKU ON AUDITORIAS_MERMA (SKU);
CREATE INDEX IDX_AUDIT_ESTADO ON AUDITORIAS_MERMA (ESTADO_APROBADO);

-- 1.7. TABLA: MOVIMIENTOS_TRAZABILIDAD (Historial Inmutable de Auditoría)
CREATE TABLE MOVIMIENTOS_TRAZABILIDAD (
    ID                  NUMBER(19) GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    SKU                 VARCHAR2(30)   NOT NULL,
    TIPO_BADGE          VARCHAR2(50)   NOT NULL,
    TITULO              VARCHAR2(150)  NOT NULL,
    DESCRIPCION         VARCHAR2(500)  NOT NULL,
    FECHA_HORA          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    USUARIO             VARCHAR2(100),
    STOCK_SAP           NUMBER(8),
    CONTEO_FISICO       NUMBER(8),
    DIFERENCIA          NUMBER(8),
    CONSTRAINT PK_MOVIMIENTOS_TRAZ PRIMARY KEY (ID),
    CONSTRAINT FK_TRAZ_PROD FOREIGN KEY (SKU) REFERENCES PRODUCTOS (SKU) ON DELETE CASCADE
);

CREATE INDEX IDX_TRAZ_SKU_FECHA ON MOVIMIENTOS_TRAZABILIDAD (SKU, FECHA_HORA);

-- 1.8. TABLA: CONFIGURACION_CICLO (Parámetros y Asignación de Responsabilidades)
CREATE TABLE CONFIGURACION_CICLO (
    ID                  NUMBER(19) GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    FRECUENCIA          VARCHAR2(120)  NOT NULL,
    CATEGORIAS_MUESTREO VARCHAR2(250)  NOT NULL,
    REGLA_MUESTREO      VARCHAR2(300)  NOT NULL,
    RESPONSABLE_CALZADO VARCHAR2(100),
    ROL_RESP_CALZADO    VARCHAR2(100),
    RESPONSABLE_JEFE    VARCHAR2(100),
    ROL_RESP_JEFE       VARCHAR2(100),
    CONSTRAINT PK_CONFIG_CICLO PRIMARY KEY (ID)
);


-- ############################################################################
-- SECCIÓN 2: DDL PARA MICROSOFT SQL SERVER 2022 (T-SQL)
-- ############################################################################

/*
   INSTRUCCIONES DE EJECUCIÓN EN SQL SERVER MANAGEMENT STUDIO (SSMS):
   USE master;
   GO
*/

-- 2.1. CREACIÓN DE BASE DE DATOS SQL SERVER
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'MarathonInventario')
BEGIN
    CREATE DATABASE MarathonInventario
    COLLATE Modern_Spanish_CI_AS;
END
GO

USE MarathonInventario;
GO

-- 2.2. LIMPIEZA DE TABLAS PREVIAS (ORDEN DE DEPENDENCIAS FK)
IF OBJECT_ID('dbo.movimientos_trazabilidad', 'U') IS NOT NULL DROP TABLE dbo.movimientos_trazabilidad;
IF OBJECT_ID('dbo.auditorias_merma', 'U') IS NOT NULL DROP TABLE dbo.auditorias_merma;
IF OBJECT_ID('dbo.conteos_fisicos', 'U') IS NOT NULL DROP TABLE dbo.conteos_fisicos;
IF OBJECT_ID('dbo.ciclos_conteo', 'U') IS NOT NULL DROP TABLE dbo.ciclos_conteo;
IF OBJECT_ID('dbo.configuracion_ciclo', 'U') IS NOT NULL DROP TABLE dbo.configuracion_ciclo;
IF OBJECT_ID('dbo.productos', 'U') IS NOT NULL DROP TABLE dbo.productos;
IF OBJECT_ID('dbo.usuarios', 'U') IS NOT NULL DROP TABLE dbo.usuarios;
GO

-- 2.3. TABLA: PRODUCTOS
CREATE TABLE dbo.productos (
    sku                 NVARCHAR(30)   NOT NULL,
    ean                 NVARCHAR(30)   NOT NULL,
    linea               NVARCHAR(50)   NOT NULL,
    marca               NVARCHAR(50)   NOT NULL,
    descripcion         NVARCHAR(200)  NOT NULL,
    talla               NVARCHAR(20)   NULL,
    color               NVARCHAR(50)   NULL,
    ubicacion           NVARCHAR(100)  NULL,
    stock_sap           INT            NOT NULL CONSTRAINT DF_prod_stock_sap DEFAULT 0,
    precio_unitario     DECIMAL(10,2)  NOT NULL CONSTRAINT DF_prod_precio DEFAULT 0.00,
    costo_unitario      DECIMAL(10,2)  NOT NULL CONSTRAINT DF_prod_costo DEFAULT 0.00,
    stock_piso_venta    INT            NULL CONSTRAINT DF_prod_piso DEFAULT 0,
    stock_almacen       INT            NULL CONSTRAINT DF_prod_almacen DEFAULT 0,
    stock_vitrina       INT            NULL CONSTRAINT DF_prod_vitrina DEFAULT 0,
    CONSTRAINT PK_productos PRIMARY KEY CLUSTERED (sku),
    CONSTRAINT UQ_productos_ean UNIQUE NONCLUSTERED (ean),
    CONSTRAINT CHK_prod_stock CHECK (stock_sap >= 0),
    CONSTRAINT CHK_prod_precio CHECK (precio_unitario >= 0.00)
);
GO

CREATE NONCLUSTERED INDEX IX_productos_linea ON dbo.productos (linea);
CREATE NONCLUSTERED INDEX IX_productos_marca ON dbo.productos (marca);
GO

-- 2.4. TABLA: USUARIOS
CREATE TABLE dbo.usuarios (
    id                  NVARCHAR(30)   NOT NULL,
    nombre              NVARCHAR(120)  NOT NULL,
    usuario             NVARCHAR(50)   NOT NULL,
    clave               NVARCHAR(100)  NOT NULL,
    rol                 NVARCHAR(50)   NOT NULL,
    sede                NVARCHAR(100)  NULL CONSTRAINT DF_user_sede DEFAULT N'Marathon Atocongo',
    codigo              NVARCHAR(30)   NULL,
    CONSTRAINT PK_usuarios PRIMARY KEY CLUSTERED (id),
    CONSTRAINT UQ_usuarios_login UNIQUE NONCLUSTERED (usuario),
    CONSTRAINT CHK_usuarios_rol CHECK (rol IN (N'Jefe de Inventario', N'Auditor Principal Calzado', N'Supervisora / Docente', N'Personal de Tienda', N'ROLE_SUPERVISOR', N'ROLE_AUDITOR', N'ROLE_OPERARIO'))
);
GO

-- 2.5. TABLA: CICLOS_CONTEO
CREATE TABLE dbo.ciclos_conteo (
    id                  NVARCHAR(50)   NOT NULL,
    linea               NVARCHAR(50)   NOT NULL,
    dia_programado      TINYINT        NOT NULL,
    nombre              NVARCHAR(100)  NOT NULL,
    descripcion         NVARCHAR(300)  NULL,
    estado              NVARCHAR(30)   NOT NULL CONSTRAINT DF_ciclo_estado DEFAULT N'Pendiente',
    frecuencia          NVARCHAR(50)   NULL,
    responsable         NVARCHAR(100)  NULL,
    fecha_limite        NVARCHAR(20)   NULL,
    CONSTRAINT PK_ciclos_conteo PRIMARY KEY CLUSTERED (id),
    CONSTRAINT CHK_ciclo_dia CHECK (dia_programado IN (9, 19, 28, 29)),
    CONSTRAINT CHK_ciclo_estado CHECK (estado IN (N'Pendiente', N'En Proceso', N'Completado', N'Aprobado'))
);
GO

-- 2.6. TABLA: CONTEOS_FISICOS
CREATE TABLE dbo.conteos_fisicos (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    sku                 NVARCHAR(30)   NOT NULL,
    conteo_fisico       INT            NOT NULL CONSTRAINT DF_conteo_fisico DEFAULT 0,
    fecha_conteo        DATETIME2(3)   NOT NULL CONSTRAINT DF_conteo_fecha DEFAULT SYSDATETIME(),
    escaneado_por       NVARCHAR(100)  NULL,
    ubicacion           NVARCHAR(100)  NULL,
    CONSTRAINT PK_conteos_fisicos PRIMARY KEY CLUSTERED (id),
    CONSTRAINT UQ_conteos_sku UNIQUE NONCLUSTERED (sku),
    CONSTRAINT FK_conteo_producto FOREIGN KEY (sku) REFERENCES dbo.productos (sku) ON DELETE CASCADE,
    CONSTRAINT CHK_conteo_positivo CHECK (conteo_fisico >= 0)
);
GO

CREATE NONCLUSTERED INDEX IX_conteos_sku ON dbo.conteos_fisicos (sku);
GO

-- 2.7. TABLA: AUDITORIAS_MERMA
CREATE TABLE dbo.auditorias_merma (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    sku                 NVARCHAR(30)   NOT NULL,
    descripcion_producto NVARCHAR(200) NULL,
    linea               NVARCHAR(50)   NULL,
    ubicacion           NVARCHAR(100)  NULL,
    stock_sap           INT            NULL CONSTRAINT DF_audit_sap DEFAULT 0,
    conteo_fisico       INT            NULL CONSTRAINT DF_audit_fisico DEFAULT 0,
    diferencia          INT            NULL CONSTRAINT DF_audit_dif DEFAULT 0,
    tipo                NVARCHAR(50)   NOT NULL,
    causal              NVARCHAR(200)  NOT NULL,
    comentario          NVARCHAR(500)  NULL,
    validado_por        NVARCHAR(100)  NULL,
    fecha_validacion    DATETIME2(3)   NOT NULL CONSTRAINT DF_audit_fecha DEFAULT SYSDATETIME(),
    estado_aprobado     BIT            NOT NULL CONSTRAINT DF_audit_aprobado DEFAULT 0,
    CONSTRAINT PK_auditorias_merma PRIMARY KEY CLUSTERED (id),
    CONSTRAINT FK_audit_producto FOREIGN KEY (sku) REFERENCES dbo.productos (sku) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_audit_sku ON dbo.auditorias_merma (sku);
CREATE NONCLUSTERED INDEX IX_audit_estado ON dbo.auditorias_merma (estado_aprobado);
GO

-- 2.8. TABLA: MOVIMIENTOS_TRAZABILIDAD
CREATE TABLE dbo.movimientos_trazabilidad (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    sku                 NVARCHAR(30)   NOT NULL,
    tipo_badge          NVARCHAR(50)   NOT NULL,
    titulo              NVARCHAR(150)  NOT NULL,
    descripcion         NVARCHAR(500)  NOT NULL,
    fecha_hora          DATETIME2(3)   NOT NULL CONSTRAINT DF_traz_fecha DEFAULT SYSDATETIME(),
    usuario             NVARCHAR(100)  NULL,
    stock_sap           INT            NULL,
    conteo_fisico       INT            NULL,
    diferencia          INT            NULL,
    CONSTRAINT PK_movimientos_trazabilidad PRIMARY KEY CLUSTERED (id),
    CONSTRAINT FK_traz_producto FOREIGN KEY (sku) REFERENCES dbo.productos (sku) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_traz_sku_fecha ON dbo.movimientos_trazabilidad (sku, fecha_hora DESC);
GO

-- 2.9. TABLA: CONFIGURACION_CICLO
CREATE TABLE dbo.configuracion_ciclo (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    frecuencia          NVARCHAR(120)  NOT NULL,
    categorias_muestreo NVARCHAR(250)  NOT NULL,
    regla_muestreo      NVARCHAR(300)  NOT NULL,
    responsable_calzado NVARCHAR(100)  NULL,
    rol_responsable_calzado NVARCHAR(100) NULL,
    responsable_jefe    NVARCHAR(100)  NULL,
    rol_responsable_jefe NVARCHAR(100) NULL,
    CONSTRAINT PK_configuracion_ciclo PRIMARY KEY CLUSTERED (id)
);
GO

-- ============================================================================
-- FIN DEL ARCHIVO DDL ORACLE & SQL SERVER
-- ============================================================================
