package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

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
  private static final PipelineId PIPELINE_ID = new PipelineId(1);
  private static Instant LATEST_TIME;
  private static Instant EARLIER_TIME;
  private static Instant EARLIEST_TIME;


  @Before
  public void setUp() throws Exception {
    pwaHuooHistoryViewService = new PwaHuooHistoryViewService(huooSummaryService,
        pwaConsentOrganisationRoleService, pipelineDetailMigrationHuooDataService, pwaConsentService, pipelineService,
        pipelineDetailService);

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);

    var today = LocalDate.now().atStartOfDay();
    LATEST_TIME = today.plusHours(13).atZone(ZoneId.systemDefault()).toInstant();
    EARLIER_TIME = today.plusHours(5).atZone(ZoneId.systemDefault()).toInstant();
    EARLIEST_TIME = today.minusDays(10).atZone(ZoneId.systemDefault()).toInstant();
  }


  @Test
  public void getDiffedHuooSummaryAtTimeOfConsentAndPipeline_diffedSummaryCreated_verifyServiceInteractions() {

    var pipelineId = new PipelineId(1);
    var pipeline = new Pipeline();

    var allConsents = new ArrayList<>(Arrays.asList(
        PwaConsentTestUtil.createPwaConsent(3, "19/W/06", LATEST_TIME, 2),
        PwaConsentTestUtil.createPwaConsent(2, "19/W/05", EARLIER_TIME, 1),
        PwaConsentTestUtil.createPwaConsent(1, "19/W/04", EARLIEST_TIME, 0)));
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(allConsents);

    var selectedConsent = allConsents.get(1);
    selectedConsent.setMasterPwa(masterPwa);
    when(pwaConsentService.getConsentById(selectedConsent.getId())).thenReturn(selectedConsent);

    when(pipelineService.getPipelineFromId(pipelineId)).thenReturn(pipeline);
    var orgRoleSummary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(Set.of());
    when(pwaConsentOrganisationRoleService.getOrganisationRoleSummaryForConsentsAndPipeline(
        List.of(selectedConsent, allConsents.get(2)), pipeline))
        .thenReturn(orgRoleSummary);

    var allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
        List.of(), List.of(), List.of(), List.of());
    when(pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(masterPwa, orgRoleSummary)).thenReturn(allOrgRolePipelineGroupsView);

    pwaHuooHistoryViewService.getDiffedHuooSummaryAtTimeOfConsentAndPipeline(selectedConsent.getId(), masterPwa, pipelineId);
    verify(huooSummaryService, times(1)).getDiffedViewUsingSummaryViews(
        allOrgRolePipelineGroupsView, allOrgRolePipelineGroupsView, HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);
  }

  @Test
  public void getAllConsentsOnOrAfterFirstConsentOfPipeline_latestConsentsUpToAndIncludingFirstConsentOfPipelineReturned() {

    var allConsents = new ArrayList<>(Arrays.asList(
        PwaConsentTestUtil.createPwaConsent(3, "19/W/06", LATEST_TIME, 2),
        PwaConsentTestUtil.createPwaConsent(2, "19/W/05", EARLIER_TIME, 1),
        PwaConsentTestUtil.createPwaConsent(1, "19/W/04", EARLIEST_TIME, 0)));
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(allConsents);

    var pipelineDetailFirstVersion = PipelineDetailTestUtil.createPipelineDetail(1, PIPELINE_ID, Instant.now(), allConsents.get(1));
    when(pipelineDetailService.getFirstConsentedPipelineDetail(PIPELINE_ID)).thenReturn(pipelineDetailFirstVersion);

    var consents = pwaHuooHistoryViewService.getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline(masterPwa, PIPELINE_ID);

    var expectedConsents = List.of(allConsents.get(0), allConsents.get(1));
    assertThat(consents).containsAll(expectedConsents);
  }

  @Test
  public void getAllConsentsOnOrAfterFirstConsentOfPipeline_migratedConsentsExcludedExceptForLatest() {

    var allConsents = new ArrayList<>(Arrays.asList(
        PwaConsentTestUtil.createPwaConsent(5, "19/W/05", LATEST_TIME, 4),
        PwaConsentTestUtil.createPwaConsent(4, "19/W/04", EARLIER_TIME, 3),
        PwaConsentTestUtil.createPwaConsent(3, "19/W/03", EARLIER_TIME.minus(10, ChronoUnit.MINUTES), 2),
        PwaConsentTestUtil.createMigratedPwaConsent(2, "19/W/02", EARLIER_TIME.minus(20, ChronoUnit.MINUTES), 1),
        PwaConsentTestUtil.createMigratedPwaConsent(1, "19/W/01", EARLIEST_TIME, 0)));
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(allConsents);

    var pipelineDetailFirstVersion = PipelineDetailTestUtil.createPipelineDetail(1, PIPELINE_ID, Instant.now(), allConsents.get(allConsents.size()-1));
    when(pipelineDetailService.getFirstConsentedPipelineDetail(PIPELINE_ID)).thenReturn(pipelineDetailFirstVersion);

    var consents = pwaHuooHistoryViewService.getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline(masterPwa, PIPELINE_ID);

    var expectedConsents = allConsents.stream().filter(consent -> !consent.isMigratedFlag()).collect(Collectors.toList());
    assertThat(consents).containsAll(expectedConsents);
  }

  @Test
  public void getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline_consentsUnOrdered_returnedConsentsAreSorted_consentsAfterPipeStartReturned() {

    var allConsents = new ArrayList<>(Arrays.asList(
        PwaConsentTestUtil.createPwaConsent(1, "1/W/1", EARLIEST_TIME),
        PwaConsentTestUtil.createPwaConsent(2, "1/W/1", EARLIEST_TIME, 0),
        PwaConsentTestUtil.createPwaConsent(3, "1/V/1", EARLIEST_TIME, 1),
        PwaConsentTestUtil.createPwaConsent(4, "2/V/1", EARLIEST_TIME, 2),
        PwaConsentTestUtil.createPwaConsent(5, "3/D/1", LATEST_TIME),
        PwaConsentTestUtil.createPwaConsent(6, "4/V/1", LATEST_TIME, 3)
    ));
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(allConsents);

    var pipelineDetailFirstVersion = PipelineDetailTestUtil.createPipelineDetail(1, PIPELINE_ID, Instant.now(), allConsents.get(0));
    when(pipelineDetailService.getFirstConsentedPipelineDetail(PIPELINE_ID)).thenReturn(pipelineDetailFirstVersion);

    var consents = pwaHuooHistoryViewService.getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline(masterPwa, PIPELINE_ID);

    var expectedConsentIds = consents.stream().map(PwaConsent::getId).collect(Collectors.toList());

    // only consents consented after the start of the pipeline are returned, 1-4 were consented before the pipeline was started
    assertThat(expectedConsentIds).containsExactly(6, 5);

  }

  @Test
  public void getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline_firstPipeDetailConsentDifferent_noError() {

    var allConsents = new ArrayList<>(Arrays.asList(
        PwaConsentTestUtil.createPwaConsent(1, "1/W/1", EARLIEST_TIME),
        PwaConsentTestUtil.createPwaConsent(2, "3/D/1", LATEST_TIME)
    ));
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(allConsents);

    var pipelineDetailFirstVersion = PipelineDetailTestUtil.createPipelineDetail(1, PIPELINE_ID, Instant.now(), new PwaConsent());
    when(pipelineDetailService.getFirstConsentedPipelineDetail(PIPELINE_ID)).thenReturn(pipelineDetailFirstVersion);

    var expectedConsentIds = pwaHuooHistoryViewService.getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline(masterPwa, PIPELINE_ID)
        .stream()
        .map(PwaConsent::getId)
        .collect(Collectors.toList());

    assertThat(expectedConsentIds).containsExactly(2);

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