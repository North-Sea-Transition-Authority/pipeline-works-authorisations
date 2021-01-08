package uk.co.ogauthority.pwa.validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsSafetyZoneForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.AfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.BeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Service
public class LocationDetailsSafetyZoneValidator implements SmartValidator {


  private final TwoFieldDateInputValidator twoFieldDateInputValidator;

  @Autowired
  public LocationDetailsSafetyZoneValidator(
      TwoFieldDateInputValidator twoFieldDateInputValidator) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
  }


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(LocationDetailsSafetyZoneForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (LocationDetailsSafetyZoneForm) target;

    if (form.getFacilities().size() == 0) {
      errors.rejectValue("facilities",
          "facilities" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select all structures within 500m");
    }

    ValidationUtils.rejectIfEmpty(errors, "psrNotificationSubmitted",
        "psrNotificationSubmitted" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select 'Yes' if you have submitted a Pipelines Safety Regulations notification to HSE");

    if (BooleanUtils.isTrue(form.getPsrNotificationSubmitted())) {
      List<Object> dateHints = new ArrayList<>();
      dateHints.add(new FormInputLabel("psrNotificationSubmittedDate"));
      dateHints.add(new BeforeDateHint(LocalDate.now(), "today's date"));

      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "psrNotificationSubmittedDate",
          form.getPsrNotificationSubmittedDate(),
          dateHints.toArray());

    } else if (BooleanUtils.isFalse(form.getPsrNotificationSubmitted())) {
      List<Object> dateHints = new ArrayList<>();
      dateHints.add(new FormInputLabel("psrNotificationExpectedSubmissionDate"));
      dateHints.add(new AfterDateHint(LocalDate.now(), "today's date"));

      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "psrNotificationExpectedSubmissionDate",
          form.getPsrNotificationExpectedSubmissionDate(),
          dateHints.toArray());
    }

  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }








}
