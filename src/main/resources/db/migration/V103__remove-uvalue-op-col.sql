ALTER TABLE ${datasource.user}.pad_design_op_conditions DROP COLUMN uvalue_op;
ALTER TABLE ${datasource.user}.pad_design_op_conditions DROP COLUMN pressure_design_intl;
ALTER TABLE ${datasource.user}.pad_design_op_conditions RENAME COLUMN pressure_op_intl TO pressure_op_min;
ALTER TABLE ${datasource.user}.pad_design_op_conditions RENAME COLUMN pressure_op_extl TO pressure_op_max;
ALTER TABLE ${datasource.user}.pad_design_op_conditions RENAME COLUMN pressure_design_extl TO pressure_design_max;