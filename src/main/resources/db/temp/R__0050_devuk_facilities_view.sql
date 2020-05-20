GRANT SELECT ON devukmgr.facilities TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.devuk_facilities AS
SELECT
  f.identifier id
, f.facility_name
FROM devukmgr.facilities f
ORDER BY f.facility_name;