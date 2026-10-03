package com.stockdual.infrastructure.client;

import com.stockdual.infrastructure.client.dto.WcBatchUpdateRequest;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
import com.stockdual.infrastructure.client.dto.WcProductCreateRequest;
import com.stockdual.infrastructure.client.dto.WcMetaData;
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

        HttpHeaders headers = createAuthHeaders(key, secret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    String.class);
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
        HttpHeaders headers = createAuthHeaders(key, secret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

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
        HttpHeaders headers = createAuthHeaders(key, secret);
        HttpEntity<WcBatchUpdateRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
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

        WcProductCreateRequest payload = WcProductCreateRequest.builder()
                .name(name)
                .type("simple")
                .status("draft")
                .sku(sku)
                .regularPrice(price != null ? price.toString() : null)
                .manageStock(stockQuantity != null ? true : null)
                .stockQuantity(stockQuantity)
                .build();

        HttpHeaders headers = createAuthHeaders(key, secret);
        HttpEntity<WcProductCreateRequest> entity = new HttpEntity<>(payload, headers);

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
            log.error("Failed to create draft product in WooCommerce: {}", e.getMessage());
            throw new RuntimeException("Error creating draft product", e);
        }
    }

    public Long createDraftProductExtended(String url, String key, String secret, Product product, String appBaseUrl) {
        String endpoint = formatEndpoint(url, "/wp-json/wc/v3/products");

        WcProductCreateRequest.WcProductCreateRequestBuilder payloadBuilder = WcProductCreateRequest.builder()
                .name(product.getName())
                .type("simple")
                .status("draft")
                .sku(product.getSku());

        if (product.getPrice() != null) {
            payloadBuilder.regularPrice(product.getPrice().toString());
        }

        if (product.getStockTotal() != null) {
            payloadBuilder.manageStock(true);
            payloadBuilder.stockQuantity(product.getStockTotal());
        }

        if (product.getDescription() != null) {
            payloadBuilder.description(product.getDescription());
        }

        if (product.getShortDescription() != null) {
            payloadBuilder.shortDescription(product.getShortDescription());
        }

        if (product.getWeight() != null) {
            payloadBuilder.weight(product.getWeight().toString());
        }

        // Dimensions
        WcProductCreateRequest.Dimensions dimensions = WcProductCreateRequest.Dimensions.builder()
                .length(product.getLength() != null ? product.getLength().toString() : "")
                .width(product.getWidth() != null ? product.getWidth().toString() : "")
                .height(product.getHeight() != null ? product.getHeight().toString() : "")
                .build();
        payloadBuilder.dimensions(dimensions);

        // Tags
        if (product.getTags() != null && !product.getTags().trim().isEmpty()) {
            java.util.List<WcProductCreateRequest.Tag> tagList = new java.util.ArrayList<>();
            for (String tag : product.getTags().split(",")) {
                if (!tag.trim().isEmpty()) {
                    tagList.add(WcProductCreateRequest.Tag.builder().name(tag.trim()).build());
                }
            }
            payloadBuilder.tags(tagList);
        }

        // Categories
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
        } else if (product.getWcCategory() != null) {
            categoryName = product.getWcCategory();
        }

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            java.util.List<WcProductCreateRequest.Category> catList = new java.util.ArrayList<>();
            catList.add(WcProductCreateRequest.Category.builder().name(categoryName.trim()).build());
            payloadBuilder.categories(catList);
        }

        // Metadata (Google and Facebook SEO Fields)
        java.util.List<WcMetaData> metaList = new java.util.ArrayList<>();
        if (product.getGoogleMetadata() != null && !product.getGoogleMetadata().trim().isEmpty()) {
            metaList.add(WcMetaData.builder()
                    .key("_google_product_metadata")
                    .value(product.getGoogleMetadata())
                    .build());
        }
        if (product.getFacebookMetadata() != null && !product.getFacebookMetadata().trim().isEmpty()) {
            metaList.add(WcMetaData.builder()
                    .key("_facebook_product_metadata")
                    .value(product.getFacebookMetadata())
                    .build());
        }
        if (!metaList.isEmpty()) {
            payloadBuilder.metaData(metaList);
        }

        // Image Mapping
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            java.util.List<WcProductCreateRequest.Image> imgList = new java.util.ArrayList<>();
            String absoluteImgUrl = product.getImageUrl();
            if (!absoluteImgUrl.startsWith("http")) {
                String baseUrl = (appBaseUrl != null) ? appBaseUrl : "http://host.docker.internal:8080";
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                absoluteImgUrl = baseUrl + (absoluteImgUrl.startsWith("/") ? "" : "/") + absoluteImgUrl;
            }
            imgList.add(WcProductCreateRequest.Image.builder().src(absoluteImgUrl).build());
            payloadBuilder.images(imgList);
        }

        WcProductCreateRequest payload = payloadBuilder.build();

        HttpHeaders headers = createAuthHeaders(key, secret);
        HttpEntity<WcProductCreateRequest> entity = new HttpEntity<>(payload, headers);

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
