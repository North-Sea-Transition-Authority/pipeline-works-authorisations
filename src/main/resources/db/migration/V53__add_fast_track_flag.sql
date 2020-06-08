ALTER TABLE ${datasource.user}.pwa_application_details
ADD submitted_as_fast_track_flag NUMBER
CHECK(submitted_as_fast_track_flag IN (0, 1) OR submitted_as_fast_track_flag IS NULL);

-- not recalculating this for in-flight apps on non-live environments
UPDATE ${datasource.user}.pwa_application_details
SET submitted_as_fast_track_flag = 0
WHERE submitted_timestamp IS NOT NULL;

ALTER TABLE ${datasource.user}.pwa_application_details
ADD CONSTRAINT pad_sub_ft_flag_ck CHECK (
  (submitted_as_fast_track_flag IS NULL AND submitted_timestamp IS NULL)
  OR
  (submitted_as_fast_track_flag IS NOT NULL AND submitted_timestamp IS NOT NULL)
);