ALTER TABLE ${datasource.user}.pipeline_org_role_links DROP CONSTRAINT porl_location_ck;

ALTER TABLE ${datasource.user}.pipeline_org_role_links ADD (
  section_number NUMBER
, CONSTRAINT porl_location_ck CHECK (
  (
    from_location IS NULL
    AND from_location_mode IS NULL
    AND to_location IS NULL
    AND to_location_mode IS NULL
    AND section_number IS NULL
  )
  OR
  (
    from_location IS NOT NULL
    AND from_location_mode IS NOT NULL
    AND to_location IS NOT NULL
    AND to_location_mode IS NOT NULL
    AND section_number IS NOT NULL
  )
  )
);


ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links DROP CONSTRAINT pporl_location_ck;

ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links ADD (
  section_number NUMBER
, CONSTRAINT pporl_location_ck CHECK (
  (
    from_location IS NULL
    AND from_location_mode IS NULL
    AND to_location IS NULL
    AND to_location_mode IS NULL
    AND section_number IS NULL
  )
  OR
  (
    from_location IS NOT NULL
    AND from_location_mode IS NOT NULL
    AND to_location IS NOT NULL
    AND to_location_mode IS NOT NULL
    AND section_number IS NOT NULL
  )
  )
);
