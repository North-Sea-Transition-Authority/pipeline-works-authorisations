ALTER TABLE ${datasource.user}.pad_pipeline_ident_data ADD (
  external_diameter_txt VARCHAR2(4000),
  internal_diameter_txt VARCHAR2(4000),
  wall_thickness_txt VARCHAR2(4000),
  insulation_coating_type_txt VARCHAR2(4000),
  maop_txt VARCHAR2(4000),
  products_to_be_conveyed_txt VARCHAR2(4000)
);