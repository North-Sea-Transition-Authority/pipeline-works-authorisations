CREATE TABLE ${datasource.user}.pad_bundles (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_id INTEGER NOT NULL
, name VARCHAR2(4000) NOT NULL
, CONSTRAINT pad_bundle_pad_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_bundle_pad_fk_idx ON ${datasource.user}.pad_bundles(pad_id);

CREATE TABLE ${datasource.user}.pad_bundle_links (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pb_id NOT NULL
, pp_id INTEGER NOT NULL
, CONSTRAINT pad_bundle_links_pb_fk FOREIGN KEY (pb_id) REFERENCES ${datasource.user}.pad_bundles(id)
, CONSTRAINT pad_bundle_links_pp_fk FOREIGN KEY (pp_id) REFERENCES ${datasource.user}.pad_pipelines(id)
);

CREATE INDEX ${datasource.user}.pad_bundle_links_pb_fk_idx ON ${datasource.user}.pad_bundle_links(pb_id);
CREATE INDEX ${datasource.user}.pad_bundle_links_pp_fk_idx ON ${datasource.user}.pad_bundle_links(pp_id);

