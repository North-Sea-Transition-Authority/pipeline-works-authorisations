ALTER TABLE ${datasource.user}.pwa_application_details
ADD pipelines_crossed NUMBER CHECK(pipelines_crossed IN (0, 1) OR pipelines_crossed IS NULL);

ALTER TABLE ${datasource.user}.pwa_application_details
ADD cables_crossed NUMBER CHECK(cables_crossed IN (0, 1) OR cables_crossed IS NULL);

ALTER TABLE ${datasource.user}.pwa_application_details
ADD median_line_crossed NUMBER CHECK(median_line_crossed IN (0, 1) OR median_line_crossed IS NULL);