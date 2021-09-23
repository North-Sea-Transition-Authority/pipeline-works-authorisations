package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalPlaceHint;
import uk.co.ogauthority.pwa.util.forminputs.decimal.NonNegativeNumberHint;
import uk.co.ogauthority.pwa.util.forminputs.decimal.SmallerThanNumberHint;
import uk.co.ogauthority.pwa.util.validation.PipelineValidationUtils;

@Service
public class PipelineIdentDataFormValidator implements SmartValidator {


  private final DecimalInputValidator decimalInputValidator;

  @Autowired
  public PipelineIdentDataFormValidator(
      DecimalInputValidator decimalInputValidator) {
    this.decimalInputValidator = decimalInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineIdentDataForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    // if being used as a sub-form, need to know the name of the sub-form to create the errors properly
    var fieldPrefix = validationHints[0] != null ? validationHints[0] + "." : "";
    var form = (PipelineIdentDataForm) target;
    var coreType = (PipelineCoreType) validationHints[1];
    var isDefiningStructure = (PipelineIdentDataValidationRule) validationHints[2];


    if (coreType.equals(PipelineCoreType.MULTI_CORE)) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "componentPartsDescription",
          "componentPartsDescription" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a description of the component parts");

      ValidatorUtils.validateDefaultStringLength(
          errors, fieldPrefix + "componentPartsDescription", form::getComponentPartsDescription, "Description of component part");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "productsToBeConveyedMultiCore",
          "productsToBeConveyedMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a description for the products to be conveyed");

      ValidatorUtils.validateDefaultStringLength(
          errors, fieldPrefix + "productsToBeConveyedMultiCore", form::getProductsToBeConveyedMultiCore, "Products to be conveyed");

      if (isDefiningStructure.equals(PipelineIdentDataValidationRule.AS_SECTION)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "externalDiameterMultiCore",
            "externalDiameterMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a description for the external diameter");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "externalDiameterMultiCore", form::getExternalDiameterMultiCore, "External diameter");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "internalDiameterMultiCore",
            "internalDiameterMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a description for the internal diameter");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "internalDiameterMultiCore", form::getInternalDiameterMultiCore, "Internal diameter");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "wallThicknessMultiCore",
            "wallThicknessMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a description for the wall thickness");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "wallThicknessMultiCore", form::getWallThicknessMultiCore, "Wall thickness");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "maopMultiCore",
            "maopMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter a description for the MAOP");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "maopMultiCore", form::getMaopMultiCore, "MAOP");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "insulationCoatingTypeMultiCore",
            "insulationCoatingTypeMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a description for the insulation / coating type");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "insulationCoatingTypeMultiCore", form::getInsulationCoatingTypeMultiCore,
            "Insulation / coating type");
      }


    } else {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "productsToBeConveyed",
          "productsToBeConveyed" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the products to be conveyed");

      ValidatorUtils.validateDefaultStringLength(
          errors, fieldPrefix + "productsToBeConveyed", form::getProductsToBeConveyed, "Products to be conveyed");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "componentPartsDescription",
          "componentPartsDescription" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a description of the component parts");

      ValidatorUtils.validateDefaultStringLength(
          errors, fieldPrefix + "componentPartsDescription", form::getComponentPartsDescription, "Description of component parts");

      if (isDefiningStructure.equals(PipelineIdentDataValidationRule.AS_SECTION)) {

        validateDecimal(form.getExternalDiameter(), errors, fieldPrefix + "externalDiameter", "external diameter");

        var internalDiameterHints = new ArrayList<>();
        if (form.getExternalDiameter().asBigDecimal().isPresent()) {
          internalDiameterHints.add(new SmallerThanNumberHint(form.getExternalDiameter().createBigDecimalOrNull(), "external diameter"));
        }
        validateDecimal(form.getInternalDiameter(), errors, fieldPrefix + "internalDiameter", "internal diameter", internalDiameterHints);

        validateDecimal(form.getWallThickness(), errors, fieldPrefix + "wallThickness", "wall thickness");

        validateDecimal(form.getMaop(), errors, fieldPrefix + "maop", "MAOP");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "insulationCoatingType",
            "insulationCoatingType" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter the insulation / coating type");

        ValidatorUtils.validateDefaultStringLength(
            errors, fieldPrefix + "insulationCoatingType", form::getInsulationCoatingType, "Insulation / coating type");
      }


    }

  }


  private void validateDecimal(
      DecimalInput decimalInput, Errors errors, String fieldPath, String formInputLabel, List<Object> additionalHints) {

    var validationHints = new ArrayList<>(List.of(
        new FormInputLabel(formInputLabel),
        new DecimalPlaceHint(PipelineValidationUtils.getMaxIdentLengthDp()),
        new NonNegativeNumberHint()));
    validationHints.addAll(additionalHints);

    ValidatorUtils.invokeNestedValidator(
        errors,
        decimalInputValidator,
        fieldPath,
        decimalInput,
        validationHints.toArray());
  }

  private void validateDecimal(DecimalInput decimalInput, Errors errors, String fieldPath, String formInputLabel) {
    validateDecimal(decimalInput, errors, fieldPath, formInputLabel, List.of());
  }


}
