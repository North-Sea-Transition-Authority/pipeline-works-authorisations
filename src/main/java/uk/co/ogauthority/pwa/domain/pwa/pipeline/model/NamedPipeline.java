package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.math.BigDecimal;
import org.apache.commons.lang3.BooleanUtils;

/**
 * This interface is used to produce the name of a pipeline.
 *
 * <p>
 * It is a slimmed down version of PipelineOverview, reducing the complexity needed when the name  of a pipeline
 * is the only information required.
 * </p>
 */
public interface NamedPipeline {

  Integer getPipelineId();

  /**
   * pipelineName is used for PWA users to easily identify a pipeline on an application,
   * where as the pipeline number uniquely identifies a pipeline and is used as the main reference by external applications.
   * Note: Pipeline Name logic is duplicated in 'api_vw_pwa_pipeline_details'. Remember to update  that view should this change.
   */
  default String getPipelineName() {
    var pipelineName = getPipelineNumber() + " - ";
    var pipelineType = getPipelineType() != null ? getPipelineType() : PipelineType.UNKNOWN;
    var coreType = pipelineType.getCoreType();

    if (coreType.equals(PipelineCoreType.SINGLE_CORE) && getMaxExternalDiameter() != null) {
      pipelineName += getMaxExternalDiameter() + " Millimetre ";
    }

    pipelineName += pipelineType.getDisplayName();

    if (BooleanUtils.isTrue(getPipelineInBundle())) {
      pipelineName += " (" + getBundleName() + ")";
    }
    return pipelineName;
  }

  PipelineType getPipelineType();

  Boolean getPipelineInBundle();

  String getBundleName();

  BigDecimal getMaxExternalDiameter();

  String getPipelineNumber();

}
