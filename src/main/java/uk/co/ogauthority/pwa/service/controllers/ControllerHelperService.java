package uk.co.ogauthority.pwa.service.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;

@Service
public class ControllerHelperService {

  public static final String UPLOADED_FILE_ERROR_ELEMENT_ID = FileValidationUtils.FIELD_NAME + "-error";

  private final MessageSource messageSource;

  @Autowired
  public ControllerHelperService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Standardises basic form POST behaviour, allows controllers to either return a ModelAndView that's failed validation
   * (populated with validation errors) or do a caller-specified action if passed validation.
   * @param bindingResult result of binding the form object from request
   * @param modelAndView the model and view to add the validation errors to if validation failed during binding
   * @param ifValid the action to perform if the validation passes
   * @return passed-in ModelAndView with validation errors added if validation failed, caller-specified ModelAndView otherwise
   */
  public ModelAndView checkErrorsAndRedirect(BindingResult bindingResult,
                                             ModelAndView modelAndView,
                                             Supplier<ModelAndView> ifValid) {

    if (bindingResult.hasErrors()) {
      addFieldValidationErrors(modelAndView, bindingResult);
      return modelAndView;
    }

    return ifValid.get();

  }

  /**
   * Adds field validation errors to a model and view.
   * @param modelAndView The model and view which failed validation
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  private void addFieldValidationErrors(ModelAndView modelAndView, BindingResult bindingResult) {

    List<ErrorItem> errorList = new ArrayList<>();
    IntStream.range(0, bindingResult.getFieldErrors().size()).forEach(index -> {

      var fieldError = bindingResult.getFieldErrors().get(index);

      // try to get a message from the custom message store for the error, fallback to default message
      String errorMessage = messageSource.getMessage(
          getTypeMismatchErrorCode(fieldError).orElse(""),
          null,
          fieldError.getDefaultMessage(),
          Locale.getDefault());

      errorList.add(new ErrorItem(index, getFieldNameForFieldError(fieldError), errorMessage));

    });

    modelAndView.addObject("errorList", errorList);

  }


  /*Checks the error codes of the FieldError for a match in the set of override codes and then uses the override field name
  otherwise just use the field name on the FieldError*/
  private String getFieldNameForFieldError(FieldError fieldError) {

    var fieldName = fieldError.getField();
    var errorCodeToFieldNameOverrideMap = Map.of(
        FileValidationUtils.ABOVE_LIMIT_ERROR_CODE, UPLOADED_FILE_ERROR_ELEMENT_ID,
        FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE, UPLOADED_FILE_ERROR_ELEMENT_ID
    );

    if (fieldError.getCodes() == null) {
      return fieldName;
    }

    var errorCodes = new HashSet<>(Arrays.asList(fieldError.getCodes()));
    return errorCodeToFieldNameOverrideMap.keySet().stream()
        .filter(errorCodes::contains)
        .map(errorCodeToFieldNameOverrideMap::get)
        .findFirst().orElse(fieldName);
  }


  private Optional<String> getTypeMismatchErrorCode(FieldError fieldError) {

    boolean isTypeMismatch = Objects.equals(fieldError.getCode(), "typeMismatch");

    // if there's no type mismatch, no need to find specific error code
    if (!isTypeMismatch) {
      return Optional.empty();
    }

    // if we have a type mismatch, find the mismatch code for the type we were expecting
    return Optional.ofNullable(fieldError.getCodes())
        .flatMap(codes -> Arrays.stream(codes)
        .filter(code -> code.contains("typeMismatch.java."))
        .findFirst());

  }

}
