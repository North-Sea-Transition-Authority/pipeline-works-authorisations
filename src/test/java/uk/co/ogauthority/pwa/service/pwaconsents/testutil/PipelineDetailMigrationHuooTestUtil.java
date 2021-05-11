package uk.co.ogauthority.pwa.service.pwaconsents.testutil;

import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationHuooData;

public class PipelineDetailMigrationHuooTestUtil {



  private PipelineDetailMigrationHuooTestUtil(){}


  public static PipelineDetailMigrationHuooData createMigrationHuooData(PipelineDetail pipelineDetail) {

    var pipelineDetailMigrationHuooData = new PipelineDetailMigrationHuooData();
    pipelineDetailMigrationHuooData.setPipelineDetail(pipelineDetail);
    return pipelineDetailMigrationHuooData;
  }





}
