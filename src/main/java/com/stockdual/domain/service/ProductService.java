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

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    @Transactional
    public void deleteAllUnlinkedProducts() {
        productRepository.deleteAllUnlinkedProducts();
    }

    public void saveProduct(Product product) {
        if (product == null) {
            System.err.println("Product is null. Are you sure you have connected your form to the application?");
            return;
        }
        // Centralize business logic: Calculate total stock before saving
        product.setStockTotal((product.getStockLocal() != null ? product.getStockLocal() : 0) +
                (product.getStockDeposito() != null ? product.getStockDeposito() : 0) +
                (product.getStockGalpon() != null ? product.getStockGalpon() : 0));
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
