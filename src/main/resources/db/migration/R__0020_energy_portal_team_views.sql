GRANT SELECT ON decmgr.xview_resources TO ${datasource.user};
GRANT SELECT ON decmgr.xview_resource_types TO ${datasource.user};
GRANT SELECT ON decmgr.xview_resource_type_roles TO ${datasource.user};
GRANT SELECT ON decmgr.xview_resource_type_privs TO ${datasource.user};
GRANT SELECT ON decmgr.resource_usages_current TO ${datasource.user};
GRANT SELECT ON decmgr.resource_members_current TO ${datasource.user};
GRANT SELECT ON decmgr.xview_resource_people_history TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.portal_resources AS
SELECT xr.res_id
     , xr.res_type
     , xr.res_name
     , xr.description
FROM decmgr.xview_resources xr;

CREATE OR REPLACE VIEW ${datasource.user}.portal_resource_types AS
SELECT xrt.res_type
     , xrt.res_type_title
     , xrt.res_type_description
     , xrt.scoped_within
FROM decmgr.xview_resource_types xrt;

CREATE OR REPLACE VIEW ${datasource.user}.portal_resource_type_roles AS
SELECT xrtr.res_type
     , xrtr.role_name
     , xrtr.role_title
     , xrtr.role_description
     , xrtr.min_mems
     , xrtr.max_mems
     , xrtr.display_seq
FROM decmgr.xview_resource_type_roles xrtr;

CREATE OR REPLACE VIEW ${datasource.user}.portal_resource_type_role_priv AS
SELECT xrtp.role_name
     , xrtp.res_type
     , xrtp.default_system_priv
FROM decmgr.xview_resource_type_privs xrtp;


CREATE OR REPLACE VIEW ${datasource.user}.portal_resource_usages_current AS
SELECT ruc.res_id
     , ruc.uref
     , ruc.purpose
FROM decmgr.resource_usages_current ruc;

-- This is the slowest base query
CREATE OR REPLACE VIEW ${datasource.user}.portal_res_members_current AS
SELECT DISTINCT rmc.res_id
              , rmc.person_id
FROM decmgr.resource_members_current rmc
WHERE rmc.wua_id IS NOT NULL;

CREATE OR REPLACE VIEW ${datasource.user}.portal_res_memb_current_roles AS
SELECT DISTINCT rmc.person_id
              , rmc.res_id
              , rmc.res_type
              , rmc.role_name
FROM decmgr.resource_members_current rmc
WHERE rmc.wua_id IS NOT NULL;

CREATE OR REPLACE VIEW ${datasource.user}.people AS
SELECT
    xrph.rp_id id
  , xrph.forename
  , xrph.surname
  , xrph.portal_email_address email_address
FROM decmgr.xview_resource_people_history xrph
WHERE xrph.status_control = 'C'


