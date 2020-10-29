package uk.co.ogauthority.pwa.validators;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.form.location.CoordinateFormTestUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsValidatorTest {

  private final String CONCRETE_MATTRESS_LENGTH_ATTR = "concreteMattressLength";
  private final String CONCRETE_MATTRESS_WIDTH_ATTR = "concreteMattressWidth";
  private final String CONCRETE_MATTRESS_DEPTH_ATTR = "concreteMattressDepth";

  private TwoFieldDateInputValidator twoFieldDateInputValidator;
  private CoordinateFormValidator coordinateFormValidator;

  private PermanentDepositsValidator validator;

  private PermanentDepositsForm form;

  @Before
  public void setUp() throws Exception {
    twoFieldDateInputValidator = new TwoFieldDateInputValidator();
    coordinateFormValidator = new CoordinateFormValidator();

    validator = new PermanentDepositsValidator(
        twoFieldDateInputValidator,
        coordinateFormValidator
    );

    form = new PermanentDepositsForm();
    form.setToDate(new TwoFieldDateInput(1, 1));
    form.setFromDate(new TwoFieldDateInput(1, 1));
    form.setFromCoordinateForm(CoordinateFormTestUtils.createDefaultForm());
    form.setToCoordinateForm(CoordinateFormTestUtils.createDefaultForm());
  }

  @Test
  public void supports_whenSupported() {
    assertThat(validator.supports(PermanentDepositsForm.class)).isTrue();
  }

  @Test
  public void supports_whenNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenConcreteMattress_andNullData() {

    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_LENGTH_ATTR))),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_WIDTH_ATTR))),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONCRETE_MATTRESS_DEPTH_ATTR)))
    );

  }

  @Test
  public void validate_whenConcreteMattress_andValidData() {

    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(BigDecimal.ONE);
    form.setConcreteMattressWidth(BigDecimal.TEN);
    form.setConcreteMattressDepth(BigDecimal.TEN);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).doesNotContainKeys(
        CONCRETE_MATTRESS_LENGTH_ATTR,
        CONCRETE_MATTRESS_WIDTH_ATTR,
        CONCRETE_MATTRESS_DEPTH_ATTR
    );

  }

  @Test
  public void validate_whenConcreteMattress_andMaxDpExceeded() {

    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setConcreteMattressLength(BigDecimal.valueOf(1.111));
    form.setConcreteMattressWidth(BigDecimal.valueOf(1.111));
    form.setConcreteMattressDepth(BigDecimal.valueOf(1.111));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).contains(
        entry(CONCRETE_MATTRESS_LENGTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_LENGTH_ATTR))),
        entry(CONCRETE_MATTRESS_WIDTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_WIDTH_ATTR))),
        entry(CONCRETE_MATTRESS_DEPTH_ATTR,
            Set.of(FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(CONCRETE_MATTRESS_DEPTH_ATTR)))
    );

  }
}