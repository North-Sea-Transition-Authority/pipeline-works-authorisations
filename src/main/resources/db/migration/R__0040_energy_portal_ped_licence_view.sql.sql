GRANT SELECT ON pedmgr.ped_licence_details TO ${datasource.user};
GRANT SELECT ON pedmgr.ped_licences TO ${datasource.user};
GRANT SELECT ON pedmgr.ped_current_data_points TO ${datasource.user};
GRANT SELECT ON pedmgr.ped_current_licence_blocks TO ${datasource.user};
GRANT SELECT ON pedmgr.ped_licence_master TO ${datasource.user};
GRANT SELECT ON pedmgr.unlicensed_blocks TO ${datasource.user};
GRANT SELECT ON pedmgr.xview_ped_ld_current TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.ped_licences AS
SELECT
  xplc.ped_lm_id plm_id
, xplc.licence_type
, xplc.licence_no licence_number
, xplc.licence_type || xplc.licence_no licence_name
, xplc.licence_status
FROM pedmgr.ped_licence_details pld
JOIN pedmgr.ped_licences pl ON pl.id = pld.pedl_id
JOIN pedmgr.ped_current_data_points pdp ON pdp.id = pl.ped_dp_id AND pdp.ped_sim_id = 0
JOIN pedmgr.xview_ped_ld_current xplc ON xplc.ped_ld_id = pld.id;

CREATE OR REPLACE VIEW ${datasource.user}.ped_blocks AS
SELECT
  b.composite_key
, b.block_ref
, b.quadrant_no
, b.block_no
, b.suffix
, b.plm_id
, b.location
FROM (
  SELECT
    pclb.block_ref || pclb.quadrant_no || TO_CHAR(pclb.block_no) || pclb.suffix || pl.plm_id composite_key
  , pclb.block_ref
  , pclb.quadrant_no
  , TO_CHAR(pclb.block_no) block_no
  , pclb.suffix
  , pl.plm_id
  , pclb.location
  FROM pedmgr.ped_current_licence_blocks pclb
  JOIN ${datasource.user}.ped_licences pl ON pl.licence_type = pclb.licence_type AND pl.licence_number = pclb.licence_no
  UNION ALL
  SELECT
    ub.block_ref || ub.quadrant_no || TO_CHAR(ub.block_no) || ub.block_suffix || '_UNLICENCED' composite_key
  , ub.block_ref
  , ub.quadrant_no
  , TO_CHAR(ub.block_no) block_no
  , ub.block_suffix suffix
  , null plm_id
  , ub.type location
  FROM pedmgr.unlicensed_blocks ub
) b;