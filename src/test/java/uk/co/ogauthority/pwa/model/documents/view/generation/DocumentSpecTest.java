package uk.co.ogauthority.pwa.model.documents.view.generation;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSpecTest {

  @Test
  void getSpecForApplication_shouldReturnCorrectSpecForAllApplicationTypes() {

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DEPOSIT_CONSENT, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.DEPOSIT_PETROLEUM_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_2_VARIATION, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DECOMMISSIONING, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.HUOO_VARIATION, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.HUOO_PETROLEUM_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.PIPELINE_RECORD_MANAGEMENT, PwaResourceType.PETROLEUM))
        .isEqualTo(DocumentSpec.PIPELINE_RECORD_MANAGEMENT_PETROLEUM_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.INITIAL, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.INITIAL_HYDROGEN_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DEPOSIT_CONSENT, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.DEPOSIT_HYDROGEN_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.VARIATION_HYDROGEN_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_2_VARIATION, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.VARIATION_HYDROGEN_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.VARIATION_HYDROGEN_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DECOMMISSIONING, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.VARIATION_HYDROGEN_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.HUOO_VARIATION, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.HUOO_HYDROGEN_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.PIPELINE_RECORD_MANAGEMENT, PwaResourceType.HYDROGEN))
        .isEqualTo(DocumentSpec.PIPELINE_RECORD_MANAGEMENT_HYDROGEN_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.INITIAL, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.INITIAL_CCUS_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DEPOSIT_CONSENT, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.DEPOSIT_CCUS_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.VARIATION_CCUS_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.CAT_2_VARIATION, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.VARIATION_CCUS_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.VARIATION_CCUS_CONSENT_DOCUMENT);
    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.DECOMMISSIONING, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.VARIATION_CCUS_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.HUOO_VARIATION, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.HUOO_CCUS_CONSENT_DOCUMENT);

    assertThat(DocumentSpec.getSpecForApplication(PwaApplicationType.PIPELINE_RECORD_MANAGEMENT, PwaResourceType.CCUS))
        .isEqualTo(DocumentSpec.PIPELINE_RECORD_MANAGEMENT_CCUS_CONSENT_DOCUMENT);
  }

  @Test
  void getSpecForApplication_shouldHaveExactlyOneMatchingSpecPerApplicationAndResourceType() {

    for (var resourceType : PwaResourceType.values()) {
      var mnem = DocumentTemplateMnem.getMnemFromResourceType(resourceType);

      var mappedAppTypes = mnem.getDocumentSpecs().stream()
          .flatMap(spec -> spec.getApplicationType().stream())
          .collect(Collectors.toSet());

      for (var appType : mappedAppTypes) {

        var matchCount = mnem.getDocumentSpecs().stream()
            .filter(spec -> spec.getApplicationType().contains(appType))
            .count();

        assertThat(matchCount).as("Found multiple documents", appType.name(), resourceType.name()).isEqualTo(1L);
      }
    }
  }
}