package com.stockdual.domain.service;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
@Slf4j
@Service
public class GeminiService {
 
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
 
    public GeminiService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }
 
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductCopyResult {
        private String name;
        private String description;
        private String shortDescription;
        private String tags;
    }
 
    /**
     * Sends the product image to Gemini to generate descriptive copy.
     * 
     * @param imageBytes the product photo bytes
     * @param apiKey the Gemini API key
     * @return the generated copywriting results
     * @throws Exception if API call fails
     */
    public ProductCopyResult generateProductCopy(byte[] imageBytes, String apiKey) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("La API Key de Gemini no está configurada.");
        }
 
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey.trim();
 
        // 1. Encode image bytes to Base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
 
        // 2. Build Request Body for Gemini Multimodal API
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", "Analyze this product image. Generate a JSON object containing: "
                + "'name' (a concise, commercial product name), "
                + "'description' (a detailed e-commerce description), "
                + "'shortDescription' (a bulleted list summarizing the key details of the product), "
                + "and 'tags' (a comma-separated string of 5-8 e-commerce tags). "
                + "Provide only the raw JSON. Do not include markdown code block characters like ```json.");
 
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mimeType", "image/jpeg");
        inlineData.put("data", base64Image);
 
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inlineData", inlineData);
 
        Map<String, Object> partContainer = new HashMap<>();
        partContainer.put("parts", List.of(textPart, imagePart));
 
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
 
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(partContainer));
        payload.put("generationConfig", generationConfig);
 
        // 3. Make HTTP POST Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
 
        log.info("Sending request to Gemini API...");
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
 
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("La llamada a la API de Gemini falló con código: " + response.getStatusCode());
        }
 
        // 4. Parse the Multimodal JSON Response
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");
 
        if (textNode.isMissingNode()) {
            log.error("Gemini Response structure invalid: {}", response.getBody());
            throw new RuntimeException("Respuesta de Gemini inválida o vacía.");
        }
 
        String rawJson = textNode.asText();
        log.info("Received generated JSON content from Gemini: {}", rawJson);
 
        // Deserialize the generated e-commerce copy into the DTO
        return objectMapper.readValue(rawJson, ProductCopyResult.class);
    }
}
