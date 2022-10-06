CREATE OR REPLACE VIEW ${datasource.user}.vw_portal_org_unit_searchable AS
    (SELECT
        pou.ou_id as org_unit_id,
        pou.name || NVL2(poud.registered_number,(' (' || poud.registered_number || ')'),'') as org_search_name,
        pou.org_grp_id as org_grp_id,
        pou.is_active as is_active
        FROM
            ${datasource.user}.portal_organisation_units pou
            LEFT JOIN ${datasource.user}.portal_org_unit_detail poud ON pou.ou_id = poud.ou_id);