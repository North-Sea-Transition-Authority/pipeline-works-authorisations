CREATE OR REPLACE FUNCTION pwa.random_uuid
    RETURN VARCHAR2
AS LANGUAGE JAVA
    NAME 'java.util.UUID.randomUUID() return String';

CREATE TABLE pwa.file_id_map (
    new_file_id VARCHAR2(4000),
    old_file_id VARCHAR2(4000),
    usage_type VARCHAR2(4000),
    usage_id VARCHAR2(4000),
    document_type VARCHAR2(4000)
);

BEGIN

    savemgr.savetool.table_save(
            p_owner => 'pwa'
        , p_name => 'pad_files'
    );

    savemgr.savetool.table_save(
            p_owner => 'pwa'
        , p_name => 'app_files'
    );

    FOR pad_record IN (
        SELECT
            hextoraw(REPLACE(random_uuid(), '-', '')) new_file_id,
            uf.file_id old_file_id,
            sfm.s3_bucket bucket,
            sfm.s3_path key,
            uf.file_name name,
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
        WHERE sfm.migrated_timestamp IS NOT NULL
        ) LOOP
            INSERT INTO pwa.FILE_UPLOAD_LIBRARY_UPLOADED_FILES (
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
            ) VALUES (
                pad_record.new_file_id,
                pad_record.bucket,
                pad_record.key,
                pad_record.name,
                pad_record.content_type,
                pad_record.content_length,
                pad_record.uploaded_at,
                pad_record.usage_id,
                pad_record.usage_type,
                pad_record.document_type,
                pad_record.description,
                pad_record.uploaded_by
            );

            IF pad_record.document_type = 'DEPOSIT_DRAWINGS' OR pad_record.document_type = 'PIPELINE_DRAWINGS' THEN

                INSERT INTO pwa.file_id_map (
                    new_file_id,
                    old_file_id,
                    usage_type,
                    usage_id,
                    document_type
                ) VALUES (
                    lower(regexp_replace(rawtohex(pad_record.new_file_id), '(\S{8})(\S{4})(\S{4})(\S{4})(.*)', '\1-\2-\3-\4-\5')),
                    pad_record.old_file_id,
                    pad_record.usage_type,
                    pad_record.usage_id,
                    pad_record.document_type
                );

            END IF;

    END LOOP;

    FOR app_record IN (
        SELECT
            hextoraw(REPLACE(random_uuid(), '-', '')) new_file_id,
            uf.file_id old_file_id,
            sfm.s3_bucket bucket,
            sfm.s3_path key,
            uf.file_name name,
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
        WHERE sfm.migrated_timestamp IS NOT NULL
        ) LOOP
            INSERT INTO pwa.FILE_UPLOAD_LIBRARY_UPLOADED_FILES (
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
            ) VALUES (
                app_record.new_file_id,
                app_record.bucket,
                app_record.key,
                app_record.name,
                app_record.content_type,
                app_record.content_length,
                app_record.uploaded_at,
                app_record.usage_id,
                app_record.usage_type,
                app_record.document_type,
                app_record.description,
                app_record.uploaded_by
            );

            IF app_record.document_type = 'CASE_NOTES' OR app_record.document_type = 'CONSULTATION_RESPONSE' OR app_record.document_type = 'PUBLIC_NOTICE' THEN

                INSERT INTO pwa.file_id_map (
                    new_file_id,
                    old_file_id,
                    usage_type,
                    usage_id,
                    document_type
                ) VALUES (
                    lower(regexp_replace(rawtohex(app_record.new_file_id), '(\S{8})(\S{4})(\S{4})(\S{4})(.*)', '\1-\2-\3-\4-\5')),
                    app_record.old_file_id,
                    app_record.usage_type,
                    app_record.usage_id,
                    app_record.document_type
                );

            END IF;

    END LOOP;

    INSERT INTO pwa.FILE_UPLOAD_LIBRARY_UPLOADED_FILES (
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
        sfm.filename name,
        'application/pdf' content_type,
        dbms_lob.getlength(dr.generated_doc),
        dr.completed_on,
        pc.id,
        'PwaConsent',
        'CONSENT_DOCUMENT',
        '',
        dr.scheduled_by_person_id
    FROM pwa.docgen_runs dr
        JOIN pwa.document_instances di ON dr.di_id = di.id
        JOIN pwa.PWA_CONSENTS pc ON pc.docgen_run_id = dr.ID
        JOIN promotemgr.s3_file_migration sfm ON sfm.fox_file_id = to_char(dr.id) AND sfm.reference = pc.id
    WHERE dr.docgen_type = 'FULL'
        AND sfm.migrated_timestamp IS NOT NULL;

    INSERT INTO pwa.FILE_UPLOAD_LIBRARY_UPLOADED_FILES (
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
        sfm.filename name,
        'application/pdf' content_type,
        dbms_lob.getlength(dr.generated_doc),
        dr.completed_on,
        pa.id,
        'PwaApplication',
        'CONSENT_PREVIEW',
        '',
        dr.scheduled_by_person_id
    FROM pwa.docgen_runs dr
        JOIN pwa.document_instances di ON dr.di_id = di.id
        JOIN pwa.pwa_applications pa ON di.pwa_application_id = pa.id
        JOIN promotemgr.s3_file_migration sfm ON sfm.fox_file_id = to_char(dr.id) AND sfm.reference = pa.id
    WHERE dr.docgen_type = 'PREVIEW'
      AND sfm.migrated_timestamp IS NOT NULL;

END;