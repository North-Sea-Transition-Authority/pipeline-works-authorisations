package uk.co.ogauthority.pwa.validators;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_DP_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import io.micrometer.core.instrument.util.StringUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.PwaNumberUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.DateWithinRangeHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Service
public class PermanentDepositsValidator implements SmartValidator {

  private static final int CONCRETE_MATTRESS_DIMENSION_MAX_DECIMAL_PLACES = 2;

  private final TwoFieldDateInputValidator twoFieldDateInputValidator;
  private final CoordinateFormValidator coordinateFormValidator;

  @Autowired
  public PermanentDepositsValidator(TwoFieldDateInputValidator twoFieldDateInputValidator,
                                    CoordinateFormValidator coordinateFormValidator) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
    this.coordinateFormValidator = coordinateFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PermanentDepositsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, new Object[0]);
  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (PermanentDepositsForm) o;

    if (BooleanUtils.isFalse(form.getDepositIsForConsentedPipeline()) && BooleanUtils.isFalse(form.getDepositIsForPipelinesOnOtherApp())) {
      errors.rejectValue("depositIsForConsentedPipeline", "depositIsForConsentedPipeline" + FieldValidationErrorCodes.INVALID.getCode(),
          "Select 'Yes' to one or both of the pipeline linking questions.");
      errors.rejectValue("depositIsForPipelinesOnOtherApp", "depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.INVALID.getCode(),
          "Select 'Yes' to one or both of the pipeline linking questions.");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "depositIsForConsentedPipeline",
        "depositIsForConsentedPipeline" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select 'Yes' if deposit is for a consented pipeline or a pipeline that is on this application");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "depositIsForPipelinesOnOtherApp",
        "depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select 'Yes' if deposit is for proposed pipelines on other applications that haven’t been consented");

    if (BooleanUtils.isTrue(form.getDepositIsForConsentedPipeline())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "selectedPipelines", "selectedPipelines.required",
          "Select at least one pipeline");
    }
    if (BooleanUtils.isTrue(form.getDepositIsForPipelinesOnOtherApp())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "appRefAndPipelineNum",
          "appRefAndPipelineNum" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the application reference and proposed pipeline number for each pipeline");
    }


    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "depositReference", "depositReference.required",
        "Enter a deposit reference");

    if (StringUtils.isNotBlank(form.getDepositReference()) && validationHints[0] instanceof PermanentDepositService) {
      var permanentDepositsService = (PermanentDepositService) validationHints[0];
      var pwaApplicationDetail = (PwaApplicationDetail) validationHints[1];
      ValidatorUtils.validateBooleanTrue(errors, permanentDepositsService.isDepositReferenceUnique(
          form.getDepositReference(), form.getEntityID(), pwaApplicationDetail),
          "depositReference", "Deposit reference must be unique, enter a different reference");
    }

    validateDateIsFutureDate(errors, "deposit start date", "fromDate", form.getFromDate());
    validateDateIsFutureDate(errors, "deposit end date", "toDate", form.getToDate());
    validateDateIsWithinRange(errors, "toDate", form.getFromDate(), form.getToDate());

    if (form.getMaterialType() == null) {
      errors.rejectValue("materialType", "materialType.required",
          "Select a material type.");
    } else {
      validateMaterialTypes(form, errors);
    }


    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.MANDATORY, "Start point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.MANDATORY, "Finish point");

    if (form.getFootnote() != null) {
      ValidatorUtils.validateDefaultStringLength(errors, "footnote", form::getFootnote, "Any other information");
    }
  }


  private void validateMaterialTypes(PermanentDepositsForm form, Errors errors) {

    if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressLength", REQUIRED.errorCode("concreteMattressLength"),
          "Enter a concrete mattress length");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressWidth", REQUIRED.errorCode("concreteMattressWidth"),
          "Enter a concrete mattress width");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressDepth", REQUIRED.errorCode("concreteMattressDepth"),
          "Enter a concrete mattress depth");

      if (!PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(
          form.getConcreteMattressLength(),
          CONCRETE_MATTRESS_DIMENSION_MAX_DECIMAL_PLACES,
          true)
      ) {
        errors.rejectValue("concreteMattressLength",
            MAX_DP_EXCEEDED.errorCode("concreteMattressLength"),
            "Enter a maximum of 2 decimal places for concrete mattress length");
      }

      if (!PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(
          form.getConcreteMattressWidth(),
          CONCRETE_MATTRESS_DIMENSION_MAX_DECIMAL_PLACES,
          true)
      ) {
        errors.rejectValue("concreteMattressWidth",
            MAX_DP_EXCEEDED.errorCode("concreteMattressWidth"),
            "Enter a maximum of 2 decimal places for concrete mattress width");
      }

      if (!PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(
          form.getConcreteMattressDepth(),
          CONCRETE_MATTRESS_DIMENSION_MAX_DECIMAL_PLACES,
          true)
      ) {
        errors.rejectValue("concreteMattressDepth",
            MAX_DP_EXCEEDED.errorCode("concreteMattressDepth"),
            "Enter a maximum of 2 decimal places for concrete mattress depth");
      }


      if (!NumberUtils.isCreatable(form.getQuantityConcrete())) {
        errors.rejectValue("quantityConcrete", "quantityConcrete.invalid",
            "Enter a valid quantity for the material type");
      }


    } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rocksSize", "rocksSize.invalid", "Enter a valid size for the material type");
      if (!NumberUtils.isCreatable(form.getQuantityRocks())) {
        errors.rejectValue("quantityRocks", "quantityRocks.invalid",
            "Enter a valid quantity for the material type");
      }


    } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groutBagsSize", "groutBagsSize.invalid",
          "Enter a valid size for the material type");
      if (!NumberUtils.isCreatable(form.getQuantityGroutBags())) {
        errors.rejectValue("quantityGroutBags", "quantityGroutBags.invalid",
            "Enter a valid quantity for the material type");
      }
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groutBagsBioDegradable", "groutBagsBioDegradable.required",
          "Select yes if the grout bags are bio degradable");
      if (BooleanUtils.isFalse(form.getGroutBagsBioDegradable()) && StringUtils.isBlank(form.getBioGroutBagsNotUsedDescription())) {
        errors.rejectValue("bioGroutBagsNotUsedDescription", "bioGroutBagsNotUsedDescription.blank",
            "Explain why bio-degradable grout bags aren’t being used");
      }


    } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherMaterialSize", "otherMaterialSize.invalid",
          "Enter a valid size for the material type");
      if (!NumberUtils.isCreatable(form.getQuantityOther())) {
        errors.rejectValue("quantityOther", "quantityOther.invalid",
            "Enter a valid quantity for the material type");
      }
    }
  }


  public void validateDateIsFutureDate(Errors errors, String formLabel, String targetPath, TwoFieldDateInput formField) {
    List<Object> toDateHints = new ArrayList<>();
    toDateHints.add(new FormInputLabel(formLabel));
    toDateHints.add(new OnOrAfterDateHint(LocalDate.now(), "current date"));
    ValidatorUtils.invokeNestedValidator(
        errors,
        twoFieldDateInputValidator,
        targetPath,
        formField,
        toDateHints.toArray());
  }

  private void validateDateIsWithinRange(
      Errors errors, String targetPath, TwoFieldDateInput fromTwoFieldDate, TwoFieldDateInput toTwoFieldDate) {

    var fromDateOpt = fromTwoFieldDate.createDate();
    var toDateOpt = toTwoFieldDate.createDate();
    var maxMonthRange = 12;

    if (fromDateOpt.isPresent() && toDateOpt.isPresent()) {
      List<Object> dateHints = new ArrayList<>();
      dateHints.add(new FormInputLabel("Deposit end date"));
      dateHints.add(new DateWithinRangeHint(fromDateOpt.get(),
          fromDateOpt.get().plusMonths(maxMonthRange), maxMonthRange + " months of the deposit start date"));

      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          targetPath,
          toTwoFieldDate,
          dateHints.toArray());

    }
  }

}
