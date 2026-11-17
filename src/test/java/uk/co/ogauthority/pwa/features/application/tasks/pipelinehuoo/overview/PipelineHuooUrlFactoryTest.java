package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller.ModifyPipelineHuooJourneyController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class PipelineHuooUrlFactoryTest {
  private final int APP_ID = 1;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final HuooRole ROLE = HuooRole.HOLDER;

  private PipelineHuooUrlFactory pipelineHuooUrlFactory;

  @BeforeEach
  void setup() {
    pipelineHuooUrlFactory = new PipelineHuooUrlFactory(APP_ID, APP_TYPE);
  }

  @Test
  void changeGroupPipelineOwnersUrl_expectedUrlProduced() {
    var pipelineId = new PipelineId(1);
    var orgUnitId = new OrganisationUnitId(2);
    var treaty = TreatyAgreement.ANY_TREATY_COUNTRY;
    var pipelinesAndOrgRoleGroupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        Set.of(pipelineId),
        Set.of(
            OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(orgUnitId),
            OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(treaty)
        ),
        List.of("PL1"),
        List.of("org", "treaty")
    );

    var expectedUrl = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroup(
        APP_TYPE,
        APP_ID,
        ROLE,
        pipelinesAndOrgRoleGroupView.getGroupKey(),
        null,
        null
    ));

    assertThat(pipelineHuooUrlFactory.changeGroupPipelineOwnersUrl(ROLE, pipelinesAndOrgRoleGroupView))
        .isEqualTo(expectedUrl)
        .doesNotContain("?");
  }

  @Test
  void assignUnassignedPipelineOwnersUrl_expectedUrlProduced() {
    var expectedUrl = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).assignUnassignedPipelines(
        APP_TYPE,
        APP_ID,
        ROLE,
        null
    ));

    assertThat(pipelineHuooUrlFactory.assignUnassignedPipelineOwnersUrl(ROLE))
        .isEqualTo(expectedUrl)
        .doesNotContain("?");
  }

}
