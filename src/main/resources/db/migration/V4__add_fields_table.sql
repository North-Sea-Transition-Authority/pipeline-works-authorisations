GRANT SELECT ON devukmgr.fields TO ${datasource.user};
GRANT REFERENCES ON devukmgr.fields TO ${datasource.user};

CREATE TABLE ${datasource.user}.pad_fields (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, field_id NUMBER
, field_name_manual_entry VARCHAR2(4000)
, application_detail_id NUMBER
, CONSTRAINT fields_adid_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
, CONSTRAINT fields_ck CHECK(
    (field_id IS NOT NULL AND field_name_manual_entry IS NULL)
    OR (field_id IS NULL AND field_name_manual_entry IS NOT NULL)
  )
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.fields_fid_fk ON ${datasource.user}.pad_fields (field_id)
TABLESPACE tbsidx;

CREATE INDEX ${datasource.user}.fields_adid_fk ON ${datasource.user}.pad_fields (application_detail_id)
TABLESPACE tbsidx;

ALTER TABLE ${datasource.user}.pwa_application_details
ADD (
  is_linked_to_field NUMBER
, CONSTRAINT field_link_ck CHECK(is_linked_to_field IN (0, 1) OR is_linked_to_field IS NULL)
);