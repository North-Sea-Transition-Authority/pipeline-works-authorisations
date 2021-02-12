/**
  No requirements as yet to build out user management of fees. Just make sure we have a model that
  will allow this to be built later on without huge model changes RE cardinality.
  Intentionally ignored:
    1. fee period domains - only single domain (pwa_apps) at the moment
    2. staged/draft fee periods - support update fee and periods via patch only atm
    3. fee items not related to application types - this makes it simple for now and we dont know future requirements
 */
CREATE TABLE ${datasource.user}.fee_periods (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

-- generally there will be 1 of these per fee_period. Added so that user management of fee periods can be added more easily later.
CREATE TABLE ${datasource.user}.fee_period_details(
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, fee_period_id INTEGER NOT NULL
, FOREIGN KEY(fee_period_id) REFERENCES ${datasource.user}.fee_periods(id)
, period_start_timestamp TIMESTAMP NOT NULL
, period_end_timestamp TIMESTAMP
, tip_flag INTEGER
, CONSTRAINT fdp_tip_flag_ck CHECK (tip_flag IN (0, 1))
);

CREATE INDEX ${datasource.user}.fpd_fk_idx ON  ${datasource.user}.fee_period_details(fee_period_id);

-- master list of fee items that can be added a fee period. Fee period defines the amount charged per fee_item
-- contains basic items that can be added to fee periods.
-- Uses app_type for simplicity but if new charging domains (non app type related) get added this will be a pain point.
CREATE TABLE ${datasource.user}.fee_items(
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_application_type VARCHAR2(4000) NOT NULL
, display_description VARCHAR2(4000) NOT NULL -- e.g "Charge for submitting an application of type [X]"
);

CREATE INDEX ${datasource.user}.fi_app_type_idx ON  ${datasource.user}.fee_items(pwa_application_type);

-- this does not support versioning within a detail.
-- If you want change the fees you can
-- 1. update linked fee items in place for immediate/un tracked changes - not recommended unless dev-ing
-- 2. create new tip fee_period_detail and copy each fee item into it and set the amounts or add/remove fee items
-- 3. create a new fee period entirely to represent the time when the existing fees were valid and when the new fees come into effect
CREATE TABLE ${datasource.user}.fee_period_detail_fee_items(
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, fee_period_detail_id INTEGER NOT NULL
, CONSTRAINT fpdfi_fpd_fk FOREIGN KEY(fee_period_detail_id) REFERENCES ${datasource.user}.fee_period_details(id)
, fee_item_id INTEGER NOT NULL
, CONSTRAINT fpdfi_fi_fk FOREIGN KEY(fee_item_id) REFERENCES ${datasource.user}.fee_items(id)
, penny_amount INTEGER NOT NULL
, CONSTRAINT fpdfi_penny_amount_ck CHECK (penny_amount >= 0)
);

CREATE INDEX ${datasource.user}.fpdfi_fpd_fk_idx ON  ${datasource.user}.fee_period_detail_fee_items(fee_period_detail_id);
CREATE INDEX ${datasource.user}.fpdfi_fi_fk_idx ON  ${datasource.user}.fee_period_detail_fee_items(fee_item_id);