# Architectural Decision Log

This log lists the architectural decisions for PWA.

<!-- adrlog -- Regenerate the content by using "npx adr-log -d docs/adr/ -i". -->

- [ADR-0000](0000-use-markdown-architectural-decision-records.md) - Use Markdown Architectural Decision Records
- [ADR-0001](0001-use-integers-rather-than-entity-mapping-for-audit-users.md) - Store wuaId integers for audit purposes rather than fully mapped WebUserAccount entities
- [ADR-0002](0002-avoid-lazy-loading.md) - Avoid lazy loading child entities
- [ADR-0003](0003-avoid-passing-form-submit-urls-to-views.md) - Avoid passing form submit urls to views
- [ADR-0004](0004-foreign-keys-to-tables-outside-app-schema.md) - Foreign keys to tables outside app schema
- [ADR-0005](0005-use-converters-rather-than-pre-post-load.md) - Use JPA Attribute Converters over Hibernate PrePersist/PostLoad hooks

<!-- adrlogstop -->

For new ADRs, please use [template.md](template.md) as basis.

More information on MADR is available at <https://adr.github.io/madr/>.

General information about architectural decision records is available at <https://adr.github.io/>.
