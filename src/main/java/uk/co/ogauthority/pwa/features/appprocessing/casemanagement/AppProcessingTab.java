package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

public enum AppProcessingTab {

  TASKS(
      "Tasks",
      "Application status",
      "tasks",
      "tasks",
      10
  ),
  CASE_HISTORY(
      "Case history",
      "Case history",
      "case-history",
      "caseHistory",
      20
  );

  private final String regulatorConsulteeLabel;
  private final String industryLabel;
  private final String anchor;
  private final String value;
  private final int displayOrder;

  AppProcessingTab(String regulatorConsulteeLabel, String industryLabel, String anchor, String value,
                   int displayOrder) {
    this.regulatorConsulteeLabel = regulatorConsulteeLabel;
    this.industryLabel = industryLabel;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
  }

  public static Stream<AppProcessingTab> stream() {
    return Stream.of(AppProcessingTab.values());
  }

  public static AppProcessingTab resolveByValue(String tabValue) {
    return AppProcessingTab.stream()
        .filter(tab -> tab.getValue().equals(tabValue))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't resolve AppProcessingTab using value: [%s]", tabValue)));
  }

  public String getRegulatorConsulteeLabel() {
    return regulatorConsulteeLabel;
  }

  public String getIndustryLabel() {
    return industryLabel;
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

  public String getLabel(boolean industryFlag) {
    return industryFlag ? industryLabel : regulatorConsulteeLabel;
  }

}
