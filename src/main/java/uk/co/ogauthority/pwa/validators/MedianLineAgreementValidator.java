package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;

@Service
public class MedianLineAgreementValidator implements Validator {
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
        errors.rejectValue("negotiatorNameIfOngoing", "negotiatorNameIfOngoing.required",
            "You must provide the name of the negotiator");
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfOngoing())) {
        errors.rejectValue("negotiatorEmailIfOngoing", "negotiatorEmailIfOngoing.required",
            "You must provide a contact email for the negotiator");
      }
    } else if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      if (StringUtils.isBlank(form.getNegotiatorNameIfCompleted())) {
        errors.rejectValue("negotiatorNameIfCompleted", "negotiatorNameIfCompleted.required",
            "You must provide the name of the negotiator");
      }
      if (StringUtils.isBlank(form.getNegotiatorEmailIfCompleted())) {
        errors.rejectValue("negotiatorEmailIfCompleted", "negotiatorEmailIfCompleted.required",
            "You must provide a contact email for the negotiator");
      }
    }
  }
}
