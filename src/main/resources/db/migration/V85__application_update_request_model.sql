CREATE TABLE ${datasource.user}.application_update_requests  (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_id NUMBER NOT NULL
, CONSTRAINT aur_pad_id_fk FOREIGN KEY(pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
, requested_by_person_id NUMBER NOT NULL
, requested_timestamp TIMESTAMP NOT NULL
, request_reason VARCHAR2(4000) NOT NULL
);

CREATE INDEX ${datasource.user}.aur_pad_id_fk_idx ON ${datasource.user}.application_update_requests(pad_id);
CREATE INDEX ${datasource.user}.aur_req_person_id_fk_idx ON ${datasource.user}.application_update_requests(requested_by_person_id);