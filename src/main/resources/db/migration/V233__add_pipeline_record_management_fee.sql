DECLARE
    l_fee_period_detail_id NUMBER;
    l_fee_item_id NUMBER;

BEGIN

    INSERT INTO ${datasource.user}.fee_items (pwa_application_type, pwa_application_fee_type, display_description)
    VALUES ('PIPELINE_RECORD_MANAGEMENT', 'DEFAULT', 'Charge for submitting a pipeline record management application')
    RETURNING id
    INTO l_fee_item_id;

    SELECT fpd.id
    INTO l_fee_period_detail_id
    FROM ${datasource.user}.fee_period_details fpd
    WHERE fpd.tip_flag = 1
    AND period_end_timestamp IS NULL;

    INSERT INTO ${datasource.user}.fee_period_detail_fee_items (fee_item_id, fee_period_detail_id, penny_amount)
    VALUES (l_fee_item_id, l_fee_period_detail_id, 51000);

END;