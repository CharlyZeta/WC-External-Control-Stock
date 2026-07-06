package com.stockdual.domain.service;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
 
class GeminiServiceTest {
 
    private GeminiService geminiService;
 
    @Mock
    private RestTemplateBuilder restTemplateBuilder;
 
    @Mock
    private RestTemplate restTemplate;
 
    private ObjectMapper objectMapper;
 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        geminiService = new GeminiService(restTemplateBuilder, objectMapper);
    }
 
    @Test
    void testGenerateProductCopySuccess() throws Exception {
        String mockResponseBody = "{\n" +
                "  \"candidates\": [\n" +
                "    {\n" +
                "      \"content\": {\n" +
                "        \"parts\": [\n" +
                "          {\n" +
                "            \"text\": \"{\\n  \\\"name\\\": \\\"Camisa Slim Fit\\\",\\n  \\\"description\\\": \\\"Una elegante camisa de vestir estilo slim fit.\\\",\\n  \\\"shortDescription\\\": \\\"Algodón 100%\\\\nEstilo Slim Fit\\\",\\n  \\\"tags\\\": \\\"camisa, slim, fit, vestir\\\"\\n}\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
 
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));
 
        byte[] dummyBytes = new byte[]{10, 20, 30};
        GeminiService.ProductCopyResult result = geminiService.generateProductCopy(dummyBytes, "dummy-api-key");
 
        assertNotNull(result);
        assertEquals("Camisa Slim Fit", result.getName());
        assertEquals("Una elegante camisa de vestir estilo slim fit.", result.getDescription());
        assertEquals("Algodón 100%\nEstilo Slim Fit", result.getShortDescription());
        assertEquals("camisa, slim, fit, vestir", result.getTags());
    }
 
    @Test
    void testGenerateProductCopyEmptyKeyThrowsException() {
        byte[] dummyBytes = new byte[]{10, 20, 30};
        assertThrows(IllegalArgumentException.class, () -> {
            geminiService.generateProductCopy(dummyBytes, "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            geminiService.generateProductCopy(dummyBytes, null);
        });
    }
 
    @Test
    void testGenerateProductCopyFailureResponseThrowsException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));
 
        byte[] dummyBytes = new byte[]{10, 20, 30};
        assertThrows(RuntimeException.class, () -> {
            geminiService.generateProductCopy(dummyBytes, "key");
        });
    }
}
