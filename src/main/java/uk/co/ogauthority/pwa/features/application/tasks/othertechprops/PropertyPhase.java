package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

public enum PropertyPhase {

    OIL(
        "Oil",
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    CONDENSATE(
        "Condensate",
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    GAS(
        "Gas"),
    WATER(
        "Water",
        List.of(PwaResourceType.HYDROGEN, PwaResourceType.PETROLEUM)),
    LIQUID(
        "Liquid",
        List.of(PwaResourceType.CCUS)),
    DENSE(
        "Dense",
        List.of(PwaResourceType.CCUS)),
    OTHER(
        "Other");

  private final String displayText;

  private final List<PwaResourceType> resourceTypes;

  PropertyPhase(String displayText) {
    this.resourceTypes = PwaResourceType.getAll();
    this.displayText = displayText;
  }

  PropertyPhase(String displayText, List<PwaResourceType> resourceTypes) {
    this.resourceTypes = resourceTypes;
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public List<PwaResourceType> getResourceTypes() {
    return resourceTypes;
  }

  public static List<PropertyPhase> asList() {
    return Arrays.asList(PropertyPhase.values());
  }

  public static List<PropertyPhase> asList(PwaResourceType resourceType) {
    return asList().stream()
        .filter(property -> property.getResourceTypes().contains(resourceType))
        .collect(Collectors.toList());
  }

  public static Stream<PropertyPhase> stream() {
    return Arrays.stream(PropertyPhase.values());
  }
}
