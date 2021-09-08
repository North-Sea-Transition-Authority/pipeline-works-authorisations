package uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent;

import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.PreSendForApprovalChecksView;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.MailMergeUtils;

@Service
public class SendConsentForApprovalFormValidator implements SmartValidator {

  private static final String CONSENTS_REVIEWED_ATTR = "parallelConsentsReviewedIfApplicable";
  private static final String COVER_LETTER_ATTR = "coverLetterText";

  @Override
  public boolean supports(Class<?> clazz) {
    return SendConsentForApprovalForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Not implemented. Validation requires hints to be provided.");

  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (SendConsentForApprovalForm) target;
    var preApprovalCheckView = Arrays.stream(validationHints)
        .filter(o -> o.getClass().equals(PreSendForApprovalChecksView.class))
        .map(o -> (PreSendForApprovalChecksView) o)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Must provide pre-approval checks view"));

    if (!preApprovalCheckView.getParallelConsentViews().isEmpty()) {
      ValidationUtils.rejectIfEmpty(
          errors,
          CONSENTS_REVIEWED_ATTR,
          FieldValidationErrorCodes.REQUIRED.errorCode(CONSENTS_REVIEWED_ATTR),
          "Confirm changes in listed consents are reflected in this consent");
    }

    ValidationUtils.rejectIfEmpty(
        errors,
        COVER_LETTER_ATTR,
        FieldValidationErrorCodes.REQUIRED.errorCode(COVER_LETTER_ATTR),
        "Enter some email cover letter text");

    if (form.getCoverLetterText() != null && MailMergeUtils.textContainsManualMergeDelimiters(form.getCoverLetterText())) {
      errors.rejectValue(COVER_LETTER_ATTR, FieldValidationErrorCodes.INVALID.errorCode(COVER_LETTER_ATTR),
          String.format("Remove '%s' from the cover letter text", MailMergeFieldType.MANUAL.getOpeningDelimiter()));
    }

  }

}
