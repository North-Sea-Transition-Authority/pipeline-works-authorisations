package uk.co.ogauthority.pwa.validators.consultations;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConsultationResponseValidator implements Validator {

  private final ConsultationResponseDataValidator responseDataValidator;

  @Autowired
  public ConsultationResponseValidator(ConsultationResponseDataValidator responseDataValidator) {
    this.responseDataValidator = responseDataValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ConsultationResponseForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (ConsultationResponseForm) target;

    // sort form map by response group display order to ensure the validation errors are ordered correctly
    form.getResponseDataForms().entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getKey().getDisplayOrder()))
        .forEach(e -> ValidatorUtils.invokeNestedValidator(errors, responseDataValidator,
            "responseDataForms[" + e.getKey() + "]", e.getValue(), e.getKey()));

    validateFileUploads(form, errors);
  }

  private void validateFileUploads(ConsultationResponseForm form, Errors errors) {
    // check if EIA or Habitats reg response requires file upload
    boolean fileRequired = filesRequiredByResponse(
        form,
        ConsultationResponseOptionGroup.EIA_REGS,
        Set.of(ConsultationResponseOption.EIA_AGREE)
    ) || filesRequiredByResponse(
        form,
        ConsultationResponseOptionGroup.HABITATS_REGS,
        Set.of(ConsultationResponseOption.HABITATS_AGREE)
    );

    var requiredFileCount = fileRequired ? 1 : 0;

    FileUploadUtils.validateMinFileLimit(form, errors, requiredFileCount,
        String.format(
            "You must upload at least %s file(s) in order to support your decision",
            requiredFileCount)
    );
    FileUploadUtils.validateFilesDescriptionLength(form, errors);
  }

  // helper which returns 1 if option group exists on form and response is contained in provided set, else 0;
  private boolean filesRequiredByResponse(ConsultationResponseForm form,
                                      ConsultationResponseOptionGroup consultationResponseOptionGroup,
                                      Set<ConsultationResponseOption> setOfResponsesRequiringFileUpload) {
    var response = form.getResponseDataForms()
        .getOrDefault(consultationResponseOptionGroup, new ConsultationResponseDataForm())
        .getConsultationResponseOption();

    return Objects.nonNull(response) && setOfResponsesRequiringFileUpload.contains(response);
  }

}
