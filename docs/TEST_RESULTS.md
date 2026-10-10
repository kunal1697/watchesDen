# Test Execution Results

Recorded on **9 Oct 2026** on macOS (JDK 19 compiling to release 17, Maven 3.9.9, Node 19.8).
CI re-runs every suite on each push. See the *Backend tests*, *Frontend tests* and *Functional tests (Newman)* checks
on the commit, plus the uploaded `backend-reports` artifact for JaCoCo coverage.

## Summary

| Suite | Command | Tests | Passed | Failed |
|---|---|---|---|---|
| Backend unit (surefire) | `cd backend && mvn verify` | 45 | 45 | 0 |
| Backend integration (failsafe) | same | 21 | 21 | 0 |
| Frontend unit/component (Vitest) | `cd frontend && npm test` | 18 | 18 | 0 |
| Functional E2E (Newman, via gateway, real PostgreSQL) | `npx newman run postman/…` | 43 requests / 116 assertions | 116 | 0 |

## Backend — unit tests (45)

| Module | Class | Tests |
|---|---|---|
| common | `JwtServiceTest`: round trip, tampering, wrong key, expiry, short secret | 5 |
| api-gateway | `RoutePolicyTest`: public auth/catalog/swagger, protected cart/orders, admin detection | 5 |
| user-service | `AuthServiceTest`: hashing + email normalisation, duplicate email, login ok/wrong/unknown, missing profile | 6 |
| product-service | `InventoryServiceTest`: option pricing, unavailable lines, non-customizable, unknown option, duplicate part, ordered reservation, insufficient stock, missing product, release | 9 |
| product-service | `ProductServiceTest`: create normalisation, 404s, soft delete, option grouping | 5 |
| order-service | `OrderServiceTest`: empty cart, snapshot + clear + event, compensation on failure, idempotent replay, racing duplicate key, cancel releases stock, cannot cancel shipped, owner scoping, state machine | 9 |
| order-service | `CartServiceTest`: order-insensitive merge, new line for new config, qty cap, unavailable product, subtotal excludes unavailable, empty cart | 6 |

## Backend — integration tests (21)

Full Spring context, Flyway migrations, MockMvc HTTP layer, H2 in PostgreSQL mode.

| Class | Scenarios | Tests |
|---|---|---|
| `AuthApiIT` | register → login (case-insensitive email) → profile; duplicate 409; field errors + correlation header; wrong password 401; seeded admin; missing identity 401 | 6 |
| `ProductApiIT` | pagination; text + price search; movement search; price sort; details + 404 body; collections + options; admin CRUD with RBAC; admin validation; **all-or-nothing reserve**; **20 concurrent reservations on stock 3 → exactly 3 succeed, stock 0**; customization quote; product photo URLs | 11 |
| `OrderApiIT` | full journey (add, merge, update, checkout, idempotent replay, empty cart, history, owner scoping, cancel + release); admin fulfilment with illegal transition; empty-cart checkout + nested validation; auth & ownership on cart | 4 |

```
api-gateway/target/surefire-reports      tests=5  failures=0 errors=0 skipped=0
common/target/surefire-reports           tests=5  failures=0 errors=0 skipped=0
order-service/target/surefire-reports    tests=15 failures=0 errors=0 skipped=0
product-service/target/surefire-reports  tests=14 failures=0 errors=0 skipped=0
user-service/target/surefire-reports     tests=6  failures=0 errors=0 skipped=0
order-service/target/failsafe-reports    tests=4  failures=0 errors=0 skipped=0
product-service/target/failsafe-reports  tests=11 failures=0 errors=0 skipped=0
user-service/target/failsafe-reports     tests=6  failures=0 errors=0 skipped=0
```

## Frontend (18)

```
 ✓ src/test/client.test.js (6 tests)       API client: token, errors, 401 hook, 204, network failure
 ✓ src/test/utils.test.js (4 tests)        INR formatting, image sizing, uuid
 ✓ src/test/components.test.jsx (5 tests)  ProductCard, StatusBadge, form validation
 ✓ src/test/Products.test.jsx (3 tests)    listing, search filters sent to API, error state

 Test Files  4 passed (4)
      Tests  18 passed (18)
```

Production build: `dist/assets/index-*.js 192 kB (61 kB gzip)`, `index-*.css 3.6 kB (1.2 kB gzip)`.

## Functional — Newman against the running stack

Stack: PostgreSQL 16, Eureka, gateway, user/product/order services. All traffic went through `http://localhost:8080`.

```
┌─────────────────────────┬───────────────────┬───────────────────┐
│                         │          executed │            failed │
├─────────────────────────┼───────────────────┼───────────────────┤
│              iterations │                 1 │                 0 │
│                requests │                43 │                 0 │
│            test-scripts │                86 │                 0 │
│      prerequest-scripts │                 2 │                 0 │
│              assertions │               116 │                 0 │
├─────────────────────────┴───────────────────┴───────────────────┤
│ total run duration: 4.2s                                        │
│ average response time: 86ms [min: 2ms, max: 908ms, s.d.: 189ms] │
└─────────────────────────────────────────────────────────────────┘
```

Folders: health & OpenAPI · auth (register, duplicate, invalid, login, wrong password, profile, admin login) · catalog
(paging + sort, search, filters, details, 404, collections, options) · cart (401, add, customized pricing, merge,
invalid option, update, view) · checkout & orders (validation, checkout, **idempotent replay**, empty cart, history,
details) · admin & security (customer 403, **spoofed role header 403**, invalid token 401, list, confirm, illegal
transition 409, cancel, product create / invalid / update / list / deactivate / hidden).

Swagger aggregation was also checked: `/swagger-ui.html` → 302 to the UI, which lists user-service (3 paths),
product-service and order-service (9 paths each). `/internal/**` returns **404** at the gateway (not exposed).

## CI/CD pipeline status

The workflow is in `.github/workflows/ci.yml` and runs on the first push to GitHub. Every stage it runs was executed
locally above. One difference: the Docker image build and `docker compose up` were not run locally because the Docker
daemon was unavailable on the build machine. The equivalent services were run as JARs against a local PostgreSQL 16
instead, and `docker compose config` validated the Compose file.
