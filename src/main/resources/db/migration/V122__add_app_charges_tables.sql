-- top level charge requests. can be multiple per pwa_application. Only expect 1 to be open at any point
CREATE TABLE ${datasource.user}.pwa_app_charge_requests (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_application_id NUMBER NOT NULL
, CONSTRAINT pacr_pwa_app_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications(id)
, requested_by_person_id INTEGER NOT NULL
, requested_by_timestamp TIMESTAMP NOT NULL
);

CREATE INDEX ${datasource.user}.pacr_pwa_app_fk_idx ON ${datasource.user}.pwa_app_charge_requests(pwa_application_id);

-- detail associated with a charge request. allows for post issue withdrawal/waiving through app
CREATE TABLE ${datasource.user}.pwa_app_charge_request_details (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_app_charge_request_id INTEGER NOT NULL
, CONSTRAINT pacrd_pacr_fk FOREIGN KEY (pwa_app_charge_request_id) REFERENCES ${datasource.user}.pwa_app_charge_requests
, started_timestamp TIMESTAMP NOT NULL
, started_by_person_id NUMBER NOT NULL
, ended_timestamp TIMESTAMP
, ended_by_person_id NUMBER
, tip_flag INTEGER
, CONSTRAINT pacrd_tip_flag_ck CHECK (tip_flag IN (0, 1))
-- actual data that might change detail to detail
, auto_case_officer_person_id NUMBER NOT NULL -- pwa managers nominate a case officer. store here not in camunda for simplicity.
, total_pennies INTEGER NOT NULL -- waived payment could set 0. Not necessarily the sum of link charge request item if payment waived.
, charge_summary VARCHAR2(255) NOT NULL -- limited to the same value as the hard limit in govpay for readable descriptions
, status VARCHAR2(400) NOT NULL
, charge_waived_reason VARCHAR2(4000)
, CONSTRAINT pacrd_total_pennies_ck CHECK (total_pennies >= 0)
);

CREATE INDEX ${datasource.user}.pacrd_pacr_fk_idx ON ${datasource.user}.pwa_app_charge_request_details(pwa_app_charge_request_id);

CREATE TABLE ${datasource.user}.pwa_app_charge_request_items (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_app_charge_request_id INTEGER NOT NULL
, CONSTRAINT pacri_pacr_fk FOREIGN KEY (pwa_app_charge_request_id) REFERENCES ${datasource.user}.pwa_app_charge_requests
 -- human readable description attached to the item being charged for.
, description VARCHAR2(1000)
, penny_amount INTEGER NOT NULL
, CONSTRAINT pacri_penny_amount_ck CHECK (penny_amount >= 0)
);

CREATE INDEX ${datasource.user}.pacri_pacr_fk_idx ON ${datasource.user}.pwa_app_charge_request_items(pwa_app_charge_request_id);


CREATE TABLE ${datasource.user}.pwa_app_charge_payment_attempt (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_app_charge_request_id INTEGER NOT NULL
, CONSTRAINT pacpa_pacr_fk FOREIGN KEY (pwa_app_charge_request_id) REFERENCES ${datasource.user}.pwa_app_charge_requests(id)
, created_by_person_id INTEGER NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, ended_by_person_id INTEGER
, ended_timestamp TIMESTAMP
, active_flag INTEGER
, CONSTRAINT pacpa_active_flag_ck CHECK (active_flag IN (0, 1))
);

CREATE INDEX ${datasource.user}.pacpa_pacr_fk_idx ON ${datasource.user}.pwa_app_charge_payment_attempt(pwa_app_charge_request_id);







