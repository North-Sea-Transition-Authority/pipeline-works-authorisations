package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/* Regardless of application or consented status, provide consistent interface for basic summarised pipeline data */
// TODO - This is doing way too much as it contains the application pipeline id and an "as built" reference which is a
//  seperate application area. Prefer "NamedPipeline" in most cases as it has a reduced scope.
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

  BigDecimal getMaxExternalDiameter();

  String getBundleName();

  default PipelineCoreType getCoreType() {
    return getPipelineType().getCoreType();
  }

  PipelineFlexibility getPipelineFlexibility();

  PipelineMaterial getPipelineMaterial();

  String getOtherPipelineMaterialUsed();

  Boolean getTrenchedBuriedBackfilled();

  String getTrenchingMethodsDescription();

  PipelineStatus getPipelineStatus();

  String getPipelineStatusReason();

  String getTemporaryPipelineNumber();

  Boolean getAlreadyExistsOnSeabed();

  Boolean getPipelineInUse();

  String getFootnote();

  default AsBuiltNotificationStatus getAsBuiltNotificationStatus() {
    return null;
  }

}
