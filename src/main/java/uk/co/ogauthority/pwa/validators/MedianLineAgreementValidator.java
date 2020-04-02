package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;

@Service
public class MedianLineAgreementValidator implements Validator {

  private static String MISSING_NAME = "You must provide the name of the negotiator";
  private static String MISSING_EMAIL = "You must provide a contact email for the negotiator";

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(MedianLineAgreementsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (MedianLineAgreementsForm) target;
    if (form.getAgreementStatus() == null) {
      errors.rejectValue("agreementStatus", "agreementStatus.required", "You must select one");
    } else if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_ONGOING) {
      if (StringUtils.isBlank(form.getNegotiatorNameIfOngoing())) {
        errors.rejectValue("negotiatorNameIfOngoing", "negotiatorNameIfOngoing.required", MISSING_NAME);
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfOngoing())) {
        errors.rejectValue("negotiatorEmailIfOngoing", "negotiatorEmailIfOngoing.required", MISSING_EMAIL);
      }
    } else if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      if (StringUtils.isBlank(form.getNegotiatorNameIfCompleted())) {
        errors.rejectValue("negotiatorNameIfCompleted", "negotiatorNameIfCompleted.required", MISSING_NAME);
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfCompleted())) {
        errors.rejectValue("negotiatorEmailIfCompleted", "negotiatorEmailIfCompleted.required", MISSING_EMAIL);
      }
    }
  }
}
