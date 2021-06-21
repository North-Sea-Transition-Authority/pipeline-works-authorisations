package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
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
