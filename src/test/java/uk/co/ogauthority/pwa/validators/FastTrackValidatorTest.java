package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class FastTrackValidatorTest {

  private FastTrackValidator validator;

  @Before
  public void setUp() {
    validator = new FastTrackValidator();
  }

  @Test
  public void validate_AllFieldsEntered() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, buildForm());
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_EmptyForm() {
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, new FastTrackForm());
    assertThat(errors).containsOnly(
        entry("avoidEnvironmentalDisaster", Set.of("avoidEnvironmentalDisaster.noneSelected"))
    );
  }

  @Test
  public void validate_SectionInformationEmpty() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason("");
    form.setSavingBarrelsReason("");
    form.setProjectPlanningReason(null);
    form.setOtherReason(null);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of("environmentalDisasterReason.required")),
        entry("savingBarrelsReason", Set.of("savingBarrelsReason.required")),
        entry("projectPlanningReason", Set.of("projectPlanningReason.required")),
        entry("otherReason", Set.of("otherReason.required"))
    );
  }

  @Test
  public void validate_reasonDescriptionFieldsCharLengthOverMax() {
    var form = buildForm();
    form.setAvoidEnvironmentalDisaster(true);
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.over4000Chars());

    form.setSavingBarrels(true);
    form.setSavingBarrelsReason(ValidatorTestUtils.over4000Chars());

    form.setProjectPlanning(true);
    form.setProjectPlanningReason(ValidatorTestUtils.over4000Chars());

    form.setHasOtherReason(true);
    form.setOtherReason(ValidatorTestUtils.over4000Chars());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("environmentalDisasterReason"))),
        entry("savingBarrelsReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("savingBarrelsReason"))),
        entry("projectPlanningReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("projectPlanningReason"))),
        entry("otherReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("otherReason")))
    );
  }



  private FastTrackForm buildForm() {
    var form = new FastTrackForm();
    form.setAvoidEnvironmentalDisaster(true);
    form.setEnvironmentalDisasterReason("Env Reason");
    form.setSavingBarrels(true);
    form.setSavingBarrelsReason("Barrels Reason");
    form.setProjectPlanning(true);
    form.setProjectPlanningReason("Planning reason");
    form.setHasOtherReason(true);
    form.setOtherReason("Other reason");
    return form;
  }
}