/* 0.
   Double and triple check you've not accidentally strayed onto LIVE before you proceed.
 */
/* 1.
  Run query first to identify if the environment has pipelines without a valid org unit as a holder.
*/

SELECT t.*
FROM (
  -- consent holders org groups
  SELECT
    authd.pa_id, authd.reference
  ,  MAX(xph.pd_id) max_pd_id_on_auth
  , st.join(stagg(xpch.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') text_name
  , st.join(stagg(xou.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') org_name
  , st.join(stagg(cog.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') org_grp_name
  FROM decmgr.xview_pipelines_history xph
  JOIN decmgr.xview_pipeline_auth_details authd ON authd.pad_id = xph.pipe_auth_detail_id
  LEFT JOIN decmgr.xview_pipeline_company_hist xpch ON xpch.pd_id = xph.pd_id AND xpch.role = 'HOLDER'
  LEFT JOIN decmgr.xview_organisation_units xou ON xou.organ_id = xpch.organ_id
  LEFT JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG'
  LEFT JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type
  WHERE xph.status_control = 'C'
  AND xph.status IN ('LEGACY', 'CURRENT')
  GROUP BY authd.pa_id, authd.reference
) t
/* PWAs with no org holder */
-- WHERE t.text_name IS NULL
/

/* 2.
   Run block to update Pipelines in the legacy system so that we can actually test the migration script fully and not have
   missing pipelines causing problem in integrated Energy Portal applications
 */
DECLARE
  
  l_dummy_holder_xml XMLTYPE;
  
BEGIN
  
  SELECT
    XMLELEMENT("COMPANY"
    , XMLELEMENT("ORGAN_ID", MAX(xou.organ_id))
    , XMLELEMENT("NAME")
    , XMLELEMENT("ROLE", 'HOLDER')
    )
  INTO l_dummy_holder_xml
  FROM decmgr.xview_organisation_units xou
  JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG'
  JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type;
  
  UPDATE decmgr.pipeline_details pd
  SET pd.xml_data =
    XMLQUERY(
      'copy $xml := $pd_xml
      modify insert node $dummy_holder as last into $xml/*/COMPANY_LIST
      return $xml'
      PASSING
        pd.xml_data AS "pd_xml"
      , l_dummy_holder_xml AS "dummy_holder"
      RETURNING CONTENT
    )
  WHERE pd.id IN (
    SELECT t.max_pd_id_on_auth
    FROM (
    -- consent holders org groups
    SELECT
      MAX(xph.pd_id) max_pd_id_on_auth
    , st.join(stagg(xpch.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') text_name
    , st.join(stagg(xou.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') org_name
    , st.join(stagg(cog.name), p_remove_dups => 'true', p_order_by_clause => 'ORDER BY 1') org_grp_name
    FROM decmgr.xview_pipelines_history xph
    JOIN decmgr.xview_pipeline_auth_details authd ON authd.pad_id = xph.pipe_auth_detail_id
    LEFT JOIN decmgr.xview_pipeline_company_hist xpch ON xpch.pd_id = xph.pd_id AND xpch.role = 'HOLDER'
    LEFT JOIN decmgr.xview_organisation_units xou ON xou.organ_id = xpch.organ_id
    LEFT JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG'
    LEFT JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type
    WHERE xph.status_control = 'C'
       AND xph.status IN ('LEGACY', 'CURRENT')
    GROUP BY authd.pa_id, authd.reference
    ) t
      /* PWAs with no org holder */
    WHERE t.org_name IS NULL
  );
  
END;
/


