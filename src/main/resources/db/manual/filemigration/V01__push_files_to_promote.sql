INSERT INTO promotemgr.s3_file_migration (
    fox_file_id,
    application,
    reference,
    directory,
    filename,
    content
)
SELECT
    pf.file_id,
    'PWA' application,
    pf.pad_id reference,
    'migrated' directory,
    uf.file_name || '__' || pf.file_id filename,
    uf.file_data content
FROM pwa.pad_files pf
    LEFT JOIN pwa.uploaded_files uf on pf.file_id = uf.file_id
WHERE pf.file_id IS NOT NULL
    AND uf.file_data IS NOT NULL;

INSERT INTO promotemgr.s3_file_migration (
    fox_file_id,
    application,
    reference,
    directory,
    filename,
    content
)
SELECT
    af.file_id,
    'PWA' application,
    af.pa_id reference,
    'migrated' directory,
    uf.file_name || '__' || af.file_id filename,
    uf.file_data content
FROM pwa.app_files af
    LEFT JOIN pwa.uploaded_files uf on af.file_id = uf.file_id
WHERE af.file_id IS NOT NULL
    AND uf.file_data IS NOT NULL;

INSERT INTO promotemgr.s3_file_migration (
    fox_file_id,
    application,
    reference,
    directory,
    filename,
    content
)
SELECT
    dr.id,
    'PWA' application,
    pa.id reference,
    'migrated' directory,
    REPLACE(pa.consent_reference, '/', '-')  || ' consent document.pdf' filename,
    dr.generated_doc content
FROM pwa.docgen_runs dr
    JOIN pwa.document_instances di ON dr.di_id = di.id
    JOIN pwa.pwa_applications pa ON di.pwa_application_id = pa.id
WHERE dr.docgen_type = 'FULL'
    AND dr.status = 'COMPLETE';

INSERT INTO promotemgr.s3_file_migration (
    fox_file_id,
    application,
    reference,
    directory,
    filename,
    content
)
SELECT
    dr.id,
    'PWA' application,
    pa.id reference,
    'migrated' directory,
    REPLACE(pa.app_reference, '/', '-')  || ' consent preview' || dr.ID || '.pdf' filename,
    dr.generated_doc content
FROM pwa.docgen_runs dr
    JOIN pwa.document_instances di ON dr.di_id = di.id
    JOIN pwa.pwa_applications pa ON di.pwa_application_id = pa.id
WHERE dr.docgen_type = 'PREVIEW'
    AND dr.status = 'COMPLETE'
    AND dr.generated_doc IS NOT NULL;
