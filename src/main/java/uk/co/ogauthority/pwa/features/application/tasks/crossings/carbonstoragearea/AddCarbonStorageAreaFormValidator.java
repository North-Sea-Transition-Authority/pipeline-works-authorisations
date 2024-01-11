package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Component
public class AddCarbonStorageAreaFormValidator implements SmartValidator {

  private final EditCarbonStorageAreaCrossingFormValidator editValidator;

  private final CarbonStorageAreaCrossingService crossingService;

  public AddCarbonStorageAreaFormValidator(EditCarbonStorageAreaCrossingFormValidator editValidator,
                                           CarbonStorageAreaCrossingService crossingService) {
    this.editValidator = editValidator;
    this.crossingService = crossingService;
  }


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(AddCarbonStorageAreaCrossingForm.class);
  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (AddCarbonStorageAreaCrossingForm) target;
    PwaApplicationDetail pwaApplicationDetail = (PwaApplicationDetail) validationHints[0];

    if (form.getStorageAreaRef() == null || form.getStorageAreaRef().isEmpty()) {
      errors.rejectValue(
          "storageAreaRef",
          "storageAreaRef" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a carbon storage area reference");
    } else if (crossingService.doesAreaExistOnApp(pwaApplicationDetail, form.getStorageAreaRef())) {
      errors.rejectValue("storageAreaRef", "storageAreaRef" + FieldValidationErrorCodes.NOT_UNIQUE.getCode(),
          "The entered storage area already exists on this application");
    }
    editValidator.validate(form, errors);
  }
}
