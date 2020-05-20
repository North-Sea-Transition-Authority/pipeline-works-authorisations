GRANT SELECT ON securemgr.web_user_accounts TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.user_accounts
AS
SELECT
  wua.id wua_id
, wua.title
, wua.forename
, wua.surname
, wua.primary_email_address email_address
, wua.resource_person_id person_id
, wua.login_id login_id
, wua.account_status
FROM securemgr.web_user_accounts wua;