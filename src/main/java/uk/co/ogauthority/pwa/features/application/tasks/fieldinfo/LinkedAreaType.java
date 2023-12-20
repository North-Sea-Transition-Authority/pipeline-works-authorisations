package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;

public enum LinkedAreaType {
  FIELD(List.of(
      PwaResourceType.PETROLEUM,
      PwaResourceType.HYDROGEN)),
  CARBON_STORAGE_AREA(List.of(
      PwaResourceType.CCUS
  ));

  private List<PwaResourceType> applicableResourceTypes;

  LinkedAreaType(List<PwaResourceType> applicableResourceTypes) {
    this.applicableResourceTypes = applicableResourceTypes;
  }

  public List<PwaResourceType> getApplicableResourceTypes() {
    return applicableResourceTypes;
  }

  public static LinkedAreaType findAreaTypeByResourceType(PwaResourceType resourceType) {
    return Arrays.stream(values())
        .filter(type -> type.getApplicableResourceTypes().contains(resourceType))
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format(
                "No associated area type for resource type: %s",
                resourceType)));
  }
}
