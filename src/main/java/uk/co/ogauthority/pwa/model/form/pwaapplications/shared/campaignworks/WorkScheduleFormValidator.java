package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Component
public class WorkScheduleFormValidator implements SmartValidator {

  private final TwoFieldDateInputValidator twoFieldDateInputValidator;
  private final PadPipelineService padPipelineService;

  @Autowired
  public WorkScheduleFormValidator(TwoFieldDateInputValidator twoFieldDateInputValidator,
                                   PadPipelineService padPipelineService) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
    this.padPipelineService = padPipelineService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(WorkScheduleForm.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    validate(o, errors, new Object[0]);

  }


  @Override
  public void validate(Object o, Errors errors, Object... objects) {
    var form = (WorkScheduleForm) o;
    var pwaApplicationDetail = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(PwaApplicationDetail.class))
        .map(hint -> ((PwaApplicationDetail) hint))
        .findFirst()
        .orElseThrow(
            () -> new ActionNotAllowedException("Expected PwaApplicationDetail validation hint to be provided"));

    var campaignWorkScheduleValidationHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(CampaignWorkScheduleValidationHint.class))
        .map(hint -> ((CampaignWorkScheduleValidationHint) hint))
        .findFirst()
        .orElseThrow(
            () -> new ActionNotAllowedException("Expected campaign work schedule validation hint to be provided"));


    validateWorkStartDate(errors, form, campaignWorkScheduleValidationHint);
    validateWorkEndDate(errors, form, campaignWorkScheduleValidationHint);

    validatePipelines(errors, form, pwaApplicationDetail);

  }

  private void validateWorkStartDate(Errors errors,
                                     WorkScheduleForm form,
                                     CampaignWorkScheduleValidationHint campaignWorkScheduleValidationHint) {

    if (form.getWorkStart() == null) {
      ValidationUtils.rejectIfEmpty(
          errors,
          "workStart",
          "workStart" + FieldValidationErrorCodes.REQUIRED.getCode(), "Work start date is required");
    } else {
      Object[] workStartHints = {
          new FormInputLabel("Work start date"),
          campaignWorkScheduleValidationHint.getEarliestWorkStartDateHint()
      };

      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "workStart",
          form.getWorkStart(),
          workStartHints);
    }
  }

  private void validateWorkEndDate(Errors errors,
                                   WorkScheduleForm form,
                                   CampaignWorkScheduleValidationHint campaignWorkScheduleValidationHint) {
    // end date validation depends on overall project date and work start date
    if (form.getWorkEnd() == null) {
      ValidationUtils.rejectIfEmpty(
          errors,
          "workEnd",
          "workEnd" + FieldValidationErrorCodes.REQUIRED.getCode(),  "Work end date is required");
    } else {
      List<Object> workEndHints = new ArrayList<>();
      workEndHints.add(new FormInputLabel("Work end date"));
      if (form.getWorkStart() != null && form.getWorkStart().createDate().isPresent()) {

        workEndHints.add(new OnOrAfterDateHint(form.getWorkStart().createDate().get(), "work start date"));

      }

      workEndHints.add(campaignWorkScheduleValidationHint.getLatestWorkEndDateHint());
      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "workEnd",
          form.getWorkEnd(),
          workEndHints.toArray());
    }
  }

  private void validatePipelines(Errors errors, WorkScheduleForm form, PwaApplicationDetail pwaApplicationDetail) {
    ValidationUtils.rejectIfEmpty(errors, "padPipelineIds", "padPipelineIds.required",
        "You must select at least one pipeline");

    if (form.getPadPipelineIds() != null
        && padPipelineService.getByIdList(pwaApplicationDetail,
        form.getPadPipelineIds()).size() != form.getPadPipelineIds().size()) {
      errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "One or more selected pipelines is invalid");
    }
  }

}
