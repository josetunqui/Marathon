/**
 * MOTOR DE CRUCE Y RECONCILIACIÓN DE INVENTARIO
 * Compara Stock SAP vs Conteo Físico, clasifica estados y calcula el IRA
 */

class ReconciliationEngine {
  constructor() {
    this.storage = storageManager;
  }

  // Genera el cruce completo de datos
  getReconciliationData(filterLine = 'ALL') {
    const sapProducts = this.storage.getSapStock();
    const physicalCounts = this.storage.getPhysicalCounts();
    const auditLogs = this.storage.getAuditLogs();

    // Mapeo rápido de conteos por SKU
    const physicalMap = new Map();
    physicalCounts.forEach(item => {
      physicalMap.set(item.sku, item);
    });

    // Mapeo rápido de auditorías por SKU
    const auditMap = new Map();
    auditLogs.forEach(log => {
      auditMap.set(log.sku, log);
    });

    let items = sapProducts.map(sap => {
      const physical = physicalMap.get(sap.sku);
      const conteoFisico = physical ? physical.conteoFisico : 0;
      const tieneConteo = physical !== undefined;
      const diferencia = conteoFisico - sap.stockSap;

      let estado = 'CONFORME';
      let badgeClass = 'badge-conforme';
      let icon = 'bi-check-circle-fill';

      if (!tieneConteo && sap.stockSap > 0) {
        estado = 'NO CONTADO';
        badgeClass = 'badge-no-contado';
        icon = 'bi-clock-history';
      } else if (diferencia > 0) {
        estado = 'SOBRANTE';
        badgeClass = 'badge-sobrante';
        icon = 'bi-plus-circle-fill';
      } else if (diferencia < 0) {
        estado = 'FALTANTE';
        badgeClass = 'badge-faltante';
        icon = 'bi-dash-circle-fill';
      }

      const valorDiferencia = diferencia * sap.precioUnitario;
      const costoDiferencia = diferencia * sap.costoUnitario;
      const validacion = auditMap.get(sap.sku);

      return {
        ...sap,
        conteoFisico,
        tieneConteo,
        diferencia,
        estado,
        badgeClass,
        icon,
        valorDiferencia,
        costoDiferencia,
        esDiscrepancia: diferencia !== 0,
        estaValidado: !!validacion,
        auditoria: validacion || null,
        fechaUltimoConteo: physical ? physical.fechaConteo : 'Pendiente',
        escaneadoPor: physical ? physical.escaneadoPor : 'Sin registro'
      };
    });

    // También incluir productos que fueron escaneados físicamente pero no existen en el catálogo SAP (sobrante desconocido)
    physicalCounts.forEach(phys => {
      const existsInSap = sapProducts.some(p => p.sku === phys.sku);
      if (!existsInSap) {
        const validacion = auditMap.get(phys.sku);
        items.push({
          sku: phys.sku,
          ean: 'Desconocido',
          linea: 'Sin Clasificar',
          marca: 'No catalogado',
          descripcion: 'Producto no registrado en SAP (Sobrante Extremo)',
          talla: 'S/T',
          color: '-',
          ubicacion: phys.ubicacion || 'Almacén',
          stockSap: 0,
          conteoFisico: phys.conteoFisico,
          tieneConteo: true,
          diferencia: phys.conteoFisico,
          estado: 'SOBRANTE',
          badgeClass: 'badge-sobrante',
          icon: 'bi-exclamation-triangle-fill',
          precioUnitario: 0,
          costoUnitario: 0,
          valorDiferencia: 0,
          costoDiferencia: 0,
          esDiscrepancia: true,
          estaValidado: !!validacion,
          auditoria: validacion || null,
          fechaUltimoConteo: phys.fechaConteo,
          escaneadoPor: phys.escaneadoPor
        });
      }
    });

    if (filterLine !== 'ALL') {
      items = items.filter(item => item.linea === filterLine);
    }

    return items;
  }

  // Métricas y KPIs consolidados
  getDashboardMetrics(linea = 'ALL') {
    const items = this.getReconciliationData(linea);

    const totalSkus = items.length;
    const skusContados = items.filter(i => i.tieneConteo).length;
    const skusConformes = items.filter(i => i.estado === 'CONFORME').length;
    const skusFaltantes = items.filter(i => i.estado === 'FALTANTE').length;
    const skusSobrantes = items.filter(i => i.estado === 'SOBRANTE').length;
    const skusNoContados = items.filter(i => i.estado === 'NO CONTADO').length;

    const totalUnidadesSap = items.reduce((acc, i) => acc + (i.stockSap || 0), 0);
    const totalUnidadesFisicas = items.reduce((acc, i) => acc + (i.conteoFisico || 0), 0);

    // Unidades de discrepancia
    let unidadesFaltantes = 0;
    let unidadesSobrantes = 0;
    let mermaValorVenta = 0; // Pérdida en Soles por faltantes
    let sobranteValorVenta = 0; // Valor en Soles de sobrantes

    items.forEach(item => {
      if (item.diferencia < 0) {
        unidadesFaltantes += Math.abs(item.diferencia);
        mermaValorVenta += Math.abs(item.valorDiferencia);
      } else if (item.diferencia > 0) {
        unidadesSobrantes += item.diferencia;
        sobranteValorVenta += item.valorDiferencia;
      }
    });

    // IRA (Inventory Record Accuracy) según estándar WMS/Retail
    // Se calcula sobre los SKUs que ya fueron auditados
    const iraPercentage = skusContados > 0 
      ? Math.round((skusConformes / skusContados) * 1000) / 10 
      : 100;

    // Discrepancias validadas por el Jefe de Tienda
    const discrepanciasTotales = skusFaltantes + skusSobrantes;
    const discrepanciasValidadas = items.filter(i => i.esDiscrepancia && i.estaValidado).length;
    const discrepanciasPendientes = discrepanciasTotales - discrepanciasValidadas;

    // Métricas específicas por línea
    const metricasLineas = {
      Calzado: this.getLineStats('Calzado'),
      Ropa: this.getLineStats('Ropa'),
      Accesorios: this.getLineStats('Accesorios')
    };

    return {
      totalSkus,
      skusContados,
      skusConformes,
      skusFaltantes,
      skusSobrantes,
      skusNoContados,
      totalUnidadesSap,
      totalUnidadesFisicas,
      unidadesFaltantes,
      unidadesSobrantes,
      mermaValorVenta,
      sobranteValorVenta,
      iraPercentage,
      discrepanciasTotales,
      discrepanciasValidadas,
      discrepanciasPendientes,
      metricasLineas
    };
  }

  getLineStats(linea) {
    const items = this.getReconciliationData(linea);
    const contados = items.filter(i => i.tieneConteo).length;
    const conformes = items.filter(i => i.estado === 'CONFORME').length;
    const faltantes = items.filter(i => i.estado === 'FALTANTE').length;
    const sobrantes = items.filter(i => i.estado === 'SOBRANTE').length;
    const unidadesFaltantes = items.reduce((acc, i) => acc + (i.diferencia < 0 ? Math.abs(i.diferencia) : 0), 0);
    const valorMerma = items.reduce((acc, i) => acc + (i.diferencia < 0 ? Math.abs(i.valorDiferencia) : 0), 0);

    const ira = contados > 0 ? Math.round((conformes / contados) * 1000) / 10 : 0;

    return {
      linea,
      totalSkus: items.length,
      contados,
      conformes,
      faltantes,
      sobrantes,
      unidadesFaltantes,
      valorMerma,
      ira
    };
  }
}

const reconciliationEngine = new ReconciliationEngine();
