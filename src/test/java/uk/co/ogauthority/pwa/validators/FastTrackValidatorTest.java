package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class FastTrackValidatorTest {

  private FastTrackValidator validator;

  @Before
  public void setUp() {
    validator = new FastTrackValidator();
  }


  private Map<String, Set<String>> getFullValidationErrors(FastTrackForm form) {
    return ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
  }

  private Map<String, Set<String>> getPartialValidationErrors(FastTrackForm form) {
    return ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);
  }


  @Test
  public void validate_full_allFieldsEnteredCorrectly_valid() {
   assertThat(getFullValidationErrors(buildForm())).isEmpty();
  }

  @Test
  public void validate_full_emptyForm_invalid() {
    assertThat(getFullValidationErrors(new FastTrackForm())).containsOnly(
        entry("avoidEnvironmentalDisaster", Set.of("avoidEnvironmentalDisaster.noneSelected"))
    );
  }

  @Test
  public void validate_partial_emptyForm_valid() {
    assertThat(getPartialValidationErrors(new FastTrackForm())).isEmpty();
  }

  @Test
  public void validate_full_reasonsSelected_descriptionsEmpty_invalid() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason("");
    form.setSavingBarrelsReason("");
    form.setProjectPlanningReason(null);
    form.setOtherReason(null);
    var errors = getFullValidationErrors(form);
    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of("environmentalDisasterReason.required")),
        entry("savingBarrelsReason", Set.of("savingBarrelsReason.required")),
        entry("projectPlanningReason", Set.of("projectPlanningReason.required")),
        entry("otherReason", Set.of("otherReason.required"))
    );
  }

  @Test
  public void validate_partial_reasonsSelected_descriptionsEmpty_valid() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason("");
    form.setSavingBarrelsReason("");
    form.setProjectPlanningReason(null);
    form.setOtherReason(null);

    assertThat(getPartialValidationErrors(form)).isEmpty();
  }

  @Test
  public void validate_partial_reasonDescriptionFieldsCharLengthOverMax_invalid() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.over4000Chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.over4000Chars());
    form.setProjectPlanningReason(ValidatorTestUtils.over4000Chars());
    form.setOtherReason(ValidatorTestUtils.over4000Chars());

    var errors = getPartialValidationErrors(form);
    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("environmentalDisasterReason"))),
        entry("savingBarrelsReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("savingBarrelsReason"))),
        entry("projectPlanningReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("projectPlanningReason"))),
        entry("otherReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("otherReason")))
    );
  }

  @Test
  public void validate_full_reasonDescriptionFieldsCharLengthOverMax_invalid() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.over4000Chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.over4000Chars());
    form.setProjectPlanningReason(ValidatorTestUtils.over4000Chars());
    form.setOtherReason(ValidatorTestUtils.over4000Chars());

    var errors = getFullValidationErrors(form);
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