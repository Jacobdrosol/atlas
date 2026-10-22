# Atlas

Local catalog search reference: JSONB documents, PostgreSQL full-text search,
GIN indexes, and a Valkey cache behind a read-only Spring Boot API.

Atlas is a portfolio/reference implementation, not a production catalog. It is
local and uses no cloud services.

## What it demonstrates

- A document-shaped product model stored in PostgreSQL `JSONB`.
- A generated `tsvector` column with weighted name, description, brand, and tags.
- GIN indexes for document containment and full-text search.
- Weighted ranking with `ts_rank`.
- Category and price filters alongside full-text search.
- Product-by-SKU caching with Valkey.
- Testcontainers integration tests against PostgreSQL and Valkey.

## Why JSONB and full-text search

A separate document database and search cluster would add two services and two
consistency problems. This reference deliberately uses one PostgreSQL instance
to show the tradeoff:

- documents can evolve without a migration for every attribute;
- GIN indexes and generated search vectors handle the query shape;
- transactional data and search stay in one system;
- the cache is separate and can be invalidated or removed.

That is not the right answer for every catalog. A dedicated search engine wins
when relevance tuning, language analysis, and independent scaling matter more
than operational simplicity. The case study should make that tradeoff explicit.

## Quick start

```sh
cp .env.example .env
docker compose up --build
```

API:

- Products: <http://localhost:8081/api/products>
- Search: <http://localhost:8081/api/search?q=wireless>
- Filter: <http://localhost:8081/api/search?category=audio&maxPrice=100>
- Categories: <http://localhost:8081/api/categories>
- One product: <http://localhost:8081/api/products/AUD-001>
- Swagger UI: <http://localhost:8081/swagger-ui.html>
- Health: <http://localhost:8081/actuator/health>

Stop:

```sh
docker compose down -v
```

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/products` | First 100 products |
| GET | `/api/products/{sku}` | One product, Valkey cached |
| GET | `/api/search` | Full-text search with filters |
| GET | `/api/categories` | Distinct categories |
| GET | `/actuator/health` | Health check |

Search parameters:

- `q`: web-style full-text query
- `category`: exact category filter
- `maxPrice`: inclusive numeric ceiling
- `page`: zero-based, default 0
- `size`: 1 to 50, default 10

## Tests

```sh
cd api
./mvnw verify
```

The integration test starts PostgreSQL and Valkey with Testcontainers and checks
the catalog, weighted full-text search, category/price filtering, exact lookup,
caching path, invalid pagination, and a missing SKU.

## Sample searches

```sh
curl -s 'http://localhost:8081/api/search?q=wireless'
curl -s 'http://localhost:8081/api/search?category=audio&maxPrice=100'
curl -s 'http://localhost:8081/api/products/AUD-001'
```

## Local-only scope

No cloud deployment, authentication, writes, or production data. The next step
is a case study around JSONB versus a document database, GIN/tsvector design,
ranking, cache invalidation, and query plans at a realistic row count.

## License

MIT for Atlas source. PostgreSQL is under the PostgreSQL License; Valkey is
BSD-3-Clause. See `THIRD_PARTY_NOTICES.md`.

## Case study

- [Case study](docs/CASE-STUDY.md)
- [Architecture diagram](docs/screenshots/atlas-architecture.png)
- [API capture](docs/screenshots/atlas-swagger.png)
