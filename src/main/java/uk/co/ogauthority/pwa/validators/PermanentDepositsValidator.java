package uk.co.ogauthority.pwa.validators;


import io.micrometer.core.instrument.util.StringUtils;
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
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PermanentDepositsValidator implements SmartValidator {

  private final CoordinateFormValidator coordinateFormValidator;

  @Autowired
  public PermanentDepositsValidator(CoordinateFormValidator coordinateFormValidator) {
    this.coordinateFormValidator = coordinateFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PermanentDepositsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (PermanentDepositsForm) o;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "selectedPipelines", "selectedPipelines.required",
        "Select at least one pipeline");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "depositReference", "depositReference.required",
        "You must enter a deposit reference");

    if (StringUtils.isNotBlank(form.getDepositReference()) && validationHints[0] instanceof PermanentDepositService) {
      var permanentDepositsService = (PermanentDepositService) validationHints[0];
      var pwaApplicationDetail = (PwaApplicationDetail) validationHints[1];
      ValidatorUtils.validateBooleanTrue(errors, permanentDepositsService.isDepositReferenceUnique(
          form.getDepositReference(), form.getEntityID(), pwaApplicationDetail),
          "depositReference", "Deposit reference must be unique, enter a different reference");
    }

    ValidatorUtils.validateDateIsPresentOrFuture(
        "from", "deposit from month / year",
        form.getFromMonth(), form.getFromYear(), errors);

    ValidatorUtils.validateDateIsWithinRangeOfTarget(
        "to", "deposit to month / year",
        form.getToMonth(), form.getToYear(), form.getFromMonth(), form.getFromYear(), 12, errors);


    if (form.getMaterialType() == null) {
      errors.rejectValue("materialType", "materialType.required",
          "You must select a material type.");
    } else {
      validateMaterialTypes(form, errors);
    }


    ValidationUtils.invokeValidator(coordinateFormValidator, form.getFromCoordinateForm(), errors,
        "fromCoordinateForm", ValueRequirement.MANDATORY, "Start point");

    ValidationUtils.invokeValidator(coordinateFormValidator, form.getToCoordinateForm(), errors,
        "toCoordinateForm", ValueRequirement.MANDATORY, "Finish point");
  }



  private void validateMaterialTypes(PermanentDepositsForm form, Errors errors) {
    if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressLength", "concreteMattressLength.invalid",
          "Enter a valid length for the material type");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressWidth", "concreteMattressWidth.invalid",
          "Enter a valid width for the material type");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "concreteMattressDepth", "concreteMattressDepth.invalid",
          "Enter a valid depth for the material type");
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


}
