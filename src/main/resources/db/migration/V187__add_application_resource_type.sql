ALTER TABLE ${datasource.user}.pwa_applications ADD resource_type VARCHAR2(50);
ALTER TABLE ${datasource.user}.pwa_details ADD resource_type VARCHAR2(50);

UPDATE  ${datasource.user}.pwa_applications SET resource_type = 'PETROLEUM';
UPDATE  ${datasource.user}.pwa_details SET resource_type = 'PETROLEUM';

ALTER TABLE ${datasource.user}.pwa_applications MODIFY resource_type NOT NULL;
ALTER TABLE ${datasource.user}.pwa_details MODIFY resource_type NOT NULL;
