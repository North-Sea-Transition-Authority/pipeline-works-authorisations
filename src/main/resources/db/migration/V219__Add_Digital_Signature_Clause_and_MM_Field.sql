DECLARE
  g_digital_signature CONSTANT VARCHAR2(20) := 'DIGITAL_SIGNATURE';
  l_signature_section_id NUMBER;
  l_signature_clause_id NUMBER;
  l_signature_section_count NUMBER;
  l_mm_signature_count NUMBER;

  l_di_scv_id NUMBER;
  l_di_sc_id NUMBER;
  l_dt_sc_id NUMBER;
BEGIN

  FOR dt IN (
    SELECT dt.id
    FROM ${datasource.user}.document_templates dt
  ) LOOP
    -- Check if the DIGITAL_SIGNATURE section already exists to avoid duplicates
    SELECT COUNT(*)
    INTO l_signature_section_count
    FROM ${datasource.user}.dt_sections s
    WHERE s.dt_id = dt.id
      AND s.name = g_digital_signature;

    IF l_signature_section_count = 0 THEN
      -- Insert the DIGITAL_SIGNATURE section
      INSERT INTO ${datasource.user}.dt_sections (dt_id, name, status, start_timestamp)
      VALUES (dt.id, g_digital_signature, 'ACTIVE', SYSTIMESTAMP)
      RETURNING id INTO l_signature_section_id;

      -- Insert a clause for the digital signature
      INSERT INTO ${datasource.user}.dt_section_clauses (id, s_id)
      VALUES (${datasource.user}.dt_section_clauses_id_seq.NEXTVAL, l_signature_section_id)
      RETURNING id INTO l_signature_clause_id;

      -- Insert a clause version that includes the ((SIGNATURE)) mail merge field
      INSERT INTO ${datasource.user}.dt_section_clause_versions (
        id
      , sc_id
      , version_no
      , tip_flag
      , name
      , text
      , parent_sc_id
      , level_order
      , status
      , created_timestamp
      , created_by_person_id
      ) VALUES (
        ${datasource.user}.dt_scv_id_seq.NEXTVAL
      , l_signature_clause_id
      , 1
      , 1
      , 'Digital Signature'
      , '((DIGITAL_SIGNATURE))'
      , null -- no parent clause, top-level
      , 1
      , 'ACTIVE'
      , SYSTIMESTAMP
      , 1
      );

    END IF;

  END LOOP;

  FOR di IN (
    SELECT di.id di_id, di.dt_id
    FROM ${datasource.user}.document_instances di
  ) LOOP
    -- Check if the DIGITAL_SIGNATURE section instance already exists to avoid duplicates
    SELECT COUNT(*)
    INTO l_signature_section_count
    FROM ${datasource.user}.di_section_clauses disc
    JOIN ${datasource.user}.dt_section_clauses dtsc ON disc.dt_sc_id = dtsc.id
    JOIN ${datasource.user}.dt_sections dts ON dts.id = dtsc.s_id
    WHERE disc.di_id = di.di_id
      AND dts.name = g_digital_signature;

    IF l_signature_section_count = 0 THEN

      SELECT dtsc.id
      INTO l_dt_sc_id
      FROM ${datasource.user}.dt_sections s
      JOIN ${datasource.user}.dt_section_clauses dtsc ON dtsc.s_id = s.id
      WHERE s.dt_id = di.dt_id
        AND s.name = g_digital_signature;

      -- Insert an instance clause for the digital signature
      INSERT INTO ${datasource.user}.di_section_clauses (
        id
      , di_id
      , dt_sc_id
      )
      VALUES (
        ${datasource.user}.di_section_clauses_id_seq.nextval
      , di.di_id
      , l_dt_sc_id
      )
      RETURNING id INTO l_di_sc_id;

      -- Insert an instance clause version that includes the ((SIGNATURE)) mail merge field
      INSERT INTO ${datasource.user}.di_sc_versions (
        id
      , di_sc_id
      , version_no
      , tip_flag
      , name
      , text
      , parent_di_sc_id
      , level_order
      , status
      , created_timestamp
      , created_by_person_id
      )
      VALUES (
        ${datasource.user}.di_sc_versions_id_seq.nextval
      , l_di_sc_id
      , 1
      , 1
      , 'Digital Signature'
      , '((DIGITAL_SIGNATURE))'
      , NULL
      , 1
      , 'ACTIVE'
      , SYSTIMESTAMP
      , 1
      )
      RETURNING id INTO l_di_scv_id;

    END IF;

  END LOOP;

  -- Check if SIGNATURE mail merge field already exists
  SELECT COUNT(*)
  INTO l_mm_signature_count
  FROM ${datasource.user}.mail_merge_fields
  WHERE mnem = g_digital_signature;

  IF l_mm_signature_count = 0 THEN
      INSERT INTO ${datasource.user}.mail_merge_fields (mnem, type)
      VALUES (g_digital_signature, 'AUTOMATIC');
  END IF;

END;
/
