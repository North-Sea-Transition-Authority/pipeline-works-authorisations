package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

public enum Chemical {
  H2("H₂", 10, List.of(PwaResourceType.HYDROGEN)),
  O2("O₂", 20, List.of(PwaResourceType.HYDROGEN)),
  H2S("H₂S", 30),
  CO2("CO₂", 40),
  H2O("H₂O", 50),
  N2("N₂", 60),
  C1("C1", 70),
  C2("C2", 80),
  C3("C3", 90),
  C4("C4", 100),
  C5("C5", 110),
  C6("C6", 120),
  C7("C7", 130),
  C8("C8", 140),
  C9("C9", 150),
  C10("C10", 160),
  C11("C11", 170),
  C12_PLUS("C12+", 180);

  private final String displayText;
  private final int displayOrder;

  private final List<PwaResourceType> applicableResourceTypes;

  Chemical(String displayText, int displayOrder, List<PwaResourceType> applicableResourceTypes) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.applicableResourceTypes = applicableResourceTypes;
  }

  Chemical(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.applicableResourceTypes = PwaResourceType.getAll();
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

  public static List<Chemical> getAll() {
    return Arrays.asList(Chemical.values());
  }

  public static List<Chemical> getAllByResourceType(PwaResourceType resourceType) {
    return Arrays.stream(Chemical.values())
        .filter(chemical -> chemical.getApplicableResourceTypes().contains(resourceType))
        .collect(Collectors.toList());
  }
}
