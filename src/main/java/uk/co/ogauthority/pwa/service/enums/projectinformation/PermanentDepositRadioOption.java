package uk.co.ogauthority.pwa.service.enums.projectinformation;

import java.util.Arrays;
import java.util.List;

public enum PermanentDepositRadioOption {

  THIS_APP("Yes, as part of this application"),
  LATER_APP("Yes, as part of a later application"),
  NONE("No");

  private final String displayText;

  PermanentDepositRadioOption(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PermanentDepositRadioOption> asList() {
    return Arrays.asList(PermanentDepositRadioOption.values());
  }
}
