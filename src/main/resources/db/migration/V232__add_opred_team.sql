DECLARE

  l_uuid VARCHAR2(36);

  FUNCTION new_uuid RETURN VARCHAR2 IS
  BEGIN
    -- Creates a numeric only uuid in standard format XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
  RETURN
    LPAD(ROUND(dbms_random.value(0,99999999)), 8, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,999999999999)), 12, '0');
  END;

BEGIN

  l_uuid := new_uuid();

  INSERT INTO ${datasource.user}.teams (id, type, name)
  VALUES (l_uuid, 'SECONDARY_REGULATOR', 'OPRED');

END;
/