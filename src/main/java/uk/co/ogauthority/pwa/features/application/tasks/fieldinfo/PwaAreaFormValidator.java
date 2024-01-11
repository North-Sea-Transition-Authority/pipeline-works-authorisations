package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PwaAreaFormValidator implements SmartValidator {

  private final DevukFieldService devukFieldService;
  private final String serviceNameAcronym;

  @Autowired
  public PwaAreaFormValidator(DevukFieldService devukFieldService,
                              @Value("${service.name.acronym}") String serviceNameAcronym) {
    this.devukFieldService = devukFieldService;
    this.serviceNameAcronym = serviceNameAcronym;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return PwaAreaForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var fieldForm = (PwaAreaForm) target;
    var validationType = (ValidationType) validationHints[0];
    var pwaApplicationDetail = (PwaApplicationDetail) validationHints[1];

    // if full validation, validate everything
    if (validationType == ValidationType.FULL) {

      ValidationUtils.rejectIfEmpty(errors,
          "linkedToArea",
          FieldValidationErrorCodes.REQUIRED.errorCode("linkedToArea"),
          "Select yes if your application is linked to an area");

      if (BooleanUtils.isTrue(fieldForm.getLinkedToArea())) {
        ValidationUtils.rejectIfEmpty(errors,
            "linkedAreas",
            FieldValidationErrorCodes.REQUIRED.errorCode("linkedAreas"),
            "Enter which areas the PWA is related to");

      } else if (BooleanUtils.isFalse(fieldForm.getLinkedToArea())) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors,
            "noLinkedAreaDescription",
            FieldValidationErrorCodes.REQUIRED.errorCode("noLinkedAreaDescription"),
            "Enter a description");
      }
    }



    //Partial validation, always performed regardless
    if (BooleanUtils.isFalse(fieldForm.getLinkedToArea())) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "noLinkedAreaDescription", fieldForm::getNoLinkedAreaDescription,
          String.format("%s related to description", serviceNameAcronym));
    }

    // regardless of validation type, make sure that selected field is valid
    if (BooleanUtils.isTrue(fieldForm.getLinkedToArea())
        && fieldForm.getLinkedAreas() != null
        && !PwaResourceType.CCUS.equals(pwaApplicationDetail.getResourceType())) {
      try {
        devukFieldService.getLinkedAndManualFieldEntries(fieldForm.getLinkedAreas());
      } catch (PwaEntityNotFoundException e) {
        errors.rejectValue("linkedAreas", FieldValidationErrorCodes.INVALID.errorCode("linkedAreas"), "Select a valid area");
      }

    }

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Not implemented.");
  }
}
