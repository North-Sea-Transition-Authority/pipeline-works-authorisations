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
FROM ${datasource.user}.di_section_clauses sc
JOIN ${datasource.user}.di_sc_versions scv ON scv.di_sc_id = sc.id AND scv.tip_flag = 1
LEFT JOIN ${datasource.user}.dt_section_clauses dsc ON dsc.id = sc.dt_sc_id
JOIN ${datasource.user}.dt_sections ds ON (ds.id = dsc.s_id OR ds.id = sc.dt_s_id)
CONNECT BY scv.parent_di_sc_id = PRIOR scv.di_sc_id
START WITH scv.parent_di_sc_id IS NULL
ORDER SIBLINGS BY level_order;

CREATE OR REPLACE VIEW ${datasource.user}.vw_dt_clause_versions AS SELECT
  ds.dt_id,
  dt.mnem dt_mnem,
  ds.name section_name,
  scv.id scv_id,
  scv.sc_id,
  scv.version_no,
  scv.tip_flag,
  scv.name,
  scv.text,
  scv.parent_sc_id,
  LEVEL level_number,
  scv.level_order,
  scv.status,
  scv.created_timestamp,
  scv.created_by_person_id,
  scv.ended_timestamp,
  scv.ended_by_person_id
FROM ${datasource.user}.dt_section_clauses sc
JOIN ${datasource.user}.dt_section_clause_versions scv ON scv.sc_id = sc.id AND scv.tip_flag = 1
JOIN ${datasource.user}.dt_sections ds ON ds.id = sc.s_id
JOIN ${datasource.user}.document_templates dt ON dt.id = ds.dt_id
CONNECT BY scv.parent_sc_id = PRIOR scv.sc_id
START WITH scv.parent_sc_id IS NULL
ORDER SIBLINGS BY level_order;