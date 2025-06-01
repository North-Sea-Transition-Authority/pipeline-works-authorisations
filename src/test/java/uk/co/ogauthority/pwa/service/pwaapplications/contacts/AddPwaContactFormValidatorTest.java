package uk.co.ogauthority.pwa.service.pwaapplications.contacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.AddPwaContactFormValidator;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.form.masterpwas.contacts.AddPwaContactForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AddPwaContactFormValidatorTest {

  @Mock
  private UserAccountService userAccountService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaApplicationService pwaApplicationService;

  private AddPwaContactForm contactForm;
  private AddPwaContactFormValidator contactFormValidator;

  @BeforeEach
  void before() {
    contactForm = new AddPwaContactForm();
    contactFormValidator = new AddPwaContactFormValidator(userAccountService, pwaContactService,
        pwaApplicationService);
  }

  @Test
  void validate_userIdentifier_noErrors() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(userAccountService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(pwaContactService.personIsContactOnApplication(pwaApplication, person)).thenReturn(false);
    when(pwaApplicationService.getApplicationFromId(1)).thenReturn(pwaApplication);

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_userIdentifier_blank() {

    contactForm.setPwaApplicationId(1);

    var errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.required")));

    verifyNoInteractions(userAccountService, pwaContactService, pwaApplicationService);

  }

  @Test
  void validate_userIdentifier_userNotFound() {

    when(userAccountService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.empty());

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    var errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userNotFound")));

    verifyNoInteractions(pwaContactService, pwaApplicationService);

  }

  @Test
  void validate_userIdentifier_userAlreadyExists() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(userAccountService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(pwaContactService.personIsContactOnApplication(pwaApplication, person)).thenReturn(true);
    when(pwaApplicationService.getApplicationFromId(1)).thenReturn(pwaApplication);

    contactForm.setUserIdentifier("userId");
    contactForm.setPwaApplicationId(1);

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(contactFormValidator, contactForm);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userAlreadyExists")));

  }

}
