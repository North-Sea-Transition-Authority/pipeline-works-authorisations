/**
  The regulator has a public interface which queries the PWA tables in order to
  display data. To avoid potential issues with the source schema the service uses being
  locked by a misconfiguration on the regulator interface we provide a separate schema user
  for them to connect with. In order to ensure the interface schema can see all the relevant objects
  we loop over relevant tables and views in the source schema and grant SELECT to the interface user after
  every migration run.
 */
DECLARE

  K_SOURCE_SCHEMA_USER CONSTANT VARCHAR2(30) := UPPER('${datasource.user}');
  K_INTERFACE_SCHEMA_USER CONSTANT VARCHAR2(30) := UPPER('${datasource.bi-user}');

BEGIN

  FOR object IN (
    SELECT
      ao.owner
    , ao.object_name
    FROM all_objects ao
    WHERE UPPER(ao.owner) = K_SOURCE_SCHEMA_USER
    AND UPPER(ao.object_type) IN ('TABLE', 'VIEW')
    AND UPPER(object_name) not like 'ACT_%' -- ignore camunda tables
    AND UPPER(object_name) not like 'QRTZ%' -- ignore quartz job tables
    AND UPPER(object_name) not like 'SPRING%' -- ignore spring tables/views
    AND UPPER(object_name) not like 'FLYWAY%' -- ignore flyway tables
    AND UPPER(object_name) not like '%FLYWAY'
  ) LOOP

    EXECUTE IMMEDIATE 'GRANT SELECT ON ' || object.owner || '.' || object.object_name || ' TO ' || K_INTERFACE_SCHEMA_USER;

  END LOOP;

END;