package com.stockdual.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    private BigDecimal cost;

    @Column(length = 50)
    private String gtin;

    @Column(name = "stock_local")
    @Builder.Default
    private Integer stockLocal = 0;

    @Column(name = "stock_deposito")
    @Builder.Default
    private Integer stockDeposito = 0;

    @Column(name = "stock_galpon")
    @Builder.Default
    private Integer stockGalpon = 0;

    @Column(name = "stock_total")
    @Builder.Default
    private Integer stockTotal = 0;

    @Column(name = "stock_wc")
    @Builder.Default
    private Integer stockWc = 0;

    @Column(name = "wc_category", length = 200)
    private String wcCategory;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "wc_id", unique = true)
    private Long wcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(length = 1000)
    private String tags;

    @Column(length = 2000)
    private String attributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
