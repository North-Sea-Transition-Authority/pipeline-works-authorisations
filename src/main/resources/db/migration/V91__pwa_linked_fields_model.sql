CREATE TABLE ${datasource.user}.pwa_detail_fields(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_detail_id NUMBER NOT NULL
, devuk_field_id NUMBER
, manual_field_name VARCHAR2(4000)
, CONSTRAINT pdf_pwa_detail_id_fk FOREIGN KEY (pwa_detail_id) REFERENCES ${datasource.user}.pwa_details (id)
, CONSTRAINT pdf_field_ck CHECK(
  (devuk_field_id IS NOT NULL AND manual_field_name IS NULL)
  OR
  (devuk_field_id IS NULL AND manual_field_name IS NOT NULL)
  )
);

CREATE INDEX ${datasource.user}.pdf_pwa_detail_id_fk ON ${datasource.user}.pwa_detail_fields (pwa_detail_id);
CREATE INDEX ${datasource.user}.pdf_devuk_field_id_fk ON ${datasource.user}.pwa_detail_fields (devuk_field_id);

ALTER TABLE ${datasource.user}.pwa_details ADD (
  is_linked_to_fields INTEGER
, pwa_linked_to_description VARCHAR2(4000)
, CONSTRAINT pd_field_link_ck CHECK(
    (is_linked_to_fields = 0 AND pwa_linked_to_description IS NOT NULL)
    OR
    (is_linked_to_fields = 1 AND pwa_linked_to_description IS NULL)
    OR
    (is_linked_to_fields IS NULL AND pwa_linked_to_description IS NULL)
  )
);
