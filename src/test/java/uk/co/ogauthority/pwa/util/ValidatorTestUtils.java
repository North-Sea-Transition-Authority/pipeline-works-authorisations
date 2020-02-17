package uk.co.ogauthority.pwa.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Provides access to common methods for testing Spring validators.
 */
public class ValidatorTestUtils {

  private ValidatorTestUtils() {
    throw new AssertionError();
  }

  /**
   * Apply a validator to a form object and return a map of field id -> set of field error codes.
   */
  public static Map<String, Set<String>> getFormValidationErrors(Validator validator, Object form) {

    var errors = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, errors);

    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));

  }

}
