ALTER TABLE ${datasource.user}.pad_design_op_conditions ADD flowrate_unit VARCHAR2(50);
ALTER TABLE ${datasource.user}.pad_design_op_conditions ADD co2_density_max NUMBER;
ALTER TABLE ${datasource.user}.pad_design_op_conditions ADD co2_density_min NUMBER;
UPDATE ${datasource.user}.pad_design_op_conditions SET flowrate_unit = 'KSCM_D';
