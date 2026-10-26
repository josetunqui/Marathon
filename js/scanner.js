/**
 * CONTROLADOR DEL TERMINAL DE ESCÁNER DE CÓDIGO DE BARRAS
 * Soporta escaneo continuo USB-C / Bluetooth, incremento automático y feedback
 */

class BarcodeScannerController {
  constructor() {
    this.storage = storageManager;
    this.sound = soundEffects;
    this.currentLine = 'Calzado';
    this.currentLocation = 'Pasillo A - Estante 02';
    this.isMobileView = false;
    this.scanBuffer = '';
    this.lastKeyTime = 0;
  }

  init() {
    this.setupHardwareBarcodeListener();
  }

  // Escucha global de lectores de código de barras USB / Bluetooth
  // Los escáneres emiten caracteres muy rápido (< 50ms) y terminan con Enter
  setupHardwareBarcodeListener() {
    window.addEventListener('keydown', (e) => {
      // Ignorar si el usuario está escribiendo en campos de texto normales que no sean el escáner
      const activeTag = document.activeElement ? document.activeElement.tagName.toLowerCase() : '';
      const activeId = document.activeElement ? document.activeElement.id : '';

      if (['textarea', 'select'].includes(activeTag)) return;
      if (activeTag === 'input' && activeId !== 'scanner-barcode-input' && activeId !== 'mobile-barcode-input') return;

      const currentTime = new Date().getTime();

      if (e.key === 'Enter') {
        if (this.scanBuffer.trim().length > 2) {
          e.preventDefault();
          this.processBarcode(this.scanBuffer.trim());
          this.scanBuffer = '';
        }
      } else if (e.key.length === 1) {
        // Si el tiempo entre teclas es muy rápido o el foco está en el input del escáner
        if (currentTime - this.lastKeyTime < 60 || document.activeElement.id === 'scanner-barcode-input') {
          this.scanBuffer += e.key;
        } else {
          // Nueva ráfaga
          this.scanBuffer = e.key;
        }
        this.lastKeyTime = currentTime;
      }
    });
  }

  // Procesa el código ingresado (ya sea por escáner USB, manual o botón de prueba)
  processBarcode(code) {
    const cleanCode = code.trim().toUpperCase();
    if (!cleanCode) return { success: false, message: 'Ingrese un código válido' };

    const sapStock = this.storage.getSapStock();

    // Buscar por SKU exacto o por EAN-13
    let product = sapStock.find(p => p.sku.toUpperCase() === cleanCode || p.ean === cleanCode);

    // Si no se encuentra exacto, intentar búsqueda parcial
    if (!product) {
      product = sapStock.find(p => p.sku.toUpperCase().includes(cleanCode) || (p.ean && p.ean.includes(cleanCode)));
    }

    if (product) {
      // Producto identificado
      const updated = this.storage.incrementOrAddCount(product.sku, 1, {
        ubicacion: this.currentLocation
      });

      // Feedback sonoro y visual
      this.sound.playScanSuccess();
      this.triggerVisualFeedback(true, product);

      // Notificar al controlador de UI
      if (window.appController) {
        window.appController.onProductScanned(product, updated);
      }

      return {
        success: true,
        product,
        count: updated.conteoFisico,
        message: `Escaneado: ${product.descripcion} (Total: ${updated.conteoFisico})`
      };
    } else {
      // Producto NO registrado en SAP
      this.sound.playScanError();
      this.triggerVisualFeedback(false, { sku: cleanCode, descripcion: 'CÓDIGO NO CATALOGADO EN SAP' });

      // Se registra como sobrante desconocido para no perder la trazabilidad física
      const updated = this.storage.incrementOrAddCount(cleanCode, 1, {
        ubicacion: this.currentLocation
      });

      if (window.appController) {
        window.appController.onProductScanned({
          sku: cleanCode,
          ean: cleanCode,
          linea: 'Sin Clasificar',
          descripcion: 'Producto no catalogado en SAP (Posible mercadería externa)',
          talla: 'S/T',
          precioUnitario: 0,
          stockSap: 0
        }, updated);
      }

      return {
        success: false,
        isUnknown: true,
        message: `Alerta: Código ${cleanCode} no existe en catálogo SAP. Registrado como sobrante desconocido.`
      };
    }
  }

  // Efecto visual flash de escáner en pantalla
  triggerVisualFeedback(isSuccess, product) {
    const flashElement = document.getElementById('scanner-feedback-flash');
    const badgeElement = document.getElementById('scanner-last-item-card');

    if (flashElement) {
      flashElement.className = `scanner-flash ${isSuccess ? 'flash-success' : 'flash-error'}`;
      flashElement.style.display = 'block';
      setTimeout(() => {
        flashElement.style.display = 'none';
      }, 250);
    }

    if (badgeElement) {
      badgeElement.innerHTML = `
        <div class="d-flex align-items-center gap-3">
          <div class="scanner-status-icon ${isSuccess ? 'text-success' : 'text-danger'}">
            <i class="bi ${isSuccess ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'} fs-1"></i>
          </div>
          <div class="flex-grow-1">
            <div class="d-flex justify-content-between">
              <span class="badge ${isSuccess ? 'bg-success' : 'bg-danger'}">${product.sku}</span>
              <small class="text-secondary">${new Date().toLocaleTimeString()}</small>
            </div>
            <h6 class="mb-1 text-white fw-bold">${product.descripcion}</h6>
            <div class="d-flex gap-2 small text-light-gray">
              <span><strong>Línea:</strong> ${product.linea || 'N/A'}</span>
              <span>•</span>
              <span><strong>Ubicación:</strong> ${this.currentLocation}</span>
            </div>
          </div>
        </div>
      `;
    }
  }
}

const barcodeScanner = new BarcodeScannerController();
