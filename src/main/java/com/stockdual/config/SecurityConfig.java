package com.stockdual.config;

import com.stockdual.ui.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

@Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        // Configurar CSP para permitir imágenes externas (WooCommerce) y endurecer script execution
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' data: https: blob: http:; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com data:; " +
                    // NOTE: 'unsafe-inline' es estrictamente necesario para que Vaadin Flow inyecte:
                    // 1. Bloques de script bootstrap esenciales para inicialización del framework
                    // 2. Sincronización de estados de UI mediante setTimeout y event listeners
                    // 3. Estilos dinámicos generados por componentes de interacción de usuario
                    // SEE: https://vaadin.com/docs/latest/security/content-security-policy
                    // FUTURE: Evaluar migración a hashes de recursos noces si Vaadin lo permite
                    "script-src 'self' 'unsafe-inline'; " +
                    "connect-src 'self' https:; " +
                    "frame-ancestors 'self'; " +
                    "base-uri 'self';"
                )
            )
        );
        setLoginView(http, LoginView.class);
    }
}
