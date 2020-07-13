package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/* Regardless of application or consented status, provide consistent interface for basic summarised pipeline data */
public interface PipelineOverview {

  Integer getPadPipelineId();

  /**
   * pipelineName is used for PWA users to easily identify a pipeline on an application,
   * where as the pipeline number uniquely identifies a pipeline and is used as the main reference by external applications.
   */
  String getPipelineName();

  String getFromLocation();

  CoordinatePair getFromCoordinates();

  String getToLocation();

  CoordinatePair getToCoordinates();

  String getPipelineNumber();

  PipelineType getPipelineType();

  String getComponentParts();

  BigDecimal getLength();

  String getProductsToBeConveyed();

  Long getNumberOfIdents();
}
