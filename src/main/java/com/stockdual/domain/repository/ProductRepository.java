package com.stockdual.domain.repository;

import com.stockdual.domain.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = { "category", "brand", "supplier" })
    List<Product> findAll();

    @EntityGraph(attributePaths = { "category", "brand", "supplier" })
    List<Product> findAll(Specification<Product> spec);

    @EntityGraph(attributePaths = { "category", "brand", "supplier" })
    List<Product> findAll(Specification<Product> spec, org.springframework.data.domain.Sort sort);

    @EntityGraph(attributePaths = { "category", "brand", "supplier" })
    List<Product> findAll(org.springframework.data.domain.Sort sort);

    @Query("select p from Product p left join fetch p.category " +
            "where lower(p.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.sku) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.gtin) like lower(concat('%', :searchTerm, '%'))")
    List<Product> search(@Param("searchTerm") String searchTerm);

    @Query("select sum(p.stockTotal) from Product p")
    Long sumTotalStock();

    @Query("select sum(p.stockTotal * p.cost) from Product p")
    java.math.BigDecimal sumInventoryValue();

    @Query("select count(p) from Product p where p.stockTotal != p.stockWc")
    Long countStockAlerts();

    java.util.Optional<Product> findBySku(String sku);

    @Query("select p from Product p where p.stockTotal != p.stockWc and p.wcId is not null")
    List<Product> findMismatchedProductsForSync();

    @Query("select p from Product p where p.wcId is null")
    List<Product> findUnlinkedProductsForCreation();

    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from Product p where p.wcId is null")
    void deleteAllUnlinkedProducts();

    long count();
}
