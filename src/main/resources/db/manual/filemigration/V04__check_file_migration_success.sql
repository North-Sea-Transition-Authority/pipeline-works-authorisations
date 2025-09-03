SELECT
    CAST(pf.pad_id AS VARCHAR2(4000)),
    pf.purpose,
    count(pf.pad_id)
FROM pwa.pad_files pf
GROUP BY pf.pad_id, pf.purpose
MINUS
SELECT
    fulup.usage_id ,
    fulup.document_type,
    count(fulup.usage_id)
FROM pwa.file_upload_library_uploaded_files fulup
WHERE fulup.usage_type = 'PwaApplicationDetail'
GROUP BY fulup.usage_id, fulup.document_type
/


SELECT
    CAST(af.pa_id AS VARCHAR2(4000)),
    af.purpose,
    count(af.pa_id)
FROM pwa.app_files af
GROUP BY af.pa_id, af.purpose
MINUS
SELECT
    fulup.usage_id,
    fulup.document_type,
    count(fulup.usage_id)
FROM pwa.file_upload_library_uploaded_files fulup
WHERE fulup.usage_type = 'PwaApplication'
    AND fulup.document_type != 'CONSENT_PREVIEW'
GROUP BY fulup.usage_id, fulup.document_type
/


SELECT
    CAST(di.pwa_application_id AS VARCHAR2(4000)),
    'CONSENT_PREVIEW',
    count(di.pwa_application_id)
FROM pwa.docgen_runs dr
    JOIN pwa.document_instances di ON dr.di_id = di.id
WHERE dr.docgen_type = 'PREVIEW'
    AND dr.generated_doc IS NOT NULL
GROUP BY di.pwa_application_id, dr.docgen_type
MINUS
SELECT
    fulup.usage_id ,
    fulup.document_type,
    count(fulup.usage_id)
FROM pwa.file_upload_library_uploaded_files fulup
WHERE fulup.document_type ='CONSENT_PREVIEW'
GROUP BY fulup.usage_id, fulup.document_type
/


SELECT
    CAST(pc.id AS VARCHAR2(4000)),
    'CONSENT_DOCUMENT',
    count(pc.id)
FROM pwa.docgen_runs dr
    JOIN pwa.pwa_consents pc ON pc.docgen_run_id = dr.id
WHERE dr.docgen_type = 'FULL'
    AND dr.generated_doc IS NOT NULL
GROUP BY pc.id, dr.docgen_type
MINUS
SELECT
    fulup.usage_id ,
    fulup.document_type,
    count(fulup.usage_id)
FROM pwa.file_upload_library_uploaded_files fulup
WHERE fulup.document_type ='CONSENT_DOCUMENT'
GROUP BY fulup.usage_id, fulup.document_type
/