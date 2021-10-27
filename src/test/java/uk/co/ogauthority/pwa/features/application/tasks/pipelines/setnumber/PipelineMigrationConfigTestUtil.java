package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;


public final class PipelineMigrationConfigTestUtil {

  private PipelineMigrationConfigTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PipelineMigrationConfig create(int min, int max){
    var config = new PipelineMigrationConfig();
    config.setReservedPipelineNumberMin(min);
    config.setReservedPipelineNumberMax(max);
    return config;

  }

}