package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Base64;
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
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller.ModifyPipelineHuooJourneyController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class PipelineHuooUrlFactoryTest {
  private final int APP_ID = 1;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final HuooRole ROLE = HuooRole.HOLDER;

  private PipelineId pipelineId;
  private PickableHuooPipelineId pickableHuooPipelineId;
  private String encodedPickableWholePipelineId;

  private PipelineHuooUrlFactory pipelineHuooUrlFactory;

  @BeforeEach
  void setup() {
    pipelineId = new PipelineId(1);
    pickableHuooPipelineId = PickableHuooPipelineId.from(PickableHuooPipelineType.createPickableString(pipelineId));
    encodedPickableWholePipelineId = Base64.getEncoder().encodeToString(pickableHuooPipelineId.asString().getBytes());

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

    var expectedUrl = ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
        APP_TYPE,
        APP_ID,
        ROLE,
        null,
        ModifyPipelineHuooJourneyController.JourneyPage.ORGANISATION_SELECTION,
        Set.of(encodedPickableWholePipelineId),
        Set.of(orgUnitId.asInt()),
        Set.of(treaty)
    ));

    assertThat(pipelineHuooUrlFactory.changeGroupPipelineOwnersUrl(ROLE, pipelinesAndOrgRoleGroupView))
        .isEqualTo(expectedUrl);

  }

  @Test
  void assignUnassignedPipelineOwnersUrl_expectedUrlProduced() {
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
        Set.of(encodedPickableWholePipelineId),
        Set.of(),
        Set.of()
    ));

    assertThat(pipelineHuooUrlFactory.assignUnassignedPipelineOwnersUrl(ROLE, summaryVieWithUnassignedPipeline))
        .isEqualTo(expectedUrl);

  }

}