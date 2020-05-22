ALTER TABLE ${datasource.user}.pad_technical_drawings
MODIFY pf_id NOT NULL;

ALTER TABLE ${datasource.user}.pad_technical_drawings
MODIFY pad_id NOT NULL;

ALTER TABLE ${datasource.user}.pad_technical_drawings
MODIFY reference NOT NULL;

ALTER TABLE ${datasource.user}.pad_technical_drawing_links
MODIFY ptd_id NOT NULL;

ALTER TABLE ${datasource.user}.pad_technical_drawing_links
MODIFY pp_id NOT NULL;