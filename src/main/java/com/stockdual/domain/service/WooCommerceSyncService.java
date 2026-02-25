package com.stockdual.domain.service;

import com.stockdual.domain.entity.Product;
import com.stockdual.domain.entity.UserSettings;
import com.stockdual.domain.repository.ProductRepository;
import com.stockdual.domain.repository.UserSettingsRepository;
import com.stockdual.infrastructure.client.WooCommerceClient;
import com.stockdual.infrastructure.client.dto.WcBatchUpdateRequest;
import com.stockdual.infrastructure.client.dto.WcProductResponse;
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
            for (WcProductResponse wcProduct : wcProducts) {
                if (wcProduct.getSku() == null || wcProduct.getSku().isEmpty()) {
                    continue; // Skip products without SKU
                }

                Optional<Product> productOpt = productRepository.findBySku(wcProduct.getSku());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setStockWc(wcProduct.getStockQuantity() != null ? wcProduct.getStockQuantity() : 0);
                    product.setWcId(wcProduct.getId());
                    productRepository.save(product);
                    updatedCount++;
                }
            }
            log.info("Finished sync FROM WooCommerce. {} local products updated.", updatedCount);
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
