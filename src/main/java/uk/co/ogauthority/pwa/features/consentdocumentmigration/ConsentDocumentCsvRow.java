package uk.co.ogauthority.pwa.features.consentdocumentmigration;

public record ConsentDocumentCsvRow(
    String pwaReference,
    String field,
    String consentDocument,
    String consentDate,
    String consentType,
    String scanned,
    String notes,
    String incorrectLocation,
    String action
) {}