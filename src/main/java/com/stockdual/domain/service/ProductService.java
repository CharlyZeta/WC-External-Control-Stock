package com.stockdual.domain.service;

import com.stockdual.domain.entity.Category;
import com.stockdual.domain.entity.Product;
import com.stockdual.domain.repository.CategoryRepository;
import com.stockdual.domain.repository.ProductRepository;
import com.stockdual.domain.entity.Brand;
import com.stockdual.domain.entity.Supplier;
import com.stockdual.domain.entity.ProductFilterDTO;
import com.stockdual.domain.repository.BrandRepository;
import com.stockdual.domain.repository.SupplierRepository;
import com.stockdual.domain.repository.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    public List<Product> findProductsBasedOnFilters(ProductFilterDTO filter) {
        if (filter == null) {
            return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
        Specification<Product> spec = ProductSpecification.filterBy(filter);
        return productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<Product> findAllWithFilters(Specification<Product> spec, Sort sort) {
        if (spec == null) {
            if (sort == null)
                sort = Sort.by(Sort.Direction.ASC, "id");
            return productRepository.findAll(sort);
        }
        if (sort == null)
            sort = Sort.by(Sort.Direction.ASC, "id");
        return productRepository.findAll(spec, sort);
    }

    public List<Product> findAllProducts(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return productRepository.findAll();
        } else {
            return productRepository.search(filterText);
        }
    }

    public long countProducts() {
        return productRepository.count();
    }

    public List<String> findAllWcCategories() {
        return productRepository.findAllWcCategories();
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    @Transactional
    public void deleteAllUnlinkedProducts() {
        productRepository.deleteAllUnlinkedProducts();
    }

    @Transactional
    public void deleteAllProducts() {
        productRepository.deleteAll();
    }

    @Transactional
    public int overwriteLocalStockWithWc() {
        List<Product> products = productRepository.findAll();
        int updatedCount = 0;
        for (Product p : products) {
            int wcStock = p.getStockWc() != null ? p.getStockWc() : 0;
            int currentLocal = p.getStockLocal() != null ? p.getStockLocal() : 0;
            int currentDep = p.getStockDeposito() != null ? p.getStockDeposito() : 0;
            int currentGal = p.getStockGalpon() != null ? p.getStockGalpon() : 0;
            int currentTotal = p.getStockTotal() != null ? p.getStockTotal() : 0;
            
            if (currentLocal != wcStock || currentDep != 0 || currentGal != 0 || currentTotal != wcStock) {
                p.setStockLocal(wcStock);
                p.setStockDeposito(0);
                p.setStockGalpon(0);
                p.setStockTotal(wcStock);
                updatedCount++;
            }
        }
        productRepository.saveAll(products);
        return updatedCount;
    }

    @Transactional
    public void saveProduct(Product product) {
        if (product == null) {
            System.err.println("Product is null. Are you sure you have connected your form to the application?");
            return;
        }

        // Calculate total stock
        int totalStock = (product.getStockLocal() != null ? product.getStockLocal() : 0) +
                (product.getStockDeposito() != null ? product.getStockDeposito() : 0) +
                (product.getStockGalpon() != null ? product.getStockGalpon() : 0);

        // If the product already exists, reload from DB and only update stock fields
        if (product.getId() != null) {
            Product managed = productRepository.findById(product.getId()).orElse(null);
            if (managed != null) {
                managed.setStockLocal(product.getStockLocal());
                managed.setStockDeposito(product.getStockDeposito());
                managed.setStockGalpon(product.getStockGalpon());
                managed.setStockTotal(totalStock);
                productRepository.save(managed);
                return;
            }
        }

        // New product: save directly
        product.setStockTotal(totalStock);
        productRepository.save(product);
    }

    @Transactional
    public void generateTestData() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    Category.builder().name("Ropa").build(),
                    Category.builder().name("Calzado").build(),
                    Category.builder().name("Accesorios").build()));
        }

        if (brandRepository.count() == 0) {
            brandRepository.saveAll(List.of(
                    Brand.builder().name("Nike").build(),
                    Brand.builder().name("Adidas").build(),
                    Brand.builder().name("Puma").build()));
        }

        if (supplierRepository.count() == 0) {
            supplierRepository.saveAll(List.of(
                    Supplier.builder().name("Distr. Textiles SA").build(),
                    Supplier.builder().name("Importadora Delta").build()));
        }

        if (productRepository.count() < 10) {
            List<Category> categories = categoryRepository.findAll();
            List<Brand> brands = brandRepository.findAll();
            List<Supplier> suppliers = supplierRepository.findAll();
            Random random = new Random();
            String[] names = { "Camiseta", "Pantalón", "Zapatillas", "Gorra", "Bufanda", "Chaqueta", "Calcetines",
                    "Cinturón" };
            String[] commonTags = { "verano", "invierno", "deporte", "casual", "oferta", "nuevo" };

            for (int i = 0; i < 20; i++) {
                String name = names[random.nextInt(names.length)] + " " + (100 + i);
                String randomTags = commonTags[random.nextInt(commonTags.length)] + ", "
                        + commonTags[random.nextInt(commonTags.length)];

                Product product = Product.builder()
                        .name(name)
                        .sku("SKU-" + (1000 + i))
                        .price(BigDecimal.valueOf(100 + random.nextInt(900)))
                        .cost(BigDecimal.valueOf(50 + random.nextInt(40)))
                        .category(categories.get(random.nextInt(categories.size())))
                        .brand(brands.get(random.nextInt(brands.size())))
                        .supplier(suppliers.get(random.nextInt(suppliers.size())))
                        .tags(randomTags)
                        .attributes("Color: Variado, Talla: M")
                        .description("Descripción automática para " + name)
                        .gtin("84000000" + (1000 + i))
                        .stockLocal(random.nextInt(50))
                        .stockDeposito(random.nextInt(100))
                        .stockGalpon(random.nextInt(200))
                        .build();

                product.setStockTotal(product.getStockLocal() + product.getStockDeposito() + product.getStockGalpon());

                // 30% chance to have a mismatch in stockWc for testing highlighting
                if (random.nextInt(10) < 3) {
                    product.setStockWc(product.getStockTotal() - 5 - random.nextInt(10));
                } else {
                    product.setStockWc(product.getStockTotal());
                }

                productRepository.save(product);
            }
        }
    }
}
