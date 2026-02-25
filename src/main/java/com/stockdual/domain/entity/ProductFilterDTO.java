package com.stockdual.domain.entity; // Using entity package for simplicity, but could be a DTO package

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
    private String searchText; // Will search in Name, SKU, and Tags
    private Category category;
    private Brand brand;
    private Supplier supplier;
    private String stockStatus; // "Con Stock", "Sin Stock"
    private String location; // "Local", "Depósito", "Galpón"
    private String priceOrder; // "Menor a Mayor", "Mayor a Menor"
}
