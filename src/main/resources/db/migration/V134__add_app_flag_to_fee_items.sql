DELETE FROM ${datasource.user}.fee_period_detail_fee_items;

DELETE FROM ${datasource.user}.fee_items;

ALTER TABLE ${datasource.user}.fee_items ADD (
  pwa_application_fee_type VARCHAR2(1000) NOT NULL
);

DECLARE

  l_fee_period_detail_id NUMBER;

BEGIN

  SELECT fpd.id
  INTO l_fee_period_detail_id
  FROM ${datasource.user}.fee_period_details fpd
  WHERE fpd.tip_flag = 1;

  INSERT INTO ${datasource.user}.fee_items (pwa_application_type, pwa_application_fee_type, display_description)
  SELECT *
  FROM (
         --default charge values
         SELECT 'INITIAL', 'DEFAULT', 'Charge for submitting an initial PWA application'
         FROM dual
         UNION ALL
         SELECT 'DEPOSIT_CONSENT', 'DEFAULT', 'Charge for submitting a deposit consent application'
         FROM dual
         UNION ALL
         SELECT 'CAT_1_VARIATION', 'DEFAULT', 'Charge for submitting a category 1 variation application'
         FROM dual
         UNION ALL
         SELECT 'CAT_2_VARIATION', 'DEFAULT', 'Charge for submitting a category 2 variation application'
         FROM dual
         UNION ALL
         SELECT 'HUOO_VARIATION', 'DEFAULT', 'Charge for submitting a HUOO variation application'
         FROM dual
         UNION ALL
         SELECT 'OPTIONS_VARIATION', 'DEFAULT', 'Charge for submitting an options variation application'
         FROM dual
         UNION ALL
         SELECT 'DECOMMISSIONING', 'DEFAULT', 'Charge for submitting a decommissioning application'
         FROM dual
              -- fast track
         UNION ALL
         SELECT 'INITIAL', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'DEPOSIT_CONSENT', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'CAT_1_VARIATION', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'CAT_2_VARIATION', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'HUOO_VARIATION', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'OPTIONS_VARIATION', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
         UNION ALL
         SELECT 'DECOMMISSIONING', 'FAST_TRACK', 'Fast-track application surcharge'
         FROM dual
       );

  /** provided data
  Type    Default FT-period FT-Total Fee
  INITIAL	£2,575	4 months	£5,150
  CAT_1	  £2,575	4 months	£5,150
  CAT_2	  £1,275	6 weeks	  £2,550
  HUOO	  £1,275	6 weeks	  £2,550
  DEPOSIT	£975	  6 weeks	  £1,950
  OPTIONS	£1,275	6 weeks	  £2,550
  DECOM 	£1,275	6 months	£2,550
   */
  INSERT INTO ${datasource.user}.fee_period_detail_fee_items (fee_item_id, fee_period_detail_id, penny_amount)
  SELECT
    fi.id
  , l_fee_period_detail_id
  , CASE
      WHEN fi.pwa_application_type IN ('INITIAL', 'CAT_1_VARIATION' ) THEN 257500
      WHEN fi.pwa_application_type IN ('CAT_2_VARIATION', 'HUOO_VARIATION', 'OPTIONS_VARIATION', 'DECOMMISSIONING') THEN 127500
      WHEN fi.pwa_application_type IN ('DEPOSIT_CONSENT') THEN 97500
    END
  FROM ${datasource.user}.fee_items fi;

END;