package uk.co.ogauthority.pwa.validators.testharness;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateVariationApplicationForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class GenerateVariationApplicationValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return GenerateVariationApplicationForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {

    var form = (GenerateVariationApplicationForm) target;

    if (!ObjectUtils.anyNotNull(form.getConsentedMasterPwaId(), form.getNonConsentedMasterPwaId())) {
      errors.rejectValue(
          "consentedMasterPwaId",
          FieldValidationErrorCodes.REQUIRED.errorCode("consentedMasterPwaId"),
          "Select a PWA");
    }

    if (ObjectUtils.allNotNull(form.getConsentedMasterPwaId(), form.getNonConsentedMasterPwaId())) {
      errors.rejectValue(
          "nonConsentedMasterPwaId",
          FieldValidationErrorCodes.INVALID.errorCode("nonConsentedMasterPwaId"),
          "Select only one PWA");
    }




  }


}
