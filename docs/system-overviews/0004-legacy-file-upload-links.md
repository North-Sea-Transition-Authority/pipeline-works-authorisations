# System Overview - Legacy File Upload Links

## Introduction
This document provides a quick overview of the legacy file upload entities that were left after converting PWA to use the File Upload Spring Starter (FUSS).

## Background

### Before the conversion

Before converting to use FUSS, two different classes were used for file uploads:

- `PadFile` was used for uploads on a submission before it reached a case officer
- `AppFile` was used for uploads on a PWA after approval by a case officer

### After the conversion

After the conversion, all files use the `UploadedFile` class provided by FUSS. This links files to the PAD/PWA id that they belong to. 
Legacy systems were removed wherever possible, but remain in situations where necessary.
In places where the legacy classes are still used, uploads are handled by FUSS, and the file id is stored in the legacy objects to maintain the previous data structure.

## Document types where the legacy system has been removed

- `ADMIRALTY_CHART`
- `BLOCK_CROSSINGS`
- `CABLE_CROSSINGS`
- `CARBON_STORAGE_CROSSINGS`
- `LOCATION_DETAILS`
- `MEDIAN_LINE_CROSSING`
- `OPTIONS_TEMPLATE`
- `PARTNER_LETTERS`
- `PIPELINE_CROSSINGS`
- `PROJECT_EXTENSION`
- `PROJECT_INFORMATION`
- `SUPPLEMENTARY_DOCUMENTS`
- `UMBILICAL_CROSS_SECTION`

## Document types where the legacy system is still in place

| Document type           | Legacy file type | Reason the legacy files are still in place                                                                  |
|:------------------------|:-----------------|:------------------------------------------------------------------------------------------------------------|
| `DEPOSIT_DRAWINGS`      | `PadFile`        | Files are linked to a `PadTechnicalDrawing` and linked to a PWA application via a `PadTechnicalDrawingLink` |
| `PIPELINE_DRAWINGS`     | `PadFile`        | Files are linked to a `PadTechnicalDrawing` and linked to a PWA application via a `PadTechnicalDrawingLink` |
| `CASE_NOTES`            | `AppFile`        | Files are linked to a case note via a `caseNoteDocumentLink` not the PWA itself                             |
| `PUBLIC_NOTICE`         | `AppFile`        | Files are linked to a public notice via a `PublicNoticeDocumentLink` not the PWA itself                     |
| `CONSULTATION_RESPONSE` | `AppFile`        | Files are linked to a consultation response via a `ConsultationResponseFileLink` not the PWA itself         |
