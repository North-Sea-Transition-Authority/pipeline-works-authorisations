package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static java.util.stream.Collectors.toList;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.CAT_1_VARIATION;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.CAT_2_VARIATION;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.DECOMMISSIONING;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.DEPOSIT_CONSENT;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.HUOO_VARIATION;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.INITIAL;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType.OPTIONS_VARIATION;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public enum PermanentDepositMade {

  THIS_APP("Yes, as part of this application", true,
      List.of(INITIAL, CAT_1_VARIATION, CAT_2_VARIATION, HUOO_VARIATION, DEPOSIT_CONSENT, DECOMMISSIONING)),
  LATER_APP("Yes, as part of a later application", true,
      List.of(INITIAL, CAT_1_VARIATION, CAT_2_VARIATION, HUOO_VARIATION, DEPOSIT_CONSENT, DECOMMISSIONING)),
  YES("Yes", true,
      List.of(OPTIONS_VARIATION)),
  NONE("No", false,
      List.of(INITIAL, CAT_1_VARIATION, CAT_2_VARIATION, HUOO_VARIATION, DEPOSIT_CONSENT, OPTIONS_VARIATION, DECOMMISSIONING));

  private final String displayText;

  private final boolean permanentDepositMade;

  private final List<PwaApplicationType> supportedApplicationTypes;

  PermanentDepositMade(String displayText, boolean permanentDepositMade,
                              List<PwaApplicationType> supportedApplicationTypes) {
    this.displayText = displayText;
    this.permanentDepositMade = permanentDepositMade;
    this.supportedApplicationTypes = supportedApplicationTypes;
  }

  public String getDisplayText() {
    return displayText;
  }

  public boolean isPermanentDepositMade() {
    return permanentDepositMade;
  }

  public List<PwaApplicationType> getSupportedApplicationTypes() {
    return supportedApplicationTypes;
  }

  public static List<PermanentDepositMade> asList(PwaApplicationType pwaApplicationType) {
    return Arrays.stream(PermanentDepositMade.values())
        .filter(permanentDepositRadioOption -> permanentDepositRadioOption.supportedApplicationTypes.contains(pwaApplicationType))
        .collect(toList());
  }

}
