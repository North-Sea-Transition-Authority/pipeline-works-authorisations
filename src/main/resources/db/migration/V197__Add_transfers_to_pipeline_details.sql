ALTER TABLE ${datasource.user}.pipeline_details ADD transferred_from NUMBER;
ALTER TABLE ${datasource.user}.pipeline_details ADD transferred_to NUMBER;

ALTER TABLE ${datasource.user}.pipeline_details ADD CONSTRAINT pipeline_detail_transfer_from_fk FOREIGN KEY (transferred_from) REFERENCES ${datasource.user}.PWAS (id);
ALTER TABLE ${datasource.user}.pipeline_details ADD CONSTRAINT pipeline_detail_transfer_to_fk FOREIGN KEY (transferred_to) REFERENCES ${datasource.user}.PWAS (id);