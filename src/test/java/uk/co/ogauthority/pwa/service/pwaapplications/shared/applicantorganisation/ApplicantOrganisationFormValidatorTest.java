package uk.co.ogauthority.pwa.service.pwaapplications.shared.applicantorganisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.form.pwaapplications.ApplicantOrganisationForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantOrganisationFormValidatorTest {

  @Mock
  private ApplicantOrganisationService applicantOrganisationService;

  private ApplicantOrganisationFormValidator validator;

  private final PortalOrganisationUnit applicantOrganisation = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");

  private final MasterPwa masterPwa = new MasterPwa();
  private final WebUserAccount webUserAccount = new WebUserAccount();

  @Before
  public void setUp() throws Exception {

    validator = new ApplicantOrganisationFormValidator(applicantOrganisationService);

    when(applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, webUserAccount)).thenReturn(Set.of(applicantOrganisation));

  }

  @Test
  public void validate_validOrgSelected() {

    var form = new ApplicantOrganisationForm();
    form.setApplicantOrganisationOuId(applicantOrganisation.getOuId());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_invalidOrgSelected() {

    var form = new ApplicantOrganisationForm();
    form.setApplicantOrganisationOuId(2);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).containsOnly(
        entry("applicantOrganisationOuId", Set.of(INVALID.errorCode("applicantOrganisationOuId")))
    );

  }

  @Test
  public void validate_noOrgSelected() {

    var form = new ApplicantOrganisationForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, masterPwa, webUserAccount);

    assertThat(errors).containsOnly(
        entry("applicantOrganisationOuId", Set.of(REQUIRED.errorCode("applicantOrganisationOuId")))
    );

  }

}