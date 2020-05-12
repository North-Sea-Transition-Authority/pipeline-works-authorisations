BEGIN
  FOR huoo_row IN (
    SELECT
      por.role
    , por.id
    , por.ou_id
    , por.application_detail_id
    , por.agreement
    , por.type
    FROM ${datasource.user}.pad_organisation_roles por
  ) LOOP

      -- Iterate four times (Holder, User, Operator, Owner)
      FOR iterator IN 1 .. 4 LOOP

        FOR role_table IN (
          SELECT REGEXP_SUBSTR(huoo_row.role, '[^,]+', 1, iterator) role
          FROM dual
        ) LOOP

        -- role can be null if all four iterations were not CSV'd on the row.
        IF role_table.role IS NOT NULL THEN

          DELETE FROM ${datasource.user}.pad_organisation_roles por
          WHERE por.id = huoo_row.id;

          INSERT INTO ${datasource.user}.pad_organisation_roles (
            ou_id
          , application_detail_id
          , agreement
          , type
          , role
          ) VALUES (
            huoo_row.ou_id
          , huoo_row.application_detail_id
          , huoo_row.agreement
          , huoo_row.type
          , role_table.role
          );

        END IF;

      END LOOP;

    END LOOP;

  END LOOP;

  COMMIT;

END;