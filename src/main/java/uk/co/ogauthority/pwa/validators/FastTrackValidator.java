package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;

@Service
public class FastTrackValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(FastTrackForm.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    var form = (FastTrackForm) o;
    if (form.getAvoidEnvironmentalDisaster() == null
        && form.getSavingBarrels() == null
        && form.getProjectPlanning() == null
        && form.getHasOtherReason() == null) {
      errors.rejectValue("avoidEnvironmentalDisaster", "avoidEnvironmentalDisaster.noneSelected",
          "Select at least one reason for fast-tracking the application");
      errors.rejectValue("savingBarrels", "savingBarrels.noneSelected","");
      errors.rejectValue("projectPlanning", "projectPlanning.noneSelected","");
      errors.rejectValue("hasOtherReason", "hasOtherReason.noneSelected","");
    }
    if (BooleanUtils.isTrue(form.getAvoidEnvironmentalDisaster())
        && StringUtils.isBlank(form.getEnvironmentalDisasterReason())) {
      errors.rejectValue("environmentalDisasterReason", "environmentalDisasterReason.empty",
          "Enter a reason for selecting avoiding environmental disaster");
    }
    if (BooleanUtils.isTrue(form.getSavingBarrels()) && StringUtils.isBlank(form.getSavingBarrelsReason())) {
      errors.rejectValue("savingBarrelsReason", "savingBarrelsReason.empty",
          "Enter a reason for selecting saving barrels");
    }
    if (BooleanUtils.isTrue(form.getProjectPlanning()) && StringUtils.isBlank(form.getProjectPlanningReason())) {
      errors.rejectValue("projectPlanningReason", "projectPlanningReason.empty",
          "Enter a reason for selecting project planning");
    }
    if (BooleanUtils.isTrue(form.getHasOtherReason()) && StringUtils.isBlank(form.getOtherReason())) {
      errors.rejectValue("otherReason", "otherReason.empty",
          "Enter a reason for selecting other reasons");
    }
  }
}
