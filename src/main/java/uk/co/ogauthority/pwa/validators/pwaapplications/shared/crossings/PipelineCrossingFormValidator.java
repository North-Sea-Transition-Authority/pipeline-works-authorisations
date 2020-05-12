package uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PipelineCrossingFormValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineCrossingForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PipelineCrossingForm) target;
    ValidationUtils.rejectIfEmpty(errors, "pipelineCrossed", "pipelineCrossed" + FieldValidationErrorCodes.REQUIRED,
        "You must enter a reference for the crossed pipeline");
    if (form.getPipelineFullyOwnedByOrganisation() == null) {
      errors.rejectValue("pipelineFullyOwnedByOrganisation",
          "pipelineFullyOwnedByOrganisation" + FieldValidationErrorCodes.REQUIRED,
          "Select yes if the pipeline is fully owned by your organisation");
    } else if (!BooleanUtils.toBooleanDefaultIfNull(form.getPipelineFullyOwnedByOrganisation(), false)
        && ListUtils.emptyIfNull(form.getPipelineOwners()).isEmpty()) {
      errors.rejectValue("pipelineOwners", "pipelineOwners" + FieldValidationErrorCodes.REQUIRED,
          "You must select the owners of the pipeline");
    }
  }
}
