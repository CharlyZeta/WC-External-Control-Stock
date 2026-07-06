package com.stockdual.infrastructure.client;
 
import com.stockdual.domain.entity.Category;
import com.stockdual.domain.entity.Product;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
 
class WooCommerceClientTest {
 
    private WooCommerceClient wcClient;
 
    @Mock
    private RestTemplateBuilder restTemplateBuilder;
 
    @Mock
    private RestTemplate restTemplate;
 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        wcClient = new WooCommerceClient(restTemplateBuilder);
    }
 
    @Test
    @SuppressWarnings("unchecked")
    void testCreateDraftProductExtendedSuccess() {
        Product p = Product.builder()
                .sku("SKU-999")
                .name("Camisa Premium")
                .price(BigDecimal.valueOf(1500))
                .cost(BigDecimal.valueOf(800))
                .stockTotal(25)
                .description("Descripción Larga")
                .shortDescription("Resumen Corto")
                .weight(BigDecimal.valueOf(0.5))
                .length(BigDecimal.valueOf(30))
                .width(BigDecimal.valueOf(20))
                .height(BigDecimal.valueOf(10))
                .tags("camisa, moda")
                .imageUrl("/images/dummy.webp")
                .googleMetadata("google-seo")
                .facebookMetadata("facebook-pixel")
                .category(Category.builder().name("Ropa").build())
                .build();
 
        WcProductResponse mockWcResponse = new WcProductResponse();
        mockWcResponse.setId(12345L);
 
        // Argument captor to inspect request body
        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
 
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(WcProductResponse.class)
        )).thenReturn(new ResponseEntity<>(mockWcResponse, HttpStatus.OK));
 
        Long wcId = wcClient.createDraftProductExtended(
                "http://test-store.com",
                "ck_key",
                "cs_secret",
                p,
                "http://host.docker.internal:8080"
        );
 
        assertEquals(12345L, wcId);
 
        // Verify captured payload
        HttpEntity<Map<String, Object>> capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        Map<String, Object> payload = capturedEntity.getBody();
        assertNotNull(payload);
 
        assertEquals("Camisa Premium", payload.get("name"));
        assertEquals("SKU-999", payload.get("sku"));
        assertEquals("1500", payload.get("regular_price"));
        assertEquals(25, payload.get("stock_quantity"));
        assertEquals("Descripción Larga", payload.get("description"));
        assertEquals("Resumen Corto", payload.get("short_description"));
        assertEquals("0.5", payload.get("weight"));
 
        // Dimensions
        Map<String, String> dimensions = (Map<String, String>) payload.get("dimensions");
        assertNotNull(dimensions);
        assertEquals("30", dimensions.get("length"));
        assertEquals("20", dimensions.get("width"));
        assertEquals("10", dimensions.get("height"));
 
        // Tags
        List<Map<String, String>> tagsList = (List<Map<String, String>>) payload.get("tags");
        assertNotNull(tagsList);
        assertEquals(2, tagsList.size());
        assertEquals("camisa", tagsList.get(0).get("name"));
        assertEquals("moda", tagsList.get(1).get("name"));
 
        // Categories
        List<Map<String, String>> categoriesList = (List<Map<String, String>>) payload.get("categories");
        assertNotNull(categoriesList);
        assertEquals(1, categoriesList.size());
        assertEquals("Ropa", categoriesList.get(0).get("name"));
 
        // MetaData
        List<Map<String, Object>> metaList = (List<Map<String, Object>>) payload.get("meta_data");
        assertNotNull(metaList);
        assertEquals(2, metaList.size());
        assertEquals("_google_product_metadata", metaList.get(0).get("key"));
        assertEquals("google-seo", metaList.get(0).get("value"));
        assertEquals("_facebook_product_metadata", metaList.get(1).get("key"));
        assertEquals("facebook-pixel", metaList.get(1).get("value"));
 
        // Images
        List<Map<String, String>> imagesList = (List<Map<String, String>>) payload.get("images");
        assertNotNull(imagesList);
        assertEquals(1, imagesList.size());
        assertEquals("http://host.docker.internal:8080/images/dummy.webp", imagesList.get(0).get("src"));
    }
}
