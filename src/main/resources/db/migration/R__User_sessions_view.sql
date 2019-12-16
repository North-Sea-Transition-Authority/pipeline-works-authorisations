GRANT SELECT ON securemgr.web_user_sessions TO ${datasource.user};
GRANT SELECT ON fox5mgr.fox_sessions TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.user_sessions
AS
SELECT
  fs.id id
, wus.wua_id user_id
, wus.login_date login_timestamp
, wus.logout_date logout_timestamp
, wus.last_access_date last_access_timestamp
FROM securemgr.web_user_sessions wus
JOIN fox5mgr.fox_sessions fs ON wus.wus_id = fs.wus_id;