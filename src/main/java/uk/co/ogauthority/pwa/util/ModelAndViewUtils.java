package uk.co.ogauthority.pwa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;

/**
 * Helper class for common methods interacting with ModelAndView objects.
 */
// TODO PWA-491 remove this class after removing single caller
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

    List<ErrorItem> errorList = new ArrayList<>();
    IntStream.range(0, bindingResult.getFieldErrors().size()).forEach(index -> {
      var fieldError = bindingResult.getFieldErrors().get(index);
      errorList.add(new ErrorItem(index, fieldError.getField(), fieldError.getDefaultMessage()));
    });

    modelAndView.addObject("errorList", errorList);

  }

}
