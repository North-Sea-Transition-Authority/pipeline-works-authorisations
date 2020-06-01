package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/* Regardless of application or consented status, provide consistent interface for basic summarised pipeline data */
public interface PipelineOverview {

  Integer getPadPipelineId();

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
