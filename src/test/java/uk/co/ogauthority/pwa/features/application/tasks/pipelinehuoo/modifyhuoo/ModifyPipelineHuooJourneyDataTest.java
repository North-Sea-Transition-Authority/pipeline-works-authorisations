package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ModifyPipelineHuooJourneyDataTest {

  private static final PwaApplicationDetail APP_DETAIL = PwaApplicationTestUtil.createDefaultApplicationDetail(
      PwaApplicationType.INITIAL, 10, 11);

  private static final PwaApplicationDetail ALTERNATIVE_APP_DETAIL = PwaApplicationTestUtil.createDefaultApplicationDetail(
      PwaApplicationType.INITIAL, 20, 21);

  private ModifyPipelineHuooJourneyData journeyData;

  private PickHuooPipelinesForm form = new PickHuooPipelinesForm();

  @BeforeEach
  void setup() {

    journeyData = new ModifyPipelineHuooJourneyData();

  }


  @Test
  void updateJourneyPipelineData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2),
        Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.OWNER, Set.of("STRING2"));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.OWNER);
    assertThat(journeyData.getOrganisationUnitIds()).isEmpty();
    assertThat(journeyData.getTreatyAgreements()).isEmpty();
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING2");

  }

  @Test
  void updateJourneyPipelineData_whenRoleUnchanged_andAppDetailIdHasChanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2),
        Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyPipelineData(ALTERNATIVE_APP_DETAIL, HuooRole.HOLDER, Set.of("STRING2"));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEmpty();
    assertThat(journeyData.getTreatyAgreements()).isEmpty();
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING2");

  }

  @Test
  void updateJourneyPipelineData_whenHuooRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(
        APP_DETAIL,
        HuooRole.HOLDER,
        Set.of(1, 2),
        Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING2"));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1, 2));
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING2");
    assertThat(journeyData.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);

  }

  @Test
  void updateJourneyOrganisationData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.OWNER, Set.of(1), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.OWNER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1));
    assertThat(journeyData.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);
    assertThat(journeyData.getPickedPipelineIds()).isEmpty();

  }

  @Test
  void updateJourneyOrganisationData_whenRoleUnchanged_andPwaAppDetailHasChanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyOrganisationData(ALTERNATIVE_APP_DETAIL, HuooRole.HOLDER, Set.of(1), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1));
    assertThat(journeyData.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);
    assertThat(journeyData.getPickedPipelineIds()).isEmpty();

  }

  @Test
  void updateJourneyOrganisationData_whenRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));
    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    assertThat(journeyData.getJourneyRoleType()).isEqualTo(HuooRole.HOLDER);
    assertThat(journeyData.getOrganisationUnitIds()).isEqualTo(Set.of(1));
    assertThat(journeyData.getPickedPipelineIds()).containsExactly("STRING1");
    assertThat(journeyData.getTreatyAgreements()).containsExactly(TreatyAgreement.ANY_TREATY_COUNTRY);

  }

  @Test
  void updateFormWithPipelineJourneyData_whenRoleUnchanged() {

    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateFormWithPipelineJourneyData(APP_DETAIL, HuooRole.HOLDER, form);

    assertThat(form.getPickedPipelineStrings()).isEqualTo(Set.of("STRING1"));

  }

  @Test
  void updateFormWithPipelineJourneyData_whenRoleHasChanged() {

    journeyData.updateJourneyPipelineData(APP_DETAIL, HuooRole.HOLDER, Set.of("STRING1"));

    journeyData.updateFormWithPipelineJourneyData(APP_DETAIL, HuooRole.OWNER, form);

    assertThat(form.getPickedPipelineStrings()).isEmpty();

  }

  @Test
  void updateFormWithOrganisationRoleJourneyData_whenRoleUnchanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    journeyData.updateFormWithOrganisationRoleJourneyData(APP_DETAIL, HuooRole.HOLDER, form);

    assertThat(form.getOrganisationUnitIds()).isEqualTo(Set.of(1, 2));

  }

  @Test
  void updateFormWithOrganisationRoleJourneyData_whenRoleHasChanged() {

    journeyData.updateJourneyOrganisationData(APP_DETAIL, HuooRole.HOLDER, Set.of(1, 2), Set.of(TreatyAgreement.ANY_TREATY_COUNTRY));

    journeyData.updateFormWithOrganisationRoleJourneyData(APP_DETAIL, HuooRole.OWNER, form);

    assertThat(form.getOrganisationUnitIds()).isEmpty();

  }
}