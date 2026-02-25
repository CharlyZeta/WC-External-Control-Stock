package com.stockdual.infrastructure.client;

import com.stockdual.infrastructure.client.dto.WcBatchUpdateRequest;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
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

@Component
public class WooCommerceClient {

    private final RestTemplate restTemplate;

    public WooCommerceClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean testConnection(String url, String key, String secret) {
        if (url == null || key == null || secret == null) {
            return false;
        }

        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/");
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(key, secret));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            System.err.println("WooCommerce API Test failed: " + e.getMessage());
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
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(key, secret));

        try {
            while (hasMore) {
                String pagedUrl = UriComponentsBuilder.fromHttpUrl(endpoint)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .toUriString();

                ResponseEntity<List<WcProductResponse>> response = restTemplate.exchange(
                        pagedUrl,
                        HttpMethod.GET,
                        entity,
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
                        // Fallback if header is missing, though WP REST API should include it
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
            System.err.println("Failed to fetch products from WooCommerce: " + e.getMessage());
            throw new RuntimeException("Error fetching products from WooCommerce", e);
        }

        return allProducts;
    }

    /**
     * Sends a batch update to modify product stock quantities.
     */
    public boolean batchUpdateProducts(String url, String key, String secret, WcBatchUpdateRequest request) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products/batch");
        HttpEntity<WcBatchUpdateRequest> entity = new HttpEntity<>(request, createAuthHeaders(key, secret));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST, // Batch uses POST
                    entity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            System.err.println("Failed to execute batch update to WooCommerce: " + e.getMessage());
            throw new RuntimeException("Error executing batch update", e);
        }
    }

    public Long createDraftProduct(String url, String key, String secret, String name, String sku,
            java.math.BigDecimal price, Integer stockQuantity) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products");

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("name", name);
        payload.put("type", "simple");
        payload.put("status", "draft"); // Crucial requirement: created as draft
        payload.put("sku", sku);
        if (price != null) {
            payload.put("regular_price", price.toString());
        }
        if (stockQuantity != null) {
            payload.put("manage_stock", true);
            payload.put("stock_quantity", stockQuantity);
        }

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload, createAuthHeaders(key, secret));

        try {
            ResponseEntity<WcProductResponse> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    WcProductResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
            return null;
        } catch (RestClientException e) {
            System.err.println("Failed to create draft product in WooCommerce: " + e.getMessage());
            throw new RuntimeException("Error creating draft product", e);
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
