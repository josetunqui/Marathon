/**
 * DATA INICIAL PILOTO - MARATHON ATOCONGO
 * Sistema de Conteo Cíclico de Inventario
 * Autores: Joseph Aaron Churampi Quispe (U21212777), Jose Armando Tunqui Puelles (U23331844)
 * Docente: Mg. Anita Condo López - 2026
 */

const DEFAULT_CYCLE_SCHEDULE = [
  {
    id: 'ciclo-calzado-09',
    linea: 'Calzado',
    diaProgramado: 9,
    nombre: 'Ciclo 1: Calzado Deportivo y Urbano',
    descripcion: 'Auditoría mensual prioritaria de calzado de alta rotación (Zapatillas fútbol, running y urbanas).',
    estado: 'En Proceso', // 'Pendiente', 'En Proceso', 'Completado'
    frecuencia: 'Mensual (Día 9)',
    responsable: 'Joseph Aaron Churampi',
    fechaLimite: '2026-10-09'
  },
  {
    id: 'ciclo-ropa-19',
    linea: 'Ropa',
    diaProgramado: 19,
    nombre: 'Ciclo 2: Ropa y Textil Deportivo',
    descripcion: 'Auditoría de camisetas oficiales de clubes/selecciones, polerones, casacas y shorts.',
    estado: 'Pendiente',
    frecuencia: 'Mensual (Día 19)',
    responsable: 'Jose Armando Tunqui',
    fechaLimite: '2026-10-19'
  },
  {
    id: 'ciclo-accesorios-28',
    linea: 'Accesorios',
    diaProgramado: 28,
    nombre: 'Ciclo 3: Accesorios y Balonería',
    descripcion: 'Auditoría de balones, canilleras, guantes, medias técnicas, mochilas y gorras.',
    estado: 'Pendiente',
    frecuencia: 'Mensual (Día 28)',
    responsable: 'Joseph Aaron Churampi',
    fechaLimite: '2026-10-28'
  }
];

// Catálogo base con Stock SAP descargado y precios en Soles (S/.)
const DEFAULT_SAP_PRODUCTS = [
  // --- LÍNEA CALZADO (Día 9) ---
  {
    sku: 'CAL-NIK-001',
    ean: '7751234001015',
    linea: 'Calzado',
    marca: 'Nike',
    descripcion: 'Zapatillas Nike Mercurial Vapor 15 Club FG/MG',
    talla: '41',
    color: 'Negro / Carmesí',
    ubicacion: 'Pasillo A - Estante 02',
    stockSap: 14,
    precioUnitario: 269.90,
    costoUnitario: 160.00
  },
  {
    sku: 'CAL-NIK-002',
    ean: '7751234001022',
    linea: 'Calzado',
    marca: 'Nike',
    descripcion: 'Zapatillas Nike Air Max Excee Running',
    talla: '42',
    color: 'Blanco / Negro',
    ubicacion: 'Pasillo A - Estante 03',
    stockSap: 9,
    precioUnitario: 349.90,
    costoUnitario: 210.00
  },
  {
    sku: 'CAL-ADI-003',
    ean: '7751234001039',
    linea: 'Calzado',
    marca: 'Adidas',
    descripcion: 'Zapatillas Adidas Predator Accuracy.3 FG',
    talla: '40.5',
    color: 'Negro / Blanco / Rosa',
    ubicacion: 'Pasillo A - Estante 01',
    stockSap: 12,
    precioUnitario: 299.90,
    costoUnitario: 180.00
  },
  {
    sku: 'CAL-ADI-004',
    ean: '7751234001046',
    linea: 'Calzado',
    marca: 'Adidas',
    descripcion: 'Zapatillas Adidas Grand Court Base 2.0 Urbano',
    talla: '42',
    color: 'Blanco Clásico',
    ubicacion: 'Pasillo B - Estante 01',
    stockSap: 20,
    precioUnitario: 199.90,
    costoUnitario: 115.00
  },
  {
    sku: 'CAL-PUM-005',
    ean: '7751234001053',
    linea: 'Calzado',
    marca: 'Puma',
    descripcion: 'Zapatillas Puma Future Match TT Fútbol Césped',
    talla: '41',
    color: 'Azul Eléctrico / Naranja',
    ubicacion: 'Pasillo A - Estante 04',
    stockSap: 8,
    precioUnitario: 289.90,
    costoUnitario: 170.00
  },
  {
    sku: 'CAL-PUM-006',
    ean: '7751234001060',
    linea: 'Calzado',
    marca: 'Puma',
    descripcion: 'Zapatillas Puma Smash V2 L Casual',
    talla: '39',
    color: 'Blanco / Negro',
    ubicacion: 'Pasillo B - Estante 02',
    stockSap: 16,
    precioUnitario: 189.90,
    costoUnitario: 110.00
  },
  {
    sku: 'CAL-UND-007',
    ean: '7751234001077',
    linea: 'Calzado',
    marca: 'Under Armour',
    descripcion: 'Zapatillas UA Charged Assert 10 Running',
    talla: '43',
    color: 'Gris Grafito',
    ubicacion: 'Pasillo B - Estante 04',
    stockSap: 7,
    precioUnitario: 279.90,
    costoUnitario: 165.00
  },
  {
    sku: 'CAL-MAR-008',
    ean: '7751234001084',
    linea: 'Calzado',
    marca: 'Marathon',
    descripcion: 'Zapatillas Marathon Training Pro Flex',
    talla: '40',
    color: 'Negro / Rojo',
    ubicacion: 'Pasillo C - Estante 01',
    stockSap: 18,
    precioUnitario: 159.90,
    costoUnitario: 85.00
  },

  // --- LÍNEA ROPA (Día 19) ---
  {
    sku: 'ROP-MAR-101',
    ean: '7751234002012',
    linea: 'Ropa',
    marca: 'Marathon',
    descripcion: 'Camiseta Oficial Selección Peruana Local 2026',
    talla: 'M',
    color: 'Blanco / Rojo',
    ubicacion: 'Isla Central - Perchero 01',
    stockSap: 35,
    precioUnitario: 269.90,
    costoUnitario: 140.00
  },
  {
    sku: 'ROP-MAR-102',
    ean: '7751234002029',
    linea: 'Ropa',
    marca: 'Marathon',
    descripcion: 'Camiseta Oficial Selección Peruana Visitante',
    talla: 'L',
    color: 'Rojo / Dorado',
    ubicacion: 'Isla Central - Perchero 02',
    stockSap: 22,
    precioUnitario: 269.90,
    costoUnitario: 140.00
  },
  {
    sku: 'ROP-MAR-103',
    ean: '7751234002036',
    linea: 'Ropa',
    marca: 'Marathon',
    descripcion: 'Camiseta Universitario de Deportes Oficial 2026',
    talla: 'M',
    color: 'Crema Tradicional',
    ubicacion: 'Pared Textil - Sector Norte',
    stockSap: 28,
    precioUnitario: 249.90,
    costoUnitario: 135.00
  },
  {
    sku: 'ROP-NIK-104',
    ean: '7751234002043',
    linea: 'Ropa',
    marca: 'Nike',
    descripcion: 'Camiseta Alianza Lima Oficial Stadium 2026',
    talla: 'S',
    color: 'Azul / Blanco',
    ubicacion: 'Pared Textil - Sector Sur',
    stockSap: 19,
    precioUnitario: 279.90,
    costoUnitario: 155.00
  },
  {
    sku: 'ROP-ADI-105',
    ean: '7751234002050',
    linea: 'Ropa',
    marca: 'Adidas',
    descripcion: 'Camiseta Sporting Cristal Oficial 2026',
    talla: 'L',
    color: 'Celeste / Blanco',
    ubicacion: 'Pared Textil - Sector Este',
    stockSap: 15,
    precioUnitario: 249.90,
    costoUnitario: 135.00
  },
  {
    sku: 'ROP-NIK-106',
    ean: '7751234002067',
    linea: 'Ropa',
    marca: 'Nike',
    descripcion: 'Pantalón Buzo Nike Dri-FIT Academy',
    talla: 'M',
    color: 'Negro',
    ubicacion: 'Perchero Textil 04',
    stockSap: 24,
    precioUnitario: 179.90,
    costoUnitario: 95.00
  },
  {
    sku: 'ROP-ADI-107',
    ean: '7751234002074',
    linea: 'Ropa',
    marca: 'Adidas',
    descripcion: 'Casaca Cortaviento Adidas Tiro 24',
    talla: 'L',
    color: 'Azul Marino / Blanco',
    ubicacion: 'Perchero Textil 05',
    stockSap: 11,
    precioUnitario: 229.90,
    costoUnitario: 125.00
  },
  {
    sku: 'ROP-MAR-108',
    ean: '7751234002081',
    linea: 'Ropa',
    marca: 'Marathon',
    descripcion: 'Short Deportivo Marathon Entrenamiento Pro',
    talla: 'M',
    color: 'Negro / Rojo',
    ubicacion: 'Mesa Textil 01',
    stockSap: 30,
    precioUnitario: 79.90,
    costoUnitario: 38.00
  },

  // --- LÍNEA ACCESORIOS (Día 28) ---
  {
    sku: 'ACC-ADI-201',
    ean: '7751234003019',
    linea: 'Accesorios',
    marca: 'Adidas',
    descripcion: 'Balón de Fútbol Adidas Club N°5 Oficial',
    talla: 'N°5',
    color: 'Blanco / Negro / Neón',
    ubicacion: 'Módulo Central Accesorios',
    stockSap: 23,
    precioUnitario: 99.90,
    costoUnitario: 52.00
  },
  {
    sku: 'ACC-PUM-202',
    ean: '7751234003026',
    linea: 'Accesorios',
    marca: 'Puma',
    descripcion: 'Balón Puma Orbita LaLiga 1 Training',
    talla: 'N°5',
    color: 'Amarillo / Multicolor',
    ubicacion: 'Módulo Central Accesorios',
    stockSap: 14,
    precioUnitario: 89.90,
    costoUnitario: 46.00
  },
  {
    sku: 'ACC-NIK-203',
    ean: '7751234003033',
    linea: 'Accesorios',
    marca: 'Nike',
    descripcion: 'Mochila Nike Brasilia 9.5 Training (24L)',
    talla: 'Única',
    color: 'Negro / Logo Blanco',
    ubicacion: 'Pared Accesorios 01',
    stockSap: 12,
    precioUnitario: 149.90,
    costoUnitario: 82.00
  },
  {
    sku: 'ACC-MAR-204',
    ean: '7751234003040',
    linea: 'Accesorios',
    marca: 'Marathon',
    descripcion: 'Canilleras Marathon Pro Shield con Tobillera',
    talla: 'M',
    color: 'Rojo / Negro',
    ubicacion: 'Góndola Accesorios 02',
    stockSap: 40,
    precioUnitario: 49.90,
    costoUnitario: 22.00
  },
  {
    sku: 'ACC-ADI-205',
    ean: '7751234003057',
    linea: 'Accesorios',
    marca: 'Adidas',
    descripcion: 'Guantes de Arquero Adidas Predator Training',
    talla: '9',
    color: 'Negro / Rojo / Blanco',
    ubicacion: 'Góndola Accesorios 03',
    stockSap: 8,
    precioUnitario: 119.90,
    costoUnitario: 65.00
  },
  {
    sku: 'ACC-NIK-206',
    ean: '7751234003064',
    linea: 'Accesorios',
    marca: 'Nike',
    descripcion: 'Pack x3 Pares Medias Deportivas Nike Everyday',
    talla: 'L (39-43)',
    color: 'Blanco',
    ubicacion: 'Mostrador de Caja',
    stockSap: 45,
    precioUnitario: 59.90,
    costoUnitario: 28.00
  }
];

// Conteo físico inicial pre-cargado para la Línea Calzado (Día 9) demostrando discrepancias realistas
// (ej. zapatillas faltantes por robo o desorden, y un sobrante por error de transferencia de caja)
const DEFAULT_INITIAL_COUNTS = [
  // Faltante de 2 unidades en Nike Mercurial (Robo/Extravío común en calzado de alta gama)
  { sku: 'CAL-NIK-001', conteoFisico: 12, fechaConteo: '2026-09-23 09:30', escaneadoPor: 'Joseph Churampi', ubicacion: 'Pasillo A - Estante 02' },
  // Conforme exacto
  { sku: 'CAL-NIK-002', conteoFisico: 9, fechaConteo: '2026-09-23 09:42', escaneadoPor: 'Joseph Churampi', ubicacion: 'Pasillo A - Estante 03' },
  // Faltante de 1 unidad en Predator
  { sku: 'CAL-ADI-003', conteoFisico: 11, fechaConteo: '2026-09-23 10:05', escaneadoPor: 'Jose Tunqui', ubicacion: 'Pasillo A - Estante 01' },
  // Conforme exacto
  { sku: 'CAL-ADI-004', conteoFisico: 20, fechaConteo: '2026-09-23 10:20', escaneadoPor: 'Jose Tunqui', ubicacion: 'Pasillo B - Estante 01' },
  // Faltante de 1 unidad en Puma Future
  { sku: 'CAL-PUM-005', conteoFisico: 7, fechaConteo: '2026-09-23 10:35', escaneadoPor: 'Joseph Churampi', ubicacion: 'Pasillo A - Estante 04' },
  // Sobrante de +1 unidad (error en transferencia de caja desde tienda Marathon Jockey Plaza sin recepcionar en SAP)
  { sku: 'CAL-PUM-006', conteoFisico: 17, fechaConteo: '2026-09-23 10:48', escaneadoPor: 'Joseph Churampi', ubicacion: 'Pasillo B - Estante 02' },
  // Conforme
  { sku: 'CAL-UND-007', conteoFisico: 7, fechaConteo: '2026-09-23 11:00', escaneadoPor: 'Jose Tunqui', ubicacion: 'Pasillo B - Estante 04' },
  // Conforme
  { sku: 'CAL-MAR-008', conteoFisico: 18, fechaConteo: '2026-09-23 11:15', escaneadoPor: 'Jose Tunqui', ubicacion: 'Pasillo C - Estante 01' }
];

// Usuarios para autenticación y auditoría
const APP_USERS = [
  {
    id: 'user-01',
    nombre: 'Joseph Aaron Churampi Quispe',
    usuario: 'jchurampi',
    clave: 'marathon2026',
    rol: 'Jefe de Tienda',
    sede: 'Marathon Atocongo',
    codigo: 'U21212777'
  },
  {
    id: 'user-02',
    nombre: 'Jose Armando Tunqui Puelles',
    usuario: 'jtunqui',
    clave: 'marathon2026',
    rol: 'Supervisor de Inventario',
    sede: 'Marathon Atocongo',
    codigo: 'U23331844'
  },
  {
    id: 'user-03',
    nombre: 'Asesor Comercial / Vendedor',
    usuario: 'asesor',
    clave: '123456',
    rol: 'Personal de Tienda',
    sede: 'Marathon Atocongo',
    codigo: 'OPER-01'
  }
];

// Causales de discrepancia estandarizadas
const DISCREPANCY_REASONS = [
  'Sospecha de hurto / sustracción en piso de venta',
  'Error de transferencia física no registrada en SAP',
  'Producto traspapelado en almacén / sin caja rotulada',
  'Doble etiquetado o código de barras erróneo',
  'Devolución de cliente pendiente de ingreso al sistema',
  'Rotura o merma física no dada de baja formalmente'
];
