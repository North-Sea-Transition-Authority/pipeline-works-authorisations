package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PipelineIdentDataFormValidator implements SmartValidator {

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


    if (coreType.equals(PipelineCoreType.MULTI_CORE)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "externalDiameterMultiCore",
          "externalDiameterMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter a description for the external diameter");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "productsToBeConveyedMultiCore",
          "productsToBeConveyedMultiCore" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a description for the products to be conveyed");

      ValidatorUtils.validateDefaultStringLength(
          errors, "externalDiameterMultiCore", form::getExternalDiameterMultiCore, "External diameter");
      ValidatorUtils.validateDefaultStringLength(
          errors, "internalDiameterMultiCore", form::getExternalDiameterMultiCore, "Internal diameter");
      ValidatorUtils.validateDefaultStringLength(
          errors, "wallThicknessMultiCore", form::getExternalDiameterMultiCore, "Wall thickness");
      ValidatorUtils.validateDefaultStringLength(
          errors, "insulationCoatingTypeMultiCore", form::getExternalDiameterMultiCore, "Insulation / coating type");
      ValidatorUtils.validateDefaultStringLength(
          errors, "maopMultiCore", form::getExternalDiameterMultiCore, "MAOP");
      ValidatorUtils.validateDefaultStringLength(
          errors, "productsToBeConveyedMultiCore", form::getExternalDiameterMultiCore, "Products to be conveyed / coating type");

    } else {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "productsToBeConveyed",
          "productsToBeConveyed" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the products to be conveyed");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "externalDiameter",
          "externalDiameter" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the external diameter");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "internalDiameter", "" +
              "internalDiameter" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the internal diameter");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "wallThickness",
          "wallThickness" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the wall thickness");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "maop", "maop" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the MAOP");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "insulationCoatingType",
          "insulationCoatingType" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the insulation / coating type");

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "componentPartsDescription",
          "componentPartsDescription" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter a description of the component parts");


      ValidatorUtils.validateDefaultStringLength(
          errors, "insulationCoatingType", form::getInsulationCoatingType, "Insulation / coating type");

      ValidatorUtils.validateDefaultStringLength(
          errors, "productsToBeConveyed", form::getProductsToBeConveyed, "Products to be conveyed / coating type");
    }

  }

}
