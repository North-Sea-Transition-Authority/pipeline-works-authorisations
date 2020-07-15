ALTER TABLE ${datasource.user}.pipeline_org_role_links ADD (
  from_location VARCHAR2(4000)
, from_location_mode VARCHAR2(100)
, to_location VARCHAR2(4000)
, to_location_mode VARCHAR2(100)
, CONSTRAINT porl_location_ck CHECK (
  (from_location IS NULL
    AND from_location_mode IS NULL
    AND to_location IS NULL
    AND to_location_mode IS NULL)
  OR
  (from_location IS NOT NULL
    AND from_location_mode IS NOT NULL
    AND to_location IS NOT NULL
    AND to_location_mode IS NOT NULL)
  )
);

ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links
  DROP CONSTRAINT pporl_pipeline_role_unique;

ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links ADD (
  from_location VARCHAR2(4000)
, from_location_mode VARCHAR2(100)
, to_location VARCHAR2(4000)
, to_location_mode VARCHAR2(100)
, CONSTRAINT pporl_location_ck CHECK (
  (from_location IS NULL
    AND from_location_mode IS NULL
    AND to_location IS NULL
    AND to_location_mode IS NULL)
  OR
  (from_location IS NOT NULL
    AND from_location_mode IS NOT NULL
    AND to_location IS NOT NULL
    AND to_location_mode IS NOT NULL)
  )
);

