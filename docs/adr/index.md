# Architectural Decision Log

This log lists the architectural decisions for PWA.

<!-- adrlog -- Regenerate the content by using "npx adr-log -d docs/adr/ -i". -->

- [ADR-0000](0000-use-markdown-architectural-decision-records.md) - Use Markdown Architectural Decision Records
- [ADR-0001](0001-use-integers-rather-than-entity-mapping-for-audit-users.md) - Store wuaId integers for audit purposes rather than fully mapped WebUserAccount entities
- [ADR-0002](0002-avoid-lazy-loading.md) - Avoid lazy loading child entities
- [ADR-0003](0003-avoid-passing-form-submit-urls-to-views.md) - Avoid passing form submit urls to views
- [ADR-0004](0004-foreign-keys-to-tables-outside-app-schema.md) - Foreign keys to tables outside app schema
- [ADR-0005](0005-use-converters-rather-than-pre-post-load.md) - Use JPA Attribute Converters over Hibernate PrePersist/PostLoad hooks
- [ADR-0006](0006-access-restriction-investigation.md) - Use custom annotations for restricting access to endpoints
- [ADR-0007](0007-use-pojo-nested-validators-for-multi-value-form-inputs.md) - Use POJO nested validators for multi-value form inputs
- [ADR-0008](0008-dont-use-prepersist-preupdate-with-transient-fields.md) - Don't rely on PrePersist or PreUpdate annotations if using transient fields
- [ADR-0009](0009-flash-messages.md) - Flash message guidelines
- [ADR-0010](0010-use-ftlvariables-in-templates.md) - Define ftlvariables in .ftl templates to assist devs and avoid IntelliJ warnings
- [ADR-0011](0011-use-enum-parameters-over-booleans.md) - Use enum parameters over booleans when defining method parameters, return types etc
- [ADR-0012](0012-use-spring-message-validation-for-type-mismatches.md) - Use spring message validation for type mismatches when binding to forms
- [ADR-0013](0013-enable-idempotency-for-post-requests.md) - Enable post requests to be idempotent to prevent duplicate submissions
- [ADR-0014](0014-pwa-api-adr.md) - API to retrieve PWAs and pipelines
- [ADR-0015](0015-pwa-initial-consent-date-in-api.md) - Adding initial consent date to PWA
- [ADR-0016](0016-how-to-handle-pwa-privileges-in-conversion-teams-pattern.md) - How to handle PWA Privileges in conversion to Teams Pattern
- [ADR-0017](0017-migrate-files-from-oracle-to-s3-for-conversion-to-the-file-upload-library) - Migrate files from Oracle to s3 for conversion to the File Upload Library
- [ADR-0018](0018-upload-legacy-consent-documents.md) - Upload legacy consent documents to PWA

<!-- adrlogstop -->

For new ADRs, please use [template.md](template.md) as basis.

More information on MADR is available at <https://adr.github.io/madr/>.

General information about architectural decision records is available at <https://adr.github.io/>.
