package com.stockdual.manual;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Archivo de prueba para conexión con WooCommerce.
 * ADVERTENCIA DE SEGURIDAD: Nunca commitear credenciales reales.
 * Usar variables de entorno para las claves de API.
 */
public class WcTest {
    public static void main(String[] args) throws Exception {
        // CONFIGURACIÓN: Usar variables de entorno en producción
        String wcUrl = System.getenv("WC_API_URL") != null ? System.getenv("WC_API_URL") : "http://localhost:10014";
        String wcKey = System.getenv("WC_CONSUMER_KEY") != null ? System.getenv("WC_CONSUMER_KEY") : "TU_CONSUMER_KEY";
        String wcSecret = System.getenv("WC_CONSUMER_SECRET") != null ? System.getenv("WC_CONSUMER_SECRET") : "TU_CONSUMER_SECRET";

        String urlString = wcUrl + "/wp-json/wc/v3/products";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String auth = wcKey + ":" + wcSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        System.out.println("Response Code: " + conn.getResponseCode());

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}
