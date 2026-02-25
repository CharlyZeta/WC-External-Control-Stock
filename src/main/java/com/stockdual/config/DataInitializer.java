package com.stockdual.config;

import com.stockdual.domain.entity.AppUser;
import com.stockdual.domain.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .displayName("Administrador")
                    .role("ADMIN")
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Usuario admin creado (admin / admin123). Cambiá la contraseña en producción.");
        } else {
            log.info("ℹ️ Usuario admin ya existe, no se creó nuevamente.");
        }
    }
}
