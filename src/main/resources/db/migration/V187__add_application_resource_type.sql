ALTER TABLE ${datasource.user}.pwa_applications ADD resource_type VARCHAR2(50) DEFAULT 'PETROLEUM' NOT NULL;
ALTER TABLE ${datasource.user}.pwa_details ADD resource_type VARCHAR2(50) DEFAULT 'PETROLEUM' NOT NULL;
