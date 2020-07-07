package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;

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

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fromLocation", "fromLocation.required",
        "Enter the ident's start point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.OPTIONAL, "Start point");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "toLocation", "toLocation.required",
        "Enter the ident's finish point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.OPTIONAL, "Finish point");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "length", "length.required",
        "Enter the ident's length");

    ValidationUtils.invokeValidator(dataFormValidator, form.getDataForm(), errors, "dataForm", validationHints[1]);

  }

}
