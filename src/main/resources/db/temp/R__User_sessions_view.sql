GRANT SELECT ON securemgr.web_user_sessions TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.user_sessions
AS
SELECT
  wus.wus_id id
, wus.wua_id
, wus.login_date login_timestamp
, wus.logout_date logout_timestamp
, wus.last_access_date last_access_timestamp
FROM securemgr.web_user_sessions wus;