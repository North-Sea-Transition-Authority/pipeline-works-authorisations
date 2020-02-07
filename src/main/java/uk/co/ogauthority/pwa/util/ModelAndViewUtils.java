package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

/**
 * Helper class for common methods interacting with ModelAndView objects.
 */
public class ModelAndViewUtils {

  private ModelAndViewUtils() {
    throw new AssertionError();
  }

  /**
   * Adds field validation errors to a model and view.
   * @param modelAndView The model and view which failed validation
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  public static void addFieldValidationErrors(ModelAndView modelAndView, BindingResult bindingResult) {
    Map<String, List<String>> errorList = bindingResult.getFieldErrors().stream()
        .collect(Collectors.groupingBy(
            FieldError::getField, Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    modelAndView.addObject("errorList", errorList);

  }

}
