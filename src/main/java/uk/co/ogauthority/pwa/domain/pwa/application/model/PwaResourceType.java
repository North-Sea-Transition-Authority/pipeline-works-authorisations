package uk.co.ogauthority.pwa.domain.pwa.application.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PwaResourceType {
  PETROLEUM(
      "Petroleum",
      10,
      List.of(
          PwaApplicationType.INITIAL,
          PwaApplicationType.CAT_1_VARIATION,
          PwaApplicationType.CAT_2_VARIATION,
          PwaApplicationType.HUOO_VARIATION,
          PwaApplicationType.DEPOSIT_CONSENT,
          PwaApplicationType.OPTIONS_VARIATION,
          PwaApplicationType.DECOMMISSIONING
      )
  ),
  HYDROGEN(
      "Hydrogen",
      20,
      List.of(
          PwaApplicationType.INITIAL,
          PwaApplicationType.CAT_1_VARIATION
      )
  );

  private final String displayName;

  private final int displayOrder;

  private final List<PwaApplicationType> permittedApplicationTypes;

  PwaResourceType(String displayName, int displayOrder, List<PwaApplicationType> permittedApplicationTypes) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.permittedApplicationTypes = permittedApplicationTypes;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<PwaApplicationType> getPermittedApplicationTypes() {
    return permittedApplicationTypes;
  }

  public static List<PwaResourceType> getAll() {
    return Arrays.stream(PwaResourceType.values()).collect(Collectors.toList());
  }
}
