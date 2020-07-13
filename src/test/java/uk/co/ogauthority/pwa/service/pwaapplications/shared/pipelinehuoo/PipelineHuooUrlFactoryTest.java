package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.ModifyPipelineHuooJourneyController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelineHuooUrlFactory;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooViewTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineHuooUrlFactoryTest {
  private final int APP_ID = 1;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final HuooRole ROLE = HuooRole.HOLDER;

  private PipelineHuooUrlFactory pipelineHuooUrlFactory;

  @Before
  public void setup() {
    pipelineHuooUrlFactory = new PipelineHuooUrlFactory(APP_ID, APP_TYPE);
  }

  @Test
  public void changeGroupPipelineOwnersUrl_expectedUrlProduced() {
    var pipelineId = new PipelineId(1);
    var orgUnitId = new OrganisationUnitId(2);
    var treaty = TreatyAgreement.NORWAY;
    var pipelinesAndOrgRoleGroupView = PipelineHuooViewTestUtil.createPipelineAndOrgRoleView(
        Set.of(pipelineId),
        Set.of(
            OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(orgUnitId),
            OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(treaty)
        ),
        List.of("PL1"),
        List.of("org", "treaty")
    );

    var expectedUrl = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        APP_TYPE,
        APP_ID,
        ROLE,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.ORGANISATION_SELECTION,
        Set.of(pipelineId.asInt()),
        Set.of(orgUnitId.asInt()),
        Set.of(treaty)
    ));

    assertThat(pipelineHuooUrlFactory.changeGroupPipelineOwnersUrl(ROLE, pipelinesAndOrgRoleGroupView))
        .isEqualTo(expectedUrl);

  }

  @Test
  public void assignUnassignedPipelineOwnersUrl_expectedUrlProduced() {
    var pipelineId = new PipelineId(1);

    var summaryVieWithUnassignedPipeline = PipelineHuooViewTestUtil.createUnassignedPipelinePipelineHuooRoleSummaryView(
        ROLE,
        Set.of(pipelineId)
    );
    var expectedUrl = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        APP_TYPE,
        APP_ID,
        ROLE,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
        Set.of(pipelineId.asInt()),
        Set.of(),
        Set.of()
    ));

    assertThat(pipelineHuooUrlFactory.assignUnassignedPipelineOwnersUrl(ROLE, summaryVieWithUnassignedPipeline))
        .isEqualTo(expectedUrl);

  }

}