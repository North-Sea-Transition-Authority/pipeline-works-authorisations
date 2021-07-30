DECLARE

  PROCEDURE update_Pipeline_Detail(
    p_pipeline_detail_id NUMBER,
    p_pipeline_number VARCHAR2,
    p_pipeline_status VARCHAR2
    )
  AS
     l_new_xml XMLTYPE;
  BEGIN

    SELECT
      XMLQUERY('
      copy $dom := $xml_data
      modify(
        replace value of node $dom/PIPELINE/PIPELINE_NUMBER with $pipelineNumber
      , replace value of node $dom/PIPELINE/PIPELINE_STATUS with $pipelineStatus
      )
      return $dom
      '
      PASSING
        PD.XML_DATA AS "xml_data"
      , p_pipeline_number AS "pipelineNumber"
      , p_pipeline_status AS "pipelineStatus"
      RETURNING CONTENT
      ) new_xml_data
    INTO l_new_xml
    FROM decmgr.pipeline_details pd
    WHERE pd.id = p_pipeline_detail_id;

    UPDATE decmgr.pipeline_details pd
    SET pd.xml_data = l_new_xml
    WHERE pd.id = p_pipeline_detail_id;

    IF(SQL%ROWCOUNT != 1) THEN
      RAISE_APPLICATION_ERROR(-20123, 'More than 1 row updated!');
    END IF;
  END update_pipeline_detail;

BEGIN

  FOR pl IN (
    SELECT
      xph.pipeline_id
    , xph.pd_id
    , xph.pipeline_number
    , xph.pipeline_status
    , xph.status record_status
    FROM decmgr.xview_pipelines_history xph
    WHERE xph.pipeline_number IN ('PL461', 'PL462', 'PL463', 'PL464') AND xph.status_control = 'C'
  ) LOOP

    IF(pl.pipeline_number LIKE '%NL') THEN
      RAISE_APPLICATION_ERROR(-20124, 'pipeline number already indicates NOT_LAID');
    END IF;

    dbms_output.put_line('Updating '|| pl.pipeline_number || ' to expected status NOT_LAID via name change');

    update_pipeline_detail(
      p_pipeline_detail_id => pl.pd_id
    , p_pipeline_number => pl.pipeline_number || ' NL'
   , p_pipeline_status => pl.pipeline_status
    );

  END LOOP;

  FOR pl IN (
    SELECT
      xph.pipeline_id
         , xph.pd_id
         , xph.pipeline_number
         , xph.pipeline_status
         , xph.status record_status
    FROM decmgr.xview_pipelines_history xph
    WHERE xph.pipeline_number IN ('PL472', 'PL474', 'PL476', 'PL478')
    AND xph.status_control = 'C'
    ) LOOP

      IF(pl.pipeline_number LIKE '%RTS') THEN
        RAISE_APPLICATION_ERROR(-20124, 'pipeline number already indicates RETURNED_TO_SHORE');
      END IF;

      dbms_output.put_line('Updating '|| pl.pipeline_number || ' to expected status RETURNED_TO_SHORE via name change');

      update_pipeline_detail(
        p_pipeline_detail_id => pl.pd_id
      , p_pipeline_number => pl.pipeline_number || ' RTS'
      , p_pipeline_status => pl.pipeline_status
      );

    END LOOP;

  FOR pl IN (
    SELECT
      xph.pipeline_id
    , xph.pd_id
    , xph.pipeline_number
    , xph.pipeline_status
    , xph.status record_status
    FROM decmgr.xview_pipelines_history xph
    WHERE xph.pipeline_number IN ('PL381', 'PL382', 'PL383', 'PL384')
    AND xph.status_control = 'C'
    ) LOOP

      IF(pl.pipeline_status = 'NOT_LAID') THEN
        RAISE_APPLICATION_ERROR(-20124, 'pipeline status already indicates NOT_LAID');
      END IF;

      dbms_output.put_line('Updating '|| pl.pipeline_number || ' status to NOT_LAID');

      update_pipeline_detail(
          p_pipeline_detail_id => pl.pd_id
        , p_pipeline_number => pl.pipeline_number
        , p_pipeline_status => 'NOT_LAID'
        );

    END LOOP;

  FOR pl IN (
    SELECT
      xph.pipeline_id
     , xph.pd_id
     , xph.pipeline_number
     , xph.pipeline_status
     , xph.status record_status
    FROM decmgr.xview_pipelines_history xph
    WHERE xph.pipeline_number IN ('PL571', 'PL572', 'PL573', 'PL574')
    AND xph.status_control = 'C'
    ) LOOP

      IF(pl.pipeline_status = 'OUT_OF_USE') THEN
        RAISE_APPLICATION_ERROR(-20124, 'pipeline status already indicates OUT_OF_USE');
      END IF;

      dbms_output.put_line('Updating '|| pl.pipeline_number || ' status to OUT_OF_USE');

      update_pipeline_detail(
          p_pipeline_detail_id => pl.pd_id
        , p_pipeline_number => pl.pipeline_number
        , p_pipeline_status => 'OUT_OF_USE'
        );

    END LOOP;

END;

/

COMMIT;