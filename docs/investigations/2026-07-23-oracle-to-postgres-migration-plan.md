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
- **Each `Person` maps to a single `WebUserAccount`.** Section 3's `personId`→`wuaId` re-keying
  takes each person's one associated account and uses its `wuaId` in place of the old `personId`,
  rather than designing a business rule to pick among several accounts. This removes the hardest
  sub-problem in section 3's scope.

The work is split into **two bits, delivered in sequence**:

- **Part 1 — application changes (sections 1–4)** remove every same-instance cross-schema coupling,
  in both directions — PWA's own reads of other schemas (sections 1–3) and other systems' reads of
  PWA (section 4) — and can be built, tested, and shipped to production *while PWA is still on
  Oracle*. Doing this first means that by the time the database moves, nothing depends on
  cross-schema access any more.
- **Part 2 — database migration & Oracle→Postgres changes (sections 5–15)** is then a comparatively
  clean lift-and-shift: stand up Postgres, convert the schema/SQL, migrate the data, cut over. The
  now-unused cross-schema views and legacy migration cluster are simply dropped, because Part 1
  already removed everything that read them.

This plan is a high-level scoping exercise based on a full-repo survey (263 Flyway migration files,
201 JPA entities, the `integrations/`/`teams`/`externalapi` packages). It is meant to size the
problem and flag architectural decisions that need to be made early, not to be a detailed design.
**Effort figures are broad, single-engineer-day order-of-magnitude estimates for the PWA-side
engineering work only** — they exclude other teams' work (Energy Portal API build-out, DBA/infra
provisioning, the Oracle-side EPMQ landing/consumer/view-repoint for section 4), performance
validation and regression testing (see above), and PM/coordination overhead. Treat every number as
±40%.

## Summary table

| # | Section | Effort (days) | Risk |
|---|---|---|---|
| **Part 1 — Application changes (delivered on Oracle, ahead of the DB migration)** | | | |
| 1 | Cross-schema data access → Energy Portal API **integration** (APIs assumed to already exist; only 5 of the 6 external schemas need it) | 15–24 | High |
| 2 | Search/listing views that join cross-schema data (work area, consent search, case officer assignment, org filters) — batch API stitching | 12–20 | High |
| 3 | Retire `Person` in favour of `WebUserAccount` (real FKs, single account per person assumed, telephone number dropped) | 9–13 | High |
| 4 | Downstream consumers of PWA's own schema — EPMQ replication back to Oracle, views repointed (PETS/UKSS unchanged) | 10–15 (PWA side) | Medium |
| | *Part 1 subtotal* | *~46–72* | |
| **Part 2 — Database migration & Oracle→Postgres changes** | | | |
| 5 | Local dev / CI environment updates (existing pattern already in place) | 1 | Low |
| 6 | Test infrastructure (Postgres-based integration tests) | 8–12 | Medium |
| 7 | Oracle/Postgres data-semantics & type-mapping differences (boolean, `NUMBER`, empty-string, null-concat, identifier casing) | 5–8 | Medium |
| 8 | Flyway DDL migration (schema/type conversion) | 10–15 | Medium |
| 9 | PL/SQL packages, procedures, `afterMigrate` callbacks & one-off migration blocks (`R__0000`–`R__0025` + `afterMigrate` scripts dropped outright) | 3–5 | Medium |
| 10 | Own-schema view rewrites (mostly `CONNECT BY` → recursive CTE; `ROWNUM`/`DUAL` are quick) | 5–8 | Medium |
| 11 | Java native queries, sequences, Hibernate/driver config | 6–10 | Low |
| 12 | Vendored/library schemas (Spring Session, Quartz, Fivium starters) | 2–3 | Low |
| 13 | BLOB/LOB handling (`DocgenRun`, legacy file metadata — both columns confirmed dead) | 0.5 | Low |
| 14 | AWS DMS setup, rehearsal migrations & cutover (big-bang cutover, no CDC; includes Camunda workflow-state data) | 12–15 | High |
| 15 | Documentation & ADR updates | 0.5 | Low |
| | *Part 2 subtotal* | *~53–78* | |
| | **Total** | **~99–150 days** | |

Performance validation and regression testing are deliberately excluded from this table — see
"Context and assumptions" above; they happen during UAT and outside the development team's scope
respectively.

This is roughly **5–7.5 engineer-months** of PWA-side effort if executed serially by one person.
Within each part several sections parallelize (across Part 1, sections 2–4 fan out once section 1's
API foundation lands; across Part 2, the enablers 5–6 run during Part 1 and sections 10–13
parallelize once the schema baseline, sections 7–8, exists), so realistic wall-clock time with a
small team is considerably shorter than the sum.

---

## Timeline with a 3-engineer team

The two bits run largely in sequence, because Part 2's schema conversion and data migration should
reflect the **post-Part-1** schema — in particular section 3's `person_id`→`wua_id` re-key changes
PWA's own tables, and it's cleaner to convert and DMS the final shape once than to redo it. The one
thing that *can* overlap is Part 2's environment enablers (local dev/CI Postgres, section 5; the
Testcontainers-Postgres test harness, section 6), which have no dependency on the app changes and
can be stood up by a spare engineer during Part 1.

Dividing ~99–150 person-days by 3 gives ~33–50 person-days of capacity each; the critical-path
constraints below mean wall-clock lands higher than a naive divide suggests. With performance
validation and regression testing out of scope (see "Context and assumptions"), there's no dedicated
hardening phase — just the cutover mechanics at the end.

**Part 1 — application changes on Oracle (~6–9 weeks).** Shippable to production incrementally
while still on Oracle, which de-risks the eventual cutover.

| Phase | Elapsed | Focus | Rough allocation |
|---|---|---|---|
| 1a — API foundation | 3–4 weeks | Cross-schema → EP API integration (section 1), the foundation sections 2/3 build on | A+C: section 1, split by domain (`devukmgr`/`wellmgr`/`decmgr` orgs/people/team-role model first — all low-to-medium risk — then higher-risk `pedmgr`, `securemgr`), ~2 FTE. B: gets a head start on Part 2 enablers (sections 5–6) since they don't depend on the app changes |
| 1b — Decouple | 3–5 weeks | `Person`→`WebUserAccount` re-key (section 3, incl. the one-off `personId`→`wuaId` data-mapping script, using each person's single associated account per the assumption above); search/listing batch stitching (section 2); downstream-consumer EPMQ publishers (section 4) | A+C: section 3's FK re-keying then section 2's batch stitching (section 3's `pwa_app_assignments` view reuses the same pattern, so back to back). B: section 4 EPMQ publishers |

**Part 2 — database migration & Oracle→Postgres changes (~6–9 weeks).** Begins in earnest once
Part 1's schema changes have landed; the enablers (sections 5–6) are already done from Part 1's
spare capacity.

| Phase | Elapsed | Focus | Rough allocation |
|---|---|---|---|
| 2a — Rules & baseline | 2–3 weeks | Data-semantics/type-mapping decisions (section 7) feeding the Postgres schema baseline (section 8, via AWS SCT), then prune the migration set (section 9) | A: sections 7 + 8 jointly (semantics drives the DDL, so build them once on the agreed rules). B: section 9 |
| 2b — SQL & model conversion | 2–3 weeks | Own-schema view rewrites (section 10, mostly quick bar the recursive CTEs), native queries/sequences/driver swap (section 11), library-schema config flips (section 12), BLOB/LOB drop (section 13, half a day) | A+B: sections 10, 11 split between them. C: sections 12 + 13 |
| 2c — Migrate & cut over | 2–3 weeks | DMS rehearsal migrations including Camunda workflow-state data with a workflow-resume smoke test each round (section 14), the cutover runbook, then the final big-bang cutover; docs/ADR (section 15) | All three converging on rehearsals → cutover; docs alongside |

**Total: ~12–18 weeks (roughly 3–4.5 months) wall-clock** (Part 1 ~6–9 + Part 2 ~6–9, with the
enablers folded into Part 1's spare capacity), versus ~5–7.5 months if done by one engineer
serially — i.e. roughly a 1.7x speedup, held back because Part 1's API foundation (section 1) and
Part 2's DDL→DMS chain are each internally sequential, and a fixed 3-person team absorbs that mostly
by working longer, not wider. This excludes whatever calendar time UAT
(performance validation) and the separately-owned regression testing effort add on top, since those
sit outside this plan.

Two things can still stretch this regardless of headcount:
- **External / infra dependencies** — the Oracle-side EPMQ landing table, consumer and view-repoint
  for section 4 (with the EPMQ/platform owners; the PETS/UKSS applications themselves don't change),
  and DBA/infra provisioning for section 14 — sit outside PWA's control and are the most likely
  remaining source of schedule slip.
- **Part 1 not fully landing before Part 2's schema conversion** — if section 3's `person_id`→
  `wua_id` re-key (or any Part 1 schema change) is still in flight when the section 8 baseline is
  generated, the DDL and DMS transforms get reworked. Keep the boundary clean: Part 1 done and
  ideally in production before Part 2's conversion starts.

---

# Part 1 — Application changes (on Oracle)

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
external build work. This is Part 1 work: it runs against the app while still on Oracle (the local
views still exist), and the views themselves are only dropped later in Part 2 (section 9).

| External schema | PWA domain area | Integration notes |
|---|---|---|
| `decmgr` — organisation units/groups | `integrations/energyportal/organisations/` | `integrations/epa` GraphQL client (`OrganisationApi`) already covers some of this — confirm full field parity against `portal_organisation_units`/`portal_organisation_groups`/`portal_org_unit_detail`, then swap over. |
| `decmgr` — resource/team/role model | `integrations/energyportal/teams/external/PortalTeamAccessor.java` | Confirmed via code search that `PortalTeamAccessor` (the only consumer of the `PortalTeam*` entity family) is wired into exactly one place — `auth/saml/SamlResponseParser.java`, which fetches a person's portal system privileges once at SAML login time. PWA's ongoing `@HasAnyRole` authorization checks (`HasAnyRoleInterceptor`) go through the separate, PWA-owned `Team`/`TeamRole` model (top-level `teams/` package) instead, which is not cross-schema and isn't affected by this migration at all. This is a single, well-isolated integration point, not an app-wide dependency. |
| `decmgr` — people | `integrations/energyportal/people/` | Not a simple swap-over — `Person`'s backing view is dropped in Part 2 (section 9), and `Person` is a real persisted foreign-key target elsewhere in PWA's schema. Broken out into its own section — see section 3. |
| `devukmgr` — fields/facilities | `integrations/energyportal/devukfields/`, `devukfacilities/` | `epa` `FieldApi` likely already covers this — straightforward swap-over. |
| `pedmgr` — PEARS/PED licensing | `integrations/energyportal/pearslicensing/`, `pearslicenceapplications/` | Dedicated legacy domain — confirm API field coverage, then swap over. |
| `securemgr` — portal user accounts/sessions | `integrations/energyportal/webuseraccount/` | Auth-adjacent — needs careful review since SAML2/EPAS is the actual authentication path; this may only be account *lookup*, not authentication itself, but must be confirmed. |
| `wellmgr` — well data | (consumer not yet located) | Small surface (`api_extant_wellbores` only) — low effort once located. |

Two schemas need no API replacement at all:
- **`decmgr` — legacy pipeline authorisations** (`pipeline_authorisations`, `xview_pipeline_auth_details`, `xview_pipelines_history`, `xview_pipeline_company_hist`): the only consumers of these tables are the `mig_*` staging views in `R__0000_migration_data.sql`, which are dropped along with the rest of the `R__0000`–`R__0025` cluster (see section 9). No Java code references any of this.
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

## 3. Retire `Person` in favour of `WebUserAccount`

**Effort: 9–13 days. High risk — touches real foreign keys in PWA's own schema, not just a
view-swap.**

Section 1 lists `decmgr` — people alongside the other domains, but it isn't a simple swap-over like
those — the entity is a real persisted foreign-key target, not just a read-only lookup — so it's
broken out here. It's Part 1 work because both the `people` and `user_accounts` views still exist on
Oracle, so the re-key can be done and shipped ahead of the DB move; doing so means Part 2 can drop
the `people` view (section 9) with nothing left pointing at it.

**Why this is needed at all:** `Person` (`integrations/energyportal/people/external/Person.java`,
`@Immutable @Table(name = "people")`) maps to a view defined in `R__0020_energy_portal_team_views.sql`,
which is dropped in Part 2 (section 9). Once that view is gone, `Person` has no backing data, so
something has to replace it.

**Why `WebUserAccount` is the natural replacement, not a new API integration:**
`WebUserAccount` (`integrations/energyportal/webuseraccount/external/WebUserAccount.java`,
`@Table(name = "user_accounts")`, backed by `securemgr.web_user_accounts`) already carries a
`@ManyToOne person` link and a superset of `Person`'s useful fields (plus `title`, `loginId`,
`accountStatus`), and is already the identity object wrapped by `AuthenticatedUserAccount` (the
logged-in session principal) — most "get the current user's Person" call sites already go through
a `WebUserAccount` that's in scope, not an independent lookup. The existing `energyportalapi`
GraphQL client's `UserApi` (already wired up and consumed by `TeamMemberQueryService`) supports
looking a user up by `wuaId` (single and batch) or by email — exactly `WebUserAccount`'s identity
space — and its `User` type is richer still (`middleInitials`, `canLogin`, `isAccountShared`, none
of which the local `WebUserAccount` entity carries).

**What makes this genuinely hard, unlike most of section 1's other rows:** `Person` is a real
persisted foreign-key target in PWA's own schema, not just a read-only lookup:
- `PwaContact.person_id` (`@OneToOne`) and `DocgenRun.scheduled_by_person_id` (`@ManyToOne`) are
  actual JPA relationships to `Person` — the only two such relationships in the app.
- `Assignment.assigneePersonId`, `AssignmentAudit.assigneePersonId`/`assignedByPersonId`, and the
  `pwa_app_assignments` reporting view's `assignee_person_id` (which also denormalizes
  `assignee_name` via a `decmgr` join) all store a bare `personId` integer, following ADR-0001's
  existing "store the id, not the entity" convention — but keyed on `personId`, not `wuaId`.
- ~76 files reference `Person` across the codebase (audit-trail name resolution, notification
  recipients, contact/team-member display, workflow assignment, application involvement checks).
  Most of these are thin, mechanical lookup/display swaps once the FK columns below are re-keyed,
  but the sheer count is why this isn't a quick job.

**Cardinality is assumed away, not solved.** `WebUserAccount`'s own javadoc states a `Person` may
have **many** associated `WebUserAccount`s, which would otherwise mean every `personId` column
needs a business rule to pick a single `wuaId` for historical data with no guaranteed one-to-one
mapping. Per the assumption in "Context and assumptions," this plan takes each person's one
associated account and uses its `wuaId` — no rule design, no edge-case handling for people with
several accounts. The one-off `personId`→`wuaId` data-mapping exercise this still requires (a
straight lookup, not a designed rule) runs on Oracle as part of this re-key. If the single-account
assumption doesn't hold for some people in practice, that surfaces as a data-quality question to
resolve during the mapping exercise, not a redesign of this section.

**Telephone number is being dropped, not replaced.** `Person.telephoneNo` is actively rendered
today — confirmed via `ContactTeamMemberView` (which reads `person.getTelephoneNo()`) in three
templates (`applicationContactsSummary.ftl`, `contactTeam/removeMember.ftl`,
`contactTeam/teamMembers.ftl`). The local `WebUserAccount` entity has no phone field at all, and the
decision here is to remove the field from the UI rather than recover it via the GraphQL `User`
type's `telephoneNumber` — so this is a small deletion (the field on `ContactTeamMemberView` and
its display in the three templates), not a feature to preserve.

**Recommended approach:** re-key `PwaContact.person_id` and `DocgenRun.scheduled_by_person_id` to
`wua_id` (schema change + JPA update), re-key the `Assignment`/`AssignmentAudit` personId columns
the same way, and rework `pwa_app_assignments`' `decmgr`-joined `assignee_name` using the same
batch-API-stitching pattern as section 2 (this view belongs in that inventory too). Redirect the
notification library's `EmailRecipient` integration from `Person` to `WebUserAccount` (a small
adapter change — `WebUserAccount` doesn't implement `EmailRecipient` today but has all the fields
needed to). Then sweep the remaining read-only consumers. Retire `Person`, `PersonId`,
`PersonRepository`, `PersonService`, and `SimplePersonView` once nothing references them.

## 4. Downstream consumers of PWA's own schema

**Effort: 10–15 days PWA-side (excludes the Oracle-side landing table/consumer, which sits with the
EPMQ/platform owners). Medium risk — the consuming applications don't change, so this is no longer a
cross-team app-rebuild dependency.**

`R__0090_external_access_api_views.sql` currently grants `SELECT` on 8 `api_vw_*` views (built
from PWA's own schema) to `decmgr`, `envmgr` (PETS, `WITH GRANT OPTION`), `passmgr` (UKSS, `WITH
GRANT OPTION`), `bpmmgr`, `eemsmgr`, `appenv`. **PETS and UKSS read these views directly**, and once
PWA is on an isolated Postgres instance they lose that direct same-instance access.

**Decided approach: replicate the view data back to Oracle over EPMQ, and repoint the existing
views at the replicated copy — so the consumers don't change at all.** PWA publishes the consumed
`api_vw_*` datasets as messages over EPMQ (Energy Portal Message Queue — the SNS/SQS-backed Fivium
messaging library already a dependency here, `uk.co.fivium.energyportalmessagequeue` v4.6.0, which
PWA already *consumes* for EPAS team-role updates via `TeamRolesUpdateHandler`; this adds the
publishing side). A consumer on the Oracle side lands those messages in a table that replicates each
view's shape, and the existing `api_vw_*` views are repointed to read from that replicated table
instead of PWA's (departed) live tables. PETS, UKSS and the other view consumers keep reading the
same views under the same names — no change on their side.

**Why this is the right fit, not just an option (validated against the EDU environment source):**
the consumers don't read PWA's views standalone — their own views *JOIN* PWA data to their own
schema's tables inside Oracle. `passmgr.vw_survey_pipelines` (UKSS) joins
`pwa.api_vw_current_pipeline_orgs` to `decmgr` org tables and `passmgr.org_grp_overrides`;
`envmgr.pets_pipeline_op_project` / `pets_pipelines_mapset` (PETS) join
`pwa.api_vw_current_pipeline_data` to `envmgr.xview_pam_details_*` and `envmgr.xview_env_mapsets`.
A REST API (the alternative originally sketched here) *cannot* serve an in-database join, so it
would force these Oracle/Fox view-based consumers to be rewritten to fetch-and-stitch — a large
cross-team change. Landing the data in a local Oracle table keeps the joins working with only a
`FROM` repoint. And EPMQ is the platform's established event bus (a shared `epmq.events` package
that schemas emit over — wellmgr, pedmgr/PEARS corrections, EPAS — and which PWA already consumes),
not a bespoke mechanism; there is no live Oracle→external-DB link/gateway pattern anywhere in the
EDU source to reach for instead, and the platform's habit for cross-app data is already
"materialise a local copy and refresh it" (PETS/EEMS/WONS all use refresh-job materialized views).
An EPMQ-fed table is the event-driven continuation of that same pattern.

Scope split:
- **PWA-side (this estimate):** build the EPMQ publishers — a message definition per dataset,
  publish-on-change wiring, and an initial backfill so the replicated table starts complete. Builds
  on the existing EPMQ integration rather than a new platform. **Confirm the actually-consumed
  view set first:** the PETS/UKSS consumers only touch `api_vw_current_pipeline_data` and
  `api_vw_current_pipeline_orgs`; some of the 8 granted `api_vw_*` views may have no live external
  reader, which would narrow scope below "all 8."
- **Oracle-side (with the EPMQ/platform owners, not counted here):** the EPMQ consumer, the
  replicated landing table, and repointing the views. This can be stood up on Oracle *before*
  cutover — proving the pipeline end-to-end while PWA still runs on Oracle — so by cutover the
  consumers are already reading the replicated copy, not live PWA tables.

The main residual risk is data-freshness/drift: the replicated table is eventually-consistent via
messaging rather than a live view, so the publish-on-change coverage and the backfill need to be
right, and worth a reconciliation check during rehearsals. This is acceptable here because the
consumed data is pipeline *reference* data, not real-time transactional reads.

The BI/reporting schema is a different case — it reads the whole PWA schema via the `all_objects`
grant rather than the `api_vw_*` views, and it **keeps direct read access to the new Postgres
database, exactly as it does on Oracle today**. Unlike PETS/UKSS (Oracle apps that must join PWA
data to their own schema in-database, hence the EPMQ replication back to Oracle), BI is a reporting
consumer that can simply point at whichever database PWA lives in. So BI needs no EPMQ publisher; it
gets a BI read role with `SELECT` on the PWA schema, replacing the dropped Oracle `all_objects`
grant loop — a small Postgres-side grant set up with the schema in Part 2 (section 8), not part of
this section's estimate.

---

# Part 2 — Database migration & Oracle→Postgres changes

## 5. Local dev / CI environment updates

**Effort: 1 day. Low risk — an established pattern already exists.**

Developers currently point at a shared remote Oracle dev instance (`db-ogadev1.sb2.dev`), not a
local container — there's no local Oracle in `compose.yml`/`docker/develop.yaml` today. Unlike a
from-scratch setup, the team already has a working pattern for standing up non-Oracle services
locally/in CI, so this is a case of applying that existing pattern to Postgres (README/IntelliJ
run-config updates, `.drone.yml` service container for the `./gradlew test` stage) rather than
designing new local-dev tooling. This has no dependency on the Part 1 app changes, so it's the
natural first Part 2 task and can be pulled forward to run during Part 1.

## 6. Test infrastructure

**Effort: 8–12 days. Medium risk — currently a real coverage gap.**

Integration tests (`*IntegrationTest.java`) currently run against an in-memory H2 database with
`spring.flyway.enabled=false` and `spring.jpa.hibernate.ddl-auto=create` — Hibernate generates the
test schema from entity mappings, and **none of the actual Flyway SQL migrations are exercised by
the automated test suite today**, on Oracle or otherwise. This means the SQL migration rewrite in
sections 8–10 has no existing regression safety net. Recommend introducing Testcontainers-Postgres for at
least a subset of integration tests that actually run the real Flyway migrations, to validate the
converted DDL/views before they ever reach a shared environment. Like section 5, the harness
scaffolding has no dependency on the Part 1 app changes and can be stood up early.

## 7. Oracle/Postgres data-semantics & type-mapping differences

**Effort: 5–8 days. Medium risk to schedule, but high consequence if missed — these are the
classic silent-behaviour-change bugs of an Oracle→Postgres move.**

This is a cross-cutting concern rather than a discrete component: it defines the transformation and
mapping *rules* that section 8's DDL conversion and section 14's DMS task then apply, and it's
validated by section 6's Postgres integration tests plus UAT. It's called out separately because
an automated schema-conversion pass (section 8) will silently paper over several of these and the
resulting bugs surface at runtime, not at build time — and with a dedicated regression/performance
workstream out of scope for this plan, catching them leans on section 6 and UAT.

The concrete items, grounded in the current codebase:
- **Boolean.** Oracle has no native boolean type; Hibernate's Oracle dialect stores the app's
  boolean fields as `NUMBER(1)`. Postgres has a real `boolean`, and Hibernate's Postgres dialect
  expects it. ~193 boolean entity fields across ~29 entities are affected: the target DDL must
  declare these columns `boolean`, and the DMS task needs a `0/1 → false/true` transform on each.
  Getting the DDL type and the transform to agree with the dialect's expectation is the single
  largest item here.
- **`NUMBER` → integer types.** Oracle `NUMBER` (no precision) is arbitrary-precision; a naive SCT
  pass tends to map it to Postgres `numeric`, which is slower and can surprise Hibernate mappings
  expecting `Long`/`Integer`. Id, count, and FK columns should land on `bigint`/`integer`, not
  `numeric` — a review pass over the generated DDL.
- **Empty string vs NULL.** Oracle collapses `''` to `NULL`; Postgres keeps them distinct. The
  *data* coming across is unaffected (anything that was `''` is already `NULL` in Oracle), but
  application code that relied on that collapse — a user submitting a blank field and reading it
  back as `null` — will now read `''` on Postgres and may branch differently. Audit the handful of
  `StringUtils.isEmpty`/`isNotEmpty` call sites (7 today) and any `== null` checks on user-editable
  text for this assumption.
- **String concatenation with NULL.** Oracle `'a' || NULL` yields `'a'`; Postgres yields `NULL`.
  Relevant to the own-schema views being rewritten in section 10 and the small native-SQL surface in
  section 11 — flag it as a thing to watch during those rewrites rather than separate work.
- **Identifier casing.** Oracle folds unquoted identifiers to upper case, Postgres to lower case.
  Hibernate abstracts most of this, but any native queries or hand-written DDL referencing
  specific-case identifiers need to be consistent with what the converted schema actually creates.

## 8. Flyway DDL migration (schema/type conversion)

**Effort: 10–15 days.**

232 versioned migration files, dominated by `VARCHAR2`/`NUMBER`/`CLOB`/`BLOB` column types (124,
14, 12, 7 files respectively) plus 8 `CREATE SEQUENCE` files. Recommended approach: don't hand-port
232 historical migrations — since AWS DMS (section 14) will carry over the actual data, only the
**current end-state schema shape** matters for the new Postgres database. Use a schema-conversion
tool (e.g. AWS Schema Conversion Tool) to generate a first-pass Postgres DDL baseline from the
current Oracle schema (applying the type-mapping rules from section 7), review/clean up the
automated output, and Flyway-baseline the new Postgres migration history from there. Historical
Oracle migrations stay in the repo for audit trail; new migrations from that point are authored
directly in Postgres syntax.

The one exception requiring hand attention: `V0.1__Create_schema.sql` (tablespaces, profiles,
quotas) has no Postgres equivalent and needs a from-scratch Postgres role/schema bootstrap script.
That bootstrap also sets up the BI reporting role with read (`SELECT`) access to the PWA schema —
BI keeps a direct database connection on Postgres as it has on Oracle (see section 4), so the
dropped Oracle `all_objects` grant loop (section 9) is replaced with Postgres schema-level grants
(`GRANT SELECT ON ALL TABLES IN SCHEMA ...` plus `ALTER DEFAULT PRIVILEGES` so future tables are
covered). Small, but easy to forget until a report breaks.

Also worth resolving here: the build uses a Fivium fork of Flyway (`org.flywaydb:flyway-core:9.5.1-FIVIUM`). Its Oracle-specific reason for existing was largely parsing/splitting PL/SQL blocks (the `/` terminators in the `R__` packages and `afterMigrate` callbacks). Once those are dropped (both the packages and the callbacks are covered in section 9), evaluate whether plain upstream Flyway suffices; dropping a bespoke pinned fork is a small de-risking win, but confirm nothing else in the migration tooling depends on it first.

## 9. PL/SQL packages, procedures, `afterMigrate` callbacks & one-off migration blocks

**Effort: 3–5 days.**

**Dropped outright as no longer needed — deletion only, no assessment or replacement work in this
section:**
- `R__0000_migration_data.sql` — the `mig_*` staging views.
- `R__0001_migration_logger_package.sql` and `R__0002_migration_package.sql` (1,153 lines) — the
  one-off legacy-to-PWA data migration that transforms `mig_*` rows into master-PWA/pipeline/
  consent/as-built data, moot once DMS carries that data across directly, plus its logging helper.
- All three `afterMigrate__*.sql` callbacks — `__001_update_teams_with_new_roles.sql` (legacy
  portal-team role backfill via `XMLTYPE`/`bpmmgr` types), `__002_grant_access_to_bi_schema.sql`
  (the dynamic `all_objects` `EXECUTE IMMEDIATE` BI-grant loop), and `__003_verify_api_views.sql`
  (external-access view verification). All are Oracle PL/SQL tied to same-instance cross-schema
  access that doesn't exist on the isolated Postgres instance. (The *capabilities* two of them
  supported are re-provided elsewhere, not here: BI-schema reads by a direct Postgres read-grant
  (section 8, since BI keeps a direct database connection — see section 4), and external-access-view
  reads by section 4's EPMQ rework. These scripts themselves are simply deleted.)

**Also dropped, but noted elsewhere because sections 1/3 cover what replaces them:**
- `R__0010_energy_portal_organisation_views.sql` and `R__0020_energy_portal_team_views.sql` — the
  `decmgr`-backed organisation/team views that section 1's API integration replaces. `R__0020` also
  defines the `people` view backing the `Person` entity — see section 3.
- `R__0025_energy_portal_team_management_package.sql` — the team management package.

**The only real work in this section is triage, not translation**: the ~27 anonymous PL/SQL blocks
outside the 0000–0025 range (mostly historical one-off data seeds/fixes) are one-time data
migrations whose *effects* will already be present in the data DMS carries over — they don't need
porting at all. The remaining handful of standalone functions/procedures (e.g.
`V193__Terms_and_Conditions_historical_function.sql`) need a "does anything in `src/main/java`
still invoke this at runtime" check — only genuinely runtime-invoked logic needs a real rewrite (as
PL/pgSQL or, preferably, moved into the Java service layer where it's more testable and consistent
with the rest of the codebase).

## 10. Own-schema view rewrites

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

## 11. Java native queries, sequences, Hibernate/driver config

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

## 12. Vendored/library schemas

**Effort: 2–3 days. Low risk — mostly config flips.**

- Spring Session (`V0.2__Create_session_store.sql`) and Quartz (`V132__quartz_tables.sql`) schemas
  were hand-copied from each library's own Oracle-flavoured sample SQL. Both libraries ship
  official Postgres equivalents (`schema-postgresql.sql`, `tables_postgres.sql`) — swap to those
  directly rather than hand-translating.
- Camunda auto-manages its own `ACT_*` schema (the table DDL) per JDBC vendor at boot — no schema
  work needed here. The `ACT_*` *data* (in-flight workflow state) is a separate matter and must be
  migrated via DMS — see section 14.
- The three Fivium starter libraries in use (`file-upload-spring-boot-starter`,
  `digital-notification-library-spring-boot-starter`,
  `digital-enum-materialisation-library-spring-boot-starter`) **already ship Postgres migrations as
  their primary/default target** — this is a config-property change
  (`file-upload.flywayVendor`, `digital-notification-library.flyway-vendor`,
  `enum-materialisation.flywayVendor` from `oracle` to `postgresql`/unset), not new SQL authoring.

## 13. BLOB/LOB handling

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

## 14. AWS DMS setup, rehearsal migrations & cutover

**Effort: 12–15 days. High risk.**

Cutover is a **big-bang migration**: a maintenance window, a single full-load DMS task from Oracle
to Postgres, validation, then the application's connection string flips to Postgres and PWA comes
back online. No CDC/ongoing-replication configuration, replication-lag monitoring, or drain-then-cut
procedure is needed — that entire category of DMS complexity is out of scope. The main remaining
unknown is how long the full load actually takes, since that sets the maintenance window length.

**Camunda workflow-state data must be in the full-load set, not regenerated empty.** Camunda runs
in-process against PWA's own datasource (there is a single `spring.datasource`), so its `ACT_*`
tables — the running process instances for the three live workflows (`pwa.bpmn`,
`pwa_app_consultations.bpmn`, `pwa_app_public_notice.bpmn`) plus the full history
(`camunda.bpm.history-level=full`) — live in the PWA schema. The Camunda starter will happily
create an empty `ACT_*` schema on first boot against the new Postgres database; if that happens
instead of the data being migrated, **every in-flight case loses its workflow state** (task
assignments, process position, the domain↔process-instance correlation that drives the case task
list). So the `ACT_*` tables must be included in the DMS full-load, and each rehearsal needs a
workflow-resume smoke test — pick a mid-flight application in the migrated data and confirm its
Camunda tasks still resolve and can be actioned — not just row-count reconciliation. Quartz
(`QRTZ_*`) job-store rows are lower-stakes (jobs re-fire), and Spring Session rows are ephemeral
(users simply re-authenticate after cutover), so neither needs the same care.

Covers: DMS replication instance/task setup (full-load only, including the `ACT_*` tables), initial
schema conversion pass (AWS SCT, feeding into section 8), data validation/reconciliation tooling
(row counts, checksums, spot-checks, plus the workflow-resume check above) after each rehearsal
load, at least 2–3 rehearsal migrations into a non-prod Postgres environment to measure and tighten
the full-load duration, a written cutover runbook (maintenance-window communication, sequencing,
rollback plan, smoke-test checklist), and post-cutover sequence resynchronisation (Oracle sequence
current-values → Postgres `SETVAL`).

LOB handling needs next to no dedicated DMS tuning: both known BLOB columns
(`DocgenRun.generatedDocument`, `UploadedFileOld`) are confirmed dead (see section 13) and get
dropped before cutover rather than carried through DMS. The remaining CLOB usage across the schema
is plain long-text content (clause versions, docgen templates, log messages), not large binary
objects — a standard Oracle CLOB → Postgres `text` mapping, not a DMS large-object-mode concern. The
`R__0000`–`R__0025` cluster (section 9) also drops out of the schema-conversion pass entirely, since
none of it is carried into Postgres.

## 15. Documentation & ADR updates

**Effort: 0.5 days.**

- `docs/adr/0004-foreign-keys-to-tables-outside-app-schema.md` was written specifically because of
  Oracle same-instance FK-lock-contention concerns — its rationale changes once cross-schema access
  is removed entirely; worth a follow-up ADR recording the new approach (API-based integration)
  rather than silently orphaning ADR-0004.
- `CLAUDE.md`, README, and any onboarding docs referencing Oracle-specific setup need updating.

---

## Open items

No architectural decisions remain open — the ones surfaced during scoping are all settled: big-bang
cutover (section 14), single `WebUserAccount` per person and dropping the telephone number
(section 3), the `api_vw_*` consumers via EPMQ replication and BI/reporting via direct Postgres
access (section 4).

What's left are confirmation tasks folded into the sections above, to be done as part of the work
rather than answered before it starts:
- the exact set of `api_vw_*` views actually consumed externally (section 4);
- the `wellmgr` data consumer, not yet located in the code (section 1);
- whether `securemgr` is used for account *lookup* only or sits on the authentication path
  (section 1).
