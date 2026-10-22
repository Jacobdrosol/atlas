package com.atlas.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ProductRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<Product> findAll() {
        return jdbc.query("""
                SELECT document
                  FROM products
                 ORDER BY document->>'name'
                 LIMIT 100
                """, (rs, rowNum) -> parse(rs.getString("document")));
    }

    @Cacheable(cacheNames = "products", key = "#sku")
    public Product findBySku(String sku) {
        return jdbc.query("""
                SELECT document
                  FROM products
                 WHERE sku = ?
                """, (rs, rowNum) -> parse(rs.getString("document")), sku)
                .stream().findFirst().orElse(null);
    }

    public SearchPage search(String query, String category, BigDecimal maxPrice,
            int page, int size) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        if (normalizedQuery != null) {
            sql.append("""
                    WITH search AS (
                        SELECT websearch_to_tsquery('english', ?) AS q
                    )
                    SELECT p.document,
                           ts_rank(p.search_vector, search.q) AS rank,
                           COUNT(*) OVER() AS total
                      FROM products p
                      CROSS JOIN search
                     WHERE p.search_vector @@ search.q
                    """);
            args.add(normalizedQuery);
        } else {
            sql.append("""
                    SELECT p.document,
                           0.0 AS rank,
                           COUNT(*) OVER() AS total
                      FROM products p
                     WHERE 1 = 1
                    """);
        }

        if (category != null && !category.isBlank()) {
            sql.append(" AND p.document->>'category' = ?");
            args.add(category);
        }
        if (maxPrice != null) {
            sql.append(" AND (p.document->>'price')::numeric <= ?");
            args.add(maxPrice);
        }

        if (normalizedQuery != null) {
            sql.append(" ORDER BY rank DESC, p.document->>'name'");
        } else {
            sql.append(" ORDER BY p.document->>'name'");
        }
        sql.append(" LIMIT ? OFFSET ?");
        args.add(size);
        args.add((long) page * size);

        List<SearchRow> rows = jdbc.query(sql.toString(), (rs, rowNum) -> new SearchRow(
                parse(rs.getString("document")),
                rs.getLong("total")), args.toArray());

        long total = rows.isEmpty() ? 0 : rows.get(0).total();
        List<Product> items = rows.stream().map(SearchRow::product).toList();
        return new SearchPage(items, total, page, size);
    }

    public List<String> categories() {
        return jdbc.queryForList("""
                SELECT DISTINCT document->>'category' AS category
                  FROM products
                 ORDER BY category
                """, String.class);
    }

    private Product parse(String json) {
        try {
            return objectMapper.readValue(json, Product.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored product document is invalid JSON", ex);
        }
    }

    private record SearchRow(Product product, long total) {
    }
}
