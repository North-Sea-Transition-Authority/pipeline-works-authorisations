UPDATE ${datasource.user}.pwa_applications
SET app_reference = (
    SUBSTR(app_reference, 0,
           INSTR(app_reference, '/', -1) -1)
    );