package uk.co.ogauthority.pwa.service.workarea;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

/**
 * Differentiates between tabs requested on work area.
 */
public enum WorkAreaTab {

  OPEN_APPLICATIONS(
      "My open applications",
      "open-apps",
      "openApplications",
      10),

  OPEN_CONSULTATIONS(
      "My open consultations",
      "open-consultations",
      "openConsultations",
      20);

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;

  WorkAreaTab(String label, String anchor, String value, int displayOrder) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
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
