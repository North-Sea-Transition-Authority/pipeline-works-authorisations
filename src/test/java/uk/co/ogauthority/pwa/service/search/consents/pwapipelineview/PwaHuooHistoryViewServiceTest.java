package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaHuooHistoryViewServiceTest {

  @Mock
  private HuooSummaryService huooSummaryService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PwaHuooHistoryViewService pwaHuooHistoryViewService;

  private static PipelineId PIPELINE_ID = new PipelineId(1);
  private static int PIPELINE_DETAIL_ID1 = 1;
  private MasterPwa masterPwa;

  private static Instant TODAY_MORNING;


  @Before
  public void setUp() throws Exception {
    pwaHuooHistoryViewService = new PwaHuooHistoryViewService(huooSummaryService,
        pwaConsentOrganisationRoleService, pipelineDetailService);

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);

    var today = LocalDate.now().atStartOfDay();
    TODAY_MORNING = today.plusHours(5).atZone(ZoneId.systemDefault()).toInstant();
  }



  @Test
  public void getDiffedPipelineSummaryModel_diffedSummaryCreated_verifyServiceInteractions() {

    var pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, TODAY_MORNING);
    when(pipelineDetailService.getLatestByPipelineId(PIPELINE_ID.asInt())).thenReturn(pipelineDetail);

    var orgRoleSummary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(Set.of());
    when(pwaConsentOrganisationRoleService.getOrganisationRoleSummary(pipelineDetail)).thenReturn(orgRoleSummary);

    var allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
        List.of(), List.of(), List.of(), List.of());
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(masterPwa, orgRoleSummary)).thenReturn(allOrgRolePipelineGroupsView);

    pwaHuooHistoryViewService.getDiffedHuooSummaryModel(PIPELINE_DETAIL_ID1, PIPELINE_ID.asInt(), masterPwa);
    verify(huooSummaryService, times(1)).getDiffedViewUsingSummaryViews(allOrgRolePipelineGroupsView, allOrgRolePipelineGroupsView);
  }



}