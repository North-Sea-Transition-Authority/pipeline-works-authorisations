package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;

/**
 * This interface is used to produce the name of a pipeline.
 * <p></p>
 * It is a slimmed down version of PipelineOverview, reducing the complexity needed when the name  of a pipeline
 * is the only information required.
 */
public interface NamedPipeline {

  Integer getPipelineId();

  /**
   * pipelineName is used for PWA users to easily identify a pipeline on an application,
   * where as the pipeline number uniquely identifies a pipeline and is used as the main reference by external applications.
   */
  default String getPipelineName() {
    var pipelineName = getPipelineNumber() + " - ";
    if (getPipelineType() != null) {
      if (getPipelineType().getCoreType().equals(PipelineCoreType.SINGLE_CORE) && getMaxExternalDiameter() != null) {
        pipelineName += getMaxExternalDiameter() + " Millimetre ";
      }
    }
    if (getPipelineType() != null) {
      pipelineName += getPipelineType().getDisplayName();
    }
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
