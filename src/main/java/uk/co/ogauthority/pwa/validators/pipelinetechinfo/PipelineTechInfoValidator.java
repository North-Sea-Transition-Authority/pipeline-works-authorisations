package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;


@Service
public class PipelineTechInfoValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineTechInfoForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PipelineTechInfoForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "estimatedFieldLife", "estimatedFieldLife.required",
        "Enter a valid year for the estimated field life");

    if (BooleanUtils.isTrue(form.getPipelineDesignedToStandards())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineStandardsDescription", "pipelineStandardsDescription.required",
                "Enter details of the design codes/standards for the pipelines system");
    } else if (form.getPipelineDesignedToStandards() == null) {
      errors.rejectValue("pipelineDesignedToStandards", "pipelineDesignedToStandards.required",
           "Select whether pipeline systems have been designed in accordance with industry codes and standards");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "corrosionDescription", "corrosionDescription.required",
        "Enter a description of the corrosion management strategy");

    if (BooleanUtils.isTrue(form.getPlannedPipelineTieInPoints())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tieInPointsDescription", "tieInPointsDescription.required",
          "Enter a description of the tie-in points");
    } else if (form.getPlannedPipelineTieInPoints() == null) {
      errors.rejectValue("plannedPipelineTieInPoints", "plannedPipelineTieInPoints.required",
          "Select whether there will be any future tie-in points");
    }



  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
  }





}
