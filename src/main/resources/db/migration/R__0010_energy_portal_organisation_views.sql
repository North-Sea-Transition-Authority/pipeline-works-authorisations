GRANT SELECT ON decmgr.xview_organisation_units TO ${datasource.user};
GRANT SELECT ON decmgr.current_org_grp_organisations TO ${datasource.user};
GRANT SELECT ON decmgr.current_organisation_groups TO ${datasource.user};
GRANT SELECT ON decmgr.organisation_address_details TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.portal_organisation_units AS
SELECT
  xou.organ_id ou_id
, xou.name
, cogo.org_grp_id
, xou.start_date start_date -- organisation started, not record started
, xou.end_date end_date -- organisation ended, not record ended
, CASE WHEN xou.is_duplicate = 'Y' THEN 1 ELSE 0 END is_duplicate
, CASE WHEN xou.is_duplicate = 'Y' OR xou.end_date IS NOT NULL THEN 0 ELSE 1 END is_active
FROM decmgr.xview_organisation_units xou
-- include all org units when
-- 1. they dont appear in a REG org grp
-- 2. they only appear in an SDK org grp
LEFT JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG';

CREATE OR REPLACE VIEW ${datasource.user}.portal_organisation_groups AS
SELECT cog.id org_grp_id
, cog.name
, cog.short_name
, cog.id || '++' || cog.org_grp_type || 'ORGGRP' uref_value
FROM decmgr.current_organisation_groups cog
WHERE cog.org_grp_type = 'REG';

-- ou_id and org_unit_id are identical in order to reference the PortalOrganisationUnit entity.
CREATE OR REPLACE VIEW ${datasource.user}.portal_org_unit_detail AS
SELECT
  xou.organ_id ou_id
, xou.organ_id org_unit_id
, oad.legal_address
, oad.registered_number
FROM decmgr.xview_organisation_units xou
LEFT JOIN decmgr.organisation_address_details oad ON oad.organ_id = xou.organ_id;
