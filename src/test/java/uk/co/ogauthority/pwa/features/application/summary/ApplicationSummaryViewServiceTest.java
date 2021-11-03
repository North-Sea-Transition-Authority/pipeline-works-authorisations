package uk.co.ogauthority.pwa.features.application.summary;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.CASE_OFFICER_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.DRAFT;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.UPDATE_REQUESTED;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryViewServiceTest {

  @Mock
  private ApplicationSummaryService applicationSummaryService;

  @Mock
  private TemplateRenderingService templateRenderingService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ApplicationSummaryViewService applicationSummaryViewService;

  private PwaApplicationDetail detail;
  private AuthenticatedUserAccount user;

  private static final int APP_ID = 100;
  private static final int APP_DETAIL_ID1 = 1;
  private static final int APP_DETAIL_ID2 = 2;
  private static final int APP_DETAIL_ID3 = 3;

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;
  private static final int VERSION_3 = 3;

  private static Instant TODAY_AFTERNOON;
  private static Instant TODAY_MORNING;
  private static Instant YESTERDAY;

  @Before
  public void setUp() {
    applicationSummaryViewService = new ApplicationSummaryViewService(applicationSummaryService, templateRenderingService, pwaApplicationDetailService);
    detail = new PwaApplicationDetail();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());

    var today = LocalDate.now().atStartOfDay();
    TODAY_AFTERNOON = today.plusHours(13).atZone(ZoneId.systemDefault()).toInstant();
    TODAY_MORNING = today.plusHours(5).atZone(ZoneId.systemDefault()).toInstant();
    YESTERDAY = today.minusDays(1).atZone(ZoneId.systemDefault()).toInstant();
  }

  @Test
  public void getApplicationSummaryView_usingDetail() {

    when(applicationSummaryService.summarise(detail)).thenReturn(List.of(
        new ApplicationSectionSummary("test", List.of(SidebarSectionLink.createAnchorLink("text", "#")), Map.of("test", "1")),
        new ApplicationSectionSummary("test2", List.of(SidebarSectionLink.createAnchorLink("text2", "#")), Map.of("test", "2"))
    ));

    when(templateRenderingService.render(any(), any(), anyBoolean())).thenReturn("FAKE");

    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(detail);

    assertThat(appSummaryView.getSummaryHtml()).isEqualTo("FAKEFAKE");
    assertThat(appSummaryView.getSidebarSectionLinks())
        .extracting(SidebarSectionLink::getDisplayText)
        .containsExactly("text", "text2");
  }


  @Test
  public void getApplicationSummaryViewForId_verifyServiceInteractions() {
    when(pwaApplicationDetailService.getDetailById(APP_DETAIL_ID3)).thenReturn(detail);
    applicationSummaryViewService.getApplicationSummaryViewForAppDetailId(APP_DETAIL_ID3);
    verify(pwaApplicationDetailService).getDetailById(APP_DETAIL_ID3);
    verify(applicationSummaryService).summarise(detail);
  }


  @Test
  public void getAppDetailVersionSearchSelectorItems_onlyDetailsUpdatedOnSameDayHaveOrderTag_itemsAreOrderedLatestFirst() {

    var appDetailSubmittedTodayAfternoon = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID3, VERSION_3, TODAY_AFTERNOON, CASE_OFFICER_REVIEW);
    var appDetailSubmittedTodayMorning = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID2, VERSION_2, TODAY_MORNING, CASE_OFFICER_REVIEW);
    var appDetailSubmittedYesterday = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID1, VERSION_1, YESTERDAY, CASE_OFFICER_REVIEW);
    var pwaApplication = appDetailSubmittedTodayAfternoon.getPwaApplication();

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(
        List.of(appDetailSubmittedYesterday, appDetailSubmittedTodayAfternoon, appDetailSubmittedTodayMorning));

    var visibleApplicationVersionOptionsForUser = applicationSummaryViewService
        .getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    var visibleApplicationVersionOptionsForUserEntries = visibleApplicationVersionOptionsForUser.getApplicationVersionOptions();

    assertThat(visibleApplicationVersionOptionsForUserEntries.keySet()).containsExactly(
        appDetailSubmittedTodayAfternoon.getId().toString(), appDetailSubmittedTodayMorning.getId().toString(), appDetailSubmittedYesterday.getId().toString()
    );

    var expectedOrderTagNumber = 2;
    assertThat(visibleApplicationVersionOptionsForUserEntries.get(appDetailSubmittedTodayAfternoon.getId().toString())).contains(
        String.format("%s (%s)", DateUtils.formatDate(appDetailSubmittedTodayAfternoon.getSubmittedTimestamp()), expectedOrderTagNumber));

    expectedOrderTagNumber = 1;
    assertThat(visibleApplicationVersionOptionsForUserEntries).containsEntry(appDetailSubmittedTodayMorning.getId().toString(),
        String.format("%s (%s)", DateUtils.formatDate(appDetailSubmittedTodayMorning.getSubmittedTimestamp()), expectedOrderTagNumber));

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsEntry(appDetailSubmittedYesterday.getId().toString(),
        DateUtils.formatDate(appDetailSubmittedYesterday.getSubmittedTimestamp()));

  }

  @Test
  public void getAppDetailVersionSearchSelectorItems_onlyLatestDetailVersionHasLatestVersionText() {

    var appDetailSubmittedYesterday = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID2, VERSION_2, YESTERDAY, CASE_OFFICER_REVIEW);
    var appDetailSubmittedTodayMorning = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID1, VERSION_1, TODAY_MORNING, CASE_OFFICER_REVIEW);
    var pwaApplication = appDetailSubmittedYesterday.getPwaApplication();

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(
        List.of(appDetailSubmittedTodayMorning, appDetailSubmittedYesterday));

    var visibleApplicationVersionOptionsForUser = applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    var visibleApplicationVersionOptionsForUserEntries = visibleApplicationVersionOptionsForUser.getApplicationVersionOptions();

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsEntry(appDetailSubmittedTodayMorning.getId().toString(),
        String.format("Latest version (%s)", DateUtils.formatDate(appDetailSubmittedTodayMorning.getSubmittedTimestamp())));

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsEntry(appDetailSubmittedYesterday.getId().toString(),
        DateUtils.formatDate(appDetailSubmittedYesterday.getSubmittedTimestamp()));

  }

  @Test
  public void getAppDetailVersionSearchSelectorItems_appHasUpdateRequestAndNonIndustryEditableVersions_updateRequestVersionNotIncludedInSelectorOptions() {

    var appDetailUpdateRequested = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID2, VERSION_2, TODAY_MORNING, UPDATE_REQUESTED);
    var appDetailNoUpdateRequested = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID1, VERSION_1, YESTERDAY, CASE_OFFICER_REVIEW);
    var pwaApplication = appDetailUpdateRequested.getPwaApplication();

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(
        List.of(appDetailNoUpdateRequested, appDetailUpdateRequested));

    var visibleApplicationVersionOptionsForUser =
        applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    var visibleApplicationVersionOptionsForUserEntries = visibleApplicationVersionOptionsForUser
        .getApplicationVersionOptions();

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsOnly(entry(
        appDetailNoUpdateRequested.getId().toString(),
        String.format("Latest version (%s)", DateUtils.formatDate(appDetailNoUpdateRequested.getSubmittedTimestamp()))
    ));
  }

  @Test
  public void getAppDetailVersionSearchSelectorItems_appHasDraftAndNonIndustryEditableVersions_draftVersionNotIncludedInSelectorOptions() {

    var appDetailDraft = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID2, VERSION_2, TODAY_MORNING, DRAFT);
    var appDetailNoUpdateRequested = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID1, VERSION_1, YESTERDAY, CASE_OFFICER_REVIEW);
    var pwaApplication = appDetailDraft.getPwaApplication();

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(
        List.of(appDetailNoUpdateRequested, appDetailDraft));

    var visibleApplicationVersionOptionsForUser =
        applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    var visibleApplicationVersionOptionsForUserEntries = visibleApplicationVersionOptionsForUser
        .getApplicationVersionOptions();

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsOnly(entry(
        appDetailNoUpdateRequested.getId().toString(),
        String.format("Latest version (%s)", DateUtils.formatDate(appDetailNoUpdateRequested.getSubmittedTimestamp()))
    ));
  }

  @Test
  public void getAppDetailVersionSearchSelectorItems_userIsConsultee_versionsAreSatisfactoryAndNonSatisfactory_onlySatisfactoryVersionIncludedInOptions() {

    var appDetailSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID2, VERSION_2, TODAY_MORNING, CASE_OFFICER_REVIEW);
    appDetailSatisfactory.setConfirmedSatisfactoryTimestamp(Instant.now());
    var appDetailNonSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_ID, APP_DETAIL_ID1, VERSION_1, YESTERDAY, CASE_OFFICER_REVIEW);
    var pwaApplication = appDetailSatisfactory.getPwaApplication();

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(
        List.of(appDetailNonSatisfactory, appDetailSatisfactory));

    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of(PwaUserPrivilege.PWA_CONSULTEE));
    var visibleApplicationVersionOptionsForUser =
        applicationSummaryViewService.getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    var visibleApplicationVersionOptionsForUserEntries = visibleApplicationVersionOptionsForUser
        .getApplicationVersionOptions();

    assertThat(visibleApplicationVersionOptionsForUserEntries).containsOnly(entry(
        appDetailSatisfactory.getId().toString(),
        String.format("Latest version (%s)", DateUtils.formatDate(appDetailSatisfactory.getSubmittedTimestamp()))
    ));
  }


}
