package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignResponderValidatorTest {

  @Mock
  private AssignResponderService assignResponderService;
  private AssignResponderValidator validator;

  @BeforeEach
  void setUp() {
    validator = new AssignResponderValidator();
  }


  @Test
  void validate_form_empty() {
    var form = new AssignResponderForm();
    var consultationRequest = new ConsultationRequest();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new AssignResponderValidationHints(assignResponderService, consultationRequest));
    assertThat(errorsMap).containsOnly(
        entry("responderPersonId", Set.of("responderPersonId" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_valid() {
    var form = new AssignResponderForm();
    form.setResponderPersonId(1);
    var consultationRequest = new ConsultationRequest();
    var validResponder = new Person(1, null, null, null, null);

    when(assignResponderService.getAllRespondersForRequest(consultationRequest)).thenReturn(List.of(validResponder));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new AssignResponderValidationHints(assignResponderService, consultationRequest));
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_selectedResponder_invalid() {
    var form = new AssignResponderForm();
    form.setResponderPersonId(1);
    var consultationRequest = new ConsultationRequest();
    var validResponder = new Person(2, null, null, null, null);

    when(assignResponderService.getAllRespondersForRequest(consultationRequest)).thenReturn(List.of(validResponder));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new AssignResponderValidationHints(assignResponderService, consultationRequest));
    assertThat(errorsMap).contains(
        entry("responderPersonId", Set.of("responderPersonId" + FieldValidationErrorCodes.INVALID.getCode())));
  }







}