package com.stockdual.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "show_gtin")
    @Builder.Default
    private boolean showGtin = false;

    @Column(name = "show_category")
    @Builder.Default
    private boolean showCategory = false;

    @Column(name = "show_price")
    @Builder.Default
    private boolean showPrice = false;

    @Column(name = "show_cost")
    @Builder.Default
    private boolean showCost = false;

    @Column(name = "show_tags")
    @Builder.Default
    private boolean showTags = false;

    @Column(name = "image_size")
    @Builder.Default
    private Integer imageSize = 50;

    @Column(name = "name_column_width")
    @Builder.Default
    private String nameColumnWidth = "300px";

    @Column(name = "show_brand")
    @Builder.Default
    private boolean showBrand = false;

    @Column(name = "show_supplier")
    @Builder.Default
    private boolean showSupplier = false;

    @Column(name = "wc_api_url", length = 255)
    private String wcApiUrl;

    @Column(name = "wc_consumer_key", length = 255)
    private String wcConsumerKey;

    @Column(name = "wc_consumer_secret", length = 255)
    private String wcConsumerSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
