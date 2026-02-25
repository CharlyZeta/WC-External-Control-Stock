package com.stockdual.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WcMetaData {
    private String key;
    private Object value;
}
