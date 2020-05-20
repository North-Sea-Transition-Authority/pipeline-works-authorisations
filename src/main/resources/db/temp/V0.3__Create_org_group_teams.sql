DECLARE

  l_res_id NUMBER;

BEGIN

  FOR org_group_rec IN (
    SELECT
      cog.id || '++REGORGGRP' uref
    , cog.name org_name
    FROM decmgr.current_organisation_groups cog
    WHERE cog.org_grp_type = 'REG'
    AND EXISTS (
      SELECT 1
      FROM decmgr.current_org_grp_organisations cogo
      WHERE cogo.org_grp_id = cog.id
    )
    AND NOT EXISTS (
      SELECT 1
      FROM decmgr.resource_usages_current ruc
      JOIN decmgr.xview_resources xr ON xr.res_id = ruc.res_id
      WHERE xr.res_type = 'PWA_ORGANISATION_TEAM'
      AND ruc.uref = cog.id || '++REGORGGRP'
    )
  )
  LOOP
    l_res_id := decmgr.contact.create_default_team(
      p_resource_type => 'PWA_ORGANISATION_TEAM'
    , p_resource_name => 'PWA Organisation Team'
    , p_resource_desc => 'PWA Organisation Team - ' || org_group_rec.org_name
    , p_uref => org_group_rec.uref
    , p_uref_purpose => 'PRIMARY_DATA'
    , p_creating_wua_id => 1
    );
  END LOOP;

  COMMIT;

END;