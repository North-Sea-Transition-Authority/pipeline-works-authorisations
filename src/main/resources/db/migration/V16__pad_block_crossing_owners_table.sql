ALTER TABLE ${datasource.user}.pad_blocks ADD (
  block_owner VARCHAR2(4000) NOT NULL
);

CREATE TABLE ${datasource.user}.pad_block_crossing_owners (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_block_crossing_id NUMBER NOT NULL
, owner_ou_id NUMBER
, owner_name VARCHAR2(4000)
, CONSTRAINT pad_owner_ck CHECK ((owner_ou_id IS NULL AND owner_name IS NOT NULL) OR (owner_ou_id IS NOT NULL AND owner_name IS NULL))
, CONSTRAINT pad_bc_fk FOREIGN KEY (pad_block_crossing_id) REFERENCES ${datasource.user}.pad_blocks(id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pad_bco_fk_idx ON ${datasource.user}.pad_block_crossing_owners (pad_block_crossing_id)
TABLESPACE tbsidx;


CREATE TABLE ${datasource.user}.pad_block_crossing_files (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
  , application_detail_id NUMBER NOT NULL
  , file_id VARCHAR2(4000) NOT NULL
  , description VARCHAR2(4000)
  , file_link_status VARCHAR2(100)
  , CONSTRAINT pad_bcf_pad_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
  , CONSTRAINT pad_bcf_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
) TABLESPACE tbsdata;


CREATE INDEX ${datasource.user}.pad_bcf_pad_idx ON ${datasource.user}.pad_block_crossing_files (application_detail_id)
  TABLESPACE tbsidx;

CREATE INDEX ${datasource.user}.pad_bcf_file_idx ON ${datasource.user}.pad_block_crossing_files (file_id)
  TABLESPACE tbsidx;