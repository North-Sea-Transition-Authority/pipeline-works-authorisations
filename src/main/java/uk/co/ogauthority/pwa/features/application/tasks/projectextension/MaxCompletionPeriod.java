package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static uk.co.ogauthority.pwa.features.application.tasks.projectextension.ProjectExtensionType.NO;
import static uk.co.ogauthority.pwa.features.application.tasks.projectextension.ProjectExtensionType.YES;

import java.util.List;

public enum MaxCompletionPeriod {

  INITIAL(12, YES),
  CAT_1_VARIATION(12, YES),
  CAT_2_VARIATION(12, NO),
  HUOO_VARIATION(12, NO),
  DEPOSIT_CONSENT(12, NO),
  OPTIONS_VARIATION(6, NO),
  DECOMMISSIONING(12, NO);

  private Integer maxMonthsCompletion;

  private ProjectExtensionType extendable;

  MaxCompletionPeriod(Integer maxMonthsCompletion, ProjectExtensionType extendable) {
    this.maxMonthsCompletion = maxMonthsCompletion;
    this.extendable = extendable;
  }

  public Integer getMaxMonthsCompletion() {
    return maxMonthsCompletion;
  }

  public ProjectExtensionType getExtendable() {
    return extendable;
  }

  public boolean isExtendable() {
    return List.of(YES).contains(extendable);
  }
}
