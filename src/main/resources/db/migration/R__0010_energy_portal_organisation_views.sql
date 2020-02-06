GRANT SELECT ON decmgr.xview_organisation_units TO ${datasource.user};
GRANT SELECT ON decmgr.current_org_grp_organisations TO ${datasource.user};
GRANT SELECT ON decmgr.current_organisation_groups TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.portal_organisation_units AS
SELECT xou.organ_id ou_id, xou.name, cog.id org_grp_id
FROM decmgr.xview_organisation_units xou
JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id
JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id
WHERE xou.end_date IS NULL
AND cog.org_grp_type = 'REG'
AND xou.is_duplicate IS NULL;

CREATE OR REPLACE VIEW ${datasource.user}.portal_organisation_groups AS
SELECT cog.id org_grp_id
, cog.name
, cog.short_name
, cog.id || '++' || cog.org_grp_type || 'ORGGRP' uref_value
FROM decmgr.current_organisation_groups cog
WHERE cog.org_grp_type = 'REG';
