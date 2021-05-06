package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaHistoryViewTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaPipelineViewTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaHuooHistoryViewServiceTest {

  @Mock
  private HuooSummaryService huooSummaryService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private PipelineService pipelineService;

  private PwaHuooHistoryViewService pwaHuooHistoryViewService;

  private static int CONSENT_ID = 1;
  private MasterPwa masterPwa;

  private static Instant TODAY;
  private static Instant YESTERDAY;


  @Before
  public void setUp() throws Exception {
    pwaHuooHistoryViewService = new PwaHuooHistoryViewService(huooSummaryService,
        pwaConsentOrganisationRoleService, pwaConsentService, pipelineService);

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);

    var todayDate = LocalDate.now().atStartOfDay();
    TODAY = todayDate.plusHours(1).atZone(ZoneId.systemDefault()).toInstant();
    YESTERDAY = todayDate.minusHours(1).atZone(ZoneId.systemDefault()).toInstant();
  }




  @Test
  public void getConsentHistorySearchSelectorItems_onlyConsentsChangedOnSameDayHaveOrderTag_itemsAreOrderedLatestFirst() {

    var consentCreatedTodayAfternoon = PwaHistoryViewTestUtil.createPwaConsent(1, "5/V/21", TODAY, 2);
    var consentCreatedTodayMorning = PwaHistoryViewTestUtil.createPwaConsent(2, "4/V/21", TODAY, 1);
    var consentCreatedYesterday = PwaHistoryViewTestUtil.createPwaConsent(3, "44/V/12", YESTERDAY);

    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(
        List.of(consentCreatedYesterday, consentCreatedTodayAfternoon, consentCreatedTodayMorning));

    var consentVersionSearchSelectorItems = pwaHuooHistoryViewService.getConsentHistorySearchSelectorItems(masterPwa);

    assertThat(consentVersionSearchSelectorItems.keySet()).containsExactly(
        String.valueOf(consentCreatedTodayAfternoon.getId()), String.valueOf(consentCreatedTodayMorning.getId()), String.valueOf(consentCreatedYesterday.getId())
    );

    var expectedOrderTagNumber = 2;
    assertThat(consentVersionSearchSelectorItems.get(String.valueOf(consentCreatedTodayAfternoon.getId()))).contains(
        String.format("%s (%s) - %s", DateUtils.formatDate(consentCreatedTodayAfternoon.getConsentInstant()),
            expectedOrderTagNumber, consentCreatedTodayAfternoon.getReference()));

    expectedOrderTagNumber = 1;
    assertThat(consentVersionSearchSelectorItems.get(String.valueOf(consentCreatedTodayMorning.getId()))).isEqualTo(
        String.format("%s (%s) - %s", DateUtils.formatDate(consentCreatedTodayMorning.getConsentInstant()),
            expectedOrderTagNumber, consentCreatedTodayMorning.getReference()));

    assertThat(consentVersionSearchSelectorItems.get(String.valueOf(consentCreatedYesterday.getId()))).isEqualTo(
        DateUtils.formatDate(consentCreatedYesterday.getConsentInstant()) + " - " + consentCreatedYesterday.getReference());

  }

  @Test
  public void getConsentHistorySearchSelectorItems_consentReferenceDisplayedWhenAvailable_onlyLatestConsentHasLatestVersionText() {

    var consentWithoutRef = PwaHistoryViewTestUtil.createPwaConsent(1, null, YESTERDAY);
    var consentWithRef = PwaHistoryViewTestUtil.createPwaConsent(2, "4/V/21", TODAY);

    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(
        List.of(consentWithRef, consentWithoutRef));

    var consentVersionSearchSelectorItems = pwaHuooHistoryViewService.getConsentHistorySearchSelectorItems(masterPwa);

    assertThat(consentVersionSearchSelectorItems.get(String.valueOf(consentWithRef.getId()))).isEqualTo(
        String.format("Latest version (%s - %s)", DateUtils.formatDate(consentWithRef.getConsentInstant()), consentWithRef.getReference()));

    assertThat(consentVersionSearchSelectorItems.get(String.valueOf(consentWithoutRef.getId()))).isEqualTo(
        DateUtils.formatDate(consentWithoutRef.getConsentInstant()));
  }



  @Test
  public void getDiffedPipelineSummaryModel_diffedSummaryCreated_verifyServiceInteractions() {

    var selectedConsent = PwaPipelineViewTestUtil.createPwaConsent("19/W/07");
    when(pwaConsentService.getConsentsById(CONSENT_ID)).thenReturn(selectedConsent);
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

    pwaHuooHistoryViewService.getDiffedHuooSummaryAtTimeOfConsentAndPipeline(CONSENT_ID, masterPwa, pipelineId);
    verify(huooSummaryService, times(1)).getDiffedViewUsingSummaryViews(allOrgRolePipelineGroupsView, allOrgRolePipelineGroupsView);
  }




}