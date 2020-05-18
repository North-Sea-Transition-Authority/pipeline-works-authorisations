package uk.co.ogauthority.pwa.validators;


import io.micrometer.core.instrument.util.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PermanentDepositsValidator implements SmartValidator {


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

    if (validationHints[0] instanceof PadProjectInformation) {
      var projectInformation = (PadProjectInformation) validationHints[0];
      LocalDateTime proposedStartDate = LocalDateTime.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneOffset.UTC);
      ValidatorUtils.validateDateIsPresentOrFutureOfTarget(
          "from", "deposit from month / year",
          form.getFromMonth(), form.getFromYear(), proposedStartDate.getMonthValue(), proposedStartDate.getYear(), errors);

      ValidatorUtils.validateDateIsWithinRangeOfTarget(
          "to", "deposit to month / year",
          form.getToMonth(), form.getToYear(), form.getFromMonth(), form.getFromYear(), 12, errors);
    } else {
      errors.rejectValue("fromMonth", "fromMonth.beforeTarget",
          "Enter a month and year that is after the proposed start date.");
    }

    if (form.getMaterialType() == null) {
      errors.rejectValue("materialType", "materialType.required",
          "You must select a material type.");
    } else {
      validateMaterialTypes(form, errors);
    }


    ValidatorUtils.validateLatitude(
        errors,
        "fromLatitude",
        Pair.of("fromLatitudeDegrees", NumberUtils.createInteger(form.getFromLatitudeDegrees())),
        Pair.of("fromLatitudeMinutes", NumberUtils.createInteger(form.getFromLatitudeMinutes())),
        Pair.of("fromLatitudeSeconds", NumberUtils.createBigDecimal(form.getFromLatitudeSeconds()))
    );

    ValidatorUtils.validateLongitude(
        errors,
        "fromLongitude",
        Pair.of("fromLongitudeDegrees", NumberUtils.createInteger(form.getFromLongitudeDegrees())),
        Pair.of("fromLongitudeMinutes", NumberUtils.createInteger(form.getFromLongitudeMinutes())),
        Pair.of("fromLongitudeSeconds", NumberUtils.createBigDecimal(form.getFromLongitudeSeconds())),
        Pair.of("fromLongitudeDirection",
            form.getFromLongitudeDirection() == null ? null : LongitudeDirection.valueOf(form.getFromLongitudeDirection()))
    );

    ValidatorUtils.validateLatitude(
        errors,
        "toLatitude",
        Pair.of("toLatitudeDegrees", NumberUtils.createInteger(form.getToLatitudeDegrees())),
        Pair.of("toLatitudeMinutes", NumberUtils.createInteger(form.getToLatitudeMinutes())),
        Pair.of("toLatitudeSeconds", NumberUtils.createBigDecimal(form.getToLatitudeSeconds()))
    );

    ValidatorUtils.validateLongitude(
        errors,
        "toLongitude",
        Pair.of("toLongitudeDegrees", NumberUtils.createInteger(form.getToLongitudeDegrees())),
        Pair.of("toLongitudeMinutes", NumberUtils.createInteger(form.getToLongitudeMinutes())),
        Pair.of("toLongitudeSeconds", NumberUtils.createBigDecimal(form.getToLongitudeSeconds())),
        Pair.of("toLongitudeDirection",
            form.getToLongitudeDirection() == null ? null : LongitudeDirection.valueOf(form.getToLongitudeDirection()))
    );

  }

  private void validateMaterialTypes(PermanentDepositsForm form, Errors errors) {
    if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      if (form.getConcreteMattressLength() == null) {
        errors.rejectValue("concreteMattressLength", "concreteMattressLength.invalid",
            "Enter a valid length for the 'Concrete Mattresses' material type");
      }
      if (form.getConcreteMattressWidth() == null) {
        errors.rejectValue("concreteMattressWidth", "concreteMattressWidth.invalid",
            "Enter a valid width for the 'Concrete Mattresses' material type");
      }
      if (form.getConcreteMattressDepth() == null) {
        errors.rejectValue("concreteMattressDepth", "concreteMattressDepth.invalid",
            "Enter a valid depth for the 'Concrete Mattresses' material type");
      }
      if (!NumberUtils.isCreatable(form.getQuantityConcrete())) {
        errors.rejectValue("quantityConcrete", "quantityConcrete.invalid",
            "Enter a valid quantity for the 'Concrete Mattresses' material type");
      }


    } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
      if (form.getRocksSize() == null) {
        errors.rejectValue("rocksSize", "rocksSize.invalid",
            "Enter a valid size for the 'Rocks' material type");
      }
      if (!NumberUtils.isCreatable(form.getQuantityRocks())) {
        errors.rejectValue("quantityRocks", "quantityRocks.invalid",
            "Enter a valid quantity for the 'Rocks' material type");
      }


    } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      if (form.getGroutBagsSize() == null) {
        errors.rejectValue("groutBagsSize", "groutBagsSize.invalid",
            "Enter a valid size for the 'Grout Bags' material type");
      }
      if (!NumberUtils.isCreatable(form.getQuantityGroutBags())) {
        errors.rejectValue("quantityGroutBags", "quantityGroutBags.invalid",
            "Enter a valid quantity for the 'Grout Bags' material type");
      }
      if (form.getGroutBagsBioDegradable() == null) {
        errors.rejectValue("groutBagsBioDegradable", "groutBagsBioDegradable.required",
            "Select yes if the grout bags are bio degradable");
      }
      if (BooleanUtils.isFalse(form.getGroutBagsBioDegradable()) && StringUtils.isBlank(form.getBioGroutBagsNotUsedDescription())) {
        errors.rejectValue("bioGroutBagsNotUsedDescription", "bioGroutBagsNotUsedDescription.blank",
            "Enter a description for bio degradable grout bags not being used");
      }


    } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
      if (form.getOtherMaterialSize() == null) {
        errors.rejectValue("otherMaterialSize", "otherMaterialSize.invalid",
            "Enter a valid size for other material types");
      }
      if (!NumberUtils.isCreatable(form.getQuantityOther())) {
        errors.rejectValue("quantityOther", "quantityOther.invalid",
            "Enter a valid quantity for other material types");
      }
    }
  }


}
