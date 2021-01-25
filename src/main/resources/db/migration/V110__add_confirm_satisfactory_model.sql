ALTER TABLE ${datasource.user}.pwa_application_details ADD confirmed_satisfactory_pers_id NUMBER;

ALTER TABLE ${datasource.user}.pwa_application_details ADD confirmed_satisfactory_ts TIMESTAMP;

ALTER TABLE ${datasource.user}.pwa_application_details ADD confirmed_satisfactory_reason VARCHAR2(4000);