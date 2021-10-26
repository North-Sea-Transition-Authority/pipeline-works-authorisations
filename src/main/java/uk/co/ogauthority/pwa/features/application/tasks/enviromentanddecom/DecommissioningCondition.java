package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import java.util.Arrays;
import java.util.stream.Stream;

public enum DecommissioningCondition {

  EOL_REGULATION_STATEMENT(10,
      "I accept that options for the decommissioning of the pipeline(s) will be considered at the end of " +
          "the field life and should adhere to government policies and regulations in force at the time.",
      "Adhere to government policies and regulations"),
  EOL_REMOVAL_STATEMENT(20,
      "I accept that any mattresses or grout bags which have been installed to protect pipelines during " +
          "their operational life should be removed for disposal onshore.",
      "Mattresses or grout bags removed"),
  EOL_REMOVAL_PROPOSAL(30,
      "I accept that if the condition of the mattresses or grout bags is such that they cannot be removed " +
          "safely or efficiently then any proposal to leave them in place must be supported by an appropriate " +
          "comparative assessment of the options.",
      "Mattresses or grout bags assessment if not removed"),
  EOL_BUNDLES_STATEMENT(40,
      "We note that current OPRED guidance is that any new pipeline bundle(s) which are under construction" +
          " will have been designed for future removal, as per current requirements on decommissioning programmes.",
      "Bundle(s) under construction designed for future removal");


  private int displayOrder;
  private String conditionText;
  private String summaryText;

  DecommissioningCondition(int displayOrder, String conditionText, String summaryText) {
    this.displayOrder = displayOrder;
    this.conditionText = conditionText;
    this.summaryText = summaryText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getConditionText() {
    return conditionText;
  }

  public String getSummaryText() {
    return summaryText;
  }

  public static Stream<DecommissioningCondition> stream() {
    return Arrays.stream(DecommissioningCondition.values());
  }
}
