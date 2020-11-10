ALTER TABLE ${datasource.user}.pad_pipeline_idents ADD (
    is_defining_structure INTEGER DEFAULT 0 NOT NULL CHECK(is_defining_structure IN (0, 1))
);

ALTER TABLE ${datasource.user}.pipeline_detail_idents ADD (
    is_defining_structure INTEGER DEFAULT 0 NOT NULL CHECK(is_defining_structure IN (0, 1))
);


