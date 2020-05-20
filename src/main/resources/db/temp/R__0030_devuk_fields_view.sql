GRANT SELECT ON devukmgr.field_operator_view TO ${datasource.user};
GRANT REFERENCES ON devukmgr.field_operator_view TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.devuk_fields AS
SELECT
  fov.field_id
, fov.field_name
, fov.operator_id operator_ou_id
, f.status
FROM devukmgr.field_operator_view fov
JOIN devukmgr.fields f ON f.field_identifier = fov.field_id;