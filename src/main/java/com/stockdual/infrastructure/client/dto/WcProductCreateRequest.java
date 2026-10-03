package com.stockdual.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcProductCreateRequest {

    private String name;
    private String type; // e.g. "simple"
    private String status; // e.g. "draft"
    private String sku;

    @JsonProperty("regular_price")
    private String regularPrice;

    @JsonProperty("manage_stock")
    private Boolean manageStock;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    private String description;

    @JsonProperty("short_description")
    private String shortDescription;

    private String weight;
    private Dimensions dimensions;
    private List<Tag> tags;
    private List<Category> categories;

    @JsonProperty("meta_data")
    private List<WcMetaData> metaData;

    private List<Image> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimensions {
        private String length;
        private String width;
        private String height;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tag {
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String src;
    }
}
