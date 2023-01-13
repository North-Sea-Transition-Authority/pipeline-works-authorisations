package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

public enum MaxCompletionPeriod {

  INITIAL(12, true),
  CAT_1_VARIATION(12, true),
  CAT_2_VARIATION(12, false),
  HUOO_VARIATION(12, false),
  DEPOSIT_CONSENT(12, false),
  OPTIONS_VARIATION(6, false),
  DECOMMISSIONING(12, false);

  private Integer maxMonthsCompletion;

  private boolean extendable;

  MaxCompletionPeriod(Integer maxMonthsCompletion, boolean extendable) {
    this.maxMonthsCompletion = maxMonthsCompletion;
    this.extendable = extendable;
  }

  public Integer getMaxMonthsCompletion() {
    return maxMonthsCompletion;
  }

  public boolean isExtendable() {
    return extendable;
  }
}
