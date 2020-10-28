package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineHeaderConditionalQuestion;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.validation.PipelineValidationUtils;

@Service
public class PipelineHeaderFormValidator implements SmartValidator {

  private final CoordinateFormValidator coordinateFormValidator;

  @Autowired
  public PipelineHeaderFormValidator(CoordinateFormValidator coordinateFormValidator) {
    this.coordinateFormValidator = coordinateFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineHeaderForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (PipelineHeaderForm) target;

    PipelineValidationUtils.validateFromToLocation(form.getFromLocation(), errors, "fromLocation", "Pipeline start structure");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.MANDATORY, "Start point");

    PipelineValidationUtils.validateFromToLocation(form.getToLocation(), errors, "toLocation", "Pipeline finish structure");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.MANDATORY, "Finish point");

    ValidationUtils.rejectIfEmpty(errors, "pipelineType", "pipelineType.required",
        "Select the pipeline type");

    if (form.getPipelineType() == PipelineType.UNKNOWN) {
      errors.rejectValue("pipelineType", "pipelineType" + FieldValidationErrorCodes.INVALID.getCode(),
          "You must select a valid pipeline type");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "length", "length.required",
        "Enter the pipeline's length");

    if (form.getLength() != null) {
      PipelineValidationUtils.validateLength(form.getLength(), errors, "length", "Pipeline length");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productsToBeConveyed", "productsToBeConveyed.required",
        "Enter the products to be conveyed");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "componentPartsDescription", "componentPartsDescription.required",
        "Enter a description of the component parts");

    ValidationUtils.rejectIfEmpty(errors, "trenchedBuriedBackfilled", "trenchedBuriedBackfilled.required",
        "Select yes if the pipeline will be trenched and/or buried and/or backfilled");

    Optional.ofNullable(form.getTrenchedBuriedBackfilled())
        .filter(tru -> tru)
        .ifPresent(
            t -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, "trenchingMethods", "trenchingMethods.required",
                "Enter the trenching methods"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineFlexibility", "pipelineFlexibility.required",
        "Select an option for the pipeline flexibility");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineMaterial", "pipelineMaterial.required",
        "Select an option for the pipeline material");

    if (form.getPipelineMaterial() != null && form.getPipelineMaterial().equals(PipelineMaterial.OTHER)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherPipelineMaterialUsed",
          "otherPipelineMaterialUsed.required",
          "Enter details of other pipeline materials used");
    }

    ValidationUtils.rejectIfEmpty(errors, "pipelineDesignLife", "pipelineDesignLife.required",
        "Enter the design life of the pipeline");

    if (form.getPipelineDesignLife() != null && form.getPipelineDesignLife() < 1) {
      errors.rejectValue("pipelineDesignLife", "pipelineDesignLife.invalid",
          "Design life of the pipeline must be a positive whole number");
    }

    ValidationUtils.rejectIfEmpty(errors, "pipelineInBundle",
        "pipelineInBundle" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select yes if the full length of this pipeline is in a bundle");

    if (BooleanUtils.isTrue(form.getPipelineInBundle())) {
      ValidationUtils.rejectIfEmpty(errors, "bundleName",
          "bundleName" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the bundle name");

      if (StringUtils.length(form.getBundleName()) > 4000) {
        errors.rejectValue("bundleName",
            "bundleName" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
            "Bundle name must be 4000 characters or fewer");
      }
    }

    var pipelineStatus = (PipelineStatus) validationHints[0];
    var questionsForPipelineStatus = PipelineHeaderConditionalQuestion.getQuestionsForStatus(pipelineStatus);
    for (var question: questionsForPipelineStatus) {
      if (PipelineHeaderConditionalQuestion.OUT_OF_USE_ON_SEABED_REASON.equals(question)) {
        ValidationUtils.rejectIfEmpty(errors, "whyNotReturnedToShore",
            "whyNotReturnedToShore" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Provide a reason for why the pipeline is not being returned to shore");

        ValidatorUtils.validateDefaultStringLength(
            errors, "whyNotReturnedToShore", form::getWhyNotReturnedToShore,
            "The pipeline not being returned to shore reason must be 4000 characters or fewer");
      }
    }


  }

}
