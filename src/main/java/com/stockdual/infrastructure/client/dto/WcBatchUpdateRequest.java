package com.stockdual.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WcBatchUpdateRequest {

    @JsonProperty("update")
    private List<WcProductUpdate> update;

    @Data
    public static class WcProductUpdate {
        private Long id;

        @JsonProperty("stock_quantity")
        private Integer stockQuantity;

        @JsonProperty("manage_stock")
        private Boolean manageStock = true;
    }
}
