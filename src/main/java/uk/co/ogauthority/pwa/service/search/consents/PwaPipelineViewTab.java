package uk.co.ogauthority.pwa.service.search.consents;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

public enum PwaPipelineViewTab {

  PIPELINE_HISTORY(
      "Pipeline history",
      "pipeline-history",
      "pipelineHistory",
      10
  ),
  HUOO_HISTORY(
      "HUOO history",
      "huoo-history",
      "huooHistory",
      20
  ),
  AS_BUILT_NOTIFICATION_HISTORY(
      "As-built notification history",
      "as-built-notification-history",
      "asBuiltNotificationHistory",
      30
  );

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;

  PwaPipelineViewTab(String label, String anchor, String value, int displayOrder) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
  }

  public static Stream<PwaPipelineViewTab> stream() {
    return Stream.of(PwaPipelineViewTab.values());
  }

  public static PwaPipelineViewTab resolveByValue(String tabValue) {
    return PwaPipelineViewTab.stream()
        .filter(tab -> tab.getValue().equals(tabValue))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't resolve PwaPipelineViewTab using value: [%s]", tabValue)));
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
