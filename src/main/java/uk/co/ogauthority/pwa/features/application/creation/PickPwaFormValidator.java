package uk.co.ogauthority.pwa.features.application.creation;


import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PickPwaFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PickPwaForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Required to use validate method with hints");

  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (PickPwaForm) target;
    var appType = (PwaApplicationType) validationHints[0];

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

    // no point in having an error code onscreen, this can only happen if skullduggery occurs.
    if (form.getNonConsentedMasterPwaId() != null && !appType.equals(PwaApplicationType.DEPOSIT_CONSENT)) {
      throw new ActionNotAllowedException("Cannot select non-consented pwa with app type:" + appType);
    }
  }
}
