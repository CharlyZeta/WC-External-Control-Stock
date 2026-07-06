package com.stockdual.infrastructure.client;

import com.stockdual.infrastructure.client.dto.WcBatchUpdateRequest;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
import com.stockdual.domain.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class WooCommerceClient {

    private final RestTemplate restTemplate;

    public WooCommerceClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean testConnection(String url, String key, String secret) {
        if (url == null || key == null || secret == null) {
            log.warn("Connection test aborted: Missing URL or credentials.");
            return false;
        }

        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/system_status");
        log.info("Testing WooCommerce connection to: {}", endpoint);
        
        // Use Query String Auth for HTTP/Localhost
        String authUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("consumer_key", key)
                .queryParam("consumer_secret", secret)
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(authUrl, String.class);
            log.info("WooCommerce connection successful. Status: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("WooCommerce Auth/Client Error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("WooCommerce Server Error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.error("WooCommerce Connection Failed: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            return false;
        }
    }

    /**
     * Fetches all products from WooCommerce. Handles pagination automatically.
     */
    public List<WcProductResponse> getAllProducts(String url, String key, String secret) {
        List<WcProductResponse> allProducts = new ArrayList<>();
        int page = 1;
        int perPage = 100; // WC Max
        boolean hasMore = true;

        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products");

        try {
            while (hasMore) {
                String pagedUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .queryParam("consumer_key", key)
                        .queryParam("consumer_secret", secret)
                        .toUriString();

                ResponseEntity<List<WcProductResponse>> response = restTemplate.exchange(
                        pagedUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<WcProductResponse>>() {
                        });

                List<WcProductResponse> body = response.getBody();
                if (body != null && !body.isEmpty()) {
                    allProducts.addAll(body);

                    // Check headers for pagination
                    List<String> totalPagesHeader = response.getHeaders().get("X-WP-TotalPages");
                    if (totalPagesHeader != null && !totalPagesHeader.isEmpty()) {
                        int totalPages = Integer.parseInt(totalPagesHeader.get(0));
                        if (page >= totalPages) {
                            hasMore = false;
                        }
                    } else {
                        if (body.size() < perPage) {
                            hasMore = false;
                        }
                    }
                    page++;
                } else {
                    hasMore = false;
                }
            }
        } catch (RestClientException e) {
            log.error("Failed to fetch products from WooCommerce: {}", e.getMessage());
            throw new RuntimeException("Error fetching products from WooCommerce", e);
        }

        return allProducts;
    }

    /**
     * Sends a batch update to modify product stock quantities.
     */
    public boolean batchUpdateProducts(String url, String key, String secret, WcBatchUpdateRequest request) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products/batch");
        String authUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("consumer_key", key)
                .queryParam("consumer_secret", secret)
                .toUriString();

        HttpEntity<WcBatchUpdateRequest> entity = new HttpEntity<>(request);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Failed to execute batch update to WooCommerce: {}", e.getMessage());
            throw new RuntimeException("Error executing batch update", e);
        }
    }

    public Long createDraftProduct(String url, String key, String secret, String name, String sku,
            java.math.BigDecimal price, Integer stockQuantity) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products");
        String authUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("consumer_key", key)
                .queryParam("consumer_secret", secret)
                .toUriString();

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("name", name);
        payload.put("type", "simple");
        payload.put("status", "draft");
        payload.put("sku", sku);
        if (price != null) {
            payload.put("regular_price", price.toString());
        }
        if (stockQuantity != null) {
            payload.put("manage_stock", true);
            payload.put("stock_quantity", stockQuantity);
        }

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload);

        try {
            ResponseEntity<WcProductResponse> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.POST,
                    entity,
                    WcProductResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
            return null;
        } catch (RestClientException e) {
            log.error("Failed to create draft product in WooCommerce: {}", e.getMessage());
            throw new RuntimeException("Error creating draft product", e);
        }
    }

    public Long createDraftProductExtended(String url, String key, String secret, Product product, String appBaseUrl) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products");
        String authUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("consumer_key", key)
                .queryParam("consumer_secret", secret)
                .toUriString();

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("name", product.getName());
        payload.put("type", "simple");
        payload.put("status", "draft");
        payload.put("sku", product.getSku());

        if (product.getPrice() != null) {
            payload.put("regular_price", product.getPrice().toString());
        }

        if (product.getStockTotal() != null) {
            payload.put("manage_stock", true);
            payload.put("stock_quantity", product.getStockTotal());
        }

        if (product.getDescription() != null) {
            payload.put("description", product.getDescription());
        }

        if (product.getShortDescription() != null) {
            payload.put("short_description", product.getShortDescription());
        }

        if (product.getWeight() != null) {
            payload.put("weight", product.getWeight().toString());
        }

        // Dimensions
        java.util.Map<String, String> dimensions = new java.util.HashMap<>();
        dimensions.put("length", product.getLength() != null ? product.getLength().toString() : "");
        dimensions.put("width", product.getWidth() != null ? product.getWidth().toString() : "");
        dimensions.put("height", product.getHeight() != null ? product.getHeight().toString() : "");
        payload.put("dimensions", dimensions);

        // Tags
        if (product.getTags() != null && !product.getTags().trim().isEmpty()) {
            java.util.List<java.util.Map<String, String>> tagList = new java.util.ArrayList<>();
            for (String tag : product.getTags().split(",")) {
                if (!tag.trim().isEmpty()) {
                    java.util.Map<String, String> tagMap = new java.util.HashMap<>();
                    tagMap.put("name", tag.trim());
                    tagList.add(tagMap);
                }
            }
            payload.put("tags", tagList);
        }

        // Categories
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
        } else if (product.getWcCategory() != null) {
            categoryName = product.getWcCategory();
        }

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            java.util.List<java.util.Map<String, String>> catList = new java.util.ArrayList<>();
            java.util.Map<String, String> catMap = new java.util.HashMap<>();
            catMap.put("name", categoryName.trim());
            catList.add(catMap);
            payload.put("categories", catList);
        }

        // Metadata (Google and Facebook SEO Fields)
        java.util.List<java.util.Map<String, Object>> metaList = new java.util.ArrayList<>();
        if (product.getGoogleMetadata() != null && !product.getGoogleMetadata().trim().isEmpty()) {
            java.util.Map<String, Object> metaMap = new java.util.HashMap<>();
            metaMap.put("key", "_google_product_metadata");
            metaMap.put("value", product.getGoogleMetadata());
            metaList.add(metaMap);
        }
        if (product.getFacebookMetadata() != null && !product.getFacebookMetadata().trim().isEmpty()) {
            java.util.Map<String, Object> metaMap = new java.util.HashMap<>();
            metaMap.put("key", "_facebook_product_metadata");
            metaMap.put("value", product.getFacebookMetadata());
            metaList.add(metaMap);
        }
        if (!metaList.isEmpty()) {
            payload.put("meta_data", metaList);
        }

        // Image Mapping
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            java.util.List<java.util.Map<String, String>> imgList = new java.util.ArrayList<>();
            java.util.Map<String, String> imgMap = new java.util.HashMap<>();
            
            String absoluteImgUrl = product.getImageUrl();
            // If the image is locally served, construct the Docker-accessible address
            if (!absoluteImgUrl.startsWith("http")) {
                String baseUrl = (appBaseUrl != null) ? appBaseUrl : "http://host.docker.internal:8080";
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                absoluteImgUrl = baseUrl + (absoluteImgUrl.startsWith("/") ? "" : "/") + absoluteImgUrl;
            }
            imgMap.put("src", absoluteImgUrl);
            imgList.add(imgMap);
            payload.put("images", imgList);
        }

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload);

        try {
            ResponseEntity<WcProductResponse> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.POST,
                    entity,
                    WcProductResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
            return null;
        } catch (RestClientException e) {
            log.error("Failed to create extended draft product in WooCommerce: {}", e.getMessage());
            throw new RuntimeException("Error creating extended product in WooCommerce", e);
        }
    }

    private String formatEndpoint(String url, String path) {
        return (url.endsWith("/") ? url.substring(0, url.length() - 1) : url) + path;
    }

    private HttpHeaders createAuthHeaders(String key, String secret) {
        HttpHeaders headers = new HttpHeaders();
        String auth = key + ":" + secret;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        headers.set("Accept", "application/json");
        return headers;
    }
}
