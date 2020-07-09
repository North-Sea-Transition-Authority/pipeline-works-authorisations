ALTER TABLE ${datasource.user}.pad_pipeline_ident_data ADD (
  external_diameter_mc VARCHAR2(4000),
  internal_diameter_mc VARCHAR2(4000),
  wall_thickness_mc VARCHAR2(4000),
  insulation_coating_type_mc VARCHAR2(4000),
  maop_mc VARCHAR2(4000),
  products_to_be_conveyed_mc VARCHAR2(4000)
);