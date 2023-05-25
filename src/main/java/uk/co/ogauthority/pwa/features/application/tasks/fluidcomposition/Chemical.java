package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

public enum Chemical {
  H2("H₂", 10, List.of(PwaResourceType.HYDROGEN)),
  O2("O₂", 20, List.of(PwaResourceType.HYDROGEN)),
  H2S("H₂S", 30, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  CO2("CO₂", 40, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  H2O("H₂O", 50, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  N2("N₂", 60, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C1("C1", 70, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C2("C2", 80, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C3("C3", 90, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C4("C4", 100, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C5("C5", 110, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C6("C6", 120, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C7("C7", 130, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C8("C8", 140, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C9("C9", 150, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C10("C10", 160, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C11("C11", 170, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN)),
  C12_PLUS("C12+", 180, List.of(PwaResourceType.PETROLEUM, PwaResourceType.HYDROGEN));

  private final String displayText;
  private final int displayOrder;

  private final List<PwaResourceType> applicableResourceTypes;

  Chemical(String displayText, int displayOrder, List<PwaResourceType> applicableResourceTypes) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.applicableResourceTypes = applicableResourceTypes;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<PwaResourceType> getApplicableResourceTypes() {
    return applicableResourceTypes;
  }

  public static List<Chemical> asList() {
    return Arrays.asList(Chemical.values());
  }

  public static List<Chemical> asList(PwaResourceType resourceType) {
    return Arrays.stream(Chemical.values())
        .filter(chemical -> chemical.getApplicableResourceTypes().contains(resourceType))
        .collect(Collectors.toList());
  }
}
