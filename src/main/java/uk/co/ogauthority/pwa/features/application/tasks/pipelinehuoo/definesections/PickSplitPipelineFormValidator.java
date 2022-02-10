package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;

import java.util.Arrays;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PickSplitPipelineFormValidator implements SmartValidator {

  static final String PIPELINE_ID_ATTRIBUTE = "pipelineId";
  static final String NUMBER_OF_SECTIONS_ATTRIBUTE = "numberOfSections";

  private final PadPipelinesHuooService padPipelinesHuooService;

  @Autowired
  public PickSplitPipelineFormValidator(PadPipelinesHuooService padPipelinesHuooService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, new Object[0]);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (PickSplitPipelineForm) target;

    var pwaApplicationDetail = Arrays.stream(validationHints)
        .filter(o -> PwaApplicationDetail.class.equals(o.getClass()))
        .map(o -> (PwaApplicationDetail) o)
        .findFirst()
        .orElseThrow(() -> new ActionNotAllowedException("Validation requires PwaApplicationDetail"));

    var huooRole = Arrays.stream(validationHints)
        .filter(o -> HuooRole.class.equals(o.getClass()))
        .map(o -> (HuooRole) o)
        .findFirst()
        .orElseThrow(() -> new ActionNotAllowedException("Validation requires HuooRole"));

    ValidationUtils.rejectIfEmpty(
        errors,
        PIPELINE_ID_ATTRIBUTE,
        FieldValidationErrorCodes.REQUIRED.errorCode(PIPELINE_ID_ATTRIBUTE),
        "Select a pipeline to split");

    ValidationUtils.rejectIfEmpty(
        errors,
        NUMBER_OF_SECTIONS_ATTRIBUTE,
        FieldValidationErrorCodes.REQUIRED.errorCode(NUMBER_OF_SECTIONS_ATTRIBUTE),
        String.format("Enter the number of %s sections", huooRole.getDisplayText().toLowerCase()));


    if (ObjectUtils.allNotNull(form.getPipelineId(), form.getNumberOfSections())) {
      var formSplitablePipelineOptional = padPipelinesHuooService.getSplitablePipelineOverviewForApplication(
          pwaApplicationDetail,
          new PipelineId(form.getPipelineId())
      );


      if (formSplitablePipelineOptional.isPresent()) {
        if (form.getNumberOfSections() <= 0) {

          errors.rejectValue(
              NUMBER_OF_SECTIONS_ATTRIBUTE,
              FieldValidationErrorCodes.INVALID.errorCode(NUMBER_OF_SECTIONS_ATTRIBUTE),
              "Number of sections must be a whole number greater than 0");
        }

      } else {
        errors.rejectValue(
            PIPELINE_ID_ATTRIBUTE,
            FieldValidationErrorCodes.INVALID.errorCode(PIPELINE_ID_ATTRIBUTE),
            "Select a valid pipeline to split");
      }

    }

  }

  @Override
  public boolean supports(Class<?> clazz) {
    return PickSplitPipelineForm.class.equals(clazz);
  }
}
