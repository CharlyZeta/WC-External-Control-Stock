package com.stockdual.domain.service;

import com.stockdual.domain.entity.Product;
import com.stockdual.domain.entity.UserSettings;
import com.stockdual.domain.repository.ProductRepository;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.infrastructure.client.WooCommerceClient;
import com.stockdual.infrastructure.client.dto.WcBatchUpdateRequest;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
import com.stockdual.domain.entity.Brand;
import com.stockdual.domain.entity.Supplier;
import com.stockdual.domain.repository.BrandRepository;
import com.stockdual.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WooCommerceSyncService {

    private final WooCommerceClient wcClient;
    private final ProductRepository productRepository;
    private final UserSettingsRepository settingsRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    public void syncFromWooCommerce() {
        UserSettings settings = getSettings();
        if (!validateSettings(settings))
            return;

        try {
            log.info("Starting sync FROM WooCommerce...");
            List<WcProductResponse> wcProducts = wcClient.getAllProducts(
                    settings.getWcApiUrl(),
                    settings.getWcConsumerKey(),
                    settings.getWcConsumerSecret());

            log.info("Fetched {} products from WooCommerce.", wcProducts.size());

            int updatedCount = 0;
            int createdCount = 0;
            java.util.Set<String> wcSkus = new java.util.HashSet<>();

            for (WcProductResponse wcProduct : wcProducts) {
                if (wcProduct.getSku() == null || wcProduct.getSku().isEmpty()) {
                    continue; // Skip products without SKU
                }
                
                wcSkus.add(wcProduct.getSku());

                log.info("Processing {}, manageStock={}, stockQuantity={}", 
                        wcProduct.getSku(), wcProduct.getManageStock(), wcProduct.getStockQuantity());

                Optional<Product> productOpt = productRepository.findBySku(wcProduct.getSku());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setStockWc(wcProduct.getStockQuantity() != null ? wcProduct.getStockQuantity() : 0);
                    product.setWcId(wcProduct.getId());
                    // Sync price and name back from WooCommerce if they exist
                    if (wcProduct.getName() != null && !wcProduct.getName().isEmpty()) {
                        product.setName(wcProduct.getName());
                    }
                    if (wcProduct.getPrice() != null && !wcProduct.getPrice().isEmpty()) {
                        try {
                            product.setPrice(new java.math.BigDecimal(wcProduct.getPrice()));
                        } catch (NumberFormatException e) {
                            log.warn("Invalid price format from WooCommerce for SKU: {}", wcProduct.getSku());
                        }
                    }

                    if (wcProduct.getImages() != null && !wcProduct.getImages().isEmpty()) {
                        product.setImageUrl(wcProduct.getImages().get(0).getSrc());
                    } else {
                        product.setImageUrl(null);
                    }

                    if (wcProduct.getCategories() != null && !wcProduct.getCategories().isEmpty()) {
                        product.setWcCategory(wcProduct.getCategories().get(0).getName());
                    } else {
                        product.setWcCategory(null);
                    }

                    updateProductAttributes(product, wcProduct);

                    productRepository.save(product);
                    updatedCount++;
                } else {
                    // Create new local product
                    Product newProduct = new Product();
                    newProduct.setSku(wcProduct.getSku());
                    newProduct.setName(wcProduct.getName() != null && !wcProduct.getName().isEmpty() ? wcProduct.getName() : "Producto WooCommerce");
                    newProduct.setWcId(wcProduct.getId());
                    int wcStock = wcProduct.getStockQuantity() != null ? wcProduct.getStockQuantity() : 0;
                    newProduct.setStockWc(wcStock);
                    newProduct.setStockLocal(wcStock);
                    newProduct.setStockTotal(wcStock);
                    
                    if (wcProduct.getPrice() != null && !wcProduct.getPrice().isEmpty()) {
                        try {
                            newProduct.setPrice(new java.math.BigDecimal(wcProduct.getPrice()));
                        } catch (NumberFormatException e) {
                            newProduct.setPrice(java.math.BigDecimal.ZERO);
                        }
                    } else {
                        newProduct.setPrice(java.math.BigDecimal.ZERO);
                    }

                    if (wcProduct.getImages() != null && !wcProduct.getImages().isEmpty()) {
                        newProduct.setImageUrl(wcProduct.getImages().get(0).getSrc());
                    }

                    if (wcProduct.getCategories() != null && !wcProduct.getCategories().isEmpty()) {
                        newProduct.setWcCategory(wcProduct.getCategories().get(0).getName());
                    }
                    
                    updateProductAttributes(newProduct, wcProduct);
                    
                    productRepository.save(newProduct);
                    createdCount++;
                }
            }
            
            // Delete local products that no longer exist in WooCommerce (based on SKU)
            List<Product> allLocalProducts = productRepository.findAll();
            int deletedCount = 0;
            for (Product localProduct : allLocalProducts) {
                if (localProduct.getSku() != null && !localProduct.getSku().isEmpty() && !wcSkus.contains(localProduct.getSku())) {
                    productRepository.delete(localProduct);
                    deletedCount++;
                    log.info("Deleted local product with SKU {} as it no longer exists in WooCommerce.", localProduct.getSku());
                }
            }

            log.info("Finished sync FROM WooCommerce. Created: {}, Updated: {}, Deleted: {}.", createdCount, updatedCount, deletedCount);
        } catch (Exception e) {
            log.error("Error during sync FROM WooCommerce: {}", e.getMessage(), e);
            throw new RuntimeException("Sync from WooCommerce failed", e);
        }
    }

    public void syncToWooCommerce() {
        UserSettings settings = getSettings();
        if (!validateSettings(settings))
            return;

        try {
            log.info("Starting sync TO WooCommerce...");

            // 1. Create New Draft Products (Local products without wcId)
            List<Product> unlinkedProducts = productRepository.findUnlinkedProductsForCreation();
            if (!unlinkedProducts.isEmpty()) {
                log.info("Found {} unlinked local products. Creating as drafts in WooCommerce...",
                        unlinkedProducts.size());
                int createdCount = 0;
                for (Product p : unlinkedProducts) {
                    if (p.getSku() != null && !p.getSku().isEmpty()) {
                        try {
                            Long newWcId = wcClient.createDraftProduct(
                                    settings.getWcApiUrl(),
                                    settings.getWcConsumerKey(),
                                    settings.getWcConsumerSecret(),
                                    p.getName(),
                                    p.getSku(),
                                    p.getPrice(),
                                    p.getStockTotal());
                            if (newWcId != null) {
                                p.setWcId(newWcId);
                                p.setStockWc(p.getStockTotal());
                                createdCount++;
                            }
                        } catch (Exception e) {
                            log.error("Failed to create draft product for SKU: {}", p.getSku(), e);
                        }
                    } else {
                        log.warn("Skipping draft creation for product '{}' because SKU is missing.", p.getName());
                    }
                }
                productRepository.saveAll(unlinkedProducts);
                log.info("Successfully created {} new draft products in WooCommerce.", createdCount);
            }

            // 2. Batch Update Existing Linked Products
            List<Product> mismatchedProducts = productRepository.findMismatchedProductsForSync();

            if (mismatchedProducts.isEmpty()) {
                log.info("No products to batch sync TO WooCommerce (all match).");
                return;
            }

            WcBatchUpdateRequest request = new WcBatchUpdateRequest();
            List<WcBatchUpdateRequest.WcProductUpdate> updates = new ArrayList<>();

            for (Product p : mismatchedProducts) {
                WcBatchUpdateRequest.WcProductUpdate update = new WcBatchUpdateRequest.WcProductUpdate();
                update.setId(p.getWcId());
                update.setStockQuantity(p.getStockTotal());
                updates.add(update);
            }

            request.setUpdate(updates);

            boolean success = wcClient.batchUpdateProducts(
                    settings.getWcApiUrl(),
                    settings.getWcConsumerKey(),
                    settings.getWcConsumerSecret(),
                    request);

            if (success) {
                log.info("Successfully synced {} products TO WooCommerce.", updates.size());
                // Update local stockWc to match what we just sent
                for (Product p : mismatchedProducts) {
                    p.setStockWc(p.getStockTotal());
                }
                productRepository.saveAll(mismatchedProducts);
            } else {
                log.error("Failed to sync TO WooCommerce.");
                throw new RuntimeException("Batch update to WooCommerce failed");
            }

        } catch (Exception e) {
            log.error("Error during sync TO WooCommerce: {}", e.getMessage(), e);
            throw new RuntimeException("Sync to WooCommerce failed", e);
        }
    }

    private void updateProductAttributes(Product product, WcProductResponse wcProduct) {
        String wcSupplier = extractAttributeOrMeta(wcProduct, "proveedor", "supplier", "_supplier");
        String wcBrand = extractAttributeOrMeta(wcProduct, "marca", "brand", "_brand");
        String wcGtin = extractAttributeOrMeta(wcProduct, "gtin", "ean", "_gtin", "_ean");

        String supplierName = (wcSupplier != null && !wcSupplier.trim().isEmpty()) ? wcSupplier.trim() : "no disponible";
        Supplier supplier = supplierRepository.findByName(supplierName)
                .orElseGet(() -> supplierRepository.save(Supplier.builder().name(supplierName).build()));
        product.setSupplier(supplier);

        String brandName = (wcBrand != null && !wcBrand.trim().isEmpty()) ? wcBrand.trim() : "no disponible";
        Brand brand = brandRepository.findByName(brandName)
                .orElseGet(() -> brandRepository.save(Brand.builder().name(brandName).build()));
        product.setBrand(brand);

        product.setGtin((wcGtin != null && !wcGtin.trim().isEmpty()) ? wcGtin.trim() : "");
    }

    private String extractAttributeOrMeta(WcProductResponse wcProduct, String... keys) {
        if (wcProduct.getAttributes() != null) {
            for (WcProductResponse.WcAttribute attr : wcProduct.getAttributes()) {
                if (attr.getName() != null) {
                    for (String key : keys) {
                        if (attr.getName().toLowerCase().contains(key.toLowerCase())) {
                            if (attr.getOptions() != null && !attr.getOptions().isEmpty()) {
                                return attr.getOptions().get(0);
                            }
                        }
                    }
                }
            }
        }
        if (wcProduct.getMetaData() != null) {
            for (com.stockdual.infrastructure.client.dto.WcMetaData meta : wcProduct.getMetaData()) {
                if (meta.getKey() != null) {
                    for (String key : keys) {
                        if (meta.getKey().toLowerCase().contains(key.toLowerCase()) || meta.getKey().equalsIgnoreCase(key)) {
                            if (meta.getValue() instanceof String) {
                                return (String) meta.getValue();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private UserSettings getSettings() {
        return settingsRepository.findByUserId("admin")
                .orElseThrow(() -> new IllegalStateException("User settings not found."));
    }

    private boolean validateSettings(UserSettings settings) {
        if (settings.getWcApiUrl() == null || settings.getWcConsumerKey() == null
                || settings.getWcConsumerSecret() == null) {
            log.warn("WooCommerce credentials are not fully configured.");
            return false;
        }
        return true;
    }
}
