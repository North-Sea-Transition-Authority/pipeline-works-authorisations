ALTER TABLE ${datasource.user}.fee_periods
  ADD (
    description VARCHAR2(4000) NOT NULL
);

DECLARE

  l_fee_period_id NUMBER;
  l_fee_period_detail_id NUMBER;

BEGIN

  INSERT INTO ${datasource.user}.fee_periods(description)
  VALUES ('System launch fee period')
  RETURNING id INTO l_fee_period_id;

  INSERT INTO ${datasource.user}.fee_period_details( fee_period_id
                                                   , period_start_timestamp
                                                   , tip_flag)
  VALUES ( l_fee_period_id
         , SYSTIMESTAMP
         , 1)
  RETURNING id INTO l_fee_period_detail_id;

  INSERT INTO ${datasource.user}.fee_items (pwa_application_type, display_description)
   SELECT *
   FROM (
    SELECT 'INITIAL', 'Charge for submitting an initial PWA application' FROM dual
    UNION ALL
    SELECT 'DEPOSIT_CONSENT', 'Charge for submitting a deposit consent application' FROM dual
    UNION ALL
    SELECT 'CAT_1_VARIATION', 'Charge for submitting a category 1 variation application' FROM dual
    UNION ALL
    SELECT 'CAT_2_VARIATION', 'Charge for submitting a category 2 variation application' FROM dual
    UNION ALL
    SELECT 'HUOO_VARIATION', 'Charge for submitting a HUOO variation application' FROM dual
    UNION ALL
    SELECT 'OPTIONS_VARIATION', 'Charge for submitting an options variation application' FROM dual
    UNION ALL
    SELECT 'DECOMMISSIONING', 'Charge for submitting a decommissioning application' FROM dual
  );

  INSERT INTO ${datasource.user}.fee_period_detail_fee_items (fee_item_id, fee_period_detail_id, penny_amount)
  SELECT
    fi.id
  , l_fee_period_detail_id
  , 100
  FROM ${datasource.user}.fee_items fi;


END;

