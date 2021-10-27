package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pipeline_migration_config")
public class PipelineMigrationConfig {

  @Id
  private Integer id;

  private Integer reservedPipelineNumberMin;
  private Integer reservedPipelineNumberMax;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public Integer getReservedPipelineNumberMin() {
    return reservedPipelineNumberMin;
  }

  public void setReservedPipelineNumberMin(Integer reservedPipelineNumberMin) {
    this.reservedPipelineNumberMin = reservedPipelineNumberMin;
  }


  public Integer getReservedPipelineNumberMax() {
    return reservedPipelineNumberMax;
  }

  public void setReservedPipelineNumberMax(Integer reservedPipelineNumberMax) {
    this.reservedPipelineNumberMax = reservedPipelineNumberMax;
  }


}
