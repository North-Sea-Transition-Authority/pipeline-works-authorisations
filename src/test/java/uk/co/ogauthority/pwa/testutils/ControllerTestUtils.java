package uk.co.ogauthority.pwa.testutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.List;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public class ControllerTestUtils {

  private ControllerTestUtils() {
    throw new AssertionError();
  }

  /**
   * Use in controller tests to force a validator to return pre-defined errors during a request
   * @param validator that should return errors
   * @param fieldsWithErrors list of field ids that the validator should return errors for
   */
  public static void mockValidatorErrors(Validator validator, List<String> fieldsWithErrors) {

    doAnswer(invocation -> {
      BindingResult result = invocation.getArgument(1);
      fieldsWithErrors.forEach(field ->
          result.rejectValue(field, FieldValidationErrorCodes.INVALID.errorCode(field), "fake message"));
      return result;
    }).when(validator).validate(any(), any());

  }

  /**
   * Use in controller tests to force a smart validator to return pre-defined errors during a request
   * @param validator that should return errors
   * @param fieldsWithErrors list of field ids that the validator should return errors for
   */
  public static void mockSmartValidatorErrors(SmartValidator validator, List<String> fieldsWithErrors) {

    doAnswer(invocation -> {
      BindingResult result = invocation.getArgument(1);
      fieldsWithErrors.forEach(field ->
          result.rejectValue(field, "fake.code", "fake message"));
      return result;
    }).when(validator).validate(any(), any(), any());

  }

  /**
   * Return a binding result with a validation error when the passed-in validation type is used.
   */
  public static void failValidationWhenPost(ApplicationFormSectionService service, Object form, ValidationType validationType) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new ObjectError("fake", "fake"));
    when(service.validate(any(), any(), eq(validationType), any())).thenReturn(bindingResult);
  }

  /**
   * Return a clean binding result when the passed-in validation type is used.
   */
  public static void passValidationWhenPost(ApplicationFormSectionService service, Object form, ValidationType validationType) {
    when(service.validate(any(), any(), eq(validationType), any())).thenReturn(new BeanPropertyBindingResult(form, "form"));
  }

  public static MultiValueMap<String, String> fullValidationPostParams() {
    return new LinkedMultiValueMap<>(){{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
    }};
  }

  public static MultiValueMap<String, String> partialValidationPostParams() {
    return new LinkedMultiValueMap<>(){{
      add(ValidationType.PARTIAL.getButtonText(), ValidationType.PARTIAL.getButtonText());
    }};
  }

}
