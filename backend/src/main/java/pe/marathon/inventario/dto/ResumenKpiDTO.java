package pe.marathon.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenKpiDTO {
    private int totalSkus;
    private int totalFisico;
    private int totalSap;
    private int conformes;
    private int faltantes;
    private int sobrantes;
    private int noContados;
    private double valorSap;
    private double valorFisico;
    private double valorNetoDiferencia;
    private double costoNetoDiferencia;
    private double ira; // Índice de Exactitud de Registro de Inventario (%)
    private String iraClase; // "text-success", "text-warning", "text-danger"
    private String iraCalificacion; // "Óptimo (>= 95%)", "Alerta", "Crítico (< 85%)"
}
