package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.form.pwaapplications.ApplicantOrganisationForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantOrganisationFormValidatorTest {

  @Mock
  private ApplicantOrganisationService applicantOrganisationService;

  private ApplicantOrganisationFormValidator validator;

  private final PortalOrganisationUnit applicantOrganisation = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");

  private final MasterPwa masterPwa = new MasterPwa();
  private final WebUserAccount webUserAccount = new WebUserAccount();

  @BeforeEach
  void setUp() {

    validator = new ApplicantOrganisationFormValidator(applicantOrganisationService);

  }

  @Test
  void validate_validOrgSelected() {
    when(applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, webUserAccount)).thenReturn(Set.of(applicantOrganisation));
    var form = new ApplicantOrganisationForm();
    form.setApplicantOrganisationOuId(applicantOrganisation.getOuId());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_invalidOrgSelected() {
    when(applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, webUserAccount)).thenReturn(Set.of(applicantOrganisation));
    var form = new ApplicantOrganisationForm();
    form.setApplicantOrganisationOuId(2);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).containsOnly(
        entry("applicantOrganisationOuId", Set.of(INVALID.errorCode("applicantOrganisationOuId")))
    );

  }

  @Test
  void validate_noOrgSelected() {

    var form = new ApplicantOrganisationForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).containsOnly(
        entry("applicantOrganisationOuId", Set.of(REQUIRED.errorCode("applicantOrganisationOuId")))
    );

  }

}