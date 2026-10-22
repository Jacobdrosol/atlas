package com.atlas.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final ProductRepository products;

    public CatalogController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/products")
    public List<Product> products() {
        return products.findAll();
    }

    @GetMapping("/products/{sku}")
    public Product product(@PathVariable String sku) {
        Product product = products.findBySku(sku);
        if (product == null) {
            throw new NotFoundException("No product " + sku);
        }
        return product;
    }

    @GetMapping("/search")
    public SearchPage search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return products.search(q, category, maxPrice, page, size);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return products.categories();
    }
}
