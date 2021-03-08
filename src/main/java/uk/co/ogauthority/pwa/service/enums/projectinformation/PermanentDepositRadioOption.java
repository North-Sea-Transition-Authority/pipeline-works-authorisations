package uk.co.ogauthority.pwa.service.enums.projectinformation;

import java.util.Arrays;
import java.util.List;

public enum PermanentDepositRadioOption {

  THIS_APP("Yes, as part of this application", true),
  LATER_APP("Yes, as part of a later application", true),
  NONE("No", false);

  private final String displayText;

  private final boolean permanentDepositMade;

  PermanentDepositRadioOption(String displayText, boolean permanentDepositMade) {
    this.displayText = displayText;
    this.permanentDepositMade = permanentDepositMade;
  }

  public String getDisplayText() {
    return displayText;
  }

  public boolean isPermanentDepositMade() {
    return permanentDepositMade;
  }

  public static List<PermanentDepositRadioOption> asList() {
    return Arrays.asList(PermanentDepositRadioOption.values());
  }
}
