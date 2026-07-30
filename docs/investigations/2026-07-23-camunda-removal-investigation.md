# Removing Camunda: in-app business-state management — effort investigation

**Date:** 2026-07-23
**Area:** whole application — application/case processing workflow, task list/work area, assignments

## Context and assumptions

PWA currently runs **two workflow representations kept in sync manually** (per CLAUDE.md): the
domain `PwaApplicationStatus` field on `PwaApplicationDetail` (drives permissions, URL access
control, and almost all UI decisions), and a Camunda BPM process instance per application/
consultation/public-notice, advanced explicitly alongside each domain status change. This
investigation scopes replacing the Camunda half with a plain in-app (Java/DB) state-transition
mechanism, based on a full-repo survey (BPMN definitions, the `integrations/camunda/external/`
wrapper, all 24 call sites, the assignments feature, work-area queries, build/test config).

**Headline finding: Camunda is far more thinly and defensively used here than the "two workflow
engines" framing in CLAUDE.md suggests.** Concretely:

- Camunda's own `candidateGroups`/group-based identity model is **never read by any Java code** —
  eligibility and assignment are already fully reimplemented against PWA's own `teams`/`Role` model
  and a plain `assignments` DB table (`features/appprocessing/workflow/assignments/`).
- Permission checks and URL access control (`features/application/authorisation/context/`) read
  **only** the domain `PwaApplicationStatus` field — no file in that package imports Camunda at
  all.
- There is no admin/ops UI (no Cockpit, no REST API — just the headless engine starter), no
  timers, no service tasks/delegates, and no BPMN-to-BPMN call activities.
- Camunda state has **already been allowed to diverge from domain state in production data**:
  `PublicNoticeService.endPublicNotices(...)`/`getAvailablePublicNoticeActions(...)` carry an
  explicit guard and comment for "non-migrated workflows on public notices" with no active Camunda
  task. This is a live data point that the domain model already tolerates Camunda being wrong or
  absent for some rows — a real precedent for de-risking a cutover, not a hypothetical one.

This materially lowers both the size and the risk of this migration compared to a scenario where
Camunda's group/identity/admin features were load-bearing. The real work is concentrated in
**replicating the 16 userTask state machine's branch/loop logic in Java** and **untangling the ~24
call sites that currently call both a domain-status method and a Camunda method in the same
transaction**, not in replacing some large parallel subsystem.

**Effort figures are broad, single-engineer-day order-of-magnitude estimates**, ±40%, for
engineering work only — they exclude UAT/regression testing (assumed to sit outside the dev
team's scope, consistent with how the Oracle→Postgres investigation treated this), and exclude a
dedicated performance-validation pass.

## Summary table

| # | Section | Effort (days) | Risk |
|---|---|---|---|
| 1 | Implement the replacement state-transition mechanism (single mutable `current_task` column) | 3–5 | Low |
| 2 | Replicate the 3 BPMN state machines in Java (pwa.bpmn, consultations, public notice) | 6–10 | Medium |
| 3 | Rework the ~24 call sites that pair a domain-status change with a Camunda call | 8–12 | Medium |
| 4 | Task assignment (`WorkflowAssignmentService`/`Assignment`) — drop the Camunda-assignee write/read path | 2–3 | Low |
| 5 | Work area / task-list queries | 0.5–1 | Low |
| 6 | Message-event replacement (2 call sites: update-request, options-approved) | 1–2 | Low |
| 7 | `CamundaMigrationService` and BPMN-version-migration concern — drop entirely | 0 | None |
| 8 | Historic/audit data — Camunda history tables (`ACT_HI_*`) — confirmed not needed, drop with the schema | 0.5–1 | Low |
| 9 | Data backfill — populate the new `current_task` column for every existing application/consultation/public-notice row | 2–3 | Medium |
| 10 | Test updates (unit mocks + the one real-engine integration test) | 2–3 | Low |
| 11 | Build/config/infra removal (`build.gradle`, `application*.properties`, CI) | 0.5 | Low |
| 12 | Documentation & ADR updates | 1 | Low |
| | **Total** | **~27–41 days** | |

This is roughly **1.3–2 engineer-months** of effort if executed serially by one person. Sections
2–6 depend on section 1's mechanism landing first but can then mostly run in parallel by process
area (main application vs. consultations vs. public notice each touch disjoint files); sections
7–12 are small and can slot in around the main work.

---

## 1. Implement the replacement state-transition mechanism

**Effort: 3–5 days. Low risk — the storage-shape decision is made (see below); the remaining work
is implementing and unit-testing the mechanism itself.**

Camunda today provides three things PWA actually uses: (a) an authoritative "what task/state is
this case currently at" pointer per process instance, (b) validation that a `completeTask` call
targets the task the process is actually sitting at (throws if not), and (c) the BPMN's own
branch/loop wiring (gateway + conditional-flow expressions keyed off a `setWorkflowProperty` call
immediately before `completeTask`).

None of this requires a generic workflow/state-machine library to replace — the state machines
here are small (16 userTasks total across all three processes, one exclusive gateway, no
parallel/inclusive gateways, no sub-processes, no timers) and are already expressed as enums
(`PwaApplicationWorkflowTask`, `PwaApplicationConsultationWorkflowTask`,
`PwaApplicationPublicNoticeWorkflowTask`) plus small "decision" enums
(`ConsentReviewDecision`, `PwaAwaitPaymentResult`, etc. — these already implement `WorkflowProperty`
and encode exactly the BPMN condition-expression values).

**Decided: "current task" is a single mutable `current_task` column** per `WorkflowSubject` row
(`PwaApplication`, `ConsultationRequest`, `PublicNotice`), not an append-only history table —
consistent with PWA already having full status-history via `pad_status_versions`-style version rows
for the main application status, and with Camunda's own transition history (`ACT_HI_ACTINST`) not
being carried forward at all (section 8). The remaining implementation work: a small Java
`CaseWorkflowService.completeTask(subject, expectedTask, resultProperty)` replacing
`CamundaWorkflowService.completeTask`/`setWorkflowProperty`, which (1) asserts the row's current
task matches `expectedTask` (replacing Camunda's own guard), (2) looks up the next task via a
plain `switch`/lookup table per `WorkflowType` keyed on `(currentTask, resultProperty)` — a direct
Java transliteration of each BPMN's conditional sequence flows — and (3) writes the new value into
the `current_task` column, all inside the caller's existing `@Transactional` boundary. This keeps
the "two things change together" call shape callers already use (see section 3) rather than
introducing a new orchestration layer. Each of the 3 processes' lookup table is then a same-day
change once this pattern is implemented (see section 2).

## 2. Replicate the 3 BPMN state machines in Java

**Effort: 6–10 days.**

- **`pwa.bpmn` (8 userTasks, 1 exclusive gateway, 3 loops, 1 boundary message event)** — the most
  complex of the three: needs a lookup table keyed on `(currentTask, workflow property)` covering
  10 conditional flows, including the 3 re-entrant loops (`consentReview → caseOfficerReview` on
  `RETURN`, `issuingConsent → consentReview` on `FAILED`, `awaitApplicationPayment →
  applicationReview` on `CANCELLED`) and the one real gateway (`chargeDecision ==
  'WAIVED'/'REQUIRED'` after `applicationReview`). The boundary message event on `awaitFeedback`
  (non-interrupting, fires `updateApplication`) needs its own small "current task can be
  provisionally overridden by an update-request without losing the underlying await-feedback state"
  handling — see section 6, this is the one part of the state machine that isn't a plain linear
  `completeTask` call.
- **`pwa_app_consultations.bpmn` (2 userTasks, 0 gateways, 0 loops)** — trivial, a straight-line
  `allocation → response` transition, one day at most including tests.
- **`pwa_app_public_notice.bpmn` (6 userTasks, 0 explicit gateway shapes but 6 conditional flows,
  3 loops)** — moderate: `draft ↔ managerApproval`, `caseOfficerReview ↔ applicantUpdate`, and
  `publish ↔ waiting` are all two-way cycles already fully described by the existing
  `PublicNoticeCaseOfficerReviewResult`/`PublicNoticePublicationState`/
  `PwaApplicationPublicNoticeApprovalResult` enums — this is mechanical transliteration once the
  lookup-table pattern from `pwa.bpmn` is settled, not new design work.

Each process's lookup table should be unit-tested exhaustively against every branch the BPMN
defines (10 + 0 + 6 = 16 conditional flows total) — cheap given how few there are, and valuable
since this is the one place a transcription mistake would silently misroute a case.

## 3. Rework the ~24 call sites pairing a domain-status change with a Camunda call

**Effort: 8–12 days. Medium risk — this is where the bulk of the diff lands, but almost entirely
mechanical.**

24 files call `CamundaWorkflowService` today (`completeTask`/`setWorkflowProperty`/`startWorkflow`/
`deleteProcessAndTask`/`deleteProcessInstanceAndThenTasks`/`assignTaskToUser`). The good news,
confirmed by reading every call site: **almost all of them already wrap the domain-status change
and the Camunda call in a single `@Transactional` method** — `InitialReviewService.acceptApplication`,
`ApplicationChargeRequestService.processPaymentAttempt`/`cancelPaymentRequest`,
`ConsentReviewService.returnToCaseOfficer`/`scheduleConsentIssue`,
`ConsentIssueService.issueConsent`/`failConsentIssue`, all the public-notice services, etc. Since
the replacement mechanism from section 1 is a plain Java call inside the same transaction (not a separate
engine/process), the actual code change per call site is close to 1:1 — swap
`camundaWorkflowService.completeTask(...)`/`setWorkflowProperty(...)` for the new
`caseWorkflowService.completeTask(subject, task, result)` call, delete the now-unused
`WorkflowTaskInstance`/`WorkflowProperty` plumbing. No new transaction-boundary work is needed at
these sites.

Two call sites need real attention, not just mechanical swap-over:
- **`WithdrawApplicationService.withdrawApplication(...)`** — already **not** wrapped in one
  `@Transactional` boundary today (`setWithdrawn`, `deleteProcessInstanceAndThenTasks`, and
  `withdrawAllOpenConsultationRequests` are three separately-transactional calls). This is a
  pre-existing consistency gap, not something this migration introduces or needs to fix — treated
  as an existing bug outside this investigation's scope, so the call site is ported as-is (three
  separate transactional calls) rather than restructured.
- **`PublicNoticeService.endPublicNotices(...)`** — has to keep its existing "skip if no active
  Camunda task" guard as an equivalent "skip if no current-task record" guard, to correctly handle
  the already-existing legacy public notices with no workflow state.

`CaseReassignmentController`/`AssignCaseOfficerService`/`WorkflowAssignmentService.assignTaskInternal`
(section 4) and the 2 message-correlation call sites (section 6) are scoped separately below since they're
qualitatively different changes, not simple swap-overs.

## 4. Task assignment — drop the Camunda-assignee write/read path

**Effort: 2–3 days. Low risk.**

`WorkflowAssignmentService.assignTaskInternal(...)` currently does two independent writes for one
fact ("who is assigned"): `camundaWorkflowService.assignTaskToUser(...)` (Camunda's native
`TASK_ASSIGNEE`) and `assignmentService.createOrUpdateAssignment(...)` (PWA's own `assignments`
table, which is what all reporting/work-area/search views already join against — see section 5).
Candidate-eligibility (`getAssignmentCandidates`) already never touches Camunda (it switches on
`WorkflowAssignment.CASE_OFFICER`/`CONSULTATION_RESPONDER` against `PwaTeamService`). The only real
change: `WorkflowAssignmentService.getAssignee(...)` currently reads back from Camunda
(`getAssignedPersonId`) rather than from the local `Assignment` table — this read path needs
switching to read the `assignments` table instead (the reporting/search side already does this),
after which `assignTaskToUser`/`getAssignedPersonId` and the whole read-after-Camunda pattern can
be deleted outright. `Assignment`/`AssignmentAudit`/`AssignmentRepository`/`AssignmentAuditService`
need no changes at all — they're pure PWA tables today and remain so.

## 5. Work area / task-list queries

**Effort: 0.5–1 day. Low risk.**

`WorkAreaService`/`ApplicationWorkAreaPageService` already never query the Camunda engine — they
query the PWA `assignments` table and Flyway SQL views (`workarea_app_user_tabs`,
`vw_pwa_application_assignments`, etc.) directly. No change needed here beyond what falls out of
section 4 (the `Assignment` table stays authoritative, nothing new to wire up).

## 6. Message-event replacement

**Effort: 1–2 days. Low risk — exactly 2 call sites, both internal.**

Camunda message correlation is used for exactly one purpose across both BPMN processes that
reference it: interrupting `awaitFeedback` to move into `updateApplication`, triggered from
`ApplicationUpdateRequestService.submitApplicationUpdateRequest(...)` and (reusing the identical
message name) `OptionsCaseManagementWorkflowService.doOptionsApprovalWork(...)`. Neither is
triggered by an external system (no webhook/SNS/SQS/scheduled job calls message correlation) — both
are plain internal service calls. Replacing this is a same-shape change to section 1's mechanism: instead
of "assert current task, transition to next task," it's "record that an update was requested
against whatever task the case is currently at, transition to `updateApplication`, and remember the
prior task so it can be returned to" (the BPMN's boundary event is non-interrupting, so the
underlying `awaitFeedback` state isn't lost — the Java replacement needs the same "resume where we
left off" property, e.g. storing the interrupted task alongside the update-request row rather than
just overwriting current-task).

## 7. `CamundaMigrationService` and BPMN-version migration

**Effort: 0 days — this entire concern disappears.**

`CamundaMigrationService` exists solely to migrate in-flight Camunda process instances onto newer
deployed BPMN versions on every app boot. Once there's no BPMN/process-instance concept, this
class and its migration-plan logic (`mapEqualActivities().updateEventTriggers()`) is deleted
outright with no replacement needed — the in-app lookup tables from section 2 are just code, redeployed
like any other code change, no live "in-flight instance" migration step required.

## 8. Historic/audit data — Camunda history tables

**Effort: 0.5–1 days. Low risk — confirmed not needed; no export/migration required.**

`camunda.bpm.history-level=full` is configured, meaning Camunda's `ACT_HI_*` tables hold a complete
history of every task/activity instance ever completed, for every application/consultation/public
notice processed to date. Nothing in the Java codebase queries these tables today (confirmed —
no `@Query`/native SQL references `ACT_%` anywhere, and the one explicit reference,
`afterMigrate__002_grant_access_to_bi_schema.sql`, is a deliberate *exclusion* of these tables from
the BI grant). **Decided: this history has no reporting value and is dropped along with the engine
— no export into an equivalent in-app audit-trail shape is needed.** Remaining effort is just
confirming the Camunda schema (`ACT_*` tables) is safe to drop from each environment as part of
cutover, alongside the rest of section 11.

## 9. Data backfill — current-task state for existing rows

**Effort: 2–3 days. Medium risk.**

Every existing `PwaApplication`/`ConsultationRequest`/`PublicNotice` row with an active Camunda
process instance needs its new `current_task` column populated before cutover, derived from
`CamundaWorkflowService.getAllActiveWorkflowTasks(subject)` for each live subject (this method
already exists and is exercised by the one real-engine test, `CamundaWorkflowServiceTest`) — a
one-off migration script run as part of cutover, not a schema migration in the traditional Flyway
sense. Needs to also handle the already-known "non-migrated" legacy public notices with zero active
Camunda tasks (the Context section) — these get a null/empty `current_task`, and the guard logic
from section 3 needs to keep tolerating that, exactly as it does today.

## 10. Test updates

**Effort: 2–3 days. Low risk.**

440 of 669 test classes already mock `CamundaWorkflowService` via plain Mockito
(`@Mock private CamundaWorkflowService ...`) — swapping the mocked type to the new in-app service
across these is a mechanical rename in each affected test, not a redesign. The one test that
exercises the real Camunda engine end-to-end, `CamundaWorkflowServiceTest`
(`src/test/java/.../integrations/camunda/external/`), gets deleted and replaced by a new unit/
integration test suite for the section 1 lookup tables (already scoped into section 2's per-process effort
above) — no Camunda-specific test harness (`ProcessEngineRule`, `@Deployment`) is in use anywhere
today, so there's no test-infrastructure dependency to unwind beyond this one file.

## 11. Build/config/infra removal

**Effort: 0.5 days. Low risk — small, well-isolated surface.**

- `build.gradle:62` — remove the single `org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter:7.22.0`
  dependency (and its `jaxb-impl` exclusion, no longer needed).
- `application.properties:27-30` — remove the 4 `camunda.bpm.*` properties; remove the commented-out
  `logging.level.org.camunda.bpm.engine.*` lines in `application-debug*.properties`.
- No docker-compose/CI service needs touching — Camunda runs in-process against the app's own
  datasource today; there's no separate Camunda container anywhere in the repo.
- Camunda's own `ACT_*` schema (auto-created by the Camunda starter, not Flyway) can be dropped from
  each environment as part of cutover, after section 8/section 9 are complete.

## 12. Documentation & ADR updates

**Effort: 1 day.**

- Update or remove the "Two workflow engines running side by side" section of CLAUDE.md.
- No existing ADR references Camunda/workflow/BPMN (checked all 18) — worth adding a new ADR
  recording the in-app state-management approach and why it replaced Camunda, both for the removal
  decision itself and for the lookup-table pattern from section 1, so a future reader has a place to look
  (this codebase's ADR discipline is otherwise thorough — this is the one architecturally
  significant undocumented area found during this investigation).
