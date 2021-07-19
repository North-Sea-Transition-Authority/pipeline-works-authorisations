package uk.co.ogauthority.pwa.model.form.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ConsultationResponseOptionGroup {

  CONTENT(EnumSet.of(
      ConsultationResponseOption.CONFIRMED,
      ConsultationResponseOption.REJECTED),
      10),

  ADVICE(EnumSet.of(
      ConsultationResponseOption.PROVIDE_ADVICE,
      ConsultationResponseOption.NO_ADVICE),
      20);

  private final Set<ConsultationResponseOption> options;
  private final int displayOrder;

  ConsultationResponseOptionGroup(Set<ConsultationResponseOption> options,
                                  int displayOrder) {
    this.options = options;
    this.displayOrder = displayOrder;
  }

  public Set<ConsultationResponseOption> getOptions() {
    return options;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public ConsultationResponseOption getResponseOptionNumber(int optionNumber) {
    return (ConsultationResponseOption) getOptions().toArray()[optionNumber - 1];
  }
}
