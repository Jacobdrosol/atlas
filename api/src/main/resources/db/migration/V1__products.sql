CREATE TABLE products (
    sku        TEXT PRIMARY KEY,
    document   JSONB NOT NULL,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(document->>'name', '')), 'A') ||
        setweight(to_tsvector('english', coalesce(document->>'description', '')), 'B') ||
        setweight(to_tsvector('english', coalesce(document->>'brand', '')), 'C') ||
        setweight(to_tsvector('english', coalesce(document->>'tags', '')), 'C')
    ) STORED
);

CREATE INDEX idx_products_document ON products USING GIN (document);
CREATE INDEX idx_products_search ON products USING GIN (search_vector);
CREATE INDEX idx_products_category ON products ((document->>'category'));
