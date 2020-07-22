package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public class NamedPipelineDto implements NamedPipeline {

  private Integer pipelineId;
  private PipelineType pipelineType;
  private Boolean pipelineInBundle;
  private String bundleName;
  private BigDecimal maxExternalDiameter;
  private String pipelineNumber;

  public NamedPipelineDto(PipelineDetail detail) {
    this.pipelineId = detail.getPipelineId();
    this.pipelineType = detail.getPipelineType();
    this.pipelineInBundle = detail.getPipelineInBundle();
    this.bundleName = detail.getBundleName();
    this.maxExternalDiameter = detail.getMaxExternalDiameter();
    this.pipelineNumber = detail.getPipelineNumber();
  }

  @Override
  public Integer getPipelineId() {
    return pipelineId;
  }

  @Override
  public PipelineType getPipelineType() {
    return pipelineType;
  }

  @Override
  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  @Override
  public String getBundleName() {
    return bundleName;
  }

  @Override
  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter;
  }

  @Override
  public String getPipelineNumber() {
    return pipelineNumber;
  }
}
