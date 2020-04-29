package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PipelineHeaderFormValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineHeaderForm.class);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (PipelineHeaderForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fromLocation", "fromLocation.required",
        "Enter the pipeline's start point");

    ValidatorUtils.validateLatitude(
        errors,
        Pair.of("fromLatDeg", form.getFromLatDeg()),
        Pair.of("fromLatMin", form.getFromLatMin()),
        Pair.of("fromLatSec", form.getFromLatSec()));

    ValidatorUtils.validateLongitude(
        errors,
        Pair.of("fromLongDeg", form.getFromLongDeg()),
        Pair.of("fromLongMin", form.getFromLongMin()),
        Pair.of("fromLongSec", form.getFromLongSec()),
        Pair.of("fromLongDirection", form.getFromLongDirection()));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "toLocation", "toLocation.required",
        "Enter the pipeline's finish point");

    ValidatorUtils.validateLatitude(
        errors,
        Pair.of("toLatDeg", form.getToLatDeg()),
        Pair.of("toLatMin", form.getToLatMin()),
        Pair.of("toLatSec", form.getToLatSec()));

    ValidatorUtils.validateLongitude(
        errors,
        Pair.of("toLongDeg", form.getToLongDeg()),
        Pair.of("toLongMin", form.getToLongMin()),
        Pair.of("toLongSec", form.getToLongSec()),
        Pair.of("toLongDirection", form.getToLongDirection()));

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
