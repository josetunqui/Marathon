package pe.marathon.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SISTEMA DE CONTEO CÍCLICO DE INVENTARIO - MARATHON SPORTS ATOCONGO
 * Curso Integrador I: Sistemas - Software
 * 
 * Autores:
 * - Joseph Aaron Churampi Quispe (U21212777)
 * - Jose Armando Tunqui Puelles (U23331844)
 * Docente: Mg. Anita Condo López
 */
@SpringBootApplication
@EnableScheduling
public class MarathonInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarathonInventarioApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("🚀 MARATHON INVENTARIO ATOCONGO - BACKEND INICIADO CON ÉXITO");
        System.out.println("📡 Endpoints REST: http://localhost:8080/api/v1/inventario/reconciliacion");
        System.out.println("📖 Swagger UI Docs: http://localhost:8080/swagger-ui.html");
        System.out.println("🗄️ Consola H2 DB:  http://localhost:8080/h2-console");
        System.out.println("==================================================================");
    }
}
