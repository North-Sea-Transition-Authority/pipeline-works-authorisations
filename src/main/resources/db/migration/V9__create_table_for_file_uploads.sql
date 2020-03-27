
CREATE TABLE ${datasource.user}.uploaded_files (
  file_id VARCHAR2(4000) NOT NULL  PRIMARY KEY
, file_name VARCHAR2(4000) NOT NULL
, file_data BLOB
, content_type VARCHAR2(4000)
, file_size NUMBER
, uploaded_by_wua_id NUMBER
, last_updated_by_wua_id NUMBER
, status VARCHAR2(10)
, CONSTRAINT uploaded_files_ck1 CHECK (status IN ('CURRENT', 'DELETED'))
, upload_datetime TIMESTAMP
) TABLESPACE tbsdata;
/
