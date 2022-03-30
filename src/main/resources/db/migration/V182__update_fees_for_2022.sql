DECLARE

    l_old_fpd_id NUMBER;

    l_new_fee_period_id NUMBER;
    l_new_fpd_id NUMBER;

    PROCEDURE add_fpd_item(p_fpd_id NUMBER, p_app_type VARCHAR2, p_penny_amount NUMBER) IS

    BEGIN

        FOR fee_item IN (
            SELECT fi.id
            FROM ${datasource.user}.fee_items fi
            WHERE fi.pwa_application_type = p_app_type
        ) LOOP

            INSERT INTO ${datasource.user}.fee_period_detail_fee_items (fee_period_detail_id, fee_item_id, penny_amount)
            VALUES (p_fpd_id, fee_item.id, p_penny_amount);

        END LOOP;

    END add_fpd_item;

BEGIN

    SELECT fpd.id
    INTO l_old_fpd_id
    FROM ${datasource.user}.fee_periods p
    JOIN ${datasource.user}.fee_period_details fpd ON fpd.fee_period_id = p.id AND fpd.tip_flag = 1
    WHERE p.description = 'System launch fee period';

    -- end the old fee period
    UPDATE ${datasource.user}.fee_period_details fpd
    SET fpd.period_end_timestamp = TO_TIMESTAMP('31/03/2022 23:59:59.999999', 'DD/MM/YYYY HH24:MI:SS.FF6')
    WHERE fpd.id = l_old_fpd_id;

    -- create new fee period and detail
    INSERT INTO ${datasource.user}.fee_periods (description)
    VALUES ('2022-23')
    RETURNING id INTO l_new_fee_period_id;

    INSERT INTO ${datasource.user}.fee_period_details(fee_period_id, period_start_timestamp, tip_flag)
    VALUES (l_new_fee_period_id, TO_TIMESTAMP('01/04/2022', 'DD/MM/YYYY'), 1)
    RETURNING id INTO l_new_fpd_id;

    add_fpd_item(l_new_fpd_id, 'INITIAL', 296000);
    add_fpd_item(l_new_fpd_id, 'CAT_1_VARIATION', 296000);
    add_fpd_item(l_new_fpd_id, 'CAT_2_VARIATION', 166000);
    add_fpd_item(l_new_fpd_id, 'OPTIONS_VARIATION', 166000);
    add_fpd_item(l_new_fpd_id, 'HUOO_VARIATION', 166000);
    add_fpd_item(l_new_fpd_id, 'DECOMMISSIONING', 166000);
    add_fpd_item(l_new_fpd_id, 'DEPOSIT_CONSENT', 136000);

END;