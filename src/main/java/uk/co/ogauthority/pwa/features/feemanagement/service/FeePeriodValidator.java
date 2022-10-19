package uk.co.ogauthority.pwa.features.feemanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.util.CurrencyUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FeePeriodValidator implements Validator {

  @Override
  public void validate(Object form, Errors errors) {
    var newPeriodForm = (FeePeriodForm) form;

    if (newPeriodForm.getPeriodDescription() == null || newPeriodForm.getPeriodDescription().isEmpty()) {
      errors.rejectValue("periodDescription", "newPeriod.Description", "Enter a period name");
    }

    ValidatorUtils.validateDatePickerDateExistsAndIsValid(
        "periodStartDate",
        "Period start date",
        newPeriodForm.getPeriodStartDate(),
        errors);

    ValidatorUtils.validateDatePickerDateIsPresentOrFuture(
        "periodStartDate",
        "Period start date",
        newPeriodForm.getPeriodStartDate(),
        errors);

    //Work through each of the different types of application and validate their cost.
    //Each entry consists of ["applicationType:applicationFeeType", "cost"]
    for (var pennyAmount : newPeriodForm.getApplicationCostMap().entrySet()) {
      try {
        if (!CurrencyUtils.isValueCurrency(Double.valueOf(pennyAmount.getValue()))) {
          var fieldKey = "applicationCostMap[" + pennyAmount.getKey() + "]";

          //Application type is for New PWA's or changes to existing ones.
          var applicationType = PwaApplicationType.valueOf(pennyAmount.getKey().split(":")[0]);

          //Application Fee Type is if any surcharges are applied to the application, for fastracking as an example.
          var applicationFeeType = PwaApplicationFeeType.valueOf(pennyAmount.getKey().split(":")[1]);

          var message = applicationFeeType.getDisplayName() +
              " for " +
              applicationType.getDisplayName() +
              " must be £0.00 or more";

          errors.rejectValue(fieldKey, "newPeriod.feeValue.invalid", message);
        }
      } catch (NullPointerException | NumberFormatException e) {
        var fieldKey = "applicationCostMap[" + pennyAmount.getKey() + "]";

        var applicationType = PwaApplicationType.valueOf(pennyAmount.getKey().split(":")[0]);
        var applicationFeeType = PwaApplicationFeeType.valueOf(pennyAmount.getKey().split(":")[1]);

        var message = applicationFeeType.getDisplayName() +
            " for " +
            applicationType.getDisplayName() +
            " must be a number";

        errors.rejectValue(fieldKey, "newPeriod.feeValue.numberFormat", message);
      }
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(FeePeriodForm.class);
  }
}
