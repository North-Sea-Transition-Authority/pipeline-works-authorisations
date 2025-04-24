CREATE TABLE pwa.file_id_map (
    new_file_id VARCHAR2(4000),
    old_file_id VARCHAR2(4000),
    usage_type VARCHAR2(4000),
    usage_id VARCHAR2(4000),
    document_type VARCHAR2(4000)
);

INSERT INTO pwa.file_id_map (
    new_file_id,
    old_file_id,
    usage_type,
    usage_id,
    document_type
)
SELECT
    lower(regexp_replace(rawtohex(fuluf.id), '(\S{8})(\S{4})(\S{4})(\S{4})(.*)', '\1-\2-\3-\4-\5')) new_file_id,
    pf.file_id old_file_id,
    fuluf.usage_type,
    fuluf.usage_id,
    fuluf.document_type
FROM pwa.file_upload_library_uploaded_files fuluf
    JOIN pwa.pad_files pf ON fuluf.usage_id = pf.pad_id
        AND pf.purpose = fuluf.document_type
WHERE fuluf.usage_type = 'PwaApplicationDetail'
    AND (fuluf.document_type = 'DEPOSIT_DRAWINGS' OR fuluf.document_type = 'PIPELINE_DRAWINGS');

INSERT INTO pwa.file_id_map (
    new_file_id,
    old_file_id,
    usage_type,
    usage_id,
    document_type
)
SELECT
    lower(regexp_replace(rawtohex(fuluf.id), '(\S{8})(\S{4})(\S{4})(\S{4})(.*)', '\1-\2-\3-\4-\5')) new_file_id,
    af.file_id old_file_id,
    fuluf.usage_type,
    fuluf.usage_id,
    fuluf.document_type
FROM pwa.file_upload_library_uploaded_files fuluf
     JOIN pwa.app_files af ON fuluf.usage_id = af.pa_id
         AND af.purpose = fuluf.document_type
WHERE fuluf.usage_type = 'PwaApplication'
    AND (fuluf.document_type = 'CASE_NOTES' OR fuluf.document_type = 'CONSULTATION_RESPONSE' OR fuluf.document_type = 'PUBLIC_NOTICE');