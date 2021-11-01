package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class MedianLineAgreementValidator implements SmartValidator {

  private static final String MISSING_NAME = "Enter the name of the negotiator";
  private static final String MISSING_EMAIL = "Enter a contact email for the negotiator";

  private static final String NEGOTIATOR_NAME = "Negotiator name";
  private static final String NEGOTIATOR_EMAIL = "Negotiator email";

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(MedianLineAgreementsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, PartialValidation.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (MedianLineAgreementsForm) target;

    partialValidation(form, errors);

    if (Arrays.asList(validationHints).contains(FullValidation.class)) {
      fullValidation(form, errors);
    }

  }

  private void fullValidation(MedianLineAgreementsForm form, Errors errors) {

    ValidationUtils.rejectIfEmpty(
        errors,
        "agreementStatus",
        "agreementStatus.required",
        "Select an agreement status");

    if (MedianLineStatus.NEGOTIATIONS_ONGOING.equals(form.getAgreementStatus())) {
      if (StringUtils.isBlank(form.getNegotiatorNameIfOngoing())) {
        errors.rejectValue("negotiatorNameIfOngoing", "negotiatorNameIfOngoing.required", MISSING_NAME);
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfOngoing())) {
        errors.rejectValue("negotiatorEmailIfOngoing", "negotiatorEmailIfOngoing.required", MISSING_EMAIL);
      }
    } else if (MedianLineStatus.NEGOTIATIONS_COMPLETED.equals(form.getAgreementStatus())) {
      if (StringUtils.isBlank(form.getNegotiatorNameIfCompleted())) {
        errors.rejectValue("negotiatorNameIfCompleted", "negotiatorNameIfCompleted.required", MISSING_NAME);
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfCompleted())) {
        errors.rejectValue("negotiatorEmailIfCompleted", "negotiatorEmailIfCompleted.required", MISSING_EMAIL);
      }
    }

  }

  private void partialValidation(MedianLineAgreementsForm form, Errors errors) {

    ValidatorUtils.validateDefaultStringLength(
        errors,
        "negotiatorEmailIfOngoing",
        form::getNegotiatorEmailIfOngoing,
        NEGOTIATOR_EMAIL);
    ValidatorUtils.validateDefaultStringLength(
        errors,
        "negotiatorNameIfOngoing",
        form::getNegotiatorEmailIfOngoing,
        NEGOTIATOR_NAME);
    ValidatorUtils.validateDefaultStringLength(
        errors,
        "negotiatorEmailIfCompleted",
        form::getNegotiatorEmailIfCompleted,
        NEGOTIATOR_EMAIL);
    ValidatorUtils.validateDefaultStringLength(
        errors,
        "negotiatorNameIfCompleted", form::getNegotiatorEmailIfCompleted,
        NEGOTIATOR_NAME);

    ValidatorUtils.validateEmailIfPresent(
        errors,
        "negotiatorEmailIfOngoing",
        form::getNegotiatorEmailIfOngoing,
        NEGOTIATOR_EMAIL);

    ValidatorUtils.validateEmailIfPresent(
        errors,
        "negotiatorEmailIfCompleted",
        form::getNegotiatorEmailIfCompleted,
        NEGOTIATOR_EMAIL);
  }
}
