CREATE OR REPLACE VIEW ${datasource.user}.vw_fee_info AS
SELECT
    fp.id as fee_period_id,
    fp.description,
    fpd.period_start_timestamp,
    fpd.period_end_timestamp
FROM
    ${datasource.user}.fee_periods fp
    LEFT JOIN ${datasource.user}.fee_period_details fpd ON fp.id = fpd.fee_period_id AND tip_flag = 1
ORDER BY
    fpd.period_start_timestamp DESC;

CREATE OR REPLACE VIEW ${datasource.user}.vw_fee_items AS
SELECT
    fpdfi.id as fee_period_detail_item_id,
    fpd.fee_period_id,
    fi.display_description,
    fi.pwa_application_type,
    fi.pwa_application_fee_type,
    fpdfi.penny_amount
FROM
    ${datasource.user}.fee_items fi
    LEFT JOIN ${datasource.user}.fee_period_detail_fee_items fpdfi ON fi.id = fpdfi.fee_item_id
    LEFT JOIN ${datasource.user}.fee_period_details fpd ON fpd.id = fpdfi.fee_period_detail_id AND fpd.tip_flag = 1;