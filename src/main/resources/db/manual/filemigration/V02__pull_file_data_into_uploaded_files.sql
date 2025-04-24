CREATE OR REPLACE FUNCTION pwa.random_uuid
    RETURN VARCHAR2
AS LANGUAGE JAVA
    NAME 'java.util.UUID.randomUUID() return String';

BEGIN

INSERT INTO pwa.file_upload_library_uploaded_files (
    id,
    bucket,
    key,
    name,
    content_type,
    content_length,
    uploaded_at,
    usage_id,
    usage_type,
    document_type,
    description,
    uploaded_by
)
SELECT
    hextoraw(REPLACE(random_uuid(), '-', '')),
    sfm.s3_bucket bucket,
    sfm.s3_path key,
    uf.file_name,
    uf.content_type,
    uf.file_size content_length,
    uf.upload_datetime uploaded_at,
    pf.pad_id usage_id,
    'PwaApplicationDetail' usage_type,
    pf.purpose document_type,
    pf.description,
    uf.uploaded_by_wua_id uploaded_by
FROM
    pwa.uploaded_files uf
LEFT JOIN pwa.pad_files pf ON uf.file_id = pf.file_id
LEFT JOIN promotemgr.s3_file_migration sfm ON sfm.fox_file_id = uf.file_id AND sfm.reference = pf.pad_id
WHERE sfm.migrated_timestamp IS NOT NULL;

INSERT INTO pwa.file_upload_library_uploaded_files (
    id,
    bucket,
    key,
    name,
    content_type,
    content_length,
    uploaded_at,
    usage_id,
    usage_type,
    document_type,
    description,
    uploaded_by
)
SELECT
    hextoraw(REPLACE(random_uuid(), '-', '')),
    sfm.s3_bucket bucket,
    sfm.s3_path key,
    uf.file_name,
    uf.content_type,
    uf.file_size content_length,
    uf.upload_datetime uploaded_at,
    af.pa_id usage_id,
    'PwaApplication' usage_type,
    af.purpose document_type,
    af.description,
    uf.uploaded_by_wua_id uploaded_by
FROM
    pwa.uploaded_files uf
        LEFT JOIN pwa.app_files af ON uf.file_id = af.file_id
        LEFT JOIN promotemgr.s3_file_migration sfm ON sfm.fox_file_id = uf.file_id AND sfm.reference = af.pa_id
WHERE sfm.migrated_timestamp IS NOT NULL;

END;