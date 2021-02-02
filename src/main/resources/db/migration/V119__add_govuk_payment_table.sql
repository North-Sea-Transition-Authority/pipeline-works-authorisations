-- Use hibernate envers to audit changes to payment journey table.
CREATE TABLE ${datasource.user}.audit_revisions (
  rev INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, created_timestamp DATE
, person_id NUMBER
);

CREATE TABLE ${datasource.user}.pwa_payment_requests (
  uuid CHAR(36) PRIMARY KEY
, amount_pennies INTEGER
, requested_service VARCHAR2(4000) NOT NULL
, CONSTRAINT ppr_requested_service_ck CHECK(requested_service IN ('CARD_PAYMENT'))
, created_timestamp TIMESTAMP NOT NULL
, reference VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, metadata_json VARCHAR2(4000)
, return_url VARCHAR2(4000)
, request_status VARCHAR2(4000) NOT NULL
, request_status_timestamp TIMESTAMP NOT NULL
, request_status_message VARCHAR2(4000)
, CONSTRAINT ppr_request_status_ck CHECK(request_status IN (
                                                            'PENDING'
                                                            , 'IN_PROGRESS'
                                                            , 'FAILED_TO_CREATE'
                                                            , 'CANCELLED'
                                                            , 'PAYMENT_COMPLETE'
                                                            , 'COMPLETE_WITHOUT_PAYMENT'))
, gov_uk_payment_id VARCHAR2(1000)
, gov_uk_payment_status VARCHAR2(4000)
, gov_uk_payment_status_ts TIMESTAMP
, gov_uk_payment_status_message VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.ppr_gov_payment_id_idx ON  ${datasource.user}.pwa_payment_requests(gov_uk_payment_id);

-- envers table has new columns [rev, revtype] and foreign keys. but is identical to source except constraints are removed.
CREATE TABLE ${datasource.user}.pwa_payment_requests_aud (
  rev INTEGER NOT NULL
, revtype INTEGER
, uuid CHAR(36)
, amount_pennies INTEGER
, requested_service VARCHAR2(4000)
, created_timestamp TIMESTAMP
, reference VARCHAR2(4000)
, description VARCHAR2(4000)
, metadata_json VARCHAR2(4000)
, return_url VARCHAR2(4000)
, request_status VARCHAR2(4000)
, request_status_timestamp TIMESTAMP
, request_status_message VARCHAR2(4000)
, gov_uk_payment_id VARCHAR2(1000)
, gov_uk_payment_status VARCHAR2(4000)
, gov_uk_payment_status_ts TIMESTAMP
, gov_uk_payment_status_message VARCHAR2(4000)
, PRIMARY KEY(rev, uuid)
, FOREIGN KEY(rev) REFERENCES ${datasource.user}.audit_revisions(rev)
) TABLESPACE tbsdata;
