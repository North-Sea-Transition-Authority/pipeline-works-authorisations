package uk.co.ogauthority.pwa.service.pwaapplications.contacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.masterpwas.contacts.AddPwaContactForm;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddPwaContactFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaApplicationService pwaApplicationService;

  private AddPwaContactForm contactForm;
  private AddPwaContactFormValidator contactFormValidator;

  @Before
  public void before() {
    contactForm = new AddPwaContactForm();
    contactFormValidator = new AddPwaContactFormValidator(teamManagementService, pwaContactService,
        pwaApplicationService);
  }

  @Test
  public void validate_userIdentifier_noErrors() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(pwaContactService.personIsContactOnApplication(pwaApplication, person)).thenReturn(false);
    when(pwaApplicationService.getApplicationFromId(1)).thenReturn(pwaApplication);

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_userIdentifier_blank() {

    contactForm.setPwaApplicationId(1);

    var errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.required")));

    verifyNoInteractions(teamManagementService, pwaContactService, pwaApplicationService);

  }

  @Test
  public void validate_userIdentifier_userNotFound() {

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.empty());

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    var errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userNotFound")));

    verifyNoInteractions(pwaContactService, pwaApplicationService);

  }

  @Test
  public void validate_userIdentifier_userAlreadyExists() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(pwaContactService.personIsContactOnApplication(pwaApplication, person)).thenReturn(true);
    when(pwaApplicationService.getApplicationFromId(1)).thenReturn(pwaApplication);

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userAlreadyExists")));

  }

}
