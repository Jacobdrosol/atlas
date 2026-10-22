package com.atlas.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record Product(
        String sku,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String currency,
        List<String> tags,
        int stock,
        Map<String, Object> attributes) implements Serializable {
}
