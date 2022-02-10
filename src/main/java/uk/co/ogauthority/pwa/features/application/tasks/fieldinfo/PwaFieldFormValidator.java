package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PwaFieldFormValidator implements SmartValidator {

  private final DevukFieldService devukFieldService;
  private final String serviceNameAcronym;

  @Autowired
  public PwaFieldFormValidator(DevukFieldService devukFieldService,
                               @Value("${service.name.acronym}") String serviceNameAcronym) {
    this.devukFieldService = devukFieldService;
    this.serviceNameAcronym = serviceNameAcronym;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return PwaFieldForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var fieldForm = (PwaFieldForm) target;
    var validationType = (ValidationType) validationHints[0];

    // if full validation, validate everything
    if (validationType == ValidationType.FULL) {

      ValidationUtils.rejectIfEmpty(errors,
          "linkedToField",
          FieldValidationErrorCodes.REQUIRED.errorCode("linkedToField"),
          "Select yes if your application is linked to a field");

      if (BooleanUtils.isTrue(fieldForm.getLinkedToField())) {
        ValidationUtils.rejectIfEmpty(errors,
            "fieldIds", FieldValidationErrorCodes.REQUIRED.errorCode("fieldIds"), "Select a field");

      } else if (BooleanUtils.isFalse(fieldForm.getLinkedToField())) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors,
            "noLinkedFieldDescription",
            FieldValidationErrorCodes.REQUIRED.errorCode("noLinkedFieldDescription"),
            "Enter a description");
      }
    }



    //Partial validation, always performed regardless
    if (BooleanUtils.isFalse(fieldForm.getLinkedToField())) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "noLinkedFieldDescription", fieldForm::getNoLinkedFieldDescription,
          String.format("%s related to description", serviceNameAcronym));
    }

    // regardless of validation type, make sure that selected field is valid
    if (BooleanUtils.isTrue(fieldForm.getLinkedToField()) && fieldForm.getFieldIds() != null) {

      try {
        devukFieldService.getLinkedAndManualFieldEntries(fieldForm.getFieldIds());
      } catch (PwaEntityNotFoundException e) {
        errors.rejectValue("fieldIds", FieldValidationErrorCodes.INVALID.errorCode("fieldIds"), "Select a valid field");
      }

    }

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Not implemented.");
  }
}
