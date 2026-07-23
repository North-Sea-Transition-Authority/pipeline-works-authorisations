# Oracle → Postgres migration: high-level plan

**Date:** 2026-07-23
**Area:** whole application — database layer, cross-schema integrations, build/test infra

## Context and assumptions

PWA currently runs on a shared Oracle instance ("EDU") alongside several other Fivium/NSTA
applications' schemas (`decmgr`, `devukmgr`, `pedmgr`, `envmgr`, `securemgr`, `wellmgr`, plus a BI
reporting schema and a handful of consuming schemas — `appenv`, `bpmmgr`, `eemsmgr`, `passmgr`).
PWA reads cross-schema data via same-instance SQL views defined in Flyway repeatable migrations,
and other systems read PWA's own schema the same way (`GRANT SELECT` + `api_vw_*` views).

Target state, as scoped by the business:

- PWA gets its own Postgres database, with **no schema-level access to anything outside PWA's own
  schema**.
- Any data currently read cross-schema (organisations, teams/roles, people, DEVUK fields/facilities,
  PEARS/PED licensing, well data, portal user accounts) must instead be fetched via the **Energy
  Portal API**. `envmgr`'s environmental mapsets have no live consumer and need no API work at all
  — see section 1.
- **AWS DMS** is used to physically move PWA's own data from Oracle to Postgres.
- **The Energy Portal API endpoints needed to replace the external schemas PWA still depends on are
  assumed to already exist.** PWA-side work is therefore integration only (calling an existing API, replacing
  local view-backed JPA entities with API-client DTOs, re-pointing consumers) — not designing or
  building new API surface, and not waiting on another team to build one. This materially lowers
  both the effort and the risk of section 1 below compared to a scenario where the API had to be
  built alongside the migration.
- **Local dev/CI environment setup already has an established pattern** in the team (from prior
  work standing up non-Oracle services locally/in CI) — reused here rather than built from
  scratch, so that section is much smaller than a from-first-principles estimate would suggest.
- **Performance validation and regression testing are out of scope for this plan.** Performance
  will be validated during UAT rather than as a dedicated pre-UAT workstream, and regression
  testing is being run outside of the development team's scope. Both are real activities that still
  need to happen before go-live — they're just not counted in the estimates below, and not owned by
  this plan.

This plan is a high-level scoping exercise based on a full-repo survey (263 Flyway migration files,
201 JPA entities, the `integrations/`/`teams`/`externalapi` packages). It is meant to size the
problem and flag architectural decisions that need to be made early, not to be a detailed design.
**Effort figures are broad, single-engineer-day order-of-magnitude estimates for the PWA-side
engineering work only** — they exclude other teams' work (Energy Portal API build-out, DBA/infra
provisioning, downstream-consumer changes), performance validation and regression testing (see
above), and PM/coordination overhead. Treat every number as ±40%.

## Summary table

| # | Section | Effort (days) | Risk |
|---|---|---|---|
| 1 | Cross-schema data access → Energy Portal API **integration** (APIs assumed to already exist; only 5 of the 6 external schemas need it) | 15–24 | High |
| 2 | Search/listing views that join cross-schema data (work area, consent search, case officer assignment, org filters) — batch API stitching | 12–20 | High |
| 3 | Downstream consumers of PWA's own schema (PETS, UKSS, BI, eemsmgr) | 10–15 (PWA side) | High (cross-team) |
| 4 | Flyway DDL migration (schema/type conversion) | 10–15 | Medium |
| 5 | PL/SQL packages, procedures & one-off migration blocks (`R__0000`–`R__0025` cluster confirmed dropped) | 3–5 | Medium |
| 6 | Own-schema view rewrites (mostly `CONNECT BY` → recursive CTE; `ROWNUM`/`DUAL` are quick) | 5–8 | Medium |
| 7 | Vendored/library schemas (Spring Session, Quartz, Fivium starters) | 2–3 | Low |
| 8 | Java native queries, sequences, Hibernate/driver config | 6–10 | Low |
| 9 | BLOB/LOB handling (`DocgenRun`, legacy file metadata — both columns confirmed dead) | 0.5 | Low |
| 10 | AWS DMS setup, rehearsal migrations & cutover (big-bang cutover, no CDC; LOB tuning minimal — see section 9) | 10–13 | High |
| 11 | Test infrastructure (Postgres-based integration tests) | 8–12 | Medium |
| 12 | Local dev / CI environment updates (existing pattern already in place) | 1 | Low |
| 13 | Documentation & ADR updates | 0.5 | Low |
| | **Total** | **~83–127 days** | |

Performance validation and regression testing are deliberately excluded from this table — see
"Context and assumptions" above; they happen during UAT and outside the development team's scope
respectively.

This is roughly **4.15–6.35 engineer-months** of PWA-side effort if executed serially by one person;
several sections (4–9, 11–12) can run in parallel once the target Postgres schema shape is agreed,
so realistic wall-clock time with a small team is considerably shorter than the sum.

---

## Timeline with a 3-engineer team

Dividing ~83–127 person-days by 3 gives ~28–42 person-days of pure capacity, which would suggest
~6–8 weeks if work parallelized perfectly. It won't quite get there: section 4 (schema baseline)
blocks most other Postgres-specific work, section 6's sequential chain (view rewrites → PL/SQL
triage → test infra → BLOB verification) doesn't split well across people, and parts of section 3/
section 10 depend on external teams whose calendars don't compress just because PWA adds engineers.
With performance validation and regression testing out of scope for this plan (see "Context and
assumptions"), the tail end of the schedule is short — there's no dedicated hardening phase to
speak of, just final cutover mechanics. A realistic phased plan:

| Phase | Elapsed | Focus | Rough allocation |
|---|---|---|---|
| 0 — Foundations | 1.5–2 weeks | Postgres schema baseline (section 4), vendored library swap (section 7), driver/Hibernate config (section 8), local dev/CI Postgres (section 12, quick given the existing pattern), DMS/SCT tooling stood up (start of section 10), API integration spike per external schema (start of section 1) | A: sections 4, 7, 8. B: section 10 setup. C: section 1 spike + section 12 |
| 1 — Core build | 4–6 weeks | Cross-schema → EP API integration (section 1); own-schema view/PL-SQL/test-infra chain is the longer pole | A+C: section 1, split by domain (`devukmgr`/`wellmgr`/`decmgr` orgs/people/team-role model first — all low-to-medium risk — then the remaining higher-risk `pedmgr`, `securemgr` domains) — done in ~3–4 weeks at 2 FTE. B: own-schema view rewrites (section 6, mostly quick bar the recursive CTEs) → PL/SQL triage (section 5, `R__0000`–`R__0025` cluster confirmed dropped) → Postgres integration test infra (section 11) → BLOB/LOB verification (section 9, half a day), in sequence — this chain, not section 1, sets the phase length |
| 2 — Integration & cutover prep | 4–6 weeks | Search/listing view redesign (section 2, needs section 1 domains landed), downstream-consumer replacement APIs (section 3), DMS rehearsal migrations (section 10), docs/ADR (section 13) | A+C move onto section 2 as soon as their section 1 domains land (partial overlap with phase 1). B: section 3 + section 10 rehearsals |
| 3 — Cutover | 1–2 weeks | Final DMS cutover + rollback rehearsal (section 10) | All three, converging |

**Total: ~10–16 weeks (roughly 3–4 months) wall-clock**, versus ~4.15–6.35 months if done by one
engineer serially — i.e. 3 engineers buy something like a 1.7–2x speedup here, well short of a
full 3x, because the fixed-length foundations (phase 0) and cutover (phase 3) phases don't
compress with headcount, and section 6's sequential chain (view rewrites → PL/SQL triage → test
infra → BLOB verification) can't be split across people. This excludes whatever calendar time UAT
(performance validation) and the separately-owned regression testing effort add on top, since those
sit outside this plan.

One thing can still stretch this regardless of headcount:
- **External dependencies** — PETS/UKSS/BI teams' own timelines for section 3, DBA/infra
  provisioning for section 10 — sit outside PWA's control and are the most likely remaining source
  of schedule slip.

---

## 1. Cross-schema data access → Energy Portal API integration

**Effort: 15–24 days, assuming all required API endpoints already exist. The largest single
integration effort in this plan, but not the highest architectural risk, since no new API
design/build or cross-team wait is involved.**

Five external schemas need a live Energy Portal API integration. They're currently read directly
via cross-schema views feeding ~35 JPA `@Immutable` entities under
`integrations/energyportal/**`. With an existing API assumed for all of them, the work per domain
is: confirm the API already returns the fields the local view currently exposes, replace the
`@Immutable`-entity-over-local-view pattern with API-client calls returning DTOs, and re-point the
internal repositories/services that currently query these local views — no contract design or
external build work.

| External schema | PWA domain area | Integration notes |
|---|---|---|
| `decmgr` — organisation units/groups | `integrations/energyportal/organisations/` | `integrations/epa` GraphQL client (`OrganisationApi`) already covers some of this — confirm full field parity against `portal_organisation_units`/`portal_organisation_groups`/`portal_org_unit_detail`, then swap over. |
| `decmgr` — resource/team/role model | `integrations/energyportal/teams/external/PortalTeamAccessor.java` | Confirmed via code search that `PortalTeamAccessor` (the only consumer of the `PortalTeam*` entity family) is wired into exactly one place — `auth/saml/SamlResponseParser.java`, which fetches a person's portal system privileges once at SAML login time. PWA's ongoing `@HasAnyRole` authorization checks (`HasAnyRoleInterceptor`) go through the separate, PWA-owned `Team`/`TeamRole` model (top-level `teams/` package) instead, which is not cross-schema and isn't affected by this migration at all. This is a single, well-isolated integration point, not an app-wide dependency. |
| `decmgr` — people | `integrations/energyportal/people/` | Check `epa` `UserApi` coverage, swap over. |
| `devukmgr` — fields/facilities | `integrations/energyportal/devukfields/`, `devukfacilities/` | `epa` `FieldApi` likely already covers this — straightforward swap-over. |
| `pedmgr` — PEARS/PED licensing | `integrations/energyportal/pearslicensing/`, `pearslicenceapplications/` | Dedicated legacy domain — confirm API field coverage, then swap over. |
| `securemgr` — portal user accounts/sessions | `integrations/energyportal/webuseraccount/` | Auth-adjacent — needs careful review since SAML2/EPAS is the actual authentication path; this may only be account *lookup*, not authentication itself, but must be confirmed. |
| `wellmgr` — well data | (consumer not yet located) | Small surface (`api_extant_wellbores` only) — low effort once located. |

Two schemas need no API replacement at all:
- **`decmgr` — legacy pipeline authorisations** (`pipeline_authorisations`, `xview_pipeline_auth_details`, `xview_pipelines_history`, `xview_pipeline_company_hist`): the only consumers of these tables are the `mig_*` staging views in `R__0000_migration_data.sql`, which are dropped along with the rest of the `R__0000`–`R__0025` cluster (see section 5). No Java code references any of this.
- **`envmgr` — environmental mapsets** (`xview_env_mapsets`): the only reference anywhere in the codebase is inside `R__0002_migration_package.sql`, also dropped as part of that same cluster. The separate `envmgr.st.split(...)` string-helper call inside `R__0025_energy_portal_team_management_package.sql` (also dropped) requires no substitute either.

**Recommended approach:** work through the remaining domains roughly in order of risk (lower-risk/
likely-already-covered domains first: organisations, fields/facilities, people, well data,
team/role model; higher-risk domains last: PED licensing, secure/user accounts, where
usage/consumers are less certain), doing a quick field-parity check against each existing local
view before starting the swap, so any genuine API gap is caught early rather than discovered
mid-implementation.

## 2. Search/listing views that join cross-schema data

**Effort: 12–20 days. High risk — touches the main work-area/search screens.**

Several of PWA's *own*-schema views (work-area search, consent search, case-officer assignment,
organisation filters, fee views) currently `JOIN` directly against the local `portal_*`/`devuk_*`
views from section 1 (e.g. `R__0059_workarea_helper_views.sql`, `R__0100_consent_search_view.sql`,
`R__0120_case_officers_assignments_views.sql`, `R__0121_add_organisation_unit_view.sql`). Once
those local views disappear (replaced by API calls), these listing/search screens can no longer do
a single SQL join to enrich rows with organisation/team names — a naive per-row API call in a
paginated search grid would be an N+1 performance problem.

**Decided approach: batch API stitching** — run the PWA-only part of each search/listing query as
before, collect the distinct set of organisation/team/etc. IDs across the result page, issue a
single batched Energy Portal API call per ID set, and stitch the returned data onto the rows in the
application layer. This avoids building a scheduled sync/cache layer (the alternative considered),
at the cost of needing every affected view's consuming service to be reworked to do a two-step
fetch-then-stitch instead of a single SQL query. Each affected screen needs its batch-fetch
integrated and its pagination behaviour re-verified (batching must key off the *current page's* ID
set, not the full result set, to keep it cheap), plus re-testing since these are core
work-area/search/consent screens.

## 3. Downstream consumers of PWA's own schema

**Effort: 10–15 days PWA-side (excludes other teams' work). High risk — cross-team dependency.**

`R__0090_external_access_api_views.sql` currently grants `SELECT` on 8 `api_vw_*` views (built
from PWA's own schema) to `decmgr`, `envmgr` (PETS, `WITH GRANT OPTION`), `passmgr` (UKSS, `WITH
GRANT OPTION`), `bpmmgr`, `eemsmgr`, `appenv`. A separate BI/reporting schema is granted access to
the entire PWA schema via a dynamic `all_objects` grant loop
(`afterMigrate__002_grant_access_to_bi_schema.sql`).

Once PWA is on an isolated Postgres instance, all of these consumers need an alternative. PWA
already has an `externalapi` package ("REST endpoints/DTOs exposed to the wider Energy Portal
ecosystem" per project conventions) as the natural home for replacement endpoints covering the 8
`api_vw_*` views. The BI/reporting use case is a separate conversation — likely a scheduled
export/replication feed rather than an API, and needs its own decision.

**This section cannot be scoped precisely from the PWA codebase alone** — it needs input from the
PETS, UKSS, and BI reporting teams on what they actually consume and how they'd like to receive it
going forward. Flag early: this has the potential to become a scheduling dependency that blocks
PWA's cutover date.

## 4. Flyway DDL migration (schema/type conversion)

**Effort: 10–15 days.**

232 versioned migration files, dominated by `VARCHAR2`/`NUMBER`/`CLOB`/`BLOB` column types (124,
14, 12, 7 files respectively) plus 8 `CREATE SEQUENCE` files. Recommended approach: don't hand-port
232 historical migrations — since AWS DMS (section 10) will carry over the actual data, only the
**current end-state schema shape** matters for the new Postgres database. Use a schema-conversion
tool (e.g. AWS Schema Conversion Tool) to generate a first-pass Postgres DDL baseline from the
current Oracle schema, review/clean up the automated output, and Flyway-baseline the new Postgres
migration history from there. Historical Oracle migrations stay in the repo for audit trail; new
migrations from that point are authored directly in Postgres syntax.

The one exception requiring hand attention: `V0.1__Create_schema.sql` (tablespaces, profiles,
quotas) has no Postgres equivalent and needs a from-scratch Postgres role/schema bootstrap script.

## 5. PL/SQL packages, procedures & one-off migration blocks

**Effort: 3–5 days.**

The repeatable-migration range `R__0000` through `R__0025` is confirmed dropped as part of the
Postgres cutover, not ported:
- `R__0000_migration_data.sql` — the `mig_*` staging views.
- `R__0001_migration_logger_package.sql` and `R__0002_migration_package.sql` (1,153 lines) — the
  one-off legacy-to-PWA data migration that transforms `mig_*` rows into master-PWA/pipeline/
  consent/as-built data, moot once DMS carries that data across directly, plus its logging helper.
- `R__0010_energy_portal_organisation_views.sql` and `R__0020_energy_portal_team_views.sql` — the
  `decmgr`-backed organisation/team views that section 1's API integration replaces.
- `R__0025_energy_portal_team_management_package.sql` — the team management package.

That accounts for all 3 packages in this section, plus the two view-definition files; none of it
needs a runtime-caller check or a Postgres rewrite.

**What's left is triage, not translation**: the ~27 anonymous PL/SQL blocks outside the 0000–0025
range (mostly historical one-off data seeds/fixes) are one-time data migrations whose *effects*
will already be present in the data DMS carries over — they don't need porting at all. The
remaining handful of standalone functions/procedures (e.g.
`V193__Terms_and_Conditions_historical_function.sql`) need a "does anything in `src/main/java`
still invoke this at runtime" check — only genuinely runtime-invoked logic needs a real rewrite (as
PL/pgSQL or, preferably, moved into the Java service layer where it's more testable and consistent
with the rest of the codebase).

## 6. Own-schema view rewrites

**Effort: 5–8 days.**

~37 views across ~23 repeatable-migration files that don't touch external schemas. Most of these
need no real rewrite — plain `SELECT`/`JOIN` views that are already ANSI-SQL-compatible, just
needing a compile-check against Postgres. The genuine work is concentrated in a small number of
Oracle-only constructs:
- `CONNECT BY ... PRIOR ... START WITH ... ORDER SIBLINGS BY` (2 recursive clause-tree views in
  `R__0080_clause_views.sql`, used by document generation) → Postgres recursive CTEs
  (`WITH RECURSIVE`). This is the one non-trivial piece of this section — recursive CTEs are a
  different enough shape from `CONNECT BY` that they need writing (not just syntax substitution)
  and careful testing, since these views feed document generation.
- `ROWNUM` (`R__0100_consent_search_view.sql`) → `row_number() OVER (...)`, a mechanical one-view
  change.
- `FROM DUAL` (9 files) → drop entirely, a mechanical find-and-remove across those files.

Budget most of the effort for the two recursive clause-tree views and their regression testing
(central to generated consent documents); the `ROWNUM`/`DUAL` fixes and the compile-check pass over
the remaining ~34 views are comparatively quick.

## 7. Vendored/library schemas

**Effort: 2–3 days. Low risk — mostly config flips.**

- Spring Session (`V0.2__Create_session_store.sql`) and Quartz (`V132__quartz_tables.sql`) schemas
  were hand-copied from each library's own Oracle-flavoured sample SQL. Both libraries ship
  official Postgres equivalents (`schema-postgresql.sql`, `tables_postgres.sql`) — swap to those
  directly rather than hand-translating.
- Camunda auto-manages its own `ACT_*` schema per JDBC vendor at boot — no action needed.
- The three Fivium starter libraries in use (`file-upload-spring-boot-starter`,
  `digital-notification-library-spring-boot-starter`,
  `digital-enum-materialisation-library-spring-boot-starter`) **already ship Postgres migrations as
  their primary/default target** — this is a config-property change
  (`file-upload.flywayVendor`, `digital-notification-library.flyway-vendor`,
  `enum-materialisation.flywayVendor` from `oracle` to `postgresql`/unset), not new SQL authoring.

## 8. Java native queries, sequences, Hibernate/driver config

**Effort: 6–10 days. Low risk — small, well-isolated surface.**

- Only 1 `@Query(nativeQuery = true)` (`PwaApplicationRepository.getNextRefNum`) and 4
  `entityManager.createNativeQuery(...)` call sites across 2 files, all sequence
  `.nextval ... FROM dual` patterns or a single `TO_CHAR(..., 'YYYY')` — mechanical rewrites
  (`nextval('seq_name')`, `EXTRACT(YEAR FROM ...)`).
- 6 files use `@SequenceGenerator`/`GenerationType.SEQUENCE`, all `allocationSize = 1` (real DB
  sequence per insert, no Hibernate pooling) — straightforward `CREATE SEQUENCE` equivalents.
- Driver swap: `com.oracle.database.jdbc:ojdbc11` → `org.postgresql:postgresql` in `build.gradle`;
  `spring.datasource.driver-class-name` and `database.url` in each `application-*.properties`.
- No explicit `hibernate.dialect` is set anywhere (auto-detected from driver) — needs a smoke test,
  particularly around `hibernate.type.preferred_instant_jdbc_type=TIMESTAMP`, which is an explicit
  Oracle-timezone-semantics workaround in `application.properties` that needs re-validating against
  Postgres `timestamp`/`timestamptz` behaviour.
- Verify `hibernate.jdbc.batch_size=100` batch-insert behaviour still holds with real Postgres
  sequences (this was the original reason for switching off Hibernate identity columns per
  `V37__user_sequences_not_identity_columns...sql`).

## 9. BLOB/LOB handling

**Effort: 0.5 days. Low risk — both known BLOB columns are dead, not an open design decision;
remaining work is just confirming that in the data, not building anything.**

- `DocgenRun.generatedDocument` (`@Lob Blob`, table `docgen_runs`) is dead: tracing the real docgen
  flow (`DocgenService.processAndCompleteConsentRun`/`processAndCompletePreviewRun` →
  `documentCreationService.createConsentDocument(...)` → `handleDocumentUpload(...)` →
  `fileService.upload(...)`, the S3-backed `file-upload-spring-boot-starter`) confirmed generated
  documents are saved via the file-upload library, not onto `DocgenRun`. A repo-wide search for
  `getGeneratedDocument()`/`setGeneratedDocument()` turns up no callers outside `DocgenRun.java`
  itself (just its own getter/setter/`equals`/`hashCode`) — an orphaned leftover from before
  ADR-0017's move to S3. There's no S3-vs-`bytea` decision to make; it can simply be dropped rather
  than migrated.
- `UploadedFileOld` (legacy pre-file-upload-library metadata table, `Blob fileData`/
  `scaledImageData`) appears to only ever be queried for metadata columns (id/name/size/date), not
  the blob columns themselves, in the two consuming repositories found — same pattern as
  `DocgenRun`, likely dead for the same reason.
- Remaining work is verification, not design: a quick runtime data check on both columns (confirm
  they're genuinely never read/written in practice, not just never called in the code paths
  inspected so far) before dropping them from the Postgres schema, rather than migrating dead BLOB
  data through DMS.
- No proprietary `oracle.sql.BLOB`/`oracle.sql.CLOB` types are used anywhere — all LOB handling
  goes through standard `java.sql.Blob`/`SerialBlob`, which is portable, but is moot here if both
  columns are dropped.

## 10. AWS DMS setup, rehearsal migrations & cutover

**Effort: 10–13 days. High risk.**

Cutover is a **big-bang migration**: a maintenance window, a single full-load DMS task from Oracle
to Postgres, validation, then the application's connection string flips to Postgres and PWA comes
back online. No CDC/ongoing-replication configuration, replication-lag monitoring, or drain-then-cut
procedure is needed — that entire category of DMS complexity is out of scope. The main remaining
unknown is how long the full load actually takes, since that sets the maintenance window length.

Covers: DMS replication instance/task setup (full-load only), initial schema conversion pass (AWS
SCT, feeding into section 4), data validation/reconciliation tooling (row counts, checksums,
spot-checks) after each rehearsal load, at least 2–3 rehearsal migrations into a non-prod Postgres
environment to measure and tighten the full-load duration, a written cutover runbook (maintenance-
window communication, sequencing, rollback plan, smoke-test checklist), and post-cutover sequence
resynchronisation (Oracle sequence current-values → Postgres `SETVAL`).

LOB handling needs next to no dedicated DMS tuning: both known BLOB columns
(`DocgenRun.generatedDocument`, `UploadedFileOld`) are confirmed dead (see section 9) and get
dropped before cutover rather than carried through DMS. The remaining CLOB usage across the schema
is plain long-text content (clause versions, docgen templates, log messages), not large binary
objects — a standard Oracle CLOB → Postgres `text` mapping, not a DMS large-object-mode concern. The
`R__0000`–`R__0025` cluster (section 5) also drops out of the schema-conversion pass entirely, since
none of it is carried into Postgres.

## 11. Test infrastructure

**Effort: 8–12 days. Medium risk — currently a real coverage gap.**

Integration tests (`*IntegrationTest.java`) currently run against an in-memory H2 database with
`spring.flyway.enabled=false` and `spring.jpa.hibernate.ddl-auto=create` — Hibernate generates the
test schema from entity mappings, and **none of the actual Flyway SQL migrations are exercised by
the automated test suite today**, on Oracle or otherwise. This means the SQL migration rewrite in
sections 4–6 has no existing regression safety net. Recommend introducing Testcontainers-Postgres for at
least a subset of integration tests that actually run the real Flyway migrations, to validate the
converted DDL/views before they ever reach a shared environment.

## 12. Local dev / CI environment updates

**Effort: 1 day. Low risk — an established pattern already exists.**

Developers currently point at a shared remote Oracle dev instance (`db-ogadev1.sb2.dev`), not a
local container — there's no local Oracle in `compose.yml`/`docker/develop.yaml` today. Unlike a
from-scratch setup, the team already has a working pattern for standing up non-Oracle services
locally/in CI, so this is a case of applying that existing pattern to Postgres (README/IntelliJ
run-config updates, `.drone.yml` service container for the `./gradlew test` stage) rather than
designing new local-dev tooling.

## 13. Documentation & ADR updates

**Effort: 0.5 days.**

- `docs/adr/0004-foreign-keys-to-tables-outside-app-schema.md` was written specifically because of
  Oracle same-instance FK-lock-contention concerns — its rationale changes once cross-schema access
  is removed entirely; worth a follow-up ADR recording the new approach (API-based integration)
  rather than silently orphaning ADR-0004.
- `CLAUDE.md`, README, and any onboarding docs referencing Oracle-specific setup need updating.

---

## Key open decisions (need answers before detailed estimation)

1. **Downstream consumer replacement** (section 3): what do PETS, UKSS, and BI reporting actually need,
   and who owns building their replacement integration?
