package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaPipelineViewTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaHuooHistoryViewServiceTest {

  @Mock
  private HuooSummaryService huooSummaryService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private PipelineService pipelineService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PwaHuooHistoryViewService pwaHuooHistoryViewService;

  private MasterPwa masterPwa;


  @Before
  public void setUp() throws Exception {
    pwaHuooHistoryViewService = new PwaHuooHistoryViewService(huooSummaryService,
        pwaConsentOrganisationRoleService, pipelineDetailMigrationHuooDataService, pwaConsentService, pipelineService,
        pipelineDetailService);

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);
  }


  @Test
  public void getDiffedHuooSummaryAtTimeOfConsentAndPipeline_diffedSummaryCreated_verifyServiceInteractions() {

    var selectedConsent = PwaPipelineViewTestUtil.createPwaConsent("19/W/07");
    int consentId = 1;
    when(pwaConsentService.getConsentById(consentId)).thenReturn(selectedConsent);
    var previousConsents = List.of(PwaPipelineViewTestUtil.createPwaConsent("19/W/06"),
        PwaPipelineViewTestUtil.createPwaConsent("19/W/05"));
    when(pwaConsentService.getPwaConsentsWhereConsentInstantBefore(masterPwa, selectedConsent.getConsentInstant())).thenReturn(previousConsents);

    var pipelineId = new PipelineId(1);
    var pipeline = new Pipeline();
    when(pipelineService.getPipelineFromId(pipelineId)).thenReturn(pipeline);
    var orgRoleSummary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(Set.of());
    when(pwaConsentOrganisationRoleService.getOrganisationRoleSummaryForConsentsAndPipeline(
        List.of(selectedConsent, previousConsents.get(0), previousConsents.get(1)), pipeline))
        .thenReturn(orgRoleSummary);

    var allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
        List.of(), List.of(), List.of(), List.of());
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(masterPwa, orgRoleSummary)).thenReturn(allOrgRolePipelineGroupsView);

    pwaHuooHistoryViewService.getDiffedHuooSummaryAtTimeOfConsentAndPipeline(consentId, masterPwa, pipelineId);
    verify(huooSummaryService, times(1)).getDiffedViewUsingSummaryViews(
        allOrgRolePipelineGroupsView, allOrgRolePipelineGroupsView, HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);
  }


  @Test
  public void getOrganisationRoleSummaryForHuooMigratedData_diffedSummaryCreated_verifyServiceInteractions() {

    var selectedPipelineDetailId = 1;
    var selectedPipelineDetail = PipelineDetailTestUtil.createPipelineDetail(selectedPipelineDetailId, new PipelineId(1), Instant.now());
    when(pipelineDetailService.getByPipelineDetailId(selectedPipelineDetailId)).thenReturn(selectedPipelineDetail);

    var orgRoleSummary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(Set.of());
    when(pipelineDetailMigrationHuooDataService.getOrganisationRoleSummaryForHuooMigratedData(selectedPipelineDetail))
        .thenReturn(orgRoleSummary);

    var allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
        List.of(), List.of(), List.of(), List.of());
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(masterPwa, orgRoleSummary)).thenReturn(allOrgRolePipelineGroupsView);

    pwaHuooHistoryViewService.getOrganisationRoleSummaryForHuooMigratedData(masterPwa, selectedPipelineDetailId);
    verify(huooSummaryService, times(1)).getDiffedViewUsingSummaryViews(
        allOrgRolePipelineGroupsView, allOrgRolePipelineGroupsView, HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);
  }




}