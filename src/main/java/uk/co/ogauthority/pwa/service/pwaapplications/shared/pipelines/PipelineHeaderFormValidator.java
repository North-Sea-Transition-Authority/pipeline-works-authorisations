package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;

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

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fromLocation", "fromLocation.required",
        "Enter the pipeline's start point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.MANDATORY, "Start point");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "toLocation", "toLocation.required",
        "Enter the pipeline's finish point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.MANDATORY, "Finish point");

    ValidationUtils.rejectIfEmpty(errors, "pipelineType", "pipelineType.required",
        "Select the pipeline type");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "length", "length.required",
        "Enter the pipeline's length");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productsToBeConveyed", "productsToBeConveyed.required",
        "Enter the products to be conveyed");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "componentPartsDescription", "componentPartsDescription.required",
        "Enter a description of the component parts");

    ValidationUtils.rejectIfEmpty(errors, "trenchedBuriedBackfilled", "trenchedBuriedBackfilled.required",
        "Select yes if the pipeline will be trenched and/or buried and/or backfilled");

    Optional.ofNullable(form.getTrenchedBuriedBackfilled())
        .filter(tru -> tru)
        .ifPresent(t -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, "trenchingMethods", "trenchingMethods.required",
            "Enter the trenching methods"));

  }

}
