package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/* Regardless of application or consented status, provide consistent interface for basic summarised pipeline data */
public interface PipelineOverview extends NamedPipeline {

  Integer getPadPipelineId();

  String getFromLocation();

  CoordinatePair getFromCoordinates();

  String getToLocation();

  CoordinatePair getToCoordinates();

  String getComponentParts();

  BigDecimal getLength();

  String getProductsToBeConveyed();

  Long getNumberOfIdents();


}
