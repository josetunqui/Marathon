package pe.marathon.inventario.service.reporte;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import pe.marathon.inventario.dto.ItemReconciliacionDTO;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.dto.ResumenKpiDTO;
import pe.marathon.inventario.model.AuditoriaMerma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ===================================================================
 * 2. LIBRERÍA 2: APACHE POI 5.2.5 (org.apache.poi:poi, poi-ooxml)
 * ===================================================================
 * Servicio para la generación de reportes avanzados en formato Microsoft Excel (.xlsx)
 * con diseño corporativo Marathon Sports (colores, fuentes, bordes, formatos
 * monetarios y cálculo de métricas de inventario).
 */
@Service
public class ExcelReporteService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera el libro Excel de Reconciliación de Stock (SAP vs Conteo Físico).
     */
    public byte[] generarExcelReconciliacion(ReporteReconciliacionDTO reporte) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Reconciliación Atocongo");
            sheet.setDisplayGridlines(true);

            // Estilos
            XSSFCellStyle titleStyle = crearEstiloTitulo(workbook);
            XSSFCellStyle metaStyle = crearEstiloMetadata(workbook);
            XSSFCellStyle headerStyle = crearEstiloEncabezado(workbook);
            XSSFCellStyle dataStyle = crearEstiloDatos(workbook);
            XSSFCellStyle currencyStyle = crearEstiloMoneda(workbook);
            XSSFCellStyle intStyle = crearEstiloEntero(workbook);

            // Estilos para estados (Conforme, Faltante, Sobrante)
            XSSFCellStyle conformeStyle = crearEstiloBadge(workbook, IndexedColors.LIGHT_GREEN, IndexedColors.DARK_GREEN);
            XSSFCellStyle faltanteStyle = crearEstiloBadge(workbook, IndexedColors.CORAL, IndexedColors.DARK_RED);
            XSSFCellStyle sobranteStyle = crearEstiloBadge(workbook, IndexedColors.LIGHT_YELLOW, IndexedColors.ORANGE);

            int rowIdx = 0;

            // 1. TÍTULO PRINCIPAL
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(36);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MARATHON SPORTS — REPORTE DE CONCILIACIÓN DE INVENTARIO CÍCLICO");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

            // 2. METADATOS
            Row metaRow1 = sheet.createRow(rowIdx++);
            metaRow1.createCell(0).setCellValue("Sede: " + (reporte.getSede() != null ? reporte.getSede() : "Marathon Sports Atocongo"));
            metaRow1.getCell(0).setCellStyle(metaStyle);

            Row metaRow2 = sheet.createRow(rowIdx++);
            String fechaStr = reporte.getFechaGeneracion() != null
                    ? reporte.getFechaGeneracion().format(DATE_FORMATTER)
                    : "Fecha actual";
            metaRow2.createCell(0).setCellValue("Fecha y Hora de Emisión: " + fechaStr + " | Línea: " + reporte.getLineaFiltro());
            metaRow2.getCell(0).setCellStyle(metaStyle);

            // 3. TARJETA DE RESUMEN KPI (IRA %)
            ResumenKpiDTO kpis = reporte.getKpis();
            if (kpis != null) {
                Row kpiHeaderRow = sheet.createRow(rowIdx++);
                kpiHeaderRow.setHeightInPoints(22);
                Cell kpiH = kpiHeaderRow.createCell(0);
                kpiH.setCellValue("RESUMEN DE EXACTITUD DE REGISTRO DE INVENTARIO (IRA)");
                kpiH.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 13));

                Row kpiDataRow = sheet.createRow(rowIdx++);
                kpiDataRow.setHeightInPoints(20);
                kpiDataRow.createCell(0).setCellValue(String.format(
                        "Total SKUs: %d | Stock SAP: %d | Físico: %d | Conformes: %d | Faltantes: %d | Sobrantes: %d | IRA: %.1f%% (%s)",
                        kpis.getTotalSkus(), kpis.getTotalSap(), kpis.getTotalFisico(),
                        kpis.getConformes(), kpis.getFaltantes(), kpis.getSobrantes(),
                        kpis.getIra(), kpis.getIraCalificacion()
                ));
                kpiDataRow.getCell(0).setCellStyle(metaStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 13));
            }

            rowIdx++; // Fila vacía separadora

            // 4. CABECERAS DE TABLA
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(26);
            String[] headers = {
                    "N°", "SKU", "EAN-13", "Línea", "Marca",
                    "Descripción del Producto", "Talla", "Ubicación",
                    "Stock SAP", "Físico Real", "Diferencia", "Estado",
                    "Costo Unit. (S/)", "Impacto Neto (S/)"
            };

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // 5. LLENADO DE DATOS
            List<ItemReconciliacionDTO> items = reporte.getItems();
            int itemNum = 1;

            if (items != null) {
                for (ItemReconciliacionDTO item : items) {
                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(20);

                    // N°
                    Cell c0 = row.createCell(0);
                    c0.setCellValue(itemNum++);
                    c0.setCellStyle(intStyle);

                    // SKU
                    Cell c1 = row.createCell(1);
                    c1.setCellValue(item.getSku());
                    c1.setCellStyle(dataStyle);

                    // EAN
                    Cell c2 = row.createCell(2);
                    c2.setCellValue(item.getEan() != null ? item.getEan() : "-");
                    c2.setCellStyle(dataStyle);

                    // Línea
                    Cell c3 = row.createCell(3);
                    c3.setCellValue(item.getLinea());
                    c3.setCellStyle(dataStyle);

                    // Marca
                    Cell c4 = row.createCell(4);
                    c4.setCellValue(item.getMarca());
                    c4.setCellStyle(dataStyle);

                    // Descripción
                    Cell c5 = row.createCell(5);
                    c5.setCellValue(item.getDescripcion());
                    c5.setCellStyle(dataStyle);

                    // Talla
                    Cell c6 = row.createCell(6);
                    c6.setCellValue(item.getTalla() != null ? item.getTalla() : "-");
                    c6.setCellStyle(dataStyle);

                    // Ubicación
                    Cell c7 = row.createCell(7);
                    c7.setCellValue(item.getUbicacion() != null ? item.getUbicacion() : "-");
                    c7.setCellStyle(dataStyle);

                    // Stock SAP
                    Cell c8 = row.createCell(8);
                    c8.setCellValue(item.getStockSap() != null ? item.getStockSap() : 0);
                    c8.setCellStyle(intStyle);

                    // Físico Real
                    Cell c9 = row.createCell(9);
                    c9.setCellValue(item.getConteoFisico() != null ? item.getConteoFisico() : 0);
                    c9.setCellStyle(intStyle);

                    // Diferencia
                    Cell c10 = row.createCell(10);
                    int dif = item.getDiferencia() != null ? item.getDiferencia() : 0;
                    c10.setCellValue(dif);
                    c10.setCellStyle(intStyle);

                    // Estado
                    Cell c11 = row.createCell(11);
                    String estado = item.getEstado() != null ? item.getEstado() : "NO CONTADO";
                    c11.setCellValue(estado);
                    if ("CONFORME".equalsIgnoreCase(estado)) c11.setCellStyle(conformeStyle);
                    else if ("FALTANTE".equalsIgnoreCase(estado)) c11.setCellStyle(faltanteStyle);
                    else if ("SOBRANTE".equalsIgnoreCase(estado)) c11.setCellStyle(sobranteStyle);
                    else c11.setCellStyle(dataStyle);

                    // Costo Unitario
                    Cell c12 = row.createCell(12);
                    c12.setCellValue(item.getCostoUnitario() != null ? item.getCostoUnitario() : 0.0);
                    c12.setCellStyle(currencyStyle);

                    // Impacto Neto
                    Cell c13 = row.createCell(13);
                    c13.setCellValue(item.getCostoDiferencia() != null ? item.getCostoDiferencia() : 0.0);
                    c13.setCellStyle(currencyStyle);
                }
            }

            // Auto-ajuste de columnas
            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
                // Garantizar ancho mínimo para legibilidad
                if (sheet.getColumnWidth(col) < 3000) {
                    sheet.setColumnWidth(col, 3000);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Genera el libro Excel de Auditorías de Merma y Discrepancias.
     */
    public byte[] generarExcelAuditorias(List<AuditoriaMerma> auditorias) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Auditorías de Merma");
            sheet.setDisplayGridlines(true);

            XSSFCellStyle titleStyle = crearEstiloTitulo(workbook);
            XSSFCellStyle headerStyle = crearEstiloEncabezado(workbook);
            XSSFCellStyle dataStyle = crearEstiloDatos(workbook);
            XSSFCellStyle intStyle = crearEstiloEntero(workbook);

            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(34);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MARATHON SPORTS — REGISTRO DE AUDITORÍAS DE MERMA Y DISCREPANCIAS");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            rowIdx++; // espacio

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            String[] headers = {
                    "ID", "SKU", "Descripción", "Línea", "Ubicación",
                    "Diferencia", "Tipo", "Causal Justificada", "Estado Aprobado"
            };

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            if (auditorias != null) {
                for (AuditoriaMerma aud : auditorias) {
                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(20);

                    Cell c0 = row.createCell(0);
                    c0.setCellValue(aud.getId() != null ? aud.getId() : 0);
                    c0.setCellStyle(intStyle);

                    Cell c1 = row.createCell(1);
                    c1.setCellValue(aud.getSku());
                    c1.setCellStyle(dataStyle);

                    Cell c2 = row.createCell(2);
                    c2.setCellValue(aud.getDescripcionProducto() != null ? aud.getDescripcionProducto() : "-");
                    c2.setCellStyle(dataStyle);

                    Cell c3 = row.createCell(3);
                    c3.setCellValue(aud.getLinea() != null ? aud.getLinea() : "-");
                    c3.setCellStyle(dataStyle);

                    Cell c4 = row.createCell(4);
                    c4.setCellValue(aud.getUbicacion() != null ? aud.getUbicacion() : "-");
                    c4.setCellStyle(dataStyle);

                    Cell c5 = row.createCell(5);
                    c5.setCellValue(aud.getDiferencia() != null ? aud.getDiferencia() : 0);
                    c5.setCellStyle(intStyle);

                    Cell c6 = row.createCell(6);
                    c6.setCellValue(aud.getTipo() != null ? aud.getTipo() : "-");
                    c6.setCellStyle(dataStyle);

                    Cell c7 = row.createCell(7);
                    c7.setCellValue(aud.getCausal() != null ? aud.getCausal() : "-");
                    c7.setCellStyle(dataStyle);

                    Cell c8 = row.createCell(8);
                    boolean aprobado = Boolean.TRUE.equals(aud.getEstadoAprobado());
                    c8.setCellValue(aprobado ? "APROBADO (SAP Cuadrado)" : "PENDIENTE DE APROBACIÓN");
                    c8.setCellStyle(dataStyle);
                }
            }

            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // --- Métodos Auxiliares de Estilo POI ---

    private XSSFCellStyle crearEstiloTitulo(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloEncabezado(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    private XSSFCellStyle crearEstiloDatos(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    private XSSFCellStyle crearEstiloMoneda(XSSFWorkbook wb) {
        XSSFCellStyle style = crearEstiloDatos(wb);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("\"S/\" #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private XSSFCellStyle crearEstiloEntero(XSSFWorkbook wb) {
        XSSFCellStyle style = crearEstiloDatos(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloMetadata(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloBadge(XSSFWorkbook wb, IndexedColors bgColor, IndexedColors fontColor) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 9);
        font.setBold(true);
        font.setColor(fontColor.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(bgColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    private void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
