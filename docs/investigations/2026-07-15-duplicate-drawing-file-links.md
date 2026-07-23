# Duplicated deposit/pipeline drawing rows with broken file links

**Date:** 2026-07-15
**Area:** `permdeposit` (PAD technical drawings) and `pipelines` (pipeline technical drawings)

## Symptom

Production data showed duplicated rows for what should have been a single drawing:

- Two rows in `pad_deposit_drawings` for one logical PAD deposit drawing, only one of which was
  correctly linked to an uploaded S3 file.
- The equivalent duplication also existed for `pad_technical_drawings` (pipeline drawings).

Viewing the affected drawing summary threw:

```
PwaEntityNotFoundException: Unable to get UploadedFileView of file with ID <id>
```

This is thrown by `DepositDrawingsService.buildSummaryView()` (and the equivalent pipeline drawing
code) whenever a drawing's `PadFile` reference is non-null but its `fileId` doesn't match any
`UploadedFileView` actually available for that application detail.

## Where the drawings and files live

- `PadDepositDrawing` (`pad_deposit_drawings`) and `PadTechnicalDrawing` (`pad_technical_drawings`)
  both have a `pad_id` FK scoping them to a single `PwaApplicationDetail` (i.e. one specific
  application version), and a `pf_id` FK to a `PadFile` bridge row.
- `PadFile` (`pad_files`) is the in-schema bridge entity described in
  [ADR-0004](../adr/0004-foreign-keys-to-tables-outside-app-schema.md): it holds a `fileId`
  (a `UUID`, stored as a string) pointing at an `UploadedFile` row owned by the external
  `uk.co.fivium:file-upload-spring-boot-starter` library, which is where the S3-backed file
  metadata actually lives.

Both drawing rows and their bridge `PadFile` rows only ever get created in two places:

1. The per-page add/edit flow (`DepositDrawingsService.addDrawing`/`editDepositDrawing`) — a
   normal single-row insert/update per user submission.
2. **Application version copying** — when a new `PwaApplicationDetail` version is created (a
   variation, or an "update requested" cycle), every form section's `copySectionInformation(
   fromDetail, toDetail)` implementation clones that section's rows from the previous version into
   the new one. This is where the bug lived, in two sibling implementations:
   - `PermanentDepositService.copySectionInformation()` (deposit drawings)
   - `PadPipelineDataCopierService.copyAllPadPipelineData()` /
     `copyPipelineDrawingData()` (pipeline technical drawings), called from
     `PadPipelineTaskListService.copySectionInformation()`

## Root cause

### 1. Fragile file re-linking using a natural key instead of the id the library already returns

`EntityCopyingService.duplicateEntitiesAndSetParent(...)` (used to clone both the drawing rows and
the `PadFile` bridge rows) does a raw reflection field-copy of every entity field, including the
bridge row's `fileId` — so immediately after cloning, a copied `PadFile` row still points at the
**old** application detail's file, not the new copy.

Both `copySectionInformation` implementations tried to fix this up afterwards by:

1. Calling `PadFileManagementService.copyUploadedFiles(fromDetail, toDetail, documentType)` to
   duplicate the underlying `UploadedFile` rows via the file-upload library's
   `FileService.copy(uploadedFile, usageBuilder)` — **discarding its return value**.
2. Re-deriving the mapping from the *old* file to the *newly copied* file by matching on a natural
   key of `Pair.of(file.getName(), file.getUploadedAt())` between the pre-copy and post-copy file
   lists for that detail:

   ```java
   var fromFiles = padFileManagementService.getUploadedFiles(fromDetail, documentType).stream()
       .collect(Collectors.toMap(file -> Pair.of(file.getName(), file.getUploadedAt()), Function.identity()));
   var toFiles = padFileManagementService.getUploadedFiles(toDetail, documentType).stream()
       .collect(Collectors.toMap(file -> Pair.of(file.getName(), file.getUploadedAt()), Function.identity()));
   var originalToCopiedFileIDs = fromFiles.entrySet().stream()
       .collect(Collectors.toMap(e -> e.getValue().getId(), e -> toFiles.get(e.getKey()).getId()));
   ```

This is unreliable by construction: it depends on `(name, uploadedAt)` being unique and stable
across the copy operation, rather than using an id the copy operation could simply hand back. If
that pairing ever fails to line up cleanly (any timestamp drift on the copy, more than one file
sharing a name, or the same detail's copy step running more than once — see below), the resulting
`PadFile.fileId` on the copied drawing points at nothing valid for that detail, and viewing the
drawing later throws `PwaEntityNotFoundException`.

Decompiling `uk.co.fivium:file-upload-spring-boot-starter:v2.9.0`'s
`FileService.copy(UploadedFile, Function)` confirmed the fix that was available all along — the
method **already returns the newly created `UploadedFile`** (with its real generated id):

```java
public UploadedFile copy(UploadedFile uploadedFile, Function<FileUsage.Builder, FileUsage> fileUsageFunction) {
  ...
  newUploadedFile = uploadedFileRepository.save(newUploadedFile);
  s3FileService.copy(...);
  return newUploadedFile;
}
```

`PadFileManagementService.copyUploadedFiles` was simply throwing this away instead of building a
direct original-id → copied-id map from it.

### 2. No idempotency guard on the version-copy path

`EntityCopyingService.duplicateEntitiesAndSetParent(...)` has no de-duplication of any kind — it
unconditionally inserts a fresh copy of every source row on every call. Neither
`PermanentDepositService.copySectionInformation` nor `PadPipelineDataCopierService
.copyAllPadPipelineData` guarded against being invoked more than once for the same
`(fromDetail, toDetail)` pair. If application-version creation is ever retried for the same target
detail (e.g. a request timeout/resubmission), every deposit, drawing, pipeline, ident and drawing
link for that detail would be inserted a second time — this is the direct mechanism that produces
two `pad_deposit_drawings`/`pad_technical_drawings` rows for one logical drawing.

This class of bug is a known, previously-unresolved gap in the app: see
[ADR-0013](../adr/0013-enable-idempotency-for-post-requests.md), which documents the exact same
"duplicate entity from resubmission" problem for the "add pipeline" page but never landed on an
implemented solution — there is no idempotency-key mechanism anywhere in the codebase today.
`DepositDrawingsService.addDrawing()` still has this same unguarded-insert gap for the per-page add
flow; it was flagged but deliberately left unfixed as a larger, separate piece of work (see
"Not fixed" below).

## Fix

Applied identically to both affected features:

1. **`PadFileManagementService.copyUploadedFiles`** now returns `Map<UUID, UUID>` (original file id
   → copied file id), built directly from `FileService.copy(...)`'s return value, instead of
   `void`. This removes the natural-key correlation entirely — there is no longer a `(name,
   uploadedAt)` pairing step to get wrong. All other pre-existing callers of this method already
   ignored its return value as a statement, so this is source-compatible everywhere else.

2. **`PermanentDepositService.copySectionInformation`** and **`PadPipelineDataCopierService
   .copyPipelineDrawingData`** now consume that map directly instead of re-deriving it, removing
   ~20 lines of fragile join logic (and the now-unused `Pair`/`Function`/`Collectors` imports) from
   each.

3. Both **`PermanentDepositService.copySectionInformation`** and **`PadPipelineDataCopierService
   .copyAllPadPipelineData`** gained an idempotency guard at the top: if the target detail already
   has drawings copied into it, log a warning and return instead of copying again.

## Verification

- Added `PermanentDepositServiceTest#copySectionInformation_alreadyCopied_isNoOp` and
  `#copySectionInformation_copiesDrawingsAndRepointsFileLinkAtCopiedFile`.
- Added `PadPipelineDataCopierServiceTest` (new file — none existed before) with the equivalent two
  tests: `copyAllPadPipelineData_alreadyCopied_isNoOp` and
  `copyAllPadPipelineData_copiesDrawingsAndRepointsFileLinkAtCopiedFile`.
- Updated `PadFileManagementServiceTest#copyUploadedFiles` to stub `FileService.copy(...)`'s return
  value and assert the returned id map.
- All of the above pass, `PadPipelineTaskListServiceTest` (existing consumer) still passes,
  `compileJava`/`compileTestJava` are clean, and `checkstyleMain`/`checkstyleTest` report zero
  violations on any touched or new file.

## Not fixed (out of scope for this change)

`DepositDrawingsService.addDrawing()` (and its pipeline-drawing equivalent) still has no
double-submission guard for the per-page add flow — a double-click/back-button resubmit can still
insert two drawing rows for one form submission. This is the same unresolved problem
[ADR-0013](../adr/0013-enable-idempotency-for-post-requests.md) documents for "add pipeline."
Fixing it properly means picking an app-wide idempotency strategy (idempotency token, DB
constraint, etc.), which is a bigger decision than this investigation's scope.
