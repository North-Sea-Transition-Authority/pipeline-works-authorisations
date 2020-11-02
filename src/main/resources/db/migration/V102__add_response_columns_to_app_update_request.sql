
ALTER TABLE ${datasource.user}.application_update_requests ADD (
  response_by_person_id NUMBER
, response_timestamp TIMESTAMP
, response_pad_id NUMBER
, response_other_changes VARCHAR2(4000)
);

ALTER TABLE ${datasource.user}.application_update_requests ADD (
 CONSTRAINT aur_response_ck CHECK (
    (response_by_person_id IS NULL AND response_timestamp IS NULL AND response_pad_id IS NULL)
  OR
    (response_by_person_id IS NOT NULL AND response_timestamp IS NOT NULL AND response_pad_id IS NOT NULL)
  )
);




