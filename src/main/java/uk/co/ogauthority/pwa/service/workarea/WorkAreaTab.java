package uk.co.ogauthority.pwa.service.workarea;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

/**
 * Differentiates between tabs requested on work area.
 */
public enum WorkAreaTab {

  REGULATOR_OPEN_APPLICATIONS(
      "My open applications",
      "open-reg-apps",
      "openRegApplications",
      10,
      UserType.OGA),

  INDUSTRY_OPEN_APPLICATIONS(
      "My open applications",
      "open-ind-apps",
      "openIndApplications",
      20,
      UserType.INDUSTRY),

  INDUSTRY_SUBMITTED_APPLICATIONS(
      "My submitted applications",
      "submitted-ind-apps",
      "submittedIndApplications",
      30,
      UserType.INDUSTRY),

  OPEN_CONSULTATIONS(
      "My open consultations",
      "open-consultations",
      "openConsultations",
      40,
      UserType.CONSULTEE);

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;
  private final UserType userType;

  WorkAreaTab(String label,
              String anchor,
              String value,
              int displayOrder,
              UserType userType) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
    this.userType = userType;
  }

  public String getLabel() {
    return label;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getValue() {
    return value;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public UserType getUserType() {
    return userType;
  }

  public static Stream<WorkAreaTab> stream() {
    return Stream.of(WorkAreaTab.values());
  }

  public static WorkAreaTab fromValue(String value) {
    return WorkAreaTab.stream()
        .filter(tab -> tab.getValue().equals(value))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Couldn't find WorkAreaTab with value: %s", value)));
  }
}
