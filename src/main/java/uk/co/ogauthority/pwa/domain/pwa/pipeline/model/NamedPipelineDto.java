package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public class NamedPipelineDto implements NamedPipeline {

  private final Integer pipelineId;
  private final PipelineType pipelineType;
  private final Boolean pipelineInBundle;
  private final String bundleName;
  private final BigDecimal maxExternalDiameter;
  private final String pipelineNumber;
  private final PipelineStatus pipelineStatus;

  @VisibleForTesting
  public NamedPipelineDto(Integer pipelineId,
                          PipelineType pipelineType,
                          Boolean pipelineInBundle,
                          String bundleName,
                          BigDecimal maxExternalDiameter,
                          String pipelineNumber,
                          PipelineStatus pipelineStatus) {
    this.pipelineId = pipelineId;
    this.pipelineType = pipelineType;
    this.pipelineInBundle = pipelineInBundle;
    this.bundleName = bundleName;
    this.maxExternalDiameter = maxExternalDiameter;
    this.pipelineNumber = pipelineNumber;
    this.pipelineStatus = pipelineStatus;
  }

  public static NamedPipelineDto fromPipelineDetail(PipelineDetail detail) {
    return new NamedPipelineDto(
        detail.getPipelineId().asInt(), detail.getPipelineType(), detail.getPipelineInBundle(),
        detail.getBundleName(), detail.getMaxExternalDiameter(), detail.getPipelineNumber(),
        detail.getPipelineStatus());
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

  @Override
  public String getPipelineName() {
    return pipelineStatus == PipelineStatus.RETURNED_TO_SHORE
        ? NamedPipeline.super.getPipelineName() + " - (RTS)"
        : NamedPipeline.super.getPipelineName();
  }
}
