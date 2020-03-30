package uk.co.ogauthority.pwa.validators;

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
    if (form.getAgreementStatus().equals(MedianLineStatus.NEGOTIATIONS_ONGOING)) {
      if (form.getNegotiatorNameIfOngoing().isBlank()) {
        errors.rejectValue("negotiatorNameIfOngoing", "negotiatorNameIfOngoing.required", "You must provide the name of the negotiator.");
      }
    }
  }
}
