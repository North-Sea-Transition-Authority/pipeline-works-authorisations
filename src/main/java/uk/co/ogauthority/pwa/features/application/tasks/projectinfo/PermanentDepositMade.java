package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static java.util.stream.Collectors.toList;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.OPTIONS_VARIATION;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public enum PermanentDepositMade {

  THIS_APP("Yes, as part of this application", true, List.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.PIPELINE_RECORD_MANAGEMENT,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.DECOMMISSIONING
  )),
  LATER_APP("Yes, as part of a later application", false, List.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.PIPELINE_RECORD_MANAGEMENT,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.DECOMMISSIONING
  )),
  YES("Yes", true, List.of(OPTIONS_VARIATION)),
  NONE("No", false, List.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.PIPELINE_RECORD_MANAGEMENT,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING
  ));

  private final String displayText;

  private final boolean permanentDepositsRequiredOnApp;

  private final List<PwaApplicationType> supportedApplicationTypes;

  PermanentDepositMade(String displayText,
                       boolean permanentDepositsRequiredOnApp,
                       List<PwaApplicationType> supportedApplicationTypes) {
    this.displayText = displayText;
    this.permanentDepositsRequiredOnApp = permanentDepositsRequiredOnApp;
    this.supportedApplicationTypes = supportedApplicationTypes;
  }

  public String getDisplayText() {
    return displayText;
  }

  public boolean arePermanentDepositsRequiredOnApp() {
    return permanentDepositsRequiredOnApp;
  }

  public List<PwaApplicationType> getSupportedApplicationTypes() {
    return supportedApplicationTypes;
  }

  public static List<PermanentDepositMade> asList(PwaApplicationType pwaApplicationType) {
    return Arrays.stream(PermanentDepositMade.values())
        .filter(permanentDepositRadioOption ->
            permanentDepositRadioOption.supportedApplicationTypes.contains(pwaApplicationType))
        .collect(toList());
  }

}
