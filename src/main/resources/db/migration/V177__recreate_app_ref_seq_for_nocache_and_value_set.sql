DROP SEQUENCE ${datasource.user}.app_ref_sequence;

CREATE SEQUENCE ${datasource.user}.app_ref_sequence
START WITH ${app-ref.seq-start}
INCREMENT BY 1
NOCACHE;