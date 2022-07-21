package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import io.micrometer.core.instrument.util.StringUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalPlaceHint;
import uk.co.ogauthority.pwa.util.forminputs.decimal.PositiveNumberHint;
import uk.co.ogauthority.pwa.util.forminputs.generic.MaxLengthHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.DateWithinRangeHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Service
public class PermanentDepositsValidator implements SmartValidator {

  private final TwoFieldDateInputValidator twoFieldDateInputValidator;
  private final CoordinateFormValidator coordinateFormValidator;
  private final DecimalInputValidator decimalInputValidator;

  @Autowired
  public PermanentDepositsValidator(TwoFieldDateInputValidator twoFieldDateInputValidator,
                                    CoordinateFormValidator coordinateFormValidator,
                                    DecimalInputValidator decimalInputValidator) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
    this.coordinateFormValidator = coordinateFormValidator;
    this.decimalInputValidator = decimalInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PermanentDepositsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Not implemented. Validation requires hints to be provided.");
  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {

    var form = (PermanentDepositsForm) o;
    PermanentDepositsValidationHints depositValidationHints;

    if (validationHints[0] instanceof PermanentDepositsValidationHints) {
      depositValidationHints = (PermanentDepositsValidationHints) validationHints[0];
    } else {
      throw new UnsupportedOperationException(
          "Cannot validate Permanent Deposits Form without correct validation hints provided. Expected : " +
              PermanentDepositsValidationHints.class.toString());
    }

    if (BooleanUtils.isFalse(form.getDepositIsForConsentedPipeline()) && BooleanUtils.isFalse(
        form.getDepositIsForPipelinesOnOtherApp())) {
      errors.rejectValue("depositIsForConsentedPipeline",
          "depositIsForConsentedPipeline" + FieldValidationErrorCodes.INVALID.getCode(),
          "Select 'Yes' to one or both of the pipeline linking questions.");
      errors.rejectValue("depositIsForPipelinesOnOtherApp",
          "depositIsForPipelinesOnOtherApp" + FieldValidationErrorCodes.INVALID.getCode(),
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
      if (
          form.getSelectedPipelines() != null
          && !depositValidationHints.getAcceptedPipelineIds().containsAll(form.getSelectedPipelines())
      ) {
        errors.rejectValue(
            "selectedPipelines",
            "selectedPipelines" + FieldValidationErrorCodes.INVALID.getCode(),
            "Select a valid pipeline"
        );
      }
    }
    if (BooleanUtils.isTrue(form.getDepositIsForPipelinesOnOtherApp())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "appRefAndPipelineNum",
          "appRefAndPipelineNum" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the application reference and proposed pipeline number for each pipeline");

      ValidatorUtils.validateDefaultStringLength(
          errors, "appRefAndPipelineNum", form::getAppRefAndPipelineNum,
          "Application reference and proposed pipeline numbers");
    }

    validateDepositReference(errors, depositValidationHints, form);

    validateStartDate(errors, depositValidationHints, form.getFromDate());
    validateEndDate(errors, depositValidationHints, form.getFromDate(), form.getToDate());

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
      ValidatorUtils.validateDefaultStringLength(errors, "footnote", form::getFootnote, "Other information");
    }


  }


  private void validateMaterialTypes(PermanentDepositsForm form, Errors errors) {

    if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "concreteMattressLength",
          form.getConcreteMattressLength(),
          getPositiveNumberFieldHints("concrete mattress length")
      );

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "concreteMattressWidth",
          form.getConcreteMattressWidth(),
          getPositiveNumberFieldHints("concrete mattress width")
      );

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "concreteMattressDepth",
          form.getConcreteMattressDepth(),
          getPositiveNumberFieldHints("concrete mattress depth")
      );

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "quantityConcrete",
          form.getQuantityConcrete(),
          getPositiveNumberFieldHints("quantity material")
      );

      ValidatorUtils.validateMaxStringLength(errors, "contingencyConcreteAmount", form::getContingencyConcreteAmount,
          "Concrete mattresses contingency amount", 150);

    } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rocksSize", "rocksSize.invalid",
          "Enter a valid size for the material type");

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "quantityRocks",
          form.getQuantityRocks(),
          getPositiveNumberFieldHints("quantity rocks")
      );

      ValidatorUtils.validateMaxStringLength(errors, "rocksSize", form::getRocksSize,
          "Rock size", 20);

      ValidatorUtils.validateMaxStringLength(errors, "contingencyRocksAmount", form::getContingencyRocksAmount,
          "Rock contingency amount", 150);

    } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "groutBagsSize",
          form.getGroutBagsSize(),
          getPositiveNumberFieldHints("grout bags",
              new MaxLengthHint(20))
      );

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "quantityGroutBags",
          form.getQuantityGroutBags(),
          getPositiveNumberFieldHints("quantity grout bags",
              new MaxLengthHint(20))
      );

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groutBagsBioDegradable", "groutBagsBioDegradable.required",
          "Select yes if the grout bags are bio degradable");

      if (BooleanUtils.isFalse(form.getGroutBagsBioDegradable()) && StringUtils.isBlank(
          form.getBioGroutBagsNotUsedDescription())) {
        errors.rejectValue("bioGroutBagsNotUsedDescription", "bioGroutBagsNotUsedDescription.blank",
            "Explain why bio-degradable grout bags aren’t being used");
      }

      ValidatorUtils.validateDefaultStringLength(errors, "bioGroutBagsNotUsedDescription",
          form::getBioGroutBagsNotUsedDescription, "Explanation for not using bio-degradable grout bags");

      ValidatorUtils.validateMaxStringLength(errors, "contingencyGroutBagsAmount", form::getContingencyGroutBagsAmount,
          "Grout bags contingency amount", 150);

    } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherMaterialSize", "otherMaterialSize.invalid",
          "Enter a valid size for the material type");

      ValidatorUtils.invokeNestedValidator(
          errors,
          decimalInputValidator,
          "quantityOther",
          form.getQuantityOther(),
          getPositiveNumberFieldHints("quantity of material")
      );

      ValidatorUtils.validateMaxStringLength(errors, "otherMaterialSize", form::getOtherMaterialSize,
          "Other material size", 20);

      ValidatorUtils.validateMaxStringLength(errors, "contingencyOtherAmount", form::getContingencyOtherAmount,
          "Contingency amount",
          150);
    }
  }

  private void validateStartDate(Errors errors, PermanentDepositsValidationHints validationHints,
                                 TwoFieldDateInput depositStartDateInput) {

    List<Object> dateValidationHints = new ArrayList<>();
    dateValidationHints.add(new FormInputLabel("deposit start date"));

    if (Objects.nonNull(validationHints.getProjectInfoProposedStartTimestamp())) {
      dateValidationHints.add(new OnOrAfterDateHint(
          DateUtils.instantToLocalDate(validationHints.getProjectInfoProposedStartTimestamp()),
          "the proposed start of works date"));

    } else {
      dateValidationHints.add(new OnOrAfterDateHint(LocalDate.now(), "the current date"));
    }

    ValidatorUtils.invokeNestedValidator(
        errors,
        twoFieldDateInputValidator,
        "fromDate",
        depositStartDateInput,
        dateValidationHints.toArray());
  }

  private void validateEndDate(Errors errors, PermanentDepositsValidationHints validationHints,
                               TwoFieldDateInput depositStartDateInput, TwoFieldDateInput depositEndDateInput) {

    var startDateOpt = depositStartDateInput.createDate();
    var endDateOpt = depositEndDateInput.createDate();

    List<Object> dateValidationHints = new ArrayList<>();
    dateValidationHints.add(new FormInputLabel("Deposit end date"));

    if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
      var maxMonthRange = validationHints.getApplicationDetail().getPwaApplicationType().equals(
          PwaApplicationType.OPTIONS_VARIATION) ? 6 : 12;
      dateValidationHints.add(new DateWithinRangeHint(startDateOpt.get(),
          startDateOpt.get().plusMonths(maxMonthRange), maxMonthRange + " months of the deposit start date"));
    }

    ValidatorUtils.invokeNestedValidator(
        errors,
        twoFieldDateInputValidator,
        "toDate",
        depositEndDateInput,
        dateValidationHints.toArray());

  }


  private void validateDepositReference(Errors errors, PermanentDepositsValidationHints validationHints,
                                        PermanentDepositsForm form) {

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "depositReference", "depositReference.required",
        "Enter a deposit reference");

    ValidatorUtils.validateMaxStringLength(errors, "depositReference", form::getDepositReference, "Deposit reference",
        50);

    var depositIsNotUnique = validationHints.getExistingDepositsForApp().stream()
        .filter(deposit -> !deposit.getId().equals(form.getEntityID()))
        .anyMatch(deposit -> deposit.getReference().equalsIgnoreCase(form.getDepositReference()));

    if (depositIsNotUnique) {
      errors.rejectValue("depositReference", FieldValidationErrorCodes.NOT_UNIQUE.errorCode("depositReference"),
          "Deposit reference must be unique, enter a different reference");
    }

  }

  private Object[] getPositiveNumberFieldHints(String formInputLabelText, Object... additionalHints) {
    var standardHints =  new Object[]{
        new FormInputLabel(formInputLabelText),
        new DecimalPlaceHint(2),
        new PositiveNumberHint()
    };

    return ArrayUtils.addAll(standardHints, additionalHints);
  }
}
