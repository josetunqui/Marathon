package pe.marathon.inventario.service.reporte;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import pe.marathon.inventario.dto.ItemReconciliacionDTO;
import pe.marathon.inventario.dto.ReporteReconciliacionDTO;
import pe.marathon.inventario.dto.ResumenKpiDTO;
import pe.marathon.inventario.model.AuditoriaMerma;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ===================================================================
 * 3. LIBRERÍA 3: OPENPDF 1.3.39 (com.github.librepdf:openpdf)
 * ===================================================================
 * Servicio para la generación de Actas Oficiales de Auditoría de Inventario
 * y Dictámenes de Control de Mermas en formato PDF formal con membrete
 * corporativo, tablas resumen, métricas IRA y espacios de firma legal.
 */
@Service
public class PdfReporteService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Colores corporativos Marathon Sports
    private static final Color COLOR_MARATHON_RED = new Color(208, 2, 27);
    private static final Color COLOR_DARK_SLATE = new Color(26, 32, 44);
    private static final Color COLOR_LIGHT_BG = new Color(248, 249, 250);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    public byte[] generarActaAuditoriaPdf(ReporteReconciliacionDTO reporte, List<AuditoriaMerma> auditorias) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_MARATHON_RED);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_DARK_SLATE);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fontCuerpo = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_DARK_SLATE);
            Font fontCuerpoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_DARK_SLATE);
            Font fontNotaLegal = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);

            // 1. MEMBRETE Y ENCABEZADO OFICIAL
            Paragraph pEmpresa = new Paragraph("MARATHON SPORTS PERÚ — SEDE ATOCONGO", fontEmpresa);
            pEmpresa.setAlignment(Element.ALIGN_CENTER);
            document.add(pEmpresa);

            Paragraph pTitulo = new Paragraph("ACTA OFICIAL DE CONTEO CÍCLICO Y AUDITORÍA DE INVENTARIO", fontSubtitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingAfter(10);
            document.add(pTitulo);

            // 2. TABLA DE METADATOS DE LA AUDITORÍA
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(12);

            String fechaStr = reporte.getFechaGeneracion() != null
                    ? reporte.getFechaGeneracion().format(DATE_FORMATTER)
                    : "Fecha actual";

            metaTable.addCell(crearCeldaMetadata("N° DE ACTA: ACT-2026-ATC-001", fontCuerpoBold));
            metaTable.addCell(crearCeldaMetadata("FECHA Y HORA DE CORTE: " + fechaStr, fontCuerpo));
            metaTable.addCell(crearCeldaMetadata("SEDE: Open Plaza Atocongo (Tienda 104)", fontCuerpo));
            metaTable.addCell(crearCeldaMetadata("LÍNEA AUDITADA: " + reporte.getLineaFiltro(), fontCuerpo));
            metaTable.addCell(crearCeldaMetadata("AUDITOR LÍDER: Joseph Aaron Churampi (U21212777)", fontCuerpo));
            metaTable.addCell(crearCeldaMetadata("OPERARIO RESPONSABLE: Jose Armando Tunqui (U23331844)", fontCuerpo));

            document.add(metaTable);

            // 3. TABLA RESUMEN DE INDICADORES CLAVE (KPI & IRA)
            ResumenKpiDTO kpis = reporte.getKpis();
            if (kpis != null) {
                Paragraph pKpiTitle = new Paragraph("1. INDICADORES DE EXACTITUD DE REGISTRO DE INVENTARIO (IRA)", fontSubtitulo);
                pKpiTitle.setSpacingAfter(6);
                document.add(pKpiTitle);

                PdfPTable kpiTable = new PdfPTable(6);
                kpiTable.setWidthPercentage(100);
                kpiTable.setSpacingAfter(12);

                String[] kpiHeaders = {"SKUs Totales", "Stock SAP", "Físico Real", "Conformes", "Diferencias", "IRA % (Exactitud)"};
                for (String h : kpiHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));
                    cell.setBackgroundColor(COLOR_DARK_SLATE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    kpiTable.addCell(cell);
                }

                kpiTable.addCell(crearCeldaDato(String.valueOf(kpis.getTotalSkus()), fontCuerpoBold, Element.ALIGN_CENTER));
                kpiTable.addCell(crearCeldaDato(String.valueOf(kpis.getTotalSap()), fontCuerpo, Element.ALIGN_CENTER));
                kpiTable.addCell(crearCeldaDato(String.valueOf(kpis.getTotalFisico()), fontCuerpo, Element.ALIGN_CENTER));
                kpiTable.addCell(crearCeldaDato(String.valueOf(kpis.getConformes()), fontCuerpo, Element.ALIGN_CENTER));
                int difTotal = kpis.getFaltantes() + kpis.getSobrantes();
                kpiTable.addCell(crearCeldaDato(String.valueOf(difTotal), fontCuerpoBold, Element.ALIGN_CENTER));

                // IRA con color de estado
                Color iraColor = kpis.getIra() >= 95.0 ? COLOR_SUCCESS : COLOR_DANGER;
                Font fontIra = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, iraColor);
                kpiTable.addCell(crearCeldaDato(String.format("%.1f%%", kpis.getIra()), fontIra, Element.ALIGN_CENTER));

                document.add(kpiTable);
            }

            // 4. TABLA DETALLADA DE PRODUCTOS AUDITADOS
            Paragraph pDetalleTitle = new Paragraph("2. RESULTADOS DEL CONCILIADO FÍSICO POR SKU", fontSubtitulo);
            pDetalleTitle.setSpacingAfter(6);
            document.add(pDetalleTitle);

            PdfPTable itemsTable = new PdfPTable(new float[]{1.8f, 3.5f, 1.2f, 1.2f, 1.2f, 1.8f, 2.0f});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingAfter(14);

            String[] itemHeaders = {"SKU", "Descripción", "SAP", "Físico", "Dif.", "Estado", "Impacto Costo"};
            for (String h : itemHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));
                cell.setBackgroundColor(COLOR_MARATHON_RED);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                itemsTable.addCell(cell);
            }

            List<ItemReconciliacionDTO> items = reporte.getItems();
            if (items != null) {
                for (ItemReconciliacionDTO it : items) {
                    itemsTable.addCell(crearCeldaDato(it.getSku(), fontCuerpoBold, Element.ALIGN_LEFT));
                    itemsTable.addCell(crearCeldaDato(it.getDescripcion(), fontCuerpo, Element.ALIGN_LEFT));
                    itemsTable.addCell(crearCeldaDato(String.valueOf(it.getStockSap()), fontCuerpo, Element.ALIGN_CENTER));
                    itemsTable.addCell(crearCeldaDato(String.valueOf(it.getConteoFisico()), fontCuerpo, Element.ALIGN_CENTER));

                    int dif = it.getDiferencia() != null ? it.getDiferencia() : 0;
                    Font fontDif = dif == 0 ? fontCuerpo : (dif < 0
                            ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_DANGER)
                            : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_MARATHON_RED));
                    itemsTable.addCell(crearCeldaDato(String.valueOf(dif), fontDif, Element.ALIGN_CENTER));

                    itemsTable.addCell(crearCeldaDato(it.getEstado(), fontCuerpo, Element.ALIGN_CENTER));

                    double impacto = it.getCostoDiferencia() != null ? it.getCostoDiferencia() : 0.0;
                    itemsTable.addCell(crearCeldaDato(String.format("S/ %.2f", impacto), fontCuerpo, Element.ALIGN_RIGHT));
                }
            }
            document.add(itemsTable);

            // 5. NOTA LEGAL / DECLARACIÓN DE CONFORMIDAD
            Paragraph pCert = new Paragraph(
                    "CERTIFICACIÓN Y DICTAMEN: Por medio del presente documento, los firmantes dan fe de la veracidad " +
                    "de los conteos físicos registrados con escáner en piso de venta y almacén. Las discrepancias " +
                    "críticas detectadas quedan canalizadas al módulo de Control de Mermas conforme a los procedimientos " +
                    "de Auditoría Interna de Marathon Sports S.A. Edición 2026.",
                    fontNotaLegal
            );
            pCert.setAlignment(Element.ALIGN_JUSTIFIED);
            pCert.setSpacingAfter(25);
            document.add(pCert);

            // 6. RECUADROS PARA FIRMAS FORMALES
            PdfPTable firmasTable = new PdfPTable(2);
            firmasTable.setWidthPercentage(100);

            PdfPCell celdaFirma1 = new PdfPCell();
            celdaFirma1.setBorder(Rectangle.NO_BORDER);
            celdaFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaFirma1.addElement(new Paragraph("____________________________________________", fontCuerpo));
            celdaFirma1.addElement(new Paragraph("AUDITOR DE CONTROL DE PÉRDIDAS", fontCuerpoBold));
            celdaFirma1.addElement(new Paragraph("Joseph Aaron Churampi Quispe (U21212777)", fontCuerpo));
            celdaFirma1.addElement(new Paragraph("Marathon Sports — Sede Atocongo", fontNotaLegal));

            PdfPCell celdaFirma2 = new PdfPCell();
            celdaFirma2.setBorder(Rectangle.NO_BORDER);
            celdaFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaFirma2.addElement(new Paragraph("____________________________________________", fontCuerpo));
            celdaFirma2.addElement(new Paragraph("JEFE DE TIENDA / SUPERVISOR", fontCuerpoBold));
            celdaFirma2.addElement(new Paragraph("Mg. Anita Condo López (Docente / Tutora)", fontCuerpo));
            celdaFirma2.addElement(new Paragraph("Visto Bueno y Aprobación de Mermas SAP", fontNotaLegal));

            firmasTable.addCell(celdaFirma1);
            firmasTable.addCell(celdaFirma2);
            document.add(firmasTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el documento PDF con OpenPDF: " + e.getMessage(), e);
        }
    }

    private PdfPCell crearCeldaMetadata(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell crearCeldaDato(String texto, Font font, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        return cell;
    }
}
