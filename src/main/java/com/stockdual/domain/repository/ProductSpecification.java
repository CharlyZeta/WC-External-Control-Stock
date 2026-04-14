package com.stockdual.domain.repository;

import com.stockdual.domain.entity.Product;
import com.stockdual.domain.entity.ProductFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterBy(ProductFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearchText() != null && !filter.getSearchText().isEmpty()) {
                String likePattern = "%" + filter.getSearchText().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                Predicate skuPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likePattern);
                Predicate tagsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("tags")), likePattern);
                predicates.add(criteriaBuilder.or(namePredicate, skuPredicate, tagsPredicate));
            }

            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                Predicate localCategoryPredicate = criteriaBuilder.equal(root.get("category").get("name"), filter.getCategory());
                Predicate wcCategoryPredicate = criteriaBuilder.equal(root.get("wcCategory"), filter.getCategory());
                predicates.add(criteriaBuilder.or(localCategoryPredicate, wcCategoryPredicate));
            }

            if (filter.getBrand() != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), filter.getBrand()));
            }

            if (filter.getSupplier() != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplier"), filter.getSupplier()));
            }

            if ("Con Stock".equals(filter.getStockStatus())) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockTotal"), 0));
            } else if ("Sin Stock".equals(filter.getStockStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("stockTotal"), 0));
                // You could also add criteriaBuilder.isNull(root.get("stockTotal")) just in
                // case
            }

            if ("Local".equals(filter.getLocation())) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockLocal"), 0));
            } else if ("Depósito".equals(filter.getLocation())) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockDeposito"), 0));
            } else if ("Galpón".equals(filter.getLocation())) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stockGalpon"), 0));
            }

            if ("Menor Precio".equals(filter.getPriceOrder())) {
                query.orderBy(criteriaBuilder.asc(root.get("price")));
            } else if ("Mayor Precio".equals(filter.getPriceOrder())) {
                query.orderBy(criteriaBuilder.desc(root.get("price")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> hasSkuOrNameLike(String searchText) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + searchText.toLowerCase() + "%";
            Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
            Predicate skuPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likePattern);
            return criteriaBuilder.or(namePredicate, skuPredicate);
        };
    }

    public static Specification<Product> hasCategory(com.stockdual.domain.entity.Category category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Product> hasBrand(com.stockdual.domain.entity.Brand brand) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("brand"), brand);
    }

    public static Specification<Product> hasSupplier(com.stockdual.domain.entity.Supplier supplier) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("supplier"), supplier);
    }

    public static Specification<Product> hasStockTotalGreaterThan(int amount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("stockTotal"), amount);
    }

    public static Specification<Product> hasStockTotalEquals(int amount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("stockTotal"), amount);
    }
}
