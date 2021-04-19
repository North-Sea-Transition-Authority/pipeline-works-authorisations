CREATE TABLE ${datasource.user}.pipeline_migration_config (
  id                           NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  reserved_pipeline_number_min INTEGER NOT NULL,
  reserved_pipeline_number_max INTEGER NOT NULL,
  -- virtual column to base constraint check which prevents multiple rows in table.
  row_count_check_virtual_col  INTEGER GENERATED ALWAYS AS (1) VIRTUAL,
  CONSTRAINT single_row_constraint UNIQUE (row_count_check_virtual_col)
);

INSERT INTO ${datasource.user}.pipeline_migration_config (
  reserved_pipeline_number_min
, reserved_pipeline_number_max
) VALUES (
  5000
, 6000
);
