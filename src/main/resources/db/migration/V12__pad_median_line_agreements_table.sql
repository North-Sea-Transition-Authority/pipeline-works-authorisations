CREATE TABLE ${datasource.user}.pad_median_line_agreements (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER
, agreement_status VARCHAR2(4000)
, negotiator_name VARCHAR2(4000)
, negotiator_email VARCHAR2(4000)
, CONSTRAINT pad_median_line_agr_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_median_line_agr_pad_idx ON ${datasource.user}.pad_median_line_agreements (application_detail_id)
TABLESPACE tbsidx;