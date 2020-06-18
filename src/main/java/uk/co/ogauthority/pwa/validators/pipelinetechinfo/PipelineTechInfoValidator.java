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
        "You must enter a valid year for the estimated field life");

    if (BooleanUtils.isTrue(form.getPipelineDesignedToStandards())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineStandardsDescription", "pipelineStandardsDescription.required",
                "You must enter details of the design codes/standards for the pipelines system");
    } else if (form.getPipelineDesignedToStandards() == null) {
      errors.rejectValue("pipelineDesignedToStandards", "pipelineDesignedToStandards.required",
           "You must select whether pipeline systems have been designed in accordance with industry codes and standards");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "corrosionDescription", "corrosionDescription.required",
        "You must enter a description of the corrosion management strategy");

    if (BooleanUtils.isTrue(form.getPlannedPipelineTieInPoints())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tieInPointsDescription", "tieInPointsDescription.required",
          "You must enter a description of the tie-in points");
    } else if (form.getPlannedPipelineTieInPoints() == null) {
      errors.rejectValue("plannedPipelineTieInPoints", "plannedPipelineTieInPoints.required",
          "You must select whether there will be any future tie-in points");
    }



  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
  }





}
