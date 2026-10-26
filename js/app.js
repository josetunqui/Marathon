/**
 * APP.JS - Controlador Principal de la Interfaz Marathon Sports
 * Implementación de los 5 Prototipos Validados por el Cliente
 * Integración estricta con Spring Security 6, Apache POI y OpenPDF
 */

let usuarioActual = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Inicialización de componentes y vistas
  initNavigation();
  initAudio();
  initScannerView();
  initPreVentaView();
  initAuditoriaView();
  initTrazabilidadView();
  initConfigurationView();

  // Verificar estado de autenticación obligatorio
  verificarSesionInicial();

  // Precargar datos del sistema
  await loadAuditoriaData();
  await loadScannerData();
  await loadPreVentaData("Nike Air Max Excee Running");
  await loadTrazabilidadData("CAL-NIK-001");
  await loadConfigurationData();
});

// --- AUDIO SCANNER BEEPER (Web Audio API) ---
let audioCtx = null;
function initAudio() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (AudioContext) {
      audioCtx = new AudioContext();
    }
  } catch (e) {
    console.warn("AudioContext no disponible");
  }
}

function playScanBeep(isSuccess = true) {
  if (!audioCtx) return;
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  const osc = audioCtx.createOscillator();
  const gain = audioCtx.createGain();
  osc.connect(gain);
  gain.connect(audioCtx.destination);

  if (isSuccess) {
    osc.frequency.setValueAtTime(1760, audioCtx.currentTime); // A6 beep
    gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.12);
    osc.start(audioCtx.currentTime);
    osc.stop(audioCtx.currentTime + 0.12);
  } else {
    osc.frequency.setValueAtTime(320, audioCtx.currentTime);
    gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.25);
    osc.start(audioCtx.currentTime);
    osc.stop(audioCtx.currentTime + 0.25);
  }
}

// Flash visual en pantalla
function triggerScanFlash() {
  const flash = document.getElementById('scan-flash-overlay');
  if (flash) {
    flash.classList.add('active');
    setTimeout(() => flash.classList.remove('active'), 150);
  }
}

// Notificación Toast
function showToast(message, icon = "bi-check-circle-fill") {
  let toast = document.getElementById('app-toast-notice');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'app-toast-notice';
    toast.className = 'toast-notice';
    document.body.appendChild(toast);
  }
  toast.innerHTML = `<i class="bi ${icon} text-primary"></i> <span>${message}</span>`;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3500);
}

// --- NAVEGACIÓN ENTRE LAS 5 VISTAS ---
function initNavigation() {
  const navButtons = document.querySelectorAll('.nav-tab-btn');
  const viewSections = document.querySelectorAll('.tab-content-view');

  navButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const targetId = btn.getAttribute('data-target');

      // Actualizar botones
      navButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      // Mostrar vista destino
      viewSections.forEach(sec => {
        if (sec.id === targetId) {
          sec.classList.add('active');
        } else {
          sec.classList.remove('active');
        }
      });

      // Recarga de datos específica por tab
      if (targetId === 'view-auditoria') loadAuditoriaData();
      if (targetId === 'view-trazabilidad') loadTrazabilidadData();
      if (targetId === 'view-escaner') loadScannerData();
      if (targetId === 'view-preventa') loadPreVentaData();
      if (targetId === 'view-configuracion') loadConfigurationData();
    });
  });
}

// ==========================================================================
// 1. CONTEO ESCÁNER
// ==========================================================================
function initScannerView() {
  const input = document.getElementById('scanner-barcode-input');
  if (!input) return;

  input.addEventListener('keypress', async (e) => {
    if (e.key === 'Enter') {
      const code = input.value.trim();
      if (code) {
        await executeScan(code, 1);
        input.value = '';
      }
    }
  });

  // Botones de incremento rápido (+1, +5, +10)
  document.querySelectorAll('.btn-quick-inc').forEach(btn => {
    btn.addEventListener('click', async () => {
      const inc = parseInt(btn.getAttribute('data-inc') || "1", 10);
      const code = input.value.trim() || "790123456712";
      await executeScan(code, inc);
    });
  });

  // Ajuste manual
  const btnManual = document.getElementById('btn-scanner-ajuste-manual');
  if (btnManual) {
    btnManual.addEventListener('click', async () => {
      const code = input.value.trim() || "790123456712";
      const cantStr = prompt("Ingrese la cantidad exacta para este SKU:", "15");
      if (cantStr !== null) {
        const cant = parseInt(cantStr, 10);
        if (!isNaN(cant) && cant >= 0) {
          await executeScan(code, cant);
          showToast(`Ajuste de conteo aplicado a ${cant} unidades.`);
        }
      }
    });
  }

  // Botón directo Registrar/Leer
  const btnSubmit = document.getElementById('btn-scanner-submit');
  if (btnSubmit) {
    btnSubmit.addEventListener('click', async () => {
      await ejecutarEscaneoDirecto();
    });
  }
}

async function ejecutarEscaneoDirecto() {
  const input = document.getElementById('scanner-barcode-input');
  const code = (input ? input.value : '').trim() || "790123456712";
  await executeScan(code, 1);
}

async function executeScan(codigo, cantidad) {
  playScanBeep(true);
  triggerScanFlash();

  if (navigator.vibrate) {
    try { navigator.vibrate(50); } catch (e) {}
  }

  const res = await window.apiClient.scanCode(codigo, cantidad);

  // Actualizar tarjeta "ÚLTIMO PRODUCTO LEÍDO"
  document.getElementById('last-read-name').textContent = res.descripcion || "Zapatillas Nike Air Max Excee Running";
  document.getElementById('last-read-meta').textContent = `SKU: ${res.ean || res.sku} • Marca: ${res.marca || 'Nike'} • Talla: ${res.talla || '42'}`;
  document.getElementById('last-read-location').textContent = `Ubicación física: ${res.ubicacion || 'Pasillo A - Estante 03'}`;
  document.getElementById('last-read-qty-reg').textContent = `${String(res.cantidadRegistrada || cantidad).padStart(2, '0')} Unidad${(res.cantidadRegistrada > 1 ? 'es' : '')}`;
  document.getElementById('last-read-qty-total').textContent = `${res.conteoAcumulado || 11} Unidades`;
  document.getElementById('last-read-time').textContent = res.horaRegistro || new Date().toTimeString().split(' ')[0];

  showToast(`Escaneo exitoso: +${cantidad} u. ${res.descripcion || codigo}`);

  // Actualizar lista de capturas recientes y progreso
  await loadScannerData();
}

async function loadScannerData() {
  const progreso = await window.apiClient.getZoneProgress("Pasillo A");
  if (progreso) {
    document.getElementById('zone-pct-label').textContent = `${progreso.porcentajeCompletado}% Completado`;
    document.getElementById('zone-progress-bar').style.width = `${progreso.porcentajeCompletado}%`;
    document.getElementById('zone-skus-counted').textContent = `${progreso.skusContados} SKUs Contados`;
    document.getElementById('zone-skus-pending').textContent = `${progreso.skusPendientes} SKUs Pendientes`;
  }

  const capturas = await window.apiClient.getRecentCaptures(5);
  const container = document.getElementById('recent-captures-list');
  if (container && capturas) {
    container.innerHTML = capturas.map(c => `
      <div class="recent-capture-item">
        <div class="recent-capture-left">
          <span class="dot-green"></span>
          <span class="recent-capture-name">${c.descripcion}</span>
        </div>
        <div class="recent-capture-right">
          <span class="recent-capture-count">${c.cantidadAcumulada} u.</span>
          <span>${c.hora}</span>
        </div>
      </div>
    `).join('');
  }
}

// ==========================================================================
// ==========================================================================
// 2. CONSULTA PRE-VENTA
// ==========================================================================
let busquedaTimeout = null;

function initPreVentaView() {
  const searchInput = document.getElementById('preventa-search-input');
  const searchBtn = document.getElementById('btn-preventa-search');

  const doSearch = async () => {
    const q = searchInput.value.trim() || "Nike Air Max Excee Running";
    cerrarSugerenciasPreVenta();
    await loadPreVentaData(q);
  };

  if (searchBtn) searchBtn.addEventListener('click', doSearch);
  if (searchInput) {
    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') doSearch();
    });
  }

  // Cerrar sugerencias al hacer click fuera
  document.addEventListener('click', (e) => {
    const suggestions = document.getElementById('pv-search-suggestions');
    const input = document.getElementById('preventa-search-input');
    if (suggestions && !suggestions.contains(e.target) && e.target !== input) {
      cerrarSugerenciasPreVenta();
    }
  });

  // Chips de sugerencia rápida
  document.querySelectorAll('.chip-quick-search').forEach(chip => {
    chip.addEventListener('click', async () => {
      const q = chip.getAttribute('data-q');
      if (searchInput) searchInput.value = q;
      cerrarSugerenciasPreVenta();
      await loadPreVentaData(q);
    });
  });

  // Botón "Reservar para Pre-Venta"
  const btnReservar = document.getElementById('btn-reservar-preventa');
  if (btnReservar) {
    btnReservar.addEventListener('click', async () => {
      const sku = btnReservar.getAttribute('data-sku') || "CAL-NIK-002";
      btnReservar.disabled = true;
      const res = await window.apiClient.reservePreVenta(sku, 1);
      btnReservar.disabled = false;
      showToast(res.mensaje || "¡Unidad reservada con éxito en mostrador!");
      await loadPreVentaData(sku);
    });
  }

  // Botón "Solicitar Auditoría Rápida"
  const btnAuditoriaRapida = document.getElementById('btn-solicitar-auditoria-rapida');
  if (btnAuditoriaRapida) {
    btnAuditoriaRapida.addEventListener('click', async () => {
      const sku = btnReservar ? btnReservar.getAttribute('data-sku') || "CAL-NIK-002" : "CAL-NIK-002";
      await window.apiClient.requestFastAudit(sku, "Solicitado por asesor en mostrador");
      showToast("Solicitud de auditoría enviada al Jefe de Inventario", "bi-shield-check");
    });
  }
}

async function buscarPreVentaEnVivo(query) {
  clearTimeout(busquedaTimeout);
  const q = (query || "").trim();
  const dropdown = document.getElementById('pv-search-suggestions');
  if (!dropdown) return;

  if (q.length < 1) {
    dropdown.classList.remove('show');
    dropdown.innerHTML = '';
    return;
  }

  busquedaTimeout = setTimeout(async () => {
    const resultados = await window.apiClient.searchProducts(q);
    if (!resultados || resultados.length === 0) {
      dropdown.innerHTML = `
        <div style="padding: 12px; text-align: center; font-size: 0.8rem; color: #94A3B8;">
          <i class="bi bi-search"></i> No se encontraron productos con "${q}".
        </div>
      `;
      dropdown.classList.add('show');
      return;
    }

    const items = resultados.slice(0, 8);
    dropdown.innerHTML = `
      <div class="suggestion-header-label">
        <i class="bi bi-lightning-fill" style="color: #F59E0B;"></i> Coincidencias encontradas (${resultados.length})
      </div>
      ${items.map(p => `
        <div class="suggestion-item" onclick="seleccionarProductoPreVenta('${p.sku}')">
          <div class="suggestion-main">
            <span class="suggestion-sku-pill">${p.sku}</span>
            <div class="suggestion-info">
              <span class="suggestion-title">${p.descripcion}</span>
              <span class="suggestion-meta">Marca: ${p.marca || 'Marathon'} • Talla: ${p.talla || 'Estándar'} • ${p.ubicacion || 'Piso de Venta'}</span>
            </div>
          </div>
          <div class="suggestion-right">
            <span class="suggestion-price">S/ ${Number(p.precioUnitario || 199.90).toFixed(2)}</span>
            <span class="suggestion-stock">${p.stockSap || 10} u. en stock</span>
          </div>
        </div>
      `).join('')}
    `;
    dropdown.classList.add('show');
  }, 160);
}

function cerrarSugerenciasPreVenta() {
  const dropdown = document.getElementById('pv-search-suggestions');
  if (dropdown) dropdown.classList.remove('show');
}

function limpiarBusquedaPreVenta() {
  const input = document.getElementById('preventa-search-input');
  if (input) {
    input.value = '';
    input.focus();
  }
  cerrarSugerenciasPreVenta();
}

async function seleccionarProductoPreVenta(sku) {
  cerrarSugerenciasPreVenta();
  const input = document.getElementById('preventa-search-input');
  if (input) input.value = sku;
  await loadPreVentaData(sku);
  showToast(`Cargando disponibilidad para ${sku}`, 'bi-search');
}

async function loadPreVentaData(query = "Nike Air Max Excee Running") {
  const data = await window.apiClient.consultPreVenta(query);
  if (!data) return;

  document.getElementById('pv-prod-name').textContent = data.descripcion;
  document.getElementById('pv-prod-meta').textContent = `EAN: ${data.ean} • Marca: ${data.marca} • Color: ${data.color}`;
  document.getElementById('pv-prod-price').textContent = `Precio: S/ ${Number(data.precioUnitario).toFixed(2)}`;

  // Distribución de ubicaciones físicas
  const locList = document.getElementById('pv-locations-list');
  if (locList && data.distribucionUbicaciones) {
    locList.innerHTML = data.distribucionUbicaciones.map(loc => {
      let dotColor = "dot-green";
      if (loc.tipo === 'reserva') dotColor = "dot-blue";
      if (loc.tipo === 'vitrina') dotColor = "dot-yellow";

      return `
        <div class="location-item-box">
          <div class="location-meta-left">
            <span class="${dotColor}"></span>
            <div>
              <div class="location-name">${loc.nombreUbicacion}</div>
              <div class="location-state-sub">${loc.subtitulo}</div>
            </div>
          </div>
          <div class="location-qty">${loc.cantidad} unidades</div>
        </div>
      `;
    }).join('');
  }

  // Resumen de disponibilidad
  document.getElementById('pv-stock-total-huge').textContent = `${data.stockTotalFisico} Unidades`;
  document.getElementById('pv-stock-note').textContent = data.notaSap;

  const btnReservar = document.getElementById('btn-reservar-preventa');
  if (btnReservar) btnReservar.setAttribute('data-sku', data.sku);
}

// ==========================================================================
// 3. VALIDACIÓN & AUDITORÍA
// ==========================================================================
let listaDiscrepanciasActuales = [];

function initAuditoriaView() {
  // Inicialización de componentes de auditoría
}

async function loadAuditoriaData() {
  // Carga de KPIs ejecutivos reales
  try {
    const kpis = await window.apiClient.getKpis();
    if (kpis) {
      const iraEl = document.getElementById('kpi-ira-val');
      const sapEl = document.getElementById('kpi-stock-sap');
      const fisEl = document.getElementById('kpi-stock-fisico');
      const mermaEl = document.getElementById('kpi-impacto-merma');

      if (iraEl) {
        iraEl.textContent = `${Number(kpis.ira || 96.2).toFixed(1)}%`;
        iraEl.style.color = (kpis.ira >= 95) ? '#16A34A' : ((kpis.ira >= 85) ? '#F59E0B' : '#DC2626');
      }
      if (sapEl) sapEl.innerHTML = `${kpis.totalSap || 382} <span style="font-size: 0.9rem; font-weight: 500;">u.</span>`;
      if (fisEl) fisEl.innerHTML = `${kpis.totalFisico || 377} <span style="font-size: 0.9rem; font-weight: 500;">u.</span>`;
      if (mermaEl) {
        const costoDiff = kpis.costoNetoDiferencia !== undefined ? kpis.costoNetoDiferencia : -560.00;
        mermaEl.textContent = `S/ ${Number(costoDiff).toFixed(2)}`;
        mermaEl.style.color = costoDiff < 0 ? 'var(--color-danger)' : '#16A34A';
      }
    }
  } catch (e) {
    console.warn('[KPIs] Falló carga de KPIs:', e);
  }

  // Resumen superior de contadores
  const resumen = await window.apiClient.getAuditSummary();
  if (resumen) {
    const faltEl = document.getElementById('badge-faltantes-criticos');
    const sobrEl = document.getElementById('badge-sobrantes-analisis');
    if (faltEl) faltEl.textContent = `${resumen.faltantesCriticos} Faltantes Críticos`;
    if (sobrEl) sobrEl.textContent = `${resumen.sobrantesBajoAnalisis} Sobrante Bajo Análisis`;
  }

  const discrepancias = await window.apiClient.getPendingAudits();
  listaDiscrepanciasActuales = discrepancias || [];
  const tbody = document.getElementById('audit-table-body');
  if (!tbody || !discrepancias) return;

  tbody.innerHTML = discrepancias.map(item => {
    let pillCatClass = "pill-cat-calzado";
    if (item.linea === 'ROPA') pillCatClass = "pill-cat-ropa";
    else if (item.diferencia === -1) pillCatClass = "pill-cat-calzado-amber";

    let diffColorClass = item.diferencia < -1 ? "diff-danger" : (item.diferencia === -1 ? "diff-warning" : "diff-info");
    const isApproved = Boolean(item.estadoAprobado);

    return `
      <tr>
        <td>
          <span class="pill-categoria ${pillCatClass}">${item.linea}</span>
        </td>
        <td>
          <div class="product-row-title">${item.descripcion}</div>
          <div class="product-row-sub">${item.subtituloUbicacion}</div>
        </td>
        <td>
          <span class="sap-fis-diff ${diffColorClass}">${item.sapVsFisTexto}</span>
        </td>
        <td>
          <span class="cause-cell">${item.causaInvestigada}</span>
        </td>
        <td>
          <span class="evidence-cell">${item.evidenciaNota}</span>
        </td>
        <td>
          <div style="display: flex; gap: 4px; align-items: center; justify-content: center; flex-wrap: wrap;">
            <button type="button" class="btn-subtle-gray" onclick="abrirDetalleAuditoria(${item.id})" style="padding: 4px 8px; font-size: 0.72rem;" title="Ver expediente completo">
              <i class="bi bi-file-text"></i> Detalle
            </button>
            ${isApproved ? `
              <span class="pill-badge pill-green" style="font-size: 0.72rem;"><i class="bi bi-check2"></i> Aprobado</span>
            ` : `
              <button class="btn-primary-blue btn-aprobar-ajuste" data-id="${item.id}" data-sku="${item.sku}" data-causa="${item.causaInvestigada}" data-evidencia="${item.evidenciaNota}" style="padding: 4px 10px; font-size: 0.74rem; ${usuarioActual && usuarioActual.rol === 'OPERARIO' ? 'opacity: 0.7; background: #64748B;' : ''}">
                ${usuarioActual && usuarioActual.rol === 'OPERARIO' ? '<i class="bi bi-shield-lock"></i>' : '<i class="bi bi-check-lg"></i>'} Aprobar
              </button>
            `}
          </div>
        </td>
      </tr>
    `;
  }).join('');

  // Vincular eventos a los botones "Aprobar Ajuste" con validación de roles Spring Security
  tbody.querySelectorAll('.btn-aprobar-ajuste').forEach(btn => {
    btn.addEventListener('click', async () => {
      if (!usuarioActual) {
        showToast("Debe iniciar sesión para realizar acciones.", "bi-shield-exclamation");
        mostrarPortalLogin();
        return;
      }

      if (usuarioActual.rol === 'OPERARIO') {
        showToast("Acceso denegado: Se requiere rol SUPERVISOR para autorizar ajustes en SAP.", "bi-shield-slash-fill");
        playScanBeep(false);
        return;
      }

      const id = btn.getAttribute('data-id');
      const causa = btn.getAttribute('data-causa');
      const evidencia = btn.getAttribute('data-evidencia');

      btn.disabled = true;
      btn.textContent = "Procesando...";

      const res = await window.apiClient.approveAdjustment(id, causa, evidencia);
      showToast(res.mensaje || "Ajuste de inventario aprobado con éxito");

      // Recargar tabla de auditoría y trazabilidad
      await loadAuditoriaData();
      await loadTrazabilidadData();
    });
  });

  // Actualizar bandeja de alertas inteligentes de discrepancias
  await cargarAlertasDiscrepancias();
}

// Modal de Creación de Auditoría
async function abrirModalNuevaAuditoria() {
  if (!usuarioActual) {
    showToast("Debe iniciar sesión para registrar una auditoría.", "bi-shield-exclamation");
    mostrarPortalLogin();
    return;
  }

  // Obtener productos reales y actualizados desde la base de datos Spring Boot
  let productosLive = await window.apiClient.searchProducts("");
  if (!productosLive || productosLive.length === 0) {
    productosLive = typeof DEFAULT_SAP_PRODUCTS !== 'undefined' ? DEFAULT_SAP_PRODUCTS : [];
  }

  const select = document.getElementById('audit-new-sku-select');
  if (select && productosLive.length > 0) {
    select.innerHTML = `
      <optgroup label="Calzado Deportivo">
        ${productosLive.filter(p => (p.linea || '').toLowerCase().includes('calz')).map(p => `
          <option value="${p.sku}">${p.sku} • ${p.descripcion} (Stock SAP: ${p.stockSap} u.)</option>
        `).join('')}
      </optgroup>
      <optgroup label="Ropa & Textil">
        ${productosLive.filter(p => (p.linea || '').toLowerCase().includes('ropa')).map(p => `
          <option value="${p.sku}">${p.sku} • ${p.descripcion} (Stock SAP: ${p.stockSap} u.)</option>
        `).join('')}
      </optgroup>
      <optgroup label="Accesorios">
        ${productosLive.filter(p => (p.linea || '').toLowerCase().includes('acc')).map(p => `
          <option value="${p.sku}">${p.sku} • ${p.descripcion} (Stock SAP: ${p.stockSap} u.)</option>
        `).join('')}
      </optgroup>
    `;
    await actualizarValoresNuevoAudit(select.value || 'CAL-NIK-001');
  }

  const modal = document.getElementById('modal-nueva-auditoria');
  if (modal) modal.classList.add('active');
}

function cerrarModalNuevaAuditoria() {
  const modal = document.getElementById('modal-nueva-auditoria');
  if (modal) modal.classList.remove('active');
}

// ==========================================================================
// BANDEJA DE ALERTAS INTELIGENTES DE DISCREPANCIAS & AUDITORÍA EN 1 CLIC
// ==========================================================================
async function cargarAlertasDiscrepancias() {
  const container = document.getElementById('discrepancy-alerts-grid');
  const badgeTotal = document.getElementById('badge-total-alertas');
  const navBadge = document.getElementById('nav-discrepancy-badge');
  if (!container) return;

  try {
    const pendingAudits = await window.apiClient.getPendingAudits() || [];
    let productos = await window.apiClient.searchProducts("") || [];
    if (!productos || productos.length === 0) {
      productos = typeof DEFAULT_SAP_PRODUCTS !== 'undefined' ? DEFAULT_SAP_PRODUCTS : [];
    }

    const discrepancias = [];

    // 1. Auditorías ya registradas
    for (const audit of pendingAudits) {
      if (audit.diferencia !== 0 && !discrepancias.some(d => d.sku === audit.sku)) {
        discrepancias.push({
          sku: audit.sku,
          descripcion: audit.descripcion,
          linea: audit.linea,
          ubicacion: audit.subtituloUbicacion || 'Atocongo • Anaquel 04',
          stockSap: audit.stockSap,
          conteoFisico: audit.conteoFisico,
          diferencia: audit.diferencia,
          causaSugerida: audit.causaInvestigada || (audit.diferencia < 0 ? 'Hurto en Piso de Venta' : 'Sobrante por Devolución de Cliente'),
          estadoAprobado: Boolean(audit.estadoAprobado)
        });
      }
    }

    // 2. Discrepancias directas de catálogo no conciliadas
    for (const p of productos) {
      if (discrepancias.some(d => d.sku === p.sku)) continue;
      if (p.conteoFisico !== undefined && p.conteoFisico !== null && p.conteoFisico !== p.stockSap) {
        const diff = p.conteoFisico - p.stockSap;
        discrepancias.push({
          sku: p.sku,
          descripcion: p.descripcion,
          linea: p.linea,
          ubicacion: p.ubicacion || 'Piso de Venta',
          stockSap: p.stockSap,
          conteoFisico: p.conteoFisico,
          diferencia: diff,
          causaSugerida: diff < 0 ? 'Hurto en Piso de Venta' : 'Sobrante por Devolución de Cliente',
          estadoAprobado: false
        });
      }
    }

    // 3. Respaldo canónico de Marathon Atocongo
    if (discrepancias.length === 0) {
      const canonicas = [
        { sku: "CAL-NIK-001", descripcion: "Zapatillas Nike Mercurial Vapor 15 Club", linea: "CALZADO", ubicacion: "Atocongo • Anaquel 04", stockSap: 14, conteoFisico: 12, diferencia: -2, causaSugerida: "Hurto en Piso de Venta", estadoAprobado: false },
        { sku: "CAL-ADI-003", descripcion: "Zapatillas Adidas Predator Accuracy.3", linea: "CALZADO", ubicacion: "Atocongo • Anaquel 04", stockSap: 12, conteoFisico: 11, diferencia: -1, causaSugerida: "Picking Traspapelado", estadoAprobado: false },
        { sku: "ROP-MAR-103", descripcion: "Camiseta Oficial Universitario 2026", linea: "ROPA", ubicacion: "Atocongo • Anaquel 04", stockSap: 20, conteoFisico: 22, diferencia: 2, causaSugerida: "Error de Recepción", estadoAprobado: false },
        { sku: "ACC-ADI-201", descripcion: "Balón de Fútbol Adidas Club N°5 Oficial", linea: "ACCESORIOS", ubicacion: "Módulo Central Accesorios", stockSap: 23, conteoFisico: 24, diferencia: 1, causaSugerida: "Sobrante por Devolución de Cliente", estadoAprobado: false }
      ];
      discrepancias.push(...canonicas);
    }

    const pendientes = discrepancias.filter(d => !d.estadoAprobado);
    const totalAlertas = pendientes.length;

    if (badgeTotal) {
      badgeTotal.textContent = `${totalAlertas} Diferencias Activas`;
      badgeTotal.className = totalAlertas > 0 ? "pill-badge pill-outline-danger" : "pill-badge pill-green";
    }
    if (navBadge) {
      navBadge.textContent = totalAlertas;
      navBadge.style.display = totalAlertas > 0 ? 'inline-flex' : 'none';
    }

    if (totalAlertas === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; padding: 22px; text-align: center; background: #F0FDF4; border: 1.5px dashed #86EFAC; border-radius: 12px; color: #166534;">
          <i class="bi bi-shield-check" style="font-size: 2rem; color: #16A34A; display: block; margin-bottom: 6px;"></i>
          <strong style="font-size: 0.95rem;">¡Inventario Cuadrado! Cero discrepancias pendientes</strong>
          <div style="font-size: 0.78rem; margin-top: 4px; color: #15803D;">El Stock SAP y el Conteo Físico coinciden en los 22 productos de Marathon Atocongo.</div>
        </div>
      `;
      return;
    }

    container.innerHTML = pendientes.map(item => {
      const isSobrante = item.diferencia > 0;
      const cardClass = isSobrante ? 'discrepancy-alert-card sobrante' : 'discrepancy-alert-card';
      const pillClass = item.diferencia < -1 ? 'alert-diff-pill diff-pill-danger' : (item.diferencia === -1 ? 'alert-diff-pill diff-pill-warning' : 'alert-diff-pill diff-pill-info');
      const btnClass = isSobrante ? 'btn-autofill-audit sobrante' : 'btn-autofill-audit';
      const signo = item.diferencia > 0 ? '+' : '';

      let costoUnit = 140;
      const foundProd = productos.find(p => p.sku === item.sku);
      if (foundProd && foundProd.costoUnitario) costoUnit = foundProd.costoUnitario;
      const impactoEco = (item.diferencia * costoUnit).toFixed(2);

      return `
        <div class="${cardClass}">
          <div>
            <div class="alert-card-header">
              <span class="alert-card-sku">${item.sku}</span>
              <span class="${pillClass}">
                ${signo}${item.diferencia} u. ${isSobrante ? '(Sobrante)' : '(Faltante)'}
              </span>
            </div>
            <div class="alert-card-title">${item.descripcion}</div>
            <div class="alert-card-meta">
              <i class="bi bi-geo-alt"></i> ${item.ubicacion} • ${item.linea || 'Tienda'}
            </div>
          </div>

          <div class="alert-card-diff-row">
            <div>
              <span style="color: #64748B;">SAP:</span> <strong>${item.stockSap} u.</strong>
              <span style="color: #94A3B8; margin: 0 4px;">|</span>
              <span style="color: #64748B;">Físico:</span> <strong style="color: ${isSobrante ? '#0284C7' : '#DC2626'}">${item.conteoFisico} u.</strong>
            </div>
            <div style="font-weight: 700; color: ${isSobrante ? '#0284C7' : '#DC2626'}; font-size: 0.76rem;">
              S/ ${impactoEco}
            </div>
          </div>

          <div style="font-size: 0.72rem; color: #475569; background: #FFFFFF; padding: 5px 8px; border-radius: 6px; border: 1px solid #F1F5F9;">
            <i class="bi bi-lightbulb-fill" style="color: #F59E0B;"></i> <strong>Diagnóstico:</strong> ${item.causaSugerida}
          </div>

          <button type="button" class="${btnClass}" onclick="iniciarAuditoriaRapida('${item.sku}', ${item.stockSap}, ${item.conteoFisico}, '${item.descripcion.replace(/'/g, "\\'")}', '${(item.linea || 'CALZADO').toUpperCase()}', '${item.causaSugerida.replace(/'/g, "\\'")}')" title="Completar y registrar auditoría de ${item.sku} en 1 clic">
            <i class="bi bi-lightning-charge-fill"></i> Auditar con 1 Clic
          </button>
        </div>
      `;
    }).join('');

  } catch (err) {
    console.error('[ALERTAS] Error al cargar alertas de discrepancia:', err);
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 12px; color: #DC2626; font-size: 0.8rem;">
        No se pudieron cargar las alertas en vivo. Por favor intente re-escanear.
      </div>
    `;
  }
}

// Acción de 1 clic: autocompleta el formulario de auditoría y enfoca al usuario en confirmar la causa
async function iniciarAuditoriaRapida(sku, stockSap, conteoFisico, descripcion, linea, causaSugerida) {
  if (!usuarioActual) {
    showToast("Debe iniciar sesión para registrar una auditoría.", "bi-shield-exclamation");
    mostrarPortalLogin();
    return;
  }

  // 1. Abrir modal
  await abrirModalNuevaAuditoria();

  // 2. Seleccionar el SKU en el dropdown
  const select = document.getElementById('audit-new-sku-select');
  if (select) {
    let encontrado = false;
    for (let i = 0; i < select.options.length; i++) {
      if (select.options[i].value === sku) {
        select.selectedIndex = i;
        encontrado = true;
        break;
      }
    }
    if (!encontrado) {
      const opt = document.createElement('option');
      opt.value = sku;
      opt.textContent = `${sku} • ${descripcion} (Stock SAP: ${stockSap} u.)`;
      opt.selected = true;
      select.appendChild(opt);
    }
  }

  // 3. Forzar valores exactos detectados por el sistema
  const sapInput = document.getElementById('audit-new-stock-sap');
  const fisInput = document.getElementById('audit-new-stock-fis');
  if (sapInput) sapInput.value = stockSap;
  if (fisInput) fisInput.value = conteoFisico;

  // 4. Calcular diferencia e impacto
  calcularDiferenciaNuevoAudit();

  // 5. Seleccionar causa correspondiente
  const causaSelect = document.getElementById('audit-new-causa');
  if (causaSelect) {
    const diff = conteoFisico - stockSap;
    let matchEncontrado = false;
    if (causaSugerida) {
      const sugeridaLower = causaSugerida.toLowerCase();
      for (let i = 0; i < causaSelect.options.length; i++) {
        if (causaSelect.options[i].value.toLowerCase().includes(sugeridaLower.slice(0, 5))) {
          causaSelect.selectedIndex = i;
          matchEncontrado = true;
          break;
        }
      }
    }
    if (!matchEncontrado) {
      if (diff < -1) {
        causaSelect.value = "Hurto en Piso de Venta";
      } else if (diff === -1) {
        causaSelect.value = "Picking Traspapelado / Desubicado";
      } else {
        causaSelect.value = "Sobrante por Devolución de Cliente";
      }
    }
  }

  // 6. Pre-llenar evidencia con diagnóstico automatizado
  const evidenciaInput = document.getElementById('audit-new-evidencia');
  if (evidenciaInput) {
    const diff = conteoFisico - stockSap;
    const diffText = diff < 0 ? `Faltante crítico de ${Math.abs(diff)} u.` : `Sobrante de +${diff} u.`;
    evidenciaInput.value = `[Alerta Automática] ${diffText} detectada en conciliación cíclica. Verificación física inmediata en góndola Atocongo.`;
    evidenciaInput.focus();
  }

  showToast(`¡Discrepancia de ${sku} precargada! Haz clic en "Guardar Auditoría" para registrar.`, "bi-lightning-charge-fill");
}

async function actualizarValoresNuevoAudit(sku) {
  let sap = 14;
  let conteoActual = null;

  try {
    const trace = await window.apiClient.getTraceability(sku);
    if (trace && trace.stockTeoricoSap !== undefined) {
      sap = trace.stockTeoricoSap;
      conteoActual = trace.conteoFisicoReal;
    }
  } catch (e) {
    console.warn('[AUDIT] No se pudo obtener trazabilidad en vivo:', e);
  }

  if (conteoActual === null && typeof DEFAULT_SAP_PRODUCTS !== 'undefined') {
    const prod = DEFAULT_SAP_PRODUCTS.find(p => p.sku === sku);
    if (prod) {
      sap = prod.stockSap;
      conteoActual = (prod.conteoFisico !== undefined && prod.conteoFisico !== null) ? prod.conteoFisico : Math.max(0, sap - 1);
    }
  }

  const sapInput = document.getElementById('audit-new-stock-sap');
  const fisInput = document.getElementById('audit-new-stock-fis');

  if (sapInput) sapInput.value = sap;
  if (fisInput) fisInput.value = (conteoActual !== null && conteoActual !== undefined) ? conteoActual : Math.max(0, sap - 1);
  calcularDiferenciaNuevoAudit();
}

function calcularDiferenciaNuevoAudit() {
  const sapInput = document.getElementById('audit-new-stock-sap');
  const fisInput = document.getElementById('audit-new-stock-fis');
  const skuSelect = document.getElementById('audit-new-sku-select');
  const diffLabel = document.getElementById('audit-new-diff-label');
  const costLabel = document.getElementById('audit-new-cost-label');

  const sap = parseInt(sapInput?.value || "0", 10);
  const fis = parseInt(fisInput?.value || "0", 10);
  const diff = fis - sap;

  let costoUnit = 140.00;
  if (skuSelect && typeof DEFAULT_SAP_PRODUCTS !== 'undefined') {
    const prod = DEFAULT_SAP_PRODUCTS.find(p => p.sku === skuSelect.value);
    if (prod && prod.costoUnitario) costoUnit = prod.costoUnitario;
  }

  const impacto = diff * costoUnit;

  if (diffLabel) {
    if (diff < 0) {
      diffLabel.textContent = `${diff} unidades (Faltante Crítico)`;
      diffLabel.style.color = '#DC2626';
    } else if (diff === 0) {
      diffLabel.textContent = `0 unidades (Conforme)`;
      diffLabel.style.color = '#16A34A';
    } else {
      diffLabel.textContent = `+${diff} unidades (Sobrante por Devolución)`;
      diffLabel.style.color = '#2563EB';
    }
  }

  if (costLabel) {
    costLabel.textContent = `S/ ${Number(impacto).toFixed(2)}`;
    costLabel.style.color = impacto < 0 ? '#DC2626' : (impacto === 0 ? '#16A34A' : '#2563EB');
  }
}

async function guardarNuevaAuditoria(e) {
  if (e && e.preventDefault) e.preventDefault();

  const skuSelect = document.getElementById('audit-new-sku-select');
  const sapInput = document.getElementById('audit-new-stock-sap');
  const fisInput = document.getElementById('audit-new-stock-fis');
  const causaSelect = document.getElementById('audit-new-causa');
  const evidenciaInput = document.getElementById('audit-new-evidencia');
  const submitBtn = document.getElementById('btn-submit-nueva-auditoria');

  const sku = skuSelect ? skuSelect.value : 'CAL-NIK-001';
  const stockSap = parseInt(sapInput?.value || "0", 10);
  const conteoFisico = parseInt(fisInput?.value || "0", 10);
  const causal = causaSelect ? causaSelect.value : 'Hurto en Piso de Venta';
  const comentario = evidenciaInput ? evidenciaInput.value.trim() : 'Revisión en tienda';

  let prod = null;
  if (typeof DEFAULT_SAP_PRODUCTS !== 'undefined') {
    prod = DEFAULT_SAP_PRODUCTS.find(p => p.sku === sku);
  }

  const auditoriaPayload = {
    sku: sku,
    descripcionProducto: prod ? prod.descripcion : sku,
    linea: prod ? prod.linea.toUpperCase() : 'CALZADO',
    ubicacion: prod ? prod.ubicacion : 'Pasillo A - Estante 01',
    stockSap: stockSap,
    conteoFisico: conteoFisico,
    causal: causal,
    comentario: comentario,
    validadoPor: usuarioActual ? usuarioActual.nombreCompleto.split(' (')[0] : 'Joseph Aaron Churampi'
  };

  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Guardando...';
  }

  try {
    const res = await window.apiClient.createAudit(auditoriaPayload);
    showToast(`Auditoría registrada exitosamente para SKU ${sku}`, 'bi-shield-check');
    cerrarModalNuevaAuditoria();

    // Recargar tabla de auditorías y trazabilidad
    await loadAuditoriaData();
    await loadTrazabilidadData(sku);
  } catch (err) {
    showToast('Error al registrar la auditoría.', 'bi-exclamation-triangle');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="bi bi-check-circle-fill"></i> Guardar Auditoría';
    }
  }
}

function abrirDetalleAuditoria(id) {
  const item = listaDiscrepanciasActuales.find(x => x.id === id);
  const container = document.getElementById('detalle-audit-content');
  if (!item || !container) return;

  container.innerHTML = `
    <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px; margin-bottom: 14px;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
        <div>
          <span class="suggestion-sku-pill" style="font-size: 0.8rem;">${item.sku}</span>
          <strong style="font-size: 0.96rem; color: #0F172A; display: block; margin-top: 4px;">${item.descripcion}</strong>
        </div>
        <span class="pill-badge ${item.estadoAprobado ? 'pill-green' : 'pill-outline-danger'}">
          ${item.estadoAprobado ? 'AJUSTE APROBADO' : 'PENDIENTE DE SUPERVISIÓN'}
        </span>
      </div>
      <div style="font-size: 0.76rem; color: #64748B;">
        <i class="bi bi-geo-alt-fill"></i> ${item.subtituloUbicacion} • Sede Atocongo
      </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 14px;">
      <div style="background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 10px; text-align: center;">
        <div style="font-size: 0.72rem; color: #64748B;">Stock Teórico SAP</div>
        <div style="font-size: 1.25rem; font-weight: 800; color: #0F172A;">${item.stockSap} u.</div>
      </div>
      <div style="background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 10px; text-align: center;">
        <div style="font-size: 0.72rem; color: #64748B;">Conteo Físico Real</div>
        <div style="font-size: 1.25rem; font-weight: 800; color: #0284C7;">${item.conteoFisico} u.</div>
      </div>
      <div style="background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 10px; text-align: center;">
        <div style="font-size: 0.72rem; color: #64748B;">Diferencia Neta</div>
        <div style="font-size: 1.25rem; font-weight: 800; color: ${item.diferencia < 0 ? '#DC2626' : (item.diferencia === 0 ? '#16A34A' : '#2563EB')};">
          ${item.diferencia > 0 ? '+' : ''}${item.diferencia} u.
        </div>
      </div>
    </div>

    <div style="display: flex; flex-direction: column; gap: 8px; font-size: 0.82rem;">
      <div><strong>Causa Raíz Investigada:</strong> <span style="color: #0F172A;">${item.causaInvestigada}</span></div>
      <div><strong>Evidencia / Nota de Auditoría:</strong> <span style="color: #475569;">${item.evidenciaNota}</span></div>
      <div><strong>Metodología de Documentación:</strong> Incluido en Reporte Excel Apache POI y Acta Abierta OpenPDF.</div>
    </div>
  `;

  const modal = document.getElementById('modal-detalle-auditoria');
  if (modal) modal.classList.add('active');
}

function cerrarModalDetalleAuditoria() {
  const modal = document.getElementById('modal-detalle-auditoria');
  if (modal) modal.classList.remove('active');
}

// ==========================================================================
// 4. TRAZABILIDAD SKU
// ==========================================================================
let trazabilidadSearchTimeout = null;

function initTrazabilidadView() {
  const selectSku = document.getElementById('trazabilidad-sku-select');
  if (selectSku) {
    selectSku.addEventListener('change', async (e) => {
      await cambiarSkuTrazabilidad(e.target.value);
    });
  }

  const searchInput = document.getElementById('trazabilidad-search-input');
  if (searchInput) {
    searchInput.addEventListener('keypress', async (e) => {
      if (e.key === 'Enter') {
        const q = searchInput.value.trim();
        if (q) {
          const res = await window.apiClient.searchProducts(q);
          if (res && res.length > 0) {
            await seleccionarSkuTrazabilidad(res[0].sku);
          }
        }
      }
    });
  }

  // Cerrar sugerencias al hacer click fuera
  document.addEventListener('click', (e) => {
    const suggestions = document.getElementById('tr-search-suggestions');
    const input = document.getElementById('trazabilidad-search-input');
    if (suggestions && !suggestions.contains(e.target) && e.target !== input) {
      cerrarSugerenciasTrazabilidad();
    }
  });
}

async function buscarTrazabilidadEnVivo(query) {
  clearTimeout(trazabilidadSearchTimeout);
  const q = (query || "").trim();
  const dropdown = document.getElementById('tr-search-suggestions');
  if (!dropdown) return;

  if (q.length < 1) {
    dropdown.classList.remove('show');
    dropdown.innerHTML = '';
    return;
  }

  trazabilidadSearchTimeout = setTimeout(async () => {
    const resultados = await window.apiClient.searchProducts(q);
    if (!resultados || resultados.length === 0) {
      dropdown.innerHTML = `
        <div style="padding: 12px; text-align: center; font-size: 0.8rem; color: #94A3B8;">
          <i class="bi bi-search"></i> No se encontraron productos con "${q}".
        </div>
      `;
      dropdown.classList.add('show');
      return;
    }

    const items = resultados.slice(0, 8);
    dropdown.innerHTML = `
      <div class="suggestion-header-label">
        <i class="bi bi-diagram-3-fill" style="color: #0052CC;"></i> Productos para Trazabilidad (${resultados.length})
      </div>
      ${items.map(p => `
        <div class="suggestion-item" onclick="seleccionarSkuTrazabilidad('${p.sku}')">
          <div class="suggestion-main">
            <span class="suggestion-sku-pill">${p.sku}</span>
            <div class="suggestion-info">
              <span class="suggestion-title">${p.descripcion}</span>
              <span class="suggestion-meta">Línea: ${p.linea} • Stock SAP: ${p.stockSap} u.</span>
            </div>
          </div>
          <div class="suggestion-right">
            <span class="suggestion-stock" style="background: #E0F2FE; color: #0369A1;">Ver Timeline</span>
          </div>
        </div>
      `).join('')}
    `;
    dropdown.classList.add('show');
  }, 160);
}

function cerrarSugerenciasTrazabilidad() {
  const dropdown = document.getElementById('tr-search-suggestions');
  if (dropdown) dropdown.classList.remove('show');
}

function limpiarBusquedaTrazabilidad() {
  const input = document.getElementById('trazabilidad-search-input');
  if (input) {
    input.value = '';
    input.focus();
  }
  cerrarSugerenciasTrazabilidad();
}

async function seleccionarSkuTrazabilidad(sku) {
  cerrarSugerenciasTrazabilidad();
  const input = document.getElementById('trazabilidad-search-input');
  if (input) input.value = sku;
  await cambiarSkuTrazabilidad(sku);
  showToast(`Trazabilidad actualizada para ${sku}`, 'bi-clock-history');
}

async function cambiarSkuTrazabilidad(sku) {
  const select = document.getElementById('trazabilidad-sku-select');
  if (select && select.value !== sku) {
    select.value = sku;
  }
  const input = document.getElementById('trazabilidad-search-input');
  if (input && input.value !== sku) {
    input.value = sku;
  }

  // Actualizar chips activos
  document.querySelectorAll('.chip-sku-pill').forEach(chip => {
    chip.classList.toggle('active', chip.getAttribute('onclick')?.includes(sku));
  });

  await loadTrazabilidadData(sku);
}

async function loadTrazabilidadData(sku = "CAL-NIK-001") {
  const data = await window.apiClient.getTraceability(sku);
  if (!data) return;

  const nameEl = document.getElementById('tr-prod-name');
  const skuEl = document.getElementById('tr-prod-sku');
  const sedeEl = document.getElementById('tr-prod-sede');
  const sapEl = document.getElementById('tr-stock-sap');
  const fisEl = document.getElementById('tr-stock-fis');
  const diffEl = document.getElementById('tr-stock-diff');

  if (nameEl) nameEl.textContent = data.nombreProducto;
  if (skuEl) skuEl.textContent = `SKU: ${data.sku} • EAN: ${data.ean || data.sku}`;
  if (sedeEl) sedeEl.textContent = data.sedeAlmacen;

  if (sapEl) sapEl.textContent = `${data.stockTeoricoSap} u.`;
  if (fisEl) fisEl.textContent = `${data.conteoFisicoReal} u.`;
  if (diffEl) {
    diffEl.textContent = data.diferenciaTexto;
    if (data.diferencia < 0) {
      diffEl.className = "stat-metric-val stat-val-red";
    } else if (data.diferencia === 0) {
      diffEl.className = "stat-metric-val";
      diffEl.style.color = "#16A34A";
    } else {
      diffEl.className = "stat-metric-val stat-val-blue";
    }
  }

  // Timeline
  const timelineContainer = document.getElementById('tr-timeline-list');
  if (timelineContainer && data.historialMovimientos) {
    timelineContainer.innerHTML = data.historialMovimientos.map(m => {
      let badgeClass = "timeline-badge-blue";
      if (m.tipoBadge === 'Ajustado' || m.badgeColor === 'danger') badgeClass = "timeline-badge-red";
      if (m.tipoBadge === 'MB52 Sync' || m.tipoBadge === 'Conteo Conforme' || m.badgeColor === 'success') badgeClass = "timeline-badge-green";

      return `
        <div class="timeline-card-item">
          <div class="timeline-top-row">
            <span class="timeline-badge ${badgeClass}">${m.tipoBadge}</span>
            <span class="timeline-time-label">${m.fechaTexto}</span>
          </div>
          <div class="timeline-title">${m.titulo}</div>
          <div class="timeline-desc">${m.descripcion}</div>
        </div>
      `;
    }).join('');
  }
}

// ==========================================================================
// 5. CONFIGURACIÓN DE CICLO
// ==========================================================================
function initConfigurationView() {
  const btnGuardar = document.getElementById('btn-guardar-config-ciclo');
  if (btnGuardar) {
    btnGuardar.addEventListener('click', async () => {
      const frecuencia = document.getElementById('config-frecuencia-select').value;
      const activePills = [];
      document.querySelectorAll('.pill-select-item:not(.inactive)').forEach(p => {
        activePills.push(p.getAttribute('data-cat'));
      });

      const config = {
        frecuencia: frecuencia,
        lineasMuestreo: activePills,
        reglaMuestreo: "Filtro activo: Clasificación de productos ABC de alta rotación (Top marcas Nike, Adidas, Puma).",
        responsableCalzado: "Jose Armando Tunqui",
        rolResponsableCalzado: "Auditor Principal Calzado",
        responsableJefe: "Joseph Aaron Churampi",
        rolResponsableJefe: "Jefe de Inventario"
      };

      btnGuardar.disabled = true;
      btnGuardar.textContent = "Guardando...";

      await window.apiClient.saveCycleConfig(config);

      btnGuardar.disabled = false;
      btnGuardar.textContent = "Guardar Configuración de Ciclo";

      showToast("Configuración de ciclo guardada exitosamente");
    });
  }

  // Toggle de pills de categorías de muestreo
  document.querySelectorAll('.pill-select-item').forEach(pill => {
    pill.addEventListener('click', () => {
      pill.classList.toggle('inactive');
    });
  });
}

async function loadConfigurationData() {
  const config = await window.apiClient.getCycleConfig();
  if (config) {
    const select = document.getElementById('config-frecuencia-select');
    if (select && config.frecuencia) {
      select.value = config.frecuencia;
    }
  }
  await cargarUsuariosCRUD();
}

// ==========================================================================
// FUNCIONES DE CONTROL: SPRING SECURITY (AUTENTICACIÓN Y ROLES RBAC)
// ==========================================================================

function abrirModalSeguridad() {
  const modal = document.getElementById('security-modal');
  if (modal) modal.classList.add('active');
}

function cerrarModalSeguridad() {
  const modal = document.getElementById('security-modal');
  if (modal) modal.classList.remove('active');
}

async function autenticarComo(username, password) {
  try {
    const res = await window.apiClient.login(username, password);
    if (res.autenticado) {
      // Actualizar chips de selección visual
      document.querySelectorAll('.role-chip').forEach(c => c.classList.remove('selected'));
      const chip = document.getElementById('chip-' + username);
      if (chip) chip.classList.add('selected');

      // Actualizar información en el header
      const nameEl = document.getElementById('header-user-name');
      const roleEl = document.getElementById('header-user-role');
      const avatarEl = document.getElementById('header-user-avatar');

      if (nameEl) nameEl.textContent = res.nombreCompleto.split(' (')[0];
      if (roleEl) {
        const color = res.rol === 'SUPERVISOR' ? '#DC2626' : (res.rol === 'AUDITOR' ? '#10B981' : '#0284C7');
        roleEl.innerHTML = `<i class="bi bi-shield-check" style="color: ${color};"></i> ${res.rol} (Spring Security)`;
      }
      if (avatarEl) {
        const parts = res.nombreCompleto.split(' ');
        avatarEl.textContent = (parts[0] ? parts[0][0] : 'U') + (parts[1] ? parts[1][0] : '');
      }

      showToast(`Autenticado en Spring Security como: ${res.rol}`);
      setTimeout(() => cerrarModalSeguridad(), 600);
      await loadAuditoriaData();
      await cargarUsuariosCRUD();
    } else {
      showToast(`Error: ${res.mensaje}`);
    }
  } catch (e) {
    showToast("Error de conexión con el módulo de seguridad");
  }
}

// ==========================================================================
// PORTAL DE LOGIN OBLIGATORIO Y GESTIÓN DE SESIÓN (SPRING SECURITY 6)
// ==========================================================================

function verificarSesionInicial() {
  const sesionGuardada = sessionStorage.getItem('marathon_auth_user');
  const portal = document.getElementById('login-portal');

  if (sesionGuardada) {
    try {
      usuarioActual = JSON.parse(sesionGuardada);
      aplicarSesionEnUI(usuarioActual);
      if (portal) portal.classList.add('hidden');
    } catch (e) {
      console.warn('[AUTH] Error al leer sesión previa:', e);
      mostrarPortalLogin();
    }
  } else {
    // Si no hay sesión previa, mostrar obligatoriamente el portal de login
    mostrarPortalLogin();
  }
}

function mostrarPortalLogin() {
  usuarioActual = null;
  const portal = document.getElementById('login-portal');
  if (portal) portal.classList.remove('hidden');

  // Restablecer header a estado desconectado
  const nameEl = document.getElementById('header-user-name');
  const roleEl = document.getElementById('header-user-role');
  const avatarEl = document.getElementById('header-user-avatar');
  if (nameEl) nameEl.textContent = 'No autenticado';
  if (roleEl) roleEl.innerHTML = '<i class="bi bi-shield-lock" style="color: #94A3B8;"></i> Desconectado';
  if (avatarEl) avatarEl.textContent = '?';
}

function aplicarSesionEnUI(user) {
  if (!user) return;
  const nameEl = document.getElementById('header-user-name');
  const roleEl = document.getElementById('header-user-role');
  const avatarEl = document.getElementById('header-user-avatar');

  if (nameEl) nameEl.textContent = user.nombreCompleto.split(' (')[0];
  if (roleEl) {
    const color = user.rol === 'SUPERVISOR' ? '#DC2626' : (user.rol === 'AUDITOR' ? '#10B981' : '#0284C7');
    roleEl.innerHTML = `<i class="bi bi-shield-lock-fill" style="color: ${color};"></i> ${user.rol} (Spring Security)`;
  }
  if (avatarEl) {
    const parts = user.nombreCompleto.split(' ');
    avatarEl.textContent = (parts[0] ? parts[0][0] : 'U') + (parts[1] ? parts[1][0] : '');
  }

  // Marcar chip de seguridad en el modal
  document.querySelectorAll('.role-chip').forEach(c => c.classList.remove('selected'));
  const chip = document.getElementById('chip-' + user.username);
  if (chip) chip.classList.add('selected');
}

async function procesarLogin(e) {
  if (e && e.preventDefault) e.preventDefault();
  const userInput = document.getElementById('login-username-input');
  const passInput = document.getElementById('login-password-input');
  const submitBtn = document.getElementById('btn-login-submit');
  const alertBox = document.getElementById('login-error-alert');
  const alertMsg = document.getElementById('login-error-msg');

  const username = userInput ? userInput.value.trim() : '';
  const password = passInput ? passInput.value.trim() : '';

  if (!username || !password) {
    if (alertBox) alertBox.style.display = 'flex';
    if (alertMsg) alertMsg.textContent = 'Por favor ingrese su usuario y contraseña.';
    return;
  }

  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Validando con Spring Security...';
  }

  try {
    const res = await window.apiClient.login(username, password);
    if (res && res.autenticado) {
      usuarioActual = res;
      sessionStorage.setItem('marathon_auth_user', JSON.stringify(res));

      if (alertBox) alertBox.style.display = 'none';
      const portal = document.getElementById('login-portal');
      if (portal) portal.classList.add('hidden');

      aplicarSesionEnUI(res);
      showToast(`¡Bienvenido! Sesión iniciada como ${res.rol} (${res.nombreCompleto})`, 'bi-shield-check');

      // Direccionar a vista de inicio según el perfil
      if (res.rol === 'OPERARIO') {
        const tabEscaner = document.querySelector('[data-target="view-escaner"]');
        if (tabEscaner) tabEscaner.click();
      } else {
        const tabAuditoria = document.querySelector('[data-target="view-auditoria"]');
        if (tabAuditoria) tabAuditoria.click();
      }

      await loadAuditoriaData();
      await loadScannerData();
      await cargarUsuariosCRUD();
    } else {
      if (alertBox) alertBox.style.display = 'flex';
      if (alertMsg) alertMsg.textContent = res.mensaje || 'Credenciales incorrectas en Spring Security.';
      playScanBeep(false);
    }
  } catch (err) {
    if (alertBox) alertBox.style.display = 'flex';
    if (alertMsg) alertMsg.textContent = 'Error de conexión con el módulo Spring Security.';
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="bi bi-box-arrow-in-right"></i> Iniciar Sesión con Spring Security';
    }
  }
}

function loginRapidoDemo(username, password) {
  const userInput = document.getElementById('login-username-input');
  const passInput = document.getElementById('login-password-input');
  if (userInput) userInput.value = username;
  if (passInput) passInput.value = password;
  procesarLogin();
}

function cerrarSesion() {
  sessionStorage.removeItem('marathon_auth_user');
  usuarioActual = null;
  mostrarPortalLogin();
  showToast('Sesión cerrada correctamente.', 'bi-door-closed-fill');
}

function togglePasswordVisibility() {
  const input = document.getElementById('login-password-input');
  const icon = document.getElementById('pwd-toggle-icon');
  if (!input) return;
  if (input.type === 'password') {
    input.type = 'text';
    if (icon) {
      icon.classList.remove('bi-eye-fill');
      icon.classList.add('bi-eye-slash-fill');
    }
  } else {
    input.type = 'password';
    if (icon) {
      icon.classList.remove('bi-eye-slash-fill');
      icon.classList.add('bi-eye-fill');
    }
  }
}

// Control de descargas con verificación de permisos RBAC
function descargarExcelSeguro(linea = 'ALL') {
  if (!usuarioActual) {
    showToast('Debe iniciar sesión para descargar reportes.', 'bi-shield-exclamation');
    mostrarPortalLogin();
    return;
  }
  if (usuarioActual.rol === 'OPERARIO') {
    showToast('Acceso denegado: La descarga de matrices de conciliación en Excel (.xlsx) requiere rol AUDITOR o SUPERVISOR.', 'bi-shield-slash-fill');
    playScanBeep(false);
    return;
  }
  window.apiClient.descargarExcelReconciliacion(linea);
}

function descargarPdfSeguro(linea = 'ALL') {
  if (!usuarioActual) {
    showToast('Debe iniciar sesión para generar actas oficiales.', 'bi-shield-exclamation');
    mostrarPortalLogin();
    return;
  }
  if (usuarioActual.rol === 'OPERARIO') {
    showToast('Acceso denegado: La emisión de Actas Oficiales de Auditoría en PDF requiere rol AUDITOR o SUPERVISOR.', 'bi-shield-slash-fill');
    playScanBeep(false);
    return;
  }
  window.apiClient.descargarPdfActa(linea);
}

// ==========================================================================
// FUNCIONES ADICIONALES: RESET DEMO, FILTRO AUDITORÍA, MARBETE Y ESCÁNER
// ==========================================================================

async function reiniciarDatosDemo() {
  const btn = document.getElementById('btn-reseed-demo');
  const originalHtml = btn ? btn.innerHTML : '';
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Restaurando...';
  }

  try {
    const res = await window.apiClient.reseedDemo();
    showToast(res.mensaje || "¡Datos de prueba restaurados a valores iniciales!");

    // Recargar todas las vistas
    await loadAuditoriaData();
    await loadScannerData();
    await loadPreVentaData("Nike Air Max Excee Running");
    await loadTrazabilidadData("CAL-NIK-001");
    await loadConfigurationData();
  } catch (e) {
    showToast("Error al reiniciar los datos de demostración");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = originalHtml;
    }
  }
}

function filtrarTablaAuditoria(query) {
  const q = (query || "").toLowerCase().trim();
  const rows = document.querySelectorAll('#audit-table-body tr');
  rows.forEach(row => {
    const text = row.textContent.toLowerCase();
    row.style.display = text.includes(q) ? '' : 'none';
  });
}

async function setScannerCode(code) {
  const input = document.getElementById('scanner-barcode-input');
  if (input) input.value = code;
  await executeScan(code, 1);
}

function abrirModalEtiqueta() {
  const modal = document.getElementById('marbete-modal');
  if (!modal) return;

  // Tomar datos del último producto escaneado o del producto en pantalla
  const lastProdName = document.getElementById('last-read-name')?.textContent || "Zapatillas Nike Air Max Excee Running";
  const lastProdLoc = document.getElementById('last-read-location')?.textContent || "Pasillo A - Estante 03";
  const barcodeVal = document.getElementById('scanner-barcode-input')?.value || "790123456712";

  const titleEl = document.getElementById('marbete-prod-title');
  if (titleEl) titleEl.textContent = lastProdName;

  const skuEl = document.getElementById('marbete-sku-val');
  if (skuEl) skuEl.textContent = barcodeVal.startsWith('79') ? 'SKU: CAL-NIK-002' : `SKU: ${barcodeVal.slice(0, 11)}`;

  const eanSvg = document.getElementById('marbete-ean-svg-text');
  if (eanSvg) eanSvg.textContent = barcodeVal;

  const locEl = document.getElementById('marbete-ubicacion-text');
  if (locEl) locEl.textContent = `Ubicación: ${lastProdLoc.replace('Ubicación física: ', '')} • Auditoría Marathon 2026`;

  modal.classList.add('active');
}

function cerrarModalEtiqueta() {
  const modal = document.getElementById('marbete-modal');
  if (modal) modal.classList.remove('active');
}

function abrirModalSeguridad() {
  const modal = document.getElementById('security-modal');
  if (modal) modal.classList.add('active');
}

function cerrarModalSeguridad() {
  const modal = document.getElementById('security-modal');
  if (modal) modal.classList.remove('active');
}

// ==========================================================================
// GESTIÓN CRUD DE USUARIOS (EXCLUSIVO ROLE_SUPERVISOR)
// ==========================================================================
let usuariosCrudCache = [];

async function cargarUsuariosCRUD() {
  const tbody = document.getElementById('usuarios-table-body');
  if (!tbody) return;

  const esSupervisor = usuarioActual && (usuarioActual.rol === 'SUPERVISOR' || usuarioActual.rol === 'ROLE_SUPERVISOR');
  
  const alerta = document.getElementById('alerta-permiso-usuario');
  const btnNuevo = document.getElementById('btn-abrir-nuevo-usuario');
  if (alerta) alerta.style.display = esSupervisor ? 'none' : 'block';
  if (btnNuevo) {
    btnNuevo.disabled = !esSupervisor;
    btnNuevo.style.opacity = esSupervisor ? '1' : '0.5';
    btnNuevo.style.cursor = esSupervisor ? 'pointer' : 'not-allowed';
  }

  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 16px;"><i class="bi bi-arrow-repeat spin"></i> Cargando usuarios...</td></tr>';

  try {
    usuariosCrudCache = await window.apiClient.getUsuarios();
  } catch (e) {
    usuariosCrudCache = [];
  }

  if (!usuariosCrudCache || usuariosCrudCache.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 16px;">No se encontraron colaboradores registrados</td></tr>';
    return;
  }

  tbody.innerHTML = usuariosCrudCache.map(u => {
    const rolClean = (u.rol || '').replace('ROLE_', '');
    const badgeStyle = rolClean === 'SUPERVISOR' 
      ? 'background: #FEE2E2; color: #DC2626;' 
      : (rolClean === 'AUDITOR' ? 'background: #DCFCE7; color: #16A34A;' : 'background: #E0F2FE; color: #0284C7;');
    const avatarInitials = (u.nombre || 'U').split(' ').map(n => n[0]).slice(0, 2).join('');
    
    return `
      <tr>
        <td>
          <div style="display: flex; align-items: center; gap: 8px;">
            <div style="width: 30px; height: 30px; border-radius: 6px; background: #334155; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">
              ${avatarInitials}
            </div>
            <div>
              <strong style="color: var(--text-main); font-size: 0.84rem; display: block;">${u.nombre}</strong>
              <span style="font-size: 0.72rem; color: var(--text-muted);">${u.sede || 'Marathon Atocongo'}</span>
            </div>
          </div>
        </td>
        <td><code style="font-size: 0.82rem; color: var(--brand-blue);">${u.usuario}</code></td>
        <td><span class="pill-badge" style="${badgeStyle}; font-size: 0.72rem; font-weight: 700;">${rolClean}</span></td>
        <td style="font-size: 0.8rem; color: var(--text-secondary);">${u.sede || 'Marathon Atocongo'}</td>
        <td style="font-size: 0.78rem; font-family: monospace; color: var(--text-muted);">${u.codigo || '—'}</td>
        <td style="text-align: center; white-space: nowrap;">
          <button type="button" class="btn-crud-edit" onclick="abrirModalUsuario('${u.id}')" ${!esSupervisor ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : ''} title="Modificar datos del usuario">
            <i class="bi bi-pencil-fill"></i> Modificar
          </button>
          <button type="button" class="btn-crud-delete" onclick="eliminarUsuario('${u.id}')" ${!esSupervisor ? 'disabled style="opacity: 0.4; cursor: not-allowed;"' : ''} title="Eliminar usuario">
            <i class="bi bi-trash-fill"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function abrirModalUsuario(id = null) {
  const modal = document.getElementById('modal-crud-usuario');
  if (!modal) return;

  const esSupervisor = usuarioActual && (usuarioActual.rol === 'SUPERVISOR' || usuarioActual.rol === 'ROLE_SUPERVISOR');
  if (!esSupervisor) {
    showToast('Acceso denegado: Solo el Supervisor (Jefa de Tienda) puede gestionar usuarios.', 'bi-shield-slash-fill');
    return;
  }

  const titulo = document.getElementById('modal-usuario-titulo');
  const inputId = document.getElementById('crud-user-id');
  const inputNombre = document.getElementById('crud-user-nombre');
  const inputLogin = document.getElementById('crud-user-login');
  const inputClave = document.getElementById('crud-user-clave');
  const selectRol = document.getElementById('crud-user-rol');
  const inputCodigo = document.getElementById('crud-user-codigo');
  const inputSede = document.getElementById('crud-user-sede');

  if (id) {
    const user = usuariosCrudCache.find(u => u.id === id);
    if (user) {
      if (titulo) titulo.textContent = 'Modificar Colaborador';
      if (inputId) inputId.value = user.id;
      if (inputNombre) inputNombre.value = user.nombre;
      if (inputLogin) {
        inputLogin.value = user.usuario;
        inputLogin.readOnly = true;
      }
      if (inputClave) {
        inputClave.value = '';
        inputClave.placeholder = 'Dejar vacío para mantener la actual';
        inputClave.required = false;
      }
      if (selectRol) selectRol.value = user.rol.startsWith('ROLE_') ? user.rol : 'ROLE_' + user.rol;
      if (inputCodigo) inputCodigo.value = user.codigo || '';
      if (inputSede) inputSede.value = user.sede || 'Marathon Atocongo';
    }
  } else {
    if (titulo) titulo.textContent = 'Registrar Colaborador';
    if (inputId) inputId.value = '';
    if (inputNombre) inputNombre.value = '';
    if (inputLogin) {
      inputLogin.value = '';
      inputLogin.readOnly = false;
    }
    if (inputClave) {
      inputClave.value = '';
      inputClave.placeholder = '••••••••';
      inputClave.required = true;
    }
    if (selectRol) selectRol.value = 'ROLE_OPERARIO';
    if (inputCodigo) inputCodigo.value = 'U' + Math.floor(10000000 + Math.random() * 90000000);
    if (inputSede) inputSede.value = 'Marathon Atocongo';
  }

  modal.classList.add('active');
}

function cerrarModalUsuario() {
  const modal = document.getElementById('modal-crud-usuario');
  if (modal) modal.classList.remove('active');
}

async function guardarUsuario(e) {
  e.preventDefault();
  const id = document.getElementById('crud-user-id').value;
  const nombre = document.getElementById('crud-user-nombre').value.trim();
  const login = document.getElementById('crud-user-login').value.trim();
  const clave = document.getElementById('crud-user-clave').value.trim();
  const rol = document.getElementById('crud-user-rol').value;
  const codigo = document.getElementById('crud-user-codigo').value.trim();
  const sede = document.getElementById('crud-user-sede').value.trim();

  if (id) {
    const datos = { nombre, rol, codigo, sede };
    if (clave) datos.clave = clave;
    await window.apiClient.updateUsuario(id, datos);
    showToast(`Usuario ${login} actualizado exitosamente`, 'bi-check-circle-fill');
  } else {
    if (!clave) {
      showToast('Por favor ingrese una contraseña para el nuevo usuario', 'bi-exclamation-triangle-fill');
      return;
    }
    const nuevo = {
      id: 'user-' + Date.now().toString().slice(-4),
      nombre,
      usuario: login,
      clave,
      rol,
      codigo,
      sede
    };
    await window.apiClient.createUsuario(nuevo);
    showToast(`Nuevo usuario ${login} registrado en Spring Security`, 'bi-check-circle-fill');
  }

  cerrarModalUsuario();
  await cargarUsuariosCRUD();
}

async function eliminarUsuario(id) {
  const user = usuariosCrudCache.find(u => u.id === id);
  const nombre = user ? user.nombre : 'este usuario';
  if (!confirm(`¿Está seguro de eliminar a ${nombre} del sistema? Esta acción no se puede deshacer.`)) {
    return;
  }
  await window.apiClient.deleteUsuario(id);
  showToast('Colaborador eliminado del sistema', 'bi-trash-fill');
  await cargarUsuariosCRUD();
}

// Exportación global de funciones para el DOM
window.abrirModalSeguridad = abrirModalSeguridad;
window.cerrarModalSeguridad = cerrarModalSeguridad;
window.autenticarComo = autenticarComo;
window.verificarSesionInicial = verificarSesionInicial;
window.mostrarPortalLogin = mostrarPortalLogin;
window.procesarLogin = procesarLogin;
window.loginRapidoDemo = loginRapidoDemo;
window.cerrarSesion = cerrarSesion;
window.togglePasswordVisibility = togglePasswordVisibility;
window.descargarExcelSeguro = descargarExcelSeguro;
window.descargarPdfSeguro = descargarPdfSeguro;
window.reiniciarDatosDemo = reiniciarDatosDemo;
window.filtrarTablaAuditoria = filtrarTablaAuditoria;
window.setScannerCode = setScannerCode;
window.abrirModalEtiqueta = abrirModalEtiqueta;
window.cerrarModalEtiqueta = cerrarModalEtiqueta;
window.executeScan = executeScan;
window.ejecutarEscaneoDirecto = ejecutarEscaneoDirecto;
window.buscarPreVentaEnVivo = buscarPreVentaEnVivo;
window.cerrarSugerenciasPreVenta = cerrarSugerenciasPreVenta;
window.limpiarBusquedaPreVenta = limpiarBusquedaPreVenta;
window.seleccionarProductoPreVenta = seleccionarProductoPreVenta;
window.abrirModalNuevaAuditoria = abrirModalNuevaAuditoria;
window.cerrarModalNuevaAuditoria = cerrarModalNuevaAuditoria;
window.actualizarValoresNuevoAudit = actualizarValoresNuevoAudit;
window.calcularDiferenciaNuevoAudit = calcularDiferenciaNuevoAudit;
window.guardarNuevaAuditoria = guardarNuevaAuditoria;
window.abrirDetalleAuditoria = abrirDetalleAuditoria;
window.cerrarModalDetalleAuditoria = cerrarModalDetalleAuditoria;
window.cambiarSkuTrazabilidad = cambiarSkuTrazabilidad;
window.loadTrazabilidadData = loadTrazabilidadData;
window.buscarTrazabilidadEnVivo = buscarTrazabilidadEnVivo;
window.cerrarSugerenciasTrazabilidad = cerrarSugerenciasTrazabilidad;
window.limpiarBusquedaTrazabilidad = limpiarBusquedaTrazabilidad;
window.seleccionarSkuTrazabilidad = seleccionarSkuTrazabilidad;
window.cargarAlertasDiscrepancias = cargarAlertasDiscrepancias;
window.iniciarAuditoriaRapida = iniciarAuditoriaRapida;

// Funciones CRUD Usuarios (Exclusivo Supervisor)
window.cargarUsuariosCRUD = cargarUsuariosCRUD;
window.abrirModalUsuario = abrirModalUsuario;
window.cerrarModalUsuario = cerrarModalUsuario;
window.guardarUsuario = guardarUsuario;
window.eliminarUsuario = eliminarUsuario;



