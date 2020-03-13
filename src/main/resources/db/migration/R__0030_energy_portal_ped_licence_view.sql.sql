GRANT SELECT ON pedmgr.xview_ped_ld_current TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.ped_licences AS
SELECT
  xplc.ped_lm_id
, xplc.licence_type
, xplc.licence_no licence_number
, xplc.licence_type || xplc.licence_no licence_name
FROM pedmgr.xview_ped_ld_current xplc
WHERE xplc.licence_status = 'E';