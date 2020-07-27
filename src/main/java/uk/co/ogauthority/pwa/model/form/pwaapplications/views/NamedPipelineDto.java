package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import com.google.common.annotations.VisibleForTesting;
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

  @VisibleForTesting
  public NamedPipelineDto(Integer pipelineId, PipelineType pipelineType, Boolean pipelineInBundle,
                          String bundleName, BigDecimal maxExternalDiameter, String pipelineNumber) {
    this.pipelineId = pipelineId;
    this.pipelineType = pipelineType;
    this.pipelineInBundle = pipelineInBundle;
    this.bundleName = bundleName;
    this.maxExternalDiameter = maxExternalDiameter;
    this.pipelineNumber = pipelineNumber;
  }

  public static NamedPipelineDto fromPipelineDetail(PipelineDetail detail) {
    return new NamedPipelineDto(
        detail.getPipelineId().asInt(), detail.getPipelineType(), detail.getPipelineInBundle(),
        detail.getBundleName(), detail.getMaxExternalDiameter(), detail.getPipelineNumber()
    );
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
