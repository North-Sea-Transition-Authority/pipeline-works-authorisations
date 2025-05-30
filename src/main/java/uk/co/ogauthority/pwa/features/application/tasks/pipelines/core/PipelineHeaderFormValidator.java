package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
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
          "Select a valid pipeline type");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "length", "length.required",
        "Enter the pipeline's length");

    if (form.getLength() != null) {
      PipelineValidationUtils.validateLength(form.getLength(), errors, "length", "Pipeline length");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "componentPartsDescription", "componentPartsDescription.required",
        "Enter a description of the component parts");

    ValidatorUtils.validateDefaultStringLength(
        errors, "componentPartsDescription", form::getComponentPartsDescription,
        "Description of the component parts");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productsToBeConveyed", "productsToBeConveyed.required",
        "Enter the products to be conveyed");

    ValidatorUtils.validateDefaultStringLength(
        errors, "productsToBeConveyed", form::getProductsToBeConveyed, "Products to be conveyed");

    ValidationUtils.rejectIfEmpty(errors, "trenchedBuriedBackfilled", "trenchedBuriedBackfilled.required",
        "Select yes if the pipeline will be trenched and/or buried and/or backfilled");

    Optional.ofNullable(form.getTrenchedBuriedBackfilled())
        .filter(tru -> tru)
        .ifPresent(t -> {
          ValidationUtils.rejectIfEmptyOrWhitespace(errors, "trenchingMethods", "trenchingMethods.required",
                "Enter the trenching methods");

          ValidatorUtils.validateDefaultStringLength(
              errors, "trenchingMethods", form::getTrenchingMethods, "Trenching methods");
        });

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineFlexibility", "pipelineFlexibility.required",
        "Select an option for the pipeline flexibility");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineMaterial", "pipelineMaterial.required",
        "Select an option for the pipeline material");

    if (form.getPipelineMaterial() != null && form.getPipelineMaterial().equals(PipelineMaterial.OTHER)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherPipelineMaterialUsed",
          "otherPipelineMaterialUsed.required",
          "Enter details of other pipeline materials used");

      ValidatorUtils.validateDefaultStringLength(
          errors, "otherPipelineMaterialUsed", form::getOtherPipelineMaterialUsed, "Other pipeline materials used details");
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

      ValidatorUtils.validateDefaultStringLength(
          errors, "bundleName", form::getBundleName, "Bundle name");
    }

    var pipelineHeaderValidationHints = (PipelineHeaderValidationHints) validationHints[0];
    var requiredQuestions = pipelineHeaderValidationHints.getRequiredQuestions();

    if (requiredQuestions.contains(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON)) {
      ValidationUtils.rejectIfEmpty(errors, "whyNotReturnedToShore",
          "whyNotReturnedToShore" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Provide a reason for why the pipeline is not being returned to shore");

      ValidatorUtils.validateDefaultStringLength(
          errors, "whyNotReturnedToShore", form::getWhyNotReturnedToShore,
          "The pipeline not being returned to shore reason");
    }

    if (requiredQuestions.contains(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED)) {
      ValidationUtils.rejectIfEmpty(errors, "alreadyExistsOnSeabed", "alreadyExistsOnSeabed" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select 'Yes' if this pipeline already exists on the seabed");

      if (BooleanUtils.isTrue(form.getAlreadyExistsOnSeabed())) {
        ValidationUtils.rejectIfEmpty(errors, "pipelineInUse", "pipelineInUse" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select 'Yes' if this pipeline is in use");
      }
    }

    if (form.getFootnote() != null) {
      ValidatorUtils.validateDefaultStringLength(errors, "footnote", form::getFootnote, "Special features information");
    }

  }

}
