BEGIN

    FOR file_map_rec IN (
        SELECT fim.old_file_id, fim.new_file_id, fim.usage_id
        FROM pwa.file_id_map fim
    )
    LOOP

        UPDATE pwa.pad_files
        SET file_id = file_map_rec.new_file_id
        WHERE file_id = file_map_rec.old_file_id AND pad_id = file_map_rec.usage_id;

        UPDATE pwa.app_files
        SET file_id = file_map_rec.new_file_id
        WHERE file_id = file_map_rec.old_file_id AND pa_id = file_map_rec.usage_id;

    END LOOP;

END;