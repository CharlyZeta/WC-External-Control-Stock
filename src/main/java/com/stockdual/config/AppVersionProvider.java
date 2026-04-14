package com.stockdual.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Proveedor de información de la versión del aplicativo.
 */
@Component
public class AppVersionProvider {

    @Value("${app.version:dev}")
    private String version;

    /**
     * Obtiene la versión actual del aplicativo.
     * @return versión en formato SemVer o "dev" si no está disponible
     */
    public String getVersion() {
        if (version == null || version.contains("@project.version@")) {
            return "dev";
        }
        return version;
    }
}
