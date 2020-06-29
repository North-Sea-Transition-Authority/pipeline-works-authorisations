package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

@RunWith(MockitoJUnitRunner.class)
public class AddPipelineHuooJourneyDataTest {

  private AddPipelineHuooJourneyData journeyData;

  private PickHuooPipelinesForm form = new PickHuooPipelinesForm();

  @Before
  public void setup() {

    journeyData = new AddPipelineHuooJourneyData();

  }

  @Test
  public void updateJourneyPipelineData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));
    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyPipelineData(HuooRole.OWNER, Set.of("STRING2"));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.OWNER);
    assertThat(journeyData.getOrganisationUnitIds()).isEmpty();
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING2");

  }

  @Test
  public void updateJourneyPipelineData_whenHuooRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));
    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING2"));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1, 2));
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING2");

  }

  @Test
  public void updateJourneyOrganisationData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));
    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyOrganisationData(HuooRole.OWNER, Set.of(1));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.OWNER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1));
    assertThat(journeyData.getPickedPipelineIds()).isEmpty();

  }

  @Test
  public void updateJourneyOrganisationData_whenRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));
    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1));
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING1");

  }

  @Test
  public void updateFormWithPipelineJourneyData_whenRoleUnchanged() {

    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateFormWithPipelineJourneyData(HuooRole.HOLDER, form);

    assertThat(form.getPickedPipelineStrings()).isEqualTo(Set.of("STRING1"));

  }

  @Test
  public void updateFormWithPipelineJourneyData_whenRoleHasChanged() {

    journeyData.updateJourneyPipelineData(HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateFormWithPipelineJourneyData(HuooRole.OWNER, form);

    assertThat(form.getPickedPipelineStrings()).isEmpty();

  }

  @Test
  public void updateFormWithOrganisationRoleJourneyData_whenRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));

    journeyData.updateFormWithOrganisationRoleJourneyData(HuooRole.HOLDER, form);

    assertThat(form.getOrganisationUnitIds()).isEqualTo(Set.of(1, 2));

  }

  @Test
  public void updateFormWithOrganisationRoleJourneyData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(HuooRole.HOLDER, Set.of(1, 2));

    journeyData.updateFormWithOrganisationRoleJourneyData(HuooRole.OWNER, form);

    assertThat(form.getOrganisationUnitIds()).isEmpty();

  }
}