package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.util.validation.PipelineValidationUtils;

@Service
public class PipelineIdentFormValidator implements SmartValidator {

  private final PipelineIdentDataFormValidator dataFormValidator;
  private final CoordinateFormValidator coordinateFormValidator;

  @Autowired
  public PipelineIdentFormValidator(PipelineIdentDataFormValidator dataFormValidator,
                                    CoordinateFormValidator coordinateFormValidator) {
    this.dataFormValidator = dataFormValidator;
    this.coordinateFormValidator = coordinateFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineIdentForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (PipelineIdentForm) target;

    PipelineValidationUtils.validateFromToLocation(form.getFromLocation(), errors, "fromLocation", "Ident start structure");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.OPTIONAL, "Start point");

    PipelineValidationUtils.validateFromToLocation(form.getToLocation(), errors, "toLocation", "Ident finish structure");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.OPTIONAL, "Finish point");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "definingStructure",
        "definingStructure" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select 'Yes' if the ident is defining a structure");

    if (BooleanUtils.isTrue(form.getDefiningStructure())) {
      if (form.getLengthOptional() != null) {
        PipelineValidationUtils.validateLength(form.getLengthOptional(), errors, "lengthOptional", "Ident length");
      }

      if (form.getFromCoordinateForm() != null && form.getFromCoordinateForm().areAllFieldsNotNull()
          && form.getToCoordinateForm() != null && form.getToCoordinateForm().areAllFieldsNotNull()) {

        if (!form.getFromCoordinateForm().compareFormLatitude(form.getToCoordinateForm())) {
          errors.rejectValue("fromCoordinateForm.latitudeDegrees",
              "fromCoordinateForm.latitudeDegrees" + FieldValidationErrorCodes.INVALID.getCode(),
              "The start point and end point latitudes must match when defining a structure");
          errors.rejectValue("fromCoordinateForm.latitudeMinutes",
              "fromCoordinateForm.latitudeMinutes" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("fromCoordinateForm.latitudeSeconds",
              "fromCoordinateForm.latitudeSeconds" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.latitudeDegrees",
              "toCoordinateForm.latitudeDegrees" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.latitudeMinutes",
              "toCoordinateForm.latitudeMinutes" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.latitudeSeconds",
              "toCoordinateForm.latitudeSeconds" + FieldValidationErrorCodes.INVALID.getCode(), "");
        }

        if (!form.getFromCoordinateForm().compareFormLongitude(form.getToCoordinateForm())) {
          errors.rejectValue("fromCoordinateForm.longitudeDegrees",
              "fromCoordinateForm.longitudeDegrees" + FieldValidationErrorCodes.INVALID.getCode(),
              "The start point and end point longitudes must match when defining a structure");
          errors.rejectValue("fromCoordinateForm.longitudeMinutes",
              "fromCoordinateForm.longitudeMinutes" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("fromCoordinateForm.longitudeSeconds",
              "fromCoordinateForm.longitudeSeconds" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("fromCoordinateForm.longitudeDirection",
              "fromCoordinateForm.longitudeDirection" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.longitudeDegrees",
              "toCoordinateForm.longitudeDegrees" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.longitudeMinutes",
              "toCoordinateForm.longitudeMinutes" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.longitudeSeconds",
              "toCoordinateForm.longitudeSeconds" + FieldValidationErrorCodes.INVALID.getCode(), "");
          errors.rejectValue("toCoordinateForm.longitudeDirection",
              "toCoordinateForm.longitudeDirection" + FieldValidationErrorCodes.INVALID.getCode(), "");
        }
      }

      if (form.getFromLocation() != null && !form.getFromLocation().equals(form.getToLocation())) {
        errors.rejectValue("fromLocation", "fromLocation" + FieldValidationErrorCodes.INVALID.getCode(),
            "The start and finish ident structures must be the same.");
      }

    } else if (BooleanUtils.isFalse(form.getDefiningStructure())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "length", "length" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the ident's length");

      if (form.getLength() != null) {
        PipelineValidationUtils.validateLength(form.getLength(), errors, "length", "Ident length");
      }

    }

    var coreType = (PipelineCoreType) validationHints[1];
    var definingStructureValidationRule = PipelineIdentDataValidationRule.UNKNOWN;
    if (BooleanUtils.isTrue(form.getDefiningStructure())) {
      definingStructureValidationRule = PipelineIdentDataValidationRule.AS_STRUCTURE;
    } else if (BooleanUtils.isFalse(form.getDefiningStructure())) {
      definingStructureValidationRule = PipelineIdentDataValidationRule.AS_SECTION;
    }
    ValidationUtils.invokeValidator(dataFormValidator, form.getDataForm(), errors, "dataForm", coreType, definingStructureValidationRule);

  }

}
