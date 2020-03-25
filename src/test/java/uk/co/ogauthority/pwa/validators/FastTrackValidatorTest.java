package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

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
    var result = ValidatorTestUtils.getFormValidationErrors(validator, new FastTrackForm());
    assertThat(result).extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue).containsExactlyInAnyOrder(
        Tuple.tuple("avoidEnvironmentalDisaster", Set.of("avoidEnvironmentalDisaster.noneSelected")),
        Tuple.tuple("savingBarrels", Set.of("savingBarrels.noneSelected")),
        Tuple.tuple("projectPlanning", Set.of("projectPlanning.noneSelected")),
        Tuple.tuple("hasOtherReason", Set.of("hasOtherReason.noneSelected"))
    );
  }

  @Test
  public void validate_SectionInformationEmpty() {
    var form = buildForm();
    form.setEnvironmentalDisasterReason("");
    form.setSavingBarrelsReason("");
    form.setProjectPlanningReason(null);
    form.setOtherReason(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue).containsExactlyInAnyOrder(
        Tuple.tuple("environmentalDisasterReason", Set.of("environmentalDisasterReason.empty")),
        Tuple.tuple("savingBarrelsReason", Set.of("savingBarrelsReason.empty")),
        Tuple.tuple("projectPlanningReason", Set.of("projectPlanningReason.empty")),
        Tuple.tuple("otherReason", Set.of("otherReason.empty"))
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