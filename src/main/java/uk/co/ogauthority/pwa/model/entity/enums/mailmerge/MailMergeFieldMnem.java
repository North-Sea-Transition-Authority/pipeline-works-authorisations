package uk.co.ogauthority.pwa.model.entity.enums.mailmerge;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

public enum MailMergeFieldMnem {

  //TODO: PWA2022-81 - Convert enum to use set of allowed applications, instead of excluded

  // PWA
  PWA_REFERENCE(PwaApplicationType.excluding(PwaApplicationType.INITIAL)),

  // Project information
  PROPOSED_START_OF_WORKS_DATE,
  PROJECT_NAME,

  // Pipelines
  PL_NUMBER_LIST(PwaApplicationType.excluding(
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT
  )),

  // Drawings
  PL_DRAWING_REF_LIST(PwaApplicationType.excluding(
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT
  )),

  ADMIRALTY_CHART_REF(List.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION
  )),

  //Terms and Conditions
  VARIATION_TERM(
      List.of(
          PwaApplicationType.CAT_1_VARIATION,
          PwaApplicationType.CAT_2_VARIATION,
          PwaApplicationType.OPTIONS_VARIATION
      ),
      Set.of(
          DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
          DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
          DocumentSpec.HUOO_CONSENT_DOCUMENT)),
  HUOO_TERMS(
      List.of(
          PwaApplicationType.CAT_1_VARIATION
      ),
      Set.of(
          DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
          DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
          DocumentSpec.VARIATION_CONSENT_DOCUMENT)),
  DEPCON_TERMS(
      List.of(
          PwaApplicationType.DECOMMISSIONING
      ),
      Set.of(
        DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
        DocumentSpec.VARIATION_CONSENT_DOCUMENT,
        DocumentSpec.HUOO_CONSENT_DOCUMENT));

  private final Set<PwaApplicationType> permittedAppTypes;

  private final Set<DocumentSpec> preventedDocumentSpecs;

  MailMergeFieldMnem() {
    permittedAppTypes = EnumSet.allOf(PwaApplicationType.class);
    preventedDocumentSpecs = EnumSet.noneOf(DocumentSpec.class);
  }

  MailMergeFieldMnem(List<PwaApplicationType> permittedAppTypes) {
    this.permittedAppTypes = Set.copyOf(permittedAppTypes);
    preventedDocumentSpecs = EnumSet.noneOf(DocumentSpec.class);
  }

  MailMergeFieldMnem(List<PwaApplicationType> permittedAppTypes, Set<DocumentSpec> preventedDocumentSpecs) {
    this.permittedAppTypes = Set.copyOf(permittedAppTypes);
    this.preventedDocumentSpecs = preventedDocumentSpecs;
  }

  public Set<PwaApplicationType> getPermittedAppTypes() {
    return permittedAppTypes;
  }

  public Set<DocumentSpec> getPreventedDocumentSpecs() {
    return preventedDocumentSpecs;
  }

  public boolean appTypeIsSupported(PwaApplicationType pwaApplicationType) {
    return getPermittedAppTypes().contains(pwaApplicationType);
  }

  public boolean documentSpecIsSupported(DocumentSpec documentSpec) {
    return !getPreventedDocumentSpecs().contains(documentSpec);
  }

}
