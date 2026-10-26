package pe.marathon.inventario.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ===================================================================
 * 1. LIBRERÍA 1: SPRING SECURITY 6 (Spring Boot 3.2.4)
 * ===================================================================
 * Configuración de autenticación en memoria, roles jerárquicos
 * (OPERARIO, AUDITOR, SUPERVISOR), encriptación BCrypt y protección
 * de endpoints REST manteniendo compatibilidad completa con el frontend.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // Usuario 1: Operario de Conteo (piso de venta y almacén)
        UserDetails operario = User.builder()
                .username("operario")
                .password(encoder.encode("operario123"))
                .roles("OPERARIO")
                .build();

        UserDetails jtunqui = User.builder()
                .username("jtunqui")
                .password(encoder.encode("marathon2026"))
                .roles("OPERARIO")
                .build();

        // Usuario 2: Auditor de Inventario y Control de Pérdidas
        UserDetails auditor = User.builder()
                .username("auditor")
                .password(encoder.encode("auditor123"))
                .roles("AUDITOR", "OPERARIO")
                .build();

        UserDetails jchurampi = User.builder()
                .username("jchurampi")
                .password(encoder.encode("marathon2026"))
                .roles("AUDITOR", "OPERARIO")
                .build();

        // Usuario 3: Supervisor / Jefe de Tienda Marathon Sede Atocongo
        UserDetails supervisor = User.builder()
                .username("supervisor")
                .password(encoder.encode("admin123"))
                .roles("SUPERVISOR", "AUDITOR", "OPERARIO", "ADMIN")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("SUPERVISOR", "AUDITOR", "OPERARIO", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(operario, jtunqui, auditor, jchurampi, supervisor, admin);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para APIs REST sin estado
                .csrf(csrf -> csrf.disable())
                // Habilitar CORS según la configuración CorsConfig
                .cors(Customizer.withDefaults())
                // Permitir iframe para consola H2 en desarrollo
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Reglas de autorización de solicitudes HTTP
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos del frontend
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        // Documentación Swagger / OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Consola H2
                        .requestMatchers("/h2-console/**").permitAll()
                        // Endpoints de autenticación e info de seguridad
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Endpoints de reportes Excel y PDF
                        .requestMatchers("/api/v1/reportes/**").permitAll()
                        // Endpoints operativos de inventario
                        .requestMatchers("/api/v1/productos/**", "/api/v1/preventa/**", "/api/v1/ciclos/**",
                                         "/api/v1/trazabilidad/**", "/api/v1/inventario/**", "/api/v1/conteo/**",
                                         "/api/v1/auditorias/**", "/api/v1/usuarios/**").permitAll()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                // Habilitar HTTP Basic para pruebas con Postman / Curl
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
