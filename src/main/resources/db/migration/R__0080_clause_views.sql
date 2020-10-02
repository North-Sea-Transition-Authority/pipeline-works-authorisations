CREATE OR REPLACE VIEW ${datasource.user}.vw_di_clause_versions AS SELECT
  sc.di_id,
  ds.name section_name,
  scv.id discv_id,
  scv.di_sc_id,
  scv.version_no,
  scv.tip_flag,
  scv.name,
  scv.text,
  scv.parent_di_sc_id,
  LEVEL level_number,
  scv.level_order,
  scv.status,
  scv.created_timestamp,
  scv.created_by_person_id,
  scv.ended_timestamp,
  scv.ended_by_person_id
FROM pwa_cw.di_section_clauses sc
JOIN pwa_cw.di_sc_versions scv ON scv.di_sc_id = sc.id AND scv.tip_flag = 1
LEFT JOIN pwa_cw.dt_section_clauses dsc ON dsc.id = sc.dt_sc_id
JOIN pwa_cw.dt_sections ds ON (ds.id = dsc.s_id OR ds.id = sc.dt_s_id)
CONNECT BY scv.parent_di_sc_id = PRIOR scv.di_sc_id
START WITH scv.parent_di_sc_id IS NULL
ORDER SIBLINGS BY level_order;