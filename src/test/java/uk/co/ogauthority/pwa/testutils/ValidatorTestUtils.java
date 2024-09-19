package uk.co.ogauthority.pwa.testutils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

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
    ValidationUtils.invokeValidator(validator, form, errors, ValidationType.FULL, PwaResourceType.PETROLEUM);

    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));

  }

  /**
   * Apply a validator with hints to a form object and return a map of field id -> set of field error codes.
   */
  public static Map<String, Set<String>> getFormValidationErrors(Validator validator, Object form, Object... validationHints) {

    var errors = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, errors, validationHints);

    var errorMap = new LinkedHashMap<String, Set<String>>();

    errors.getFieldErrors()
        .forEach(error -> {
          errorMap.putIfAbsent(error.getField(), new LinkedHashSet<>());
          errorMap.get(error.getField()).add(error.getCode());
        });

    return errorMap;
  }

  /**
   * Return a map of field id -> set of field error codes for a BindingResult
   */
  public static Map<String, Set<String>> extractErrors(BindingResult bindingResult) {

    return bindingResult.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));

  }

  /**
   * Return a map of field id -> set of field error messages for a BindingResult
   */
  public static Map<String, Set<String>> extractErrorMessages(BindingResult bindingResult) {

    return bindingResult.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getDefaultMessage, Collectors.toSet())));

  }



  public static String overCharLength(int maxCharLength) {
    return StringUtils.repeat("a", maxCharLength + 1);
  }

  public static String overMaxDefaultCharLength() {
    return StringUtils.repeat("a", ValidatorUtils.MAX_DEFAULT_STRING_LENGTH + 1);
  }

  public static String exactlyMaxDefaultCharLength() {
    return StringUtils.repeat("a", ValidatorUtils.MAX_DEFAULT_STRING_LENGTH);
  }



}
