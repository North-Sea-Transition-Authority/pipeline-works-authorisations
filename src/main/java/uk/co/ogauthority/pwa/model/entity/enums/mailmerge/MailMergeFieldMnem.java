package uk.co.ogauthority.pwa.model.entity.enums.mailmerge;

import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

public enum MailMergeFieldMnem {

  //TODO: PWA2022-81 - Convert enum to use set of allowed applications, instead of excluded
  
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
  )),

  //Terms and Conditions
  VARIATION_TERM(
      Set.of(
          PwaApplicationType.INITIAL,
          PwaApplicationType.HUOO_VARIATION,
          PwaApplicationType.DEPOSIT_CONSENT),
      Set.of(
          DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
          DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
          DocumentSpec.HUOO_CONSENT_DOCUMENT)),
  HUOO_TERMS(
      Set.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION),
      Set.of(
          DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
          DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
          DocumentSpec.VARIATION_CONSENT_DOCUMENT)),
  DEPCON_TERMS(
      Set.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.OPTIONS_VARIATION),
      Set.of(
        DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
        DocumentSpec.VARIATION_CONSENT_DOCUMENT,
        DocumentSpec.HUOO_CONSENT_DOCUMENT));

  private final Set<PwaApplicationType> preventedAppTypes;

  private final Set<DocumentSpec> preventedDocumentSpecs;

  MailMergeFieldMnem() {
    preventedAppTypes = EnumSet.noneOf(PwaApplicationType.class);
    preventedDocumentSpecs = EnumSet.noneOf(DocumentSpec.class);
  }

  MailMergeFieldMnem(Set<PwaApplicationType> preventedAppTypes) {
    this.preventedAppTypes = preventedAppTypes;
    preventedDocumentSpecs = EnumSet.noneOf(DocumentSpec.class);
  }

  MailMergeFieldMnem(Set<PwaApplicationType> preventedAppTypes, Set<DocumentSpec> preventedDocumentSpecs) {
    this.preventedAppTypes = preventedAppTypes;
    this.preventedDocumentSpecs = preventedDocumentSpecs;
  }

  public Set<PwaApplicationType> getPreventedAppTypes() {
    return preventedAppTypes;
  }

  public Set<DocumentSpec> getPreventedDocumentSpecs() {
    return preventedDocumentSpecs;
  }

  public boolean appTypeIsSupported(PwaApplicationType pwaApplicationType) {
    return !getPreventedAppTypes().contains(pwaApplicationType);
  }

  public boolean documentSpecIsSupported(DocumentSpec documentSpec) {
    return !getPreventedDocumentSpecs().contains(documentSpec);
  }

}
