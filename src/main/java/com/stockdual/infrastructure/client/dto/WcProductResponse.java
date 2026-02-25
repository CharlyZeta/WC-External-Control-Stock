package com.stockdual.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WcProductResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("name")
    private String name;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("manage_stock")
    private Boolean manageStock;

    @JsonProperty("price")
    private String price;

    @JsonProperty("regular_price")
    private String regularPrice;

    // Optional fields for cost tracking if using a specific WC plugin for costs
    @JsonProperty("meta_data")
    private java.util.List<WcMetaData> metaData;
}
