package uk.co.ogauthority.pwa.validators.pwaapplications.shared.pipelines;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
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
    boolean isValidPipeline = selectablePipelines.stream()
        .anyMatch(s -> String.valueOf(s.getPipelineId()).equals(form.getPipelineId()));
    if (!isValidPipeline) {
      errors.rejectValue("pipelineId", "pipelineId" + FieldValidationErrorCodes.INVALID.getCode(),
          "Select a valid pipeline");
    }
    ValidationUtils.rejectIfEmpty(errors, "pipelineStatus",
        "pipelineStatus" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select the status of the pipeline after changes");
    if (form.getPipelineStatus() != null && form.getPipelineStatus().getHistorical()) {
      errors.rejectValue("pipelineStatus",
          "pipelineStatus" + FieldValidationErrorCodes.INVALID.getCode(),
          "The selected pipeline status is invalid");
    }
    if (form.getPipelineStatus() == PipelineStatus.OUT_OF_USE_ON_SEABED) {
      ValidationUtils.rejectIfEmpty(errors, "pipelineStatusReason",
          "pipelineStatusReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for leaving the pipeline on the seabed");
      if (StringUtils.length(form.getPipelineStatusReason()) > 4000) {
        errors.rejectValue("pipelineStatusReason",
            "pipelineStatusReason" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
            "The reason for leaving the pipeline on the seabed must be 4000 characters or less");
      }
    }
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new AccessDeniedException("Use other validate method");
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ModifyPipelineForm.class);
  }
}
