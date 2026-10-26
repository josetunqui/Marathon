/**
 * GESTOR DE ALMACENAMIENTO Y ESTADO LOCAL (localStorage)
 * Asegura persistencia de datos durante las pruebas del piloto
 */

const STORAGE_KEYS = {
  SAP_STOCK: 'marathon_sap_stock_v1',
  PHYSICAL_COUNTS: 'marathon_physical_counts_v1',
  CYCLES: 'marathon_cycles_v1',
  AUDIT_LOGS: 'marathon_audit_logs_v1',
  CURRENT_USER: 'marathon_current_user_v1',
  LAST_SAP_IMPORT: 'marathon_last_sap_import_v1',
  SCAN_HISTORY: 'marathon_scan_history_v1'
};

class StorageManager {
  constructor() {
    this.init();
  }

  init() {
    // Si no existen datos iniciales en localStorage, cargar el dataset por defecto
    if (!localStorage.getItem(STORAGE_KEYS.SAP_STOCK)) {
      this.saveSapStock(DEFAULT_SAP_PRODUCTS);
      this.setLastSapImport({
        fecha: new Date().toISOString().replace('T', ' ').substring(0, 19),
        archivo: 'SAP_MB52_ATOCONGO_DAILY.xlsx',
        usuario: 'Joseph Aaron Churampi',
        registros: DEFAULT_SAP_PRODUCTS.length
      });
    }

    if (!localStorage.getItem(STORAGE_KEYS.PHYSICAL_COUNTS)) {
      this.savePhysicalCounts(DEFAULT_INITIAL_COUNTS);
    }

    if (!localStorage.getItem(STORAGE_KEYS.CYCLES)) {
      this.saveCycles(DEFAULT_CYCLE_SCHEDULE);
    }

    if (!localStorage.getItem(STORAGE_KEYS.AUDIT_LOGS)) {
      this.saveAuditLogs([
        {
          id: 'AUD-001',
          sku: 'CAL-NIK-001',
          tipo: 'Faltante (-2)',
          causal: 'Sospecha de hurto / sustracción en piso de venta',
          comentario: 'Revisión en cámaras del pasillo A pendiente. Se cuadra stock temporalmente.',
          validadoPor: 'Joseph Aaron Churampi Quispe',
          fechaValidacion: '2026-09-23 11:20:15',
          estadoAprobado: true
        }
      ]);
    }

    if (!localStorage.getItem(STORAGE_KEYS.CURRENT_USER)) {
      this.setCurrentUser(APP_USERS[0]); // Joseph Aaron Churampi (Jefe de Tienda)
    }

    if (!localStorage.getItem(STORAGE_KEYS.SCAN_HISTORY)) {
      this.saveScanHistory([]);
    }
  }

  // --- MÉTODOS PARA STOCK SAP ---
  getSapStock() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SAP_STOCK);
      return data ? JSON.parse(data) : DEFAULT_SAP_PRODUCTS;
    } catch (e) {
      return DEFAULT_SAP_PRODUCTS;
    }
  }

  saveSapStock(products) {
    localStorage.setItem(STORAGE_KEYS.SAP_STOCK, JSON.stringify(products));
  }

  getLastSapImport() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.LAST_SAP_IMPORT);
      return data ? JSON.parse(data) : { fecha: '2026-09-23 08:00:00', archivo: 'SAP_STOCK_ATOCONGO.xlsx', usuario: 'Sistema Automático', registros: 20 };
    } catch (e) {
      return null;
    }
  }

  setLastSapImport(info) {
    localStorage.setItem(STORAGE_KEYS.LAST_SAP_IMPORT, JSON.stringify(info));
  }

  // --- MÉTODOS PARA CONTEO FÍSICO ---
  getPhysicalCounts() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PHYSICAL_COUNTS);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      return [];
    }
  }

  savePhysicalCounts(counts) {
    localStorage.setItem(STORAGE_KEYS.PHYSICAL_COUNTS, JSON.stringify(counts));
  }

  // Incrementa o registra un escaneo físico para un SKU
  incrementOrAddCount(sku, cantidad = 1, metadata = {}) {
    const counts = this.getPhysicalCounts();
    const existingIndex = counts.findIndex(c => c.sku === sku);

    const nowStr = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const currentUser = this.getCurrentUser();

    if (existingIndex >= 0) {
      counts[existingIndex].conteoFisico = (counts[existingIndex].conteoFisico || 0) + cantidad;
      counts[existingIndex].fechaConteo = nowStr;
      counts[existingIndex].escaneadoPor = currentUser ? currentUser.nombre : 'Operador';
      if (metadata.ubicacion) counts[existingIndex].ubicacion = metadata.ubicacion;
    } else {
      counts.push({
        sku: sku,
        conteoFisico: cantidad,
        fechaConteo: nowStr,
        escaneadoPor: currentUser ? currentUser.nombre : 'Operador',
        ubicacion: metadata.ubicacion || 'Piso de Venta / Pasillo'
      });
    }

    this.savePhysicalCounts(counts);

    // Guardar en el log de escaneos recientes
    this.addScanHistoryItem({
      sku: sku,
      cantidad: cantidad,
      timestamp: nowStr,
      ubicacion: metadata.ubicacion || 'General',
      usuario: currentUser ? currentUser.nombre : 'Operador'
    });

    return counts.find(c => c.sku === sku);
  }

  // Actualiza la cantidad exacta en el conteo físico
  updateCountExact(sku, exactQuantity) {
    const counts = this.getPhysicalCounts();
    const existingIndex = counts.findIndex(c => c.sku === sku);
    const nowStr = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const currentUser = this.getCurrentUser();

    if (existingIndex >= 0) {
      counts[existingIndex].conteoFisico = Math.max(0, exactQuantity);
      counts[existingIndex].fechaConteo = nowStr;
    } else {
      counts.push({
        sku: sku,
        conteoFisico: Math.max(0, exactQuantity),
        fechaConteo: nowStr,
        escaneadoPor: currentUser ? currentUser.nombre : 'Operador',
        ubicacion: 'Ajuste Manual'
      });
    }
    this.savePhysicalCounts(counts);
  }

  // Historial de escaneos recientes (para la consola del lector)
  getScanHistory() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SCAN_HISTORY);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      return [];
    }
  }

  saveScanHistory(history) {
    localStorage.setItem(STORAGE_KEYS.SCAN_HISTORY, JSON.stringify(history));
  }

  addScanHistoryItem(item) {
    const history = this.getScanHistory();
    history.unshift(item);
    if (history.length > 50) history.pop();
    this.saveScanHistory(history);
  }

  // --- MÉTODOS PARA CICLOS ---
  getCycles() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.CYCLES);
      return data ? JSON.parse(data) : DEFAULT_CYCLE_SCHEDULE;
    } catch (e) {
      return DEFAULT_CYCLE_SCHEDULE;
    }
  }

  saveCycles(cycles) {
    localStorage.setItem(STORAGE_KEYS.CYCLES, JSON.stringify(cycles));
  }

  updateCycleStatus(cycleId, nuevoEstado) {
    const cycles = this.getCycles();
    const cycle = cycles.find(c => c.id === cycleId);
    if (cycle) {
      cycle.estado = nuevoEstado;
      this.saveCycles(cycles);
    }
  }

  // --- MÉTODOS PARA AUDITORÍA Y VALIDACIONES ---
  getAuditLogs() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.AUDIT_LOGS);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      return [];
    }
  }

  saveAuditLogs(logs) {
    localStorage.setItem(STORAGE_KEYS.AUDIT_LOGS, JSON.stringify(logs));
  }

  addAuditLog(entry) {
    const logs = this.getAuditLogs();
    logs.unshift(entry);
    this.saveAuditLogs(logs);
  }

  // --- MÉTODOS DE USUARIO Y SESIÓN ---
  getCurrentUser() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.CURRENT_USER);
      return data ? JSON.parse(data) : APP_USERS[0];
    } catch (e) {
      return APP_USERS[0];
    }
  }

  setCurrentUser(user) {
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(user));
  }

  // Reiniciar a la configuración del piloto Marathon Atocongo
  resetToFactoryDefaults() {
    localStorage.removeItem(STORAGE_KEYS.SAP_STOCK);
    localStorage.removeItem(STORAGE_KEYS.PHYSICAL_COUNTS);
    localStorage.removeItem(STORAGE_KEYS.CYCLES);
    localStorage.removeItem(STORAGE_KEYS.AUDIT_LOGS);
    localStorage.removeItem(STORAGE_KEYS.LAST_SAP_IMPORT);
    localStorage.removeItem(STORAGE_KEYS.SCAN_HISTORY);
    this.init();
  }
}

const storageManager = new StorageManager();
