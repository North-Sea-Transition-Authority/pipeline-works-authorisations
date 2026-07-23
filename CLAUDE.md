# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Pipeline Works Authorisations (PWA) is a Spring Boot 3 / Java 21 web application in the Fivium "energy portal" family, used by the North Sea Transition Authority (NSTA/OGA) to manage pipeline works authorisation applications and consents — from application submission through regulator case processing to consent issue.

## Build & run

Frontend assets (SCSS/JS) must be built before the backend build will produce a working app, and depend on the `fivium-design-system-core` git submodule.

```bash
# One-time / after submodule updates
git submodule update --init --recursive --remote
cd fivium-design-system-core && npm install && npx gulp buildAll && cd ..

# Frontend build (SCSS -> CSS, JS babel transpile, copies FDS/govuk-frontend assets)
npm install
npx gulp buildAll        # = gulp sass + gulp babel; see gulpfile.js
```

Backend (Gradle):

```bash
./gradlew build                          # full build: compile, test, checkstyle, jacoco, war/jar
./gradlew test                           # run all tests (JUnit 5, 4 parallel forks)
./gradlew test --tests "uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestServiceTest"
./gradlew test --tests "*LocationDetailsServiceTest*"   # run a single test class by pattern
./gradlew checkstyleMain                 # style check (config: ide/checkstyle.xml, zero warnings allowed, fails build)
./gradlew jacocoTestReport                # coverage report
./gradlew bootRun                         # run the app (normally done via IntelliJ run config instead, see below)
```

CI (`.drone.yml`) runs, in order: fetch FDS submodule → build FDS → `npm install && npx gulp buildAll` → `./gradlew test jacocoTestReport checkstyleMain bootWar bootJar` → Docker image build/publish → Trivy scan.

### Local dev environment specifics

- App is normally run from IntelliJ (auto-detects the Spring Boot main class), not `gradlew bootRun`, because a large set of environment variables (SAML certs, S3, GOV.UK Notify/Pay keys, AWS SNS/SQS creds, DB schema) must be set on the run configuration — see README.md for the full table.
- Requires a local Camunda "fox4" engine for session sharing: `docker compose -f devtools-pwa/local-dev-compose.yml up`.
- Requires the `development, test-harness` Spring profiles active locally.
- Oracle DB; Flyway user (`pwa_xx_flyway`) must be created manually before first run — see README.md.
- Reachable at `http://localhost:8081/engedudev1/<CONTEXT_SUFFIX>/work-area` once running; auth redirects through the local fox instance.
- Checkstyle-IDEA plugin should be configured against `ide/checkstyle.xml` (version 8.40) — checkstyle failures fail the Gradle build (`maxWarnings = 0`).

## Architecture

### Two workflow engines running side by side

The application/case lifecycle is modeled by **two systems kept in sync manually, not automatically**:

1. **Domain state**: `PwaApplicationStatus` (`service/enums/pwaapplications/PwaApplicationStatus.java`) persisted on `PwaApplicationDetail`, changed via `PwaApplicationDetailService.updateStatus(...)`. This drives permission checks, URL access control, and most UI decisions.
2. **Camunda BPMN process instances**: BPMN definitions at `src/main/resources/workflow/*.bpmn` (`pwa.bpmn`, `pwa_app_consultations.bpmn`, `pwa_app_public_notice.bpmn`) contain only `userTask`s with `candidateGroups` — no service tasks, no Java delegates/listeners. All interaction is explicit, through `integrations/camunda/external/CamundaWorkflowService` (wraps `RuntimeService`/`TaskService`/`RepositoryService`). Domain entities correlate to process instances via the `WorkflowSubject` interface (business key = entity id). `WorkflowType` enum maps each BPMN process key to a `UserWorkflowTask` enum listing that workflow's task keys (e.g. `features/appprocessing/workflow/appworkflowmappings/PwaApplicationWorkflowTask`).

Feature services that transition a case (e.g. `features/appprocessing/tasks/initialreview/InitialReviewService`) call **both** the domain status update **and** `workflowService.completeTask(...)`/`setWorkflowProperty(...)` in the same method — there is no listener bridging the two, so when adding a new transition, update both explicitly. Task assignment/audit trail for Camunda tasks lives in `features/appprocessing/workflow/assignments/`.

### Package layout: legacy vs `features/`

The codebase is mid-migration from a traditional layered layout to vertical feature slices. Older code lives in flat top-level packages by *layer* (`controller/`, `service/`, `repository/`, `validators/`, `model/`, `domain/`); newer code lives under `features/<area>/` organized by *business capability*, each typically containing its own controller/service/form/validator/repository together. **Prefer the `features/` convention for new code.**

Top-level packages:
- `auth` — SAML2 authentication (EPAS IdP), `AuthenticatedUserAccount` principal, custom role-check annotations/interceptor.
- `component` — small reusable UI/backend helper components.
- `config` — Spring `@Configuration` / properties classes.
- `controller`, `service`, `repository`, `validators`, `model` — legacy layered code, organized internally by subarea (appprocessing, consultations, documents, etc.).
- `domain` — core JPA entities (`domain/pwa/...`, `domain/energyportal/...`).
- `exception` — app-wide custom exceptions.
- `externalapi` — REST endpoints/DTOs exposed to the wider Energy Portal ecosystem.
- `fds` — thin server-side glue for one Fivium Design System widget (search selector); unrelated to the design system submodule build.
- `hibernate`, `mvc` — Hibernate config/utilities; cross-cutting Spring MVC infra (error handling, argument resolvers, controller advice).
- `integrations` — outbound integrations: Camunda, Energy Portal, EPA, GOV.UK Notify, GOV.UK Pay.
- `teams` — team/role membership domain, synced from Energy Portal (`Team`, `Role`, `TeamMemberQueryService`).
- `user`, `usercontext` — user-account utilities and Energy Portal user-context glue.
- `util` — generic utilities (dates, currency, enum display, form inputs).

`features/` subpackages:
- `application` — building/submitting an application: authorisation/permissions, task-list, submission, summary, and `tasks/*` (one subpackage per application form section — location, HUOO, pipelines, crossings, deposits, etc.).
- `appprocessing` — regulator-side case processing after submission: task list, case management, consent issue, processing charges, Camunda workflow glue (`workflow/`).
- `consents` — viewing/serving issued consent documents/files.
- `consentdocumentmigration` — one-off tooling migrating legacy consent documents/CSVs.
- `datatypes` — reusable value types (coordinates, GeoJSON geometry).
- `email` — outbound email templates/properties per event, built on Fivium's notification library.
- `feedback`, `feemanagement`, `filemanagement`, `reassignment`, `termsandconditions`, `pwapay` (GOV.UK Pay), `analytics` — self-describing single-purpose feature areas.
- `generalcase` — shared "case" view concepts (task list entries, pipeline/ident diff views) reused across `application` and `appprocessing`.
- `mvcforms` — small shared form/view classes for reusable widgets.
- `webapp` — misc plumbing (devtools endpoints, footer).

### Form/controller convention (no custom wizard framework)

Multi-step application forms follow a consistent hand-rolled pattern rather than a generic wizard framework:
- **Form**: plain POJO, sometimes extending a shared base (e.g. `FileUploadForm` when the step includes uploads).
- **Validator**: separate class implementing Spring's `Validator` (see `docs/adr/0007-use-pojo-nested-validators-for-multi-value-form-inputs.md`).
- **Service**: orchestrates `mapEntityToForm(...)` / `validate(...)` / `saveEntityUsingForm(...)`.
- **Controller**: thin, one per task-step, guarded by custom annotations — `@PwaApplicationPermissionCheck`, `@PwaApplicationTypeCheck`, `@PwaApplicationStatusCheck` (`features/application/authorisation/context/`) — resolved by `PwaApplicationContextArgumentResolver` into a `PwaApplicationContext` method argument (holds the application detail, user, and resolved permissions). POST handlers use the shared idiom `controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {...save...; return redirect;})`.
- Step order is **not** hardcoded: each task controller redirects back through `PwaApplicationRedirectService.getTaskListRedirect(...)`, and the task-list feature determines what's next/available. Genuinely linear multi-page journeys use a dedicated `*JourneyController` with controller-scoped session attributes (`docs/adr/0012-use-controller-scoped-session-variables-for-multi-page-journeys.md`).

### Authorization — two layers, not Spring Security `@PreAuthorize`

1. **Team/role based**: `@HasAnyRole(roles = {...}, teamType = ...)` / `@HasAnyRoles` / `@HasAnyRoleByGroup` (package `auth`), enforced by `HasAnyRoleInterceptor` against the `teams/` model.
2. **Per-application permission based**: `@PwaApplicationPermissionCheck` / `@PwaApplicationStatusCheck` / `@PwaApplicationTypeCheck` / `@PwaResourceTypeCheck` / `@PwaApplicationNoChecks` (`features/application/authorisation/context/`), resolved into `PwaApplicationContext`.

New controllers touching an application/case should use both layers as appropriate rather than inventing new checks.

### Testing

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ. Naming: `<Class>Test.java` for units, `*IntegrationTest.java` for Spring-context tests (see `src/test/java/uk/co/ogauthority/pwa/integration/...`).
- No shared base test class — tests are Mockito-based POJO unit tests. Test data is built via per-feature `*TestUtil` static factory classes (e.g. `PwaContactTestUtil`, `PadOrganisationRoleTestUtil`, `AuthenticatedUserAccountTestUtil`) and occasional fluent `*FormBuilder` classes — follow this convention rather than hand-rolling fixtures inline or introducing a new builder style.
- `src/test/java/uk/co/ogauthority/pwa/architecture/ArchitectureTests.java` uses ArchUnit to enforce that any enum implementing `Displayable` (has `getDisplayName()`/`getDisplayOrder()`) also implements `MaterialisableEnum` — required so enum values sync into DB reference tables. Keep this in mind when adding new `Displayable` enums.
- checkstyleTest is not required to run for tests

### Database migrations (Flyway)

`src/main/resources/db/migration/`, two naming schemes:
- Versioned: `V<version>__<description>.sql`.
- Repeatable: `R__<NNNN>_<description>.sql` (zero-padded sequence controls reapply order) — almost all repeatable migrations define/refresh SQL **views** (organisation, team, consent, work-area, case-officer-assignment, etc.), consistent with `docs/adr/0002-avoid-lazy-loading.md`'s preference for views over lazy-loaded associations.

A Fivium fork of Flyway is used (`org.flywaydb:flyway-core:9.5.1-FIVIUM`).

### Architectural Decision Records

`docs/adr/` documents non-obvious conventions used throughout the codebase — read the relevant one before deviating from a pattern it covers:
- `0001` integers rather than entity mapping for audit users
- `0002` avoid lazy loading
- `0003` avoid passing form submit URLs to views
- `0004` foreign keys to tables outside the app schema
- `0005` use converters rather than pre/post-load
- `0006` access restriction investigation
- `0007` POJO nested validators for multi-value form inputs
- `0008` don't use `@PrePersist`/`@PreUpdate` with transient fields
- `0009` flash messages
- `0010` use `.ftlvariables` in templates
- `0011` prefer enum parameters over booleans
- `0012` controller-scoped session variables for multi-page journeys (also a second, differently-scoped `0012` on Spring message validation for type mismatches — duplicate numbering, both present)
- `0013` idempotency for POST requests
- `0014`/`0015` PWA API decisions
- `0016` handling PWA privileges in the "conversion teams" pattern
- `0017` migrating files from Oracle to S3 for the file-upload library
- `0018` uploading legacy consent documents

### Fivium Design System (FDS)

`fivium-design-system-core` is a git submodule (pinned via `.gitmodules` to a release branch) providing GOV.UK Design System-based Freemarker macros/SCSS/JS. Gulp copies its build output into `src/main/resources/templates/fds` (Freemarker) and `src/main/resources/public/assets/static/fds/...` (JS/images); `govuk-frontend` assets are copied similarly. The app's own SCSS (`src/main/resources/scss/*.scss`) compiles against the FDS include path. Do not hand-edit anything under `templates/fds` or `public/assets/static/fds` — it's generated; change the submodule or the app's own scss/js instead.
