/*
   For a PWA consent and a pipeline on that consent, add some new idents to the pipeline (up to 4)
   and create splits in the consented model for each ident, using a randomly assigned organisation
   from the HUOOs on the consent (for a given role).

   N.B. Should only be used on targeted migrated consents on dev/st/uat for testing purposes.
*/
DECLARE
  g_inclusive         VARCHAR2(4000) := 'INCLUSIVE';

  l_pwa_consent_id    NUMBER         := :p_pwa_consent_id;
  l_pd_id             NUMBER;

  l_detail_ident_id   NUMBER;
  l_detail_ident_data_id NUMBER;

  l_pipeline_id       NUMBER         := :p_pipeline_id;
  l_huoo_role         VARCHAR2(4000) := :p_huoo_role; -- [HOLDER, USER, OPERATOR, OWNER]

  l_start_idents      NUMBER;
  l_section_number    NUMBER          := 1;
BEGIN

  -- get number of idents
  SELECT count(*)
  INTO l_start_idents
  FROM ${datasource.user}.pipeline_detail_idents pdi
  JOIN ${datasource.user}.pipeline_details pd ON pdi.pipeline_detail_id = pd.id
  JOIN ${datasource.user}.pwa_consents pc ON pc.id = pd.pwa_consent_id
  WHERE pd.pipeline_id = l_pipeline_id
  AND pd.pwa_consent_id = l_pwa_consent_id
  AND pd.tip_flag = 1;

  -- get the tip pipeline detail for the pipeline
  SELECT pd.id
  INTO l_pd_id
  FROM ${datasource.user}.pipeline_details pd
  WHERE pd.pipeline_id = l_pipeline_id
  AND pd.tip_flag = 1;

  -- create some more idents if needed so we can showcase splits
  IF (l_start_idents < 3)
  THEN

    FOR i IN l_start_idents..3 LOOP

        INSERT INTO ${datasource.user}.pipeline_detail_idents ( pipeline_detail_id
                                                              , ident_no
                                                              , from_location
                                                              , to_location
                                                              , length)
        VALUES ( l_pd_id
               , i + 1 -- default ident number
               , 'from' || (i + 1)
               , 'to' || (i + 1)
               , 1)
        RETURNING id INTO l_detail_ident_id;

        INSERT INTO ${datasource.user}.pipeline_detail_ident_data ( pipeline_detail_ident_id
                                                                  , external_diameter
                                                                  , wall_thickness
                                                                  , maop)
        VALUES ( l_detail_ident_id
               , 5
               , 9
               , 2)
        RETURNING id INTO l_detail_ident_data_id;

    END LOOP;

  END IF;

  -- delete all existing role instances for pipeline and role
  DELETE
  FROM ${datasource.user}.pipeline_org_role_links porl
  WHERE porl.pipeline_id = l_pipeline_id
  AND porl.pwa_consent_org_role_id IN (
    SELECT por.id
    FROM ${datasource.user}.pwa_consent_organisation_roles por
    JOIN ${datasource.user}.pwa_consents pc ON por.added_by_pwa_consent_id = pc.id AND por.end_timestamp IS NULL
    WHERE pc.id = l_pwa_consent_id
    AND por.role = l_huoo_role
  );

  FOR ident IN (
    SELECT pdi.*
    FROM ${datasource.user}.pipeline_detail_idents pdi
    JOIN ${datasource.user}.pipeline_details pd ON pdi.pipeline_detail_id = pd.id
    WHERE pd.id = l_pd_id
    ORDER BY pdi.ident_no ASC
  ) LOOP
      DECLARE
        l_role_id NUMBER;
      BEGIN

        -- going to blow up if no rows returned. This is desirable.
        SELECT id
        INTO l_role_id
        FROM (
           SELECT por.id
           FROM ${datasource.user}.pwa_consent_organisation_roles por
           JOIN ${datasource.user}.pwa_consents pc ON por.added_by_pwa_consent_id = pc.id
           WHERE pc.id = l_pwa_consent_id
           AND por.end_timestamp IS NULL
           AND por.role = l_huoo_role
           ORDER BY dbms_random.random
        )
        FETCH FIRST 1 ROW ONLY;

        INSERT INTO ${datasource.user}.pipeline_org_role_links pporl (
          pipeline_id,
          pwa_consent_org_role_id,
          added_by_pwa_consent_id,
          from_location,
          from_location_mode,
          to_location,
          to_location_mode,
          section_number,
          start_timestamp)
        VALUES (
           l_pipeline_id,
           l_role_id,
           l_pwa_consent_id,
           ident.from_location,
           g_inclusive,
           ident.to_location,
           g_inclusive,
           l_section_number,
           SYSDATE
         );

        l_section_number := l_section_number + 1;

      END;

    END LOOP;

  COMMIT;
END;
/
-- find consents and pipelines to patch based on org group
SELECT
  pc.id pwa_consent_id,
  pc.reference consent_ref,
  pd.pipeline_id
FROM ${datasource.user}.pwa_consents pc
JOIN ${datasource.user}.pwa_consent_organisation_roles r ON r.added_by_pwa_consent_id = pc.id
JOIN ${datasource.user}.pipeline_details pd ON pd.pwa_consent_id = pc.id AND pd.end_timestamp IS NULL
where r.role = 'HOLDER'
and r.ou_id IN (
  SELECT cogo.organ_id
  FROM decmgr.current_org_grp_organisations cogo
  WHERE COGO.ORG_GRP_ID = 116 -- ROYAL DUTCH SHELL
)
/