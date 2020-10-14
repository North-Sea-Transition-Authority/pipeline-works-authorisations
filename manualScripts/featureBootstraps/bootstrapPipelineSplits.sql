/* Scope is only application pipelines with at least 2 idents.
   split on every pipeline ident. randomly assign one org owner of role per split.
   */
DECLARE
  g_inclusive         VARCHAR2(4000) := 'INCLUSIVE';
  g_split_huoo_type   VARCHAR2(4000) := 'UNASSIGNED_PIPELINE_SPLIT';

  l_application_id    NUMBER         := :p_application_id;
  l_pad_id            NUMBER ;

  l_pipeline_id       NUMBER         := :p_pipeline_id;
  l_huoo_role         VARCHAR2(4000) := :p_huoo_role; -- [HOLDER, USER, OPERATOR, OWNER]
  l_create_split_role VARCHAR2(4000) := COALESCE(:p_create_split_role, 'false');
  l_split_pad_ord_role_id   NUMBER;
  l_start_idents      NUMBER;
  l_section_number    NUMBER          := 1;
BEGIN
  SELECT count(*)
  INTO l_start_idents
  FROM ${datasource.user}.pad_pipeline_idents ppi
       JOIN ${datasource.user}.pad_pipelines pp ON ppi.pp_id = pp.id
       JOIN ${datasource.user}.pwa_application_details pad ON pp.pad_id = pad.id
  WHERE pp.pipeline_id = l_pipeline_id AND pad.pwa_application_id = l_application_id AND pad.tip_flag = 1;

  IF (l_start_idents < 2)
  THEN
    RAISE_APPLICATION_ERROR(-20123, 'l_start_idents < 2');
  END IF;

  SELECT pad.id
  INTO l_pad_id
  FROM ${datasource.user}.pwa_application_details pad
  WHERE pad.pwa_application_id = l_application_id AND pad.tip_flag = 1;

  -- delete all existing role instances for pipeline and role
  DELETE
  FROM ${datasource.user}.pad_pipeline_org_role_links pporl
  WHERE pporl.pipeline_id = l_pipeline_id
  AND pporl.pad_org_role_id IN (
    SELECT por.id
    FROM ${datasource.user}.pad_organisation_roles por
    JOIN ${datasource.user}.pwa_application_details pad ON por.application_detail_id = pad.id
    WHERE pad.pwa_application_id = l_application_id AND pad.tip_flag = 1 AND por.role = l_huoo_role
  );

  if(l_create_split_role = 'true') THEN
    DELETE FROM ${datasource.user}.pad_organisation_roles por
    WHERE por.id IN (
      SELECT por2.ID
      FROM ${datasource.user}.pad_organisation_roles por2
      JOIN ${datasource.user}.pwa_application_details pad ON por2.application_detail_id = pad.id
      WHERE pad.pwa_application_id = l_application_id
      AND pad.tip_flag = 1
      AND por.role = l_huoo_role
      AND por.type = g_split_huoo_type
    );

    INSERT INTO ${datasource.user}.pad_organisation_roles (application_detail_id, type, role)
    VALUES (l_pad_id, g_split_huoo_type, l_huoo_role)
    RETURNING id INTO l_split_pad_ord_role_id;

  END IF;

  FOR ident IN (
    SELECT ppi.*
--          , LEAD(ppi.from_location) OVER (ORDER BY ppi.ident_no ASC) next_from_location
--          , LAG(ppi.to_location) OVER (ORDER BY ppi.ident_no ASC) previous_to_location
    FROM ${datasource.user}.pad_pipeline_idents ppi
         JOIN ${datasource.user}.pad_pipelines pp ON ppi.pp_id = pp.id
         JOIN ${datasource.user}.pwa_application_details pad ON pp.pad_id = pad.id
    WHERE pp.pipeline_id = l_pipeline_id AND pad.pwa_application_id = l_application_id AND pad.tip_flag = 1
    ORDER BY ppi.ident_no ASC
    )
    LOOP
      DECLARE
        l_role_id NUMBER;
      BEGIN
        -- going to blow up if no rows returned. This is desirable.
        SELECT id
        INTO l_role_id
        FROM (
               SELECT por.id
               FROM ${datasource.user}.pad_organisation_roles por
                    JOIN ${datasource.user}.pwa_application_details pad ON por.application_detail_id = pad.id
               WHERE pad.pwa_application_id = l_application_id 
               AND pad.tip_flag = 1 
               AND por.role = l_huoo_role
               AND (por.id =  l_split_pad_ord_role_id OR l_split_pad_ord_role_id IS NULL)
               ORDER BY dbms_random.random
             )
        FETCH FIRST 1 ROW ONLY;

        INSERT INTO ${datasource.user}.pad_pipeline_org_role_links pporl (pipeline_id,
                                                                          pad_org_role_id,
                                                                          from_location,
                                                                          from_location_mode,
                                                                          to_location,
                                                                          to_location_mode,
                                                                          section_number)
        VALUES (l_pipeline_id,
                l_role_id,
                ident.from_location,
                g_inclusive,
                ident.to_location,
                g_inclusive,
                l_section_number);

        l_section_number := l_section_number + 1;

      END;

    END LOOP;

  COMMIT;
END;

/
