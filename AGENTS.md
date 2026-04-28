# AGENTS Guide for `mod-rtac`

## Mission and API surface
- `mod-rtac` is a Vert.x/RMB Java 21 module that returns real-time availability for discovery clients.
- Primary API is `POST /rtac-batch` (`ramls/rtac-batch.raml`, `src/main/java/org/folio/rest/impl/RtacBatchResourceImpl.java`).
- Legacy `GET /rtac/{id}` is deprecated but still implemented (`ramls/rtac.raml`, `LegacyRtacGetByIdResourceImpl.java`).

## Request flow (read this first)
- Resource impls are thin: they delegate to `FolioFacade` and map failures via `ErrorMapper.handleError(...)`.
- `FolioFacade#getItemAndHoldingInfo(RtacRequest)` orchestrates calls in this order:
  1) inventory hierarchy, 2) loans, 3) requests, 4) pieces, 5) mapping to RTAC DTOs.
- Consortia-aware behavior:
  - Detect central tenant via `/user-tenants` (`UsersClient`).
  - For central tenants, find member tenants per instance via `/search/instances/facets` facet `holdings.tenantId` (`SearchClient`).
  - Merge per-tenant holdings/items/pieces by `instanceId` in `FolioFacade#mergeTenantData`.

## Integration points and downstream contracts
- Inventory: `POST /inventory-hierarchy/items-and-holdings` with `skipSuppressedFromDiscoveryRecords=true` (`InventoryClient`).
- Loans: `GET /loan-storage/loans` with CQL batches of 50 item IDs and `status.name==open` (`CirculationClient`).
- Requests: `GET /circulation/requests` and count only open statuses (`CirculationRequestClient`).
- Pieces: `GET /orders/pieces` filtered by `displayToPublic=true and displayOnHolding=true` (`PieceClient`).
- Settings: `GET /settings/entries` with `(scope==mod-rtac and key==LOAN_TENANT)` (`SettingsClient`, README settings section).

## Project-specific behavior to preserve
- Periodical logic is domain-critical: treat instances as periodicals when mode is `serial` or nature of content includes `journal`/`newspaper` (`FolioToRtacMapper`).
- `fullPeriodicals=false` returns holding-level data for periodicals; `true` includes full item mapping.
- Inventory hierarchy can return concatenated JSON objects (not a JSON array); parsing uses Vert.x `JsonParser.objectValueMode()` (`InventoryClientTests` documents this).
- Downstream `400` errors are intentionally exposed as `500` to RTAC clients (`ErrorMapper`, `RtacBatchResourceImplTest#rtacFailureCodes`).
- `LOAN_TENANT` setting lookup is cached for 10 minutes in Vert.x context cache `rtac-cache` (`Init`, `CirculationClient`).

## Build, test, and local run workflows
- Build + tests: `mvn clean verify`
- Fast test run: `mvn test`
- Run module locally after packaging: `java -Dport=8081 -jar target/mod-rtac-fat.jar -Dhttp.port=8081` (from `descriptors/DeploymentDescriptor-template.json`).
- Docker image expects fat jar in `target/*.jar` and exposes `8081` (`Dockerfile`).

## Code conventions in this repo
- Implement generated RMB interfaces under `src/main/java/org/folio/rest/impl`; RAML drives interfaces via `domain-models-maven-plugin`.
- Prefer async composition with Vert.x `Future`/`Promise`; on enrichment failures, many clients log warning and return partial inventory data instead of failing whole request.
- Keep headers tenant-aware (`X-Okapi-Tenant`, `X-Okapi-Token`, `X-Okapi-Url`) and use case-insensitive maps in tests.
- Formatting/lint baseline: `.editorconfig` (2-space indent, 100-char max), Google Checkstyle via Maven validate phase.

## Change checklist anchors
- If API, permissions, or dependencies change, update `ramls/*` and `descriptors/ModuleDescriptor-template.json` together.
- Add/update tests in both styles used here: module-level integration (`RtacBatchResourceImplTest` + `MockServer`) and client-level WireMock tests (`src/test/java/org/folio/clients/*Tests.java`).
- Update `NEWS.md` for user-visible behavior changes (see PR template checklist).

