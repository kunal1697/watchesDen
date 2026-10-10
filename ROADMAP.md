# WatchesDen — Roadmap

Status legend: ✅ done · 🔄 in progress · ⏳ planned

| Phase | Theme | Target | Status |
|---|---|---|---|
| 1 | Assessment MVP: microservices, storefront, configurator, tests, CI | 14 Oct 2026 | ✅ |
| 2 | Event-driven order & inventory (Kafka, outbox) | Nov 2026 | ⏳ |
| 3 | Cloud deployment on AWS | Dec 2026 | ⏳ |
| 4 | Payments, notifications, hardening | Q1 2027 | ⏳ |
| 5 | Experience & growth (3D, AR, personalization) | Q2 2027 | ⏳ |

---

## Phase 1 — Assessment MVP (5 business days, due 14 Oct 2026) ✅

| Day | Deliverable | Status |
|---|---|---|
| 1 | Architecture, Maven multi-module, `common` library, Eureka, API gateway with JWT filter | ✅ |
| 2 | user-service (auth), product-service (catalog, search, options, admin CRUD, inventory), Flyway schemas + seed data | ✅ |
| 3 | order-service (cart, checkout saga, idempotency, cancellation, admin fulfilment, events) | ✅ |
| 4 | Simple React UI to exercise every API: products, detail, customize, cart, checkout, orders, admin | ✅ |
| 5 | Unit + integration + frontend tests, Postman/Newman suite, Docker Compose, GitHub Actions, README, technical doc | ✅ |

Exit criteria met: all minimum E-Commerce features, MVC in every service, REST APIs documented in Swagger, error
handling / validation / logging, passing unit + integration + functional tests, CI/CD pipeline.

## Phase 2 — Event-driven order & inventory ⏳

Goal: deepen the Use Case 2 focus areas (events, messaging, concurrency, scalability).

- [ ] Add **Kafka** (KRaft) to Compose; topics `order.events`, `inventory.events`
- [ ] **Transactional outbox** in order-service (`outbox` table written in the order transaction, relayed by a poller or Debezium), replacing the in-process `OrderEventListener`
- [ ] Automatic retry of failed stock compensations from the outbox (removes the manual `COMPENSATION FAILED` reconciliation)
- [ ] Time-boxed **stock reservations** (reserve → confirm on payment / expire after N minutes)
- [ ] **notification-service** consuming order events (email on placed / shipped / delivered)
- [ ] Resilience4j circuit breaker + retry (idempotent calls only) on `ProductClient`
- [ ] Redis cache for catalog reads and customization options
- [ ] Testcontainers (PostgreSQL + Kafka) for integration tests

## Phase 3 — Cloud deployment (AWS) ⏳

- [ ] Terraform: VPC, ECS Fargate services, ALB, RDS PostgreSQL (Multi-AZ), ECR, Secrets Manager
- [ ] Replace Eureka with ECS Service Connect / Cloud Map (keep the gateway)
- [ ] CloudFront + S3 for the SPA
- [ ] GitHub Actions **OIDC → AWS**, deploy job after `e2e` on `main`; staging → manual approval → production
- [ ] Blue/green deploys with automatic rollback on failed health checks
- [ ] Centralised logs (CloudWatch), metrics (Micrometer → Prometheus/Grafana), tracing (OpenTelemetry), correlation ID as trace attribute

## Phase 4 — Payments, notifications, hardening ⏳

- [ ] Payment provider integration (Stripe / Razorpay) with webhooks, plus a `PAYMENT_PENDING` order state
- [ ] Refunds on cancellation
- [ ] httpOnly refresh-token cookies + short-lived access tokens; logout / token revocation
- [ ] Email verification, password reset, rate limiting on auth endpoints (gateway `RequestRateLimiter` + Redis)
- [ ] mTLS or network policies between services; secrets rotation
- [ ] OWASP ZAP baseline scan, Dependabot and CodeQL in CI
- [ ] Tax and shipping-cost calculation, multi-currency

## Phase 5 — Experience & growth ⏳

- [ ] Rendered preview of custom builds (photo compositing per part)
- [ ] **AR try-on** (WebXR) from the product page
- [ ] Engraving text and limited-edition numbered builds in Customize Your Watch
- [ ] Shareable configuration links, wishlist, reviews
- [ ] Recommendations ("pairs well with") from order events
- [ ] Admin analytics dashboard (sales, top configurations, low-stock alerts)
- [ ] Lighthouse ≥ 95 performance and accessibility budget enforced in CI

## Risks & mitigations

| Risk | Mitigation |
|---|---|
| Distributed transaction failure between order and product services | Saga with compensation today; outbox + retries in phase 2 |
| Oversell under flash-sale load | Atomic conditional decrement (tested under contention); reservations with TTL in phase 2 |
| Gateway as single point of failure | Stateless; run ≥ 2 replicas behind the ALB in phase 3 |
| Basic UI may not suit real customers | Phase 5: proper storefront design (retail layout, product photography, carousel) on top of the same APIs |
