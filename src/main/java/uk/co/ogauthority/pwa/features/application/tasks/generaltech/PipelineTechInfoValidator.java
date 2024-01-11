package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import java.util.Arrays;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorUtils;


@Service
public class PipelineTechInfoValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineTechInfoForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Validation requires validation hints.");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (PipelineTechInfoForm) target;

    var validationType = Arrays.stream(validationHints)
        .filter(hint -> hint instanceof ValidationType)
        .findFirst();
    if (validationType.orElse(ValidationType.FULL) == ValidationType.FULL) {

      var resourceType = Arrays.stream(validationHints)
          .filter(hint -> hint instanceof PwaResourceType)
          .findFirst();
      if (!resourceType.orElse(PwaResourceType.PETROLEUM).equals(PwaResourceType.HYDROGEN)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "estimatedAssetLife", "estimatedAssetLife.required",
            "Enter a valid year for the estimated life of the asset");

        if (form.getEstimatedAssetLife() != null && form.getEstimatedAssetLife() <= 0) {
          errors.rejectValue("estimatedAssetLife", "estimatedAssetLife.valueOutOfRange", "Enter a value greater than 0");
        }
      }

      if (BooleanUtils.isTrue(form.getPipelineDesignedToStandards())) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineStandardsDescription", "pipelineStandardsDescription.required",
            "Enter details of the design codes/standards for the pipelines system");
      } else if (form.getPipelineDesignedToStandards() == null) {
        errors.rejectValue("pipelineDesignedToStandards", "pipelineDesignedToStandards.required",
            "Select whether pipeline systems have been designed in accordance with industry codes and standards");
      }

    }

    ValidatorUtils.validateDefaultStringLength(errors, "pipelineStandardsDescription", form::getPipelineStandardsDescription,
        "Design codes/standards");

    if (validationType.orElse(ValidationType.FULL) == ValidationType.FULL) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "corrosionDescription", "corrosionDescription.required",
          "Enter a description of the corrosion management strategy");
    }

    ValidatorUtils.validateDefaultStringLength(errors, "corrosionDescription", form::getCorrosionDescription,
        "Corrosion management strategy");

    if (validationType.orElse(ValidationType.FULL) == ValidationType.FULL) {

      if (BooleanUtils.isTrue(form.getPlannedPipelineTieInPoints())) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tieInPointsDescription", "tieInPointsDescription.required",
            "Enter a description of the tie-in points");
      } else if (form.getPlannedPipelineTieInPoints() == null) {
        errors.rejectValue("plannedPipelineTieInPoints", "plannedPipelineTieInPoints.required",
            "Select whether there will be any future tie-in points");
      }

    }

    ValidatorUtils.validateDefaultStringLength(errors, "tieInPointsDescription", form::getTieInPointsDescription,
        "Tie-in points description");

  }

}
