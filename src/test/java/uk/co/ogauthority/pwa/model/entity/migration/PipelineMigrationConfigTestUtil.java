package uk.co.ogauthority.pwa.model.entity.migration;



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