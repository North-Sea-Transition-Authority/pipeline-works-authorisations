package uk.co.ogauthority.pwa.validators.pwaapplications.shared.pipelines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ModifyPipelineService;

@Service
public class ModifyPipelineValidator implements SmartValidator {

  private final ModifyPipelineService modifyPipelineService;

  @Autowired
  public ModifyPipelineValidator(
      ModifyPipelineService modifyPipelineService) {
    this.modifyPipelineService = modifyPipelineService;
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (ModifyPipelineForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    var selectablePipelines = modifyPipelineService.getSelectableConsentedPipelines(detail);
    boolean isValidPipeline = selectablePipelines.keySet()
        .stream()
        .anyMatch(s -> s.equals(form.getPipelineId()));
    if (!isValidPipeline) {
      errors.rejectValue("pipelineId", "pipelineId" + FieldValidationErrorCodes.INVALID.getCode(),
          "You must select a valid pipeline");
    }
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {

  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ModifyPipelineForm.class);
  }
}
