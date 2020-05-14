CREATE TABLE ${datasource.user}.permanent_deposit_information(
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_detail_id NUMBER NOT NULL,
    from_month Number(2),
    from_year Number(4),
    to_month Number(2),
    to_year Number(4),
    material_type VARCHAR2(50),
    material_size VARCHAR2(20),
    concrete_mattress_length NUMBER,
    concrete_mattress_width NUMBER,
    concrete_mattress_depth NUMBER,

    grout_bags_bio_degradable INTEGER CHECK(grout_bags_bio_degradable IN (0, 1) OR grout_bags_bio_degradable IS NULL),
    bags_not_used_description VARCHAR2(500),
    quantity NUMBER,
    contingency_amount VARCHAR2(50),

    from_lat_deg NUMBER,
    from_lat_min NUMBER,
    from_lat_sec NUMBER,
    from_lat_dir VARCHAR2(5),
    from_long_deg NUMBER,
    from_long_min NUMBER,
    from_long_sec NUMBER,
    from_long_dir VARCHAR2(5),

    to_lat_deg NUMBER,
    to_lat_min NUMBER,
    to_lat_sec NUMBER,
    to_lat_dir VARCHAR2(5),
    to_long_deg NUMBER,
    to_long_min NUMBER,
    to_long_sec NUMBER,
    to_long_dir VARCHAR2(5)
);


CREATE TABLE ${datasource.user}.deposits_for_pipelines(
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    permanent_deposit_info_id INTEGER NOT NULL,
    pad_pipeline_id INTEGER NOT NULL
);

CREATE TABLE ${datasource.user}.permanent_deposit_info_files (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_detail_id NUMBER NOT NULL,
    file_id VARCHAR2(4000) NOT NULL,
    description VARCHAR2(4000),
    file_link_status VARCHAR2(100),
    CONSTRAINT perm_dep_app_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id),
    CONSTRAINT perm_dep_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
);