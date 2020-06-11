package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.repository.organisations.PortalOrganisationUnitRepository;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaHolderFormValidatorTest {

  @Mock
  private PortalOrganisationUnitRepository portalOrganisationUnitRepository;

  private PwaHolderForm holderForm;
  private PwaHolderFormValidator validator;

  @Before
  public void setUp() {
    holderForm = new PwaHolderForm();
    validator = new PwaHolderFormValidator(portalOrganisationUnitRepository);
  }

  @Test
  public void validate_holderOuId_hasErrorWhenNull() {

    holderForm.setHolderOuId(null);

    Map<String, Set<String>> fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, holderForm);

    assertThat(fieldErrors).containsOnly(entry("holderOuId", Set.of("holderOuId.required")));
    verifyNoInteractions(portalOrganisationUnitRepository);

  }

  @Test
  public void validate_holderOuId_hasErrorWhenOrgNotFound() {

    when(portalOrganisationUnitRepository.findById(123)).thenReturn(Optional.empty());
    holderForm.setHolderOuId(123);

    Map<String, Set<String>> fieldErrors = ValidatorTestUtils.getFormValidationErrors(validator, holderForm);

    assertThat(fieldErrors).containsOnly(entry("holderOuId", Set.of("holderOuId.invalidOrg")));
    verify(portalOrganisationUnitRepository, times(1)).findById(123);

  }

  @Test
  public void validate_holderOuId_noErrorsWhenOrgFound() {

    when(portalOrganisationUnitRepository.findById(123)).thenReturn(Optional.of(new PortalOrganisationUnit()));

    holderForm.setHolderOuId(123);
    var errors = new BeanPropertyBindingResult(holderForm, "form");
    ValidationUtils.invokeValidator(validator, holderForm, errors);

    assertThat(errors.hasFieldErrors("holderOuId")).isFalse();
    verify(portalOrganisationUnitRepository, times(1)).findById(123);

  }

}
