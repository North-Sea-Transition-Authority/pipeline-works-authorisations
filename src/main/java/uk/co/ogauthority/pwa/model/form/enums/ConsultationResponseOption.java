package uk.co.ogauthority.pwa.model.form.enums;

import java.util.Arrays;
import java.util.List;

public enum ConsultationResponseOption {

  CONFIRMED("Confirm contentedness"),
  REJECTED("Rejected");

  private String displayText;

  ConsultationResponseOption(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<ConsultationResponseOption> asList() {
    return Arrays.asList(ConsultationResponseOption.values());
  }


}
