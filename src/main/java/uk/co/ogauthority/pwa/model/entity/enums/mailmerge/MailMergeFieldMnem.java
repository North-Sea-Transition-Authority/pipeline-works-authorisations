package uk.co.ogauthority.pwa.model.entity.enums.mailmerge;

import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public enum MailMergeFieldMnem {

  // PWA
  PWA_REFERENCE(Set.of(
      PwaApplicationType.INITIAL
  )),

  // Project information
  PROPOSED_START_OF_WORKS_DATE,
  PROJECT_NAME,

  // Pipelines
  PL_NUMBER_LIST(Set.of(
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT
  )),

  // Drawings
  PL_DRAWING_REF_LIST(Set.of(
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT
  )),

  ADMIRALTY_CHART_REF(Set.of(
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.OPTIONS_VARIATION
  ));

  private final Set<PwaApplicationType> preventedAppTypes;

  MailMergeFieldMnem() {
    preventedAppTypes = EnumSet.noneOf(PwaApplicationType.class);
  }

  MailMergeFieldMnem(Set<PwaApplicationType> supportedAppTypes) {
    this.preventedAppTypes = supportedAppTypes;
  }

  public Set<PwaApplicationType> getPreventedAppTypes() {
    return preventedAppTypes;
  }

  public boolean appTypeIsSupported(PwaApplicationType pwaApplicationType) {
    return !getPreventedAppTypes().contains(pwaApplicationType);
  }

}