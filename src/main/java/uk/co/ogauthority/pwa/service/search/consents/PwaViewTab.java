package uk.co.ogauthority.pwa.service.search.consents;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

public enum PwaViewTab {

  PIPELINES(
      "Pipelines",
      "pipelines",
      "pipelines",
      10
  ),

  CONSENT_HISTORY(
      "Consent history",
      "consent-history",
      "consentHistory",
      20
  );

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;

  PwaViewTab(String label, String anchor, String value, int displayOrder) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
  }

  public static Stream<PwaViewTab> stream() {
    return Stream.of(PwaViewTab.values());
  }

  public static PwaViewTab resolveByValue(String tabValue) {
    return PwaViewTab.stream()
        .filter(tab -> tab.getValue().equals(tabValue))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't resolve PwaViewTab using value: [%s]", tabValue)));
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
}
