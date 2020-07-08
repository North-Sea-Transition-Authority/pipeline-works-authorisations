package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.Checkable;

/**
 * Utility class to provide useful methods for controllers.
 */
public class ControllerUtils {

  private ControllerUtils() {
    throw new AssertionError();
  }

  public static Map<String, String> asCheckboxMap(List<? extends Checkable> items) {
    return items.stream()
        .collect(Collectors.toMap(Checkable::getIdentifier, Checkable::getDisplayName));
  }

  /**
   * Standardises basic form POST behaviour, allows controllers to either return a ModelAndView that's failed validation
   * (populated with validation errors) or do a caller-specified action if passed validation.
   * @param bindingResult result of binding the form object from request
   * @param modelAndView the model and view to add the validation errors to if validation failed during binding
   * @param ifValid the action to perform if the validation passes
   * @return passed-in ModelAndView with validation errors added if validation failed, caller-specified ModelAndView otherwise
   */
  // TODO PWA-491 remove this method and update everywhere
  public static ModelAndView checkErrorsAndRedirect(BindingResult bindingResult,
                                                    ModelAndView modelAndView,
                                                    Supplier<ModelAndView> ifValid) {

    if (bindingResult.hasErrors()) {
      ModelAndViewUtils.addFieldValidationErrors(modelAndView, bindingResult);
      return modelAndView;
    }

    return ifValid.get();

  }
}
