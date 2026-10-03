package com.stockdual.infrastructure.client;
 
import com.stockdual.domain.entity.Category;
import com.stockdual.domain.entity.Product;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
import com.stockdual.infrastructure.client.dto.WcProductCreateRequest;
import com.stockdual.infrastructure.client.dto.WcMetaData;
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
        ArgumentCaptor<HttpEntity<WcProductCreateRequest>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
 
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
        HttpEntity<WcProductCreateRequest> capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        WcProductCreateRequest payload = capturedEntity.getBody();
        assertNotNull(payload);
 
        assertEquals("Camisa Premium", payload.getName());
        assertEquals("SKU-999", payload.getSku());
        assertEquals("1500", payload.getRegularPrice());
        assertEquals(25, payload.getStockQuantity());
        assertEquals("Descripción Larga", payload.getDescription());
        assertEquals("Resumen Corto", payload.getShortDescription());
        assertEquals("0.5", payload.getWeight());
 
        // Dimensions
        WcProductCreateRequest.Dimensions dimensions = payload.getDimensions();
        assertNotNull(dimensions);
        assertEquals("30", dimensions.getLength());
        assertEquals("20", dimensions.getWidth());
        assertEquals("10", dimensions.getHeight());
 
        // Tags
        List<WcProductCreateRequest.Tag> tagsList = payload.getTags();
        assertNotNull(tagsList);
        assertEquals(2, tagsList.size());
        assertEquals("camisa", tagsList.get(0).getName());
        assertEquals("moda", tagsList.get(1).getName());
 
        // Categories
        List<WcProductCreateRequest.Category> categoriesList = payload.getCategories();
        assertNotNull(categoriesList);
        assertEquals(1, categoriesList.size());
        assertEquals("Ropa", categoriesList.get(0).getName());
 
        // MetaData
        List<WcMetaData> metaList = payload.getMetaData();
        assertNotNull(metaList);
        assertEquals(2, metaList.size());
        assertEquals("_google_product_metadata", metaList.get(0).getKey());
        assertEquals("google-seo", metaList.get(0).getValue());
        assertEquals("_facebook_product_metadata", metaList.get(1).getKey());
        assertEquals("facebook-pixel", metaList.get(1).getValue());
 
        // Images
        List<WcProductCreateRequest.Image> imagesList = payload.getImages();
        assertNotNull(imagesList);
        assertEquals(1, imagesList.size());
        assertEquals("http://host.docker.internal:8080/images/dummy.webp", imagesList.get(0).getSrc());
    }
}
