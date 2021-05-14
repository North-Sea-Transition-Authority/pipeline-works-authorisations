package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.PwaHuooHistoryItemType.PIPELINE_DETAIL_MIGRATED_HUOO;
import static uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.PwaHuooHistoryItemType.PWA_CONSENT;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailMigrationHuooTestUtil;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class ViewablePipelineHuooVersionServiceTest {


  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService;

  @Mock
  private PwaHuooHistoryViewService pwaHuooHistoryViewService;

  private ViewablePipelineHuooVersionService viewablePipelineHuooVersionService;

  private MasterPwa masterPwa;

  private static Instant TODAY;
  private static Instant YESTERDAY;

  private static final Integer HUOO_VERSION_ENTITY_ID = 10;
  private static final PipelineId PIPELINE_ID = new PipelineId(1);
  private static final Integer PIPELINE_DETAIL_ID1 = 1;
  private static final Integer PIPELINE_DETAIL_ID2 = 2;
  private static final Integer PIPELINE_DETAIL_ID3 = 3;
  private static final String  CONSENT_REFERENCE = "4/V/21";


  @Before
  public void setUp() throws Exception {
    viewablePipelineHuooVersionService = new ViewablePipelineHuooVersionService(pwaConsentService,
        pipelineDetailMigrationHuooDataService, pipelineDetailService, pwaHuooHistoryViewService);

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);

    var todayDate = LocalDate.now().atStartOfDay();
    TODAY = todayDate.plusHours(1).atZone(ZoneId.systemDefault()).toInstant();
    YESTERDAY = todayDate.minusHours(1).atZone(ZoneId.systemDefault()).toInstant();
  }


  @Test
  public void getHuooHistorySearchSelectorItems_onlyPipelineDetailsWithMigratedHuoosAreUsed() {

    var pipelineDetailWithNoMigration = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY);
    var pipelineDetailWithMigration2 = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID2, PIPELINE_ID, YESTERDAY);
    var pipelineDetailWithMigration3 = PipelineDetailTestUtil.createPipelineDetail(PIPELINE_DETAIL_ID3, PIPELINE_ID, YESTERDAY);

    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID))
        .thenReturn(List.of(pipelineDetailWithNoMigration, pipelineDetailWithMigration2, pipelineDetailWithMigration3));

    when(pipelineDetailMigrationHuooDataService.getPipelineDetailMigratedHuoos(
        List.of(pipelineDetailWithNoMigration, pipelineDetailWithMigration2, pipelineDetailWithMigration3)))
        .thenReturn(List.of(
            PipelineDetailMigrationHuooTestUtil.createMigrationHuooData(pipelineDetailWithMigration2),
            PipelineDetailMigrationHuooTestUtil.createMigrationHuooData(pipelineDetailWithMigration3)));

    var huooVersionSearchSelectorItems = viewablePipelineHuooVersionService.getHuooHistorySearchSelectorItems(masterPwa, PIPELINE_ID.asInt());

    assertThat(huooVersionSearchSelectorItems).containsOnlyKeys(
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailWithMigration2.getId(),
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailWithMigration3.getId()
    );
  }

  @Test
  public void getHuooHistorySearchSelectorItems_onlyHuooSelectorItemsWithVersionChangedOnSameDayHaveOrderTag_itemsAreOrderedLatestFirst() {

    var consentCreatedTodayAfternoon = PwaConsentTestUtil.createPwaConsent(1, "5/V/21", TODAY, 2);
    var consentCreatedTodayMorning = PwaConsentTestUtil.createPwaConsent(2, CONSENT_REFERENCE, TODAY, 1);

    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(
        List.of(consentCreatedTodayAfternoon, consentCreatedTodayMorning));

    var pipelineDetailCreatedYesterday = PipelineDetailTestUtil.createPipelineDetail(
        PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY, PwaConsentTestUtil.createPwaConsent(3, "44/V/12", YESTERDAY));
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(List.of(pipelineDetailCreatedYesterday));
    when(pipelineDetailMigrationHuooDataService.getPipelineDetailMigratedHuoos(List.of(pipelineDetailCreatedYesterday))).thenReturn(
        List.of(PipelineDetailMigrationHuooTestUtil.createMigrationHuooData(pipelineDetailCreatedYesterday)));

    var huooVersionSearchSelectorItems = viewablePipelineHuooVersionService.getHuooHistorySearchSelectorItems(masterPwa, PIPELINE_ID.asInt());

    assertThat(huooVersionSearchSelectorItems.keySet()).containsExactly(
        PWA_CONSENT.getItemPrefix() + consentCreatedTodayAfternoon.getId(),
        PWA_CONSENT.getItemPrefix() + consentCreatedTodayMorning.getId(),
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailCreatedYesterday.getId());

    var expectedOrderTagNumber = 2;
    assertThat(huooVersionSearchSelectorItems.get(PWA_CONSENT.getItemPrefix() + consentCreatedTodayAfternoon.getId()))
        .contains(String.format("%s (%s) - %s", DateUtils.formatDate(consentCreatedTodayAfternoon.getConsentInstant()),
            expectedOrderTagNumber, consentCreatedTodayAfternoon.getReference()));

    expectedOrderTagNumber = 1;
    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PWA_CONSENT.getItemPrefix() + consentCreatedTodayMorning.getId(),
        String.format("%s (%s) - %s", DateUtils.formatDate(consentCreatedTodayMorning.getConsentInstant()),
            expectedOrderTagNumber, consentCreatedTodayMorning.getReference()));

    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailCreatedYesterday.getId(),
        DateUtils.formatDate(pipelineDetailCreatedYesterday.getStartTimestamp()));

  }


  @Test
  public void getHuooHistorySearchSelectorItems_consentReferenceDisplayedWhenAvailable_onlyLatestHuooSelectorItemHasLatestVersionText() {

    var consentWithRef = PwaConsentTestUtil.createPwaConsent(2, CONSENT_REFERENCE, TODAY);
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(
        List.of(consentWithRef));

    var pipelineDetailWithoutRef = PipelineDetailTestUtil.createPipelineDetail(
        PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY, PwaConsentTestUtil.createPwaConsent(3, null, YESTERDAY));
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(List.of(pipelineDetailWithoutRef));
    when(pipelineDetailMigrationHuooDataService.getPipelineDetailMigratedHuoos(List.of(pipelineDetailWithoutRef))).thenReturn(
        List.of(PipelineDetailMigrationHuooTestUtil.createMigrationHuooData(pipelineDetailWithoutRef)));

    var huooVersionSearchSelectorItems = viewablePipelineHuooVersionService.getHuooHistorySearchSelectorItems(masterPwa, PIPELINE_ID.asInt());

    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PWA_CONSENT.getItemPrefix() + consentWithRef.getId(),
        String.format("Latest version (%s - %s)", DateUtils.formatDate(consentWithRef.getConsentInstant()), consentWithRef.getReference()));

    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailWithoutRef.getId(),
        DateUtils.formatDate(pipelineDetailWithoutRef.getStartTimestamp()));
  }


  @Test
  public void getHuooHistorySearchSelectorItems_consentAndPipelineDetailBothHaveRef_consentRefDisplayedForConsentItemsOnly() {

    var consentWithRef = PwaConsentTestUtil.createPwaConsent(2, CONSENT_REFERENCE, TODAY);
    when(pwaConsentService.getConsentsByMasterPwa(masterPwa)).thenReturn(
        List.of(consentWithRef));

    var pipelineDetailWithRef = PipelineDetailTestUtil.createPipelineDetail(
        PIPELINE_DETAIL_ID1, PIPELINE_ID, YESTERDAY, PwaConsentTestUtil.createPwaConsent(3, CONSENT_REFERENCE, YESTERDAY));
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(PIPELINE_ID)).thenReturn(List.of(pipelineDetailWithRef));
    when(pipelineDetailMigrationHuooDataService.getPipelineDetailMigratedHuoos(List.of(pipelineDetailWithRef))).thenReturn(
        List.of(PipelineDetailMigrationHuooTestUtil.createMigrationHuooData(pipelineDetailWithRef)));

    var huooVersionSearchSelectorItems = viewablePipelineHuooVersionService.getHuooHistorySearchSelectorItems(masterPwa, PIPELINE_ID.asInt());

    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PWA_CONSENT.getItemPrefix() + consentWithRef.getId(),
        String.format("Latest version (%s - %s)", DateUtils.formatDate(consentWithRef.getConsentInstant()), consentWithRef.getReference()));

    assertThat(huooVersionSearchSelectorItems).containsEntry(
        PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + pipelineDetailWithRef.getId(),
        DateUtils.formatDate(pipelineDetailWithRef.getStartTimestamp()));
  }



  @Test
  public void getDiffableOrgRolePipelineGroupsFromHuooVersionString_huooVersionIdIsForConsent() {

    var huooVersionId = PWA_CONSENT.getItemPrefix() + HUOO_VERSION_ENTITY_ID;
    viewablePipelineHuooVersionService.getDiffableOrgRolePipelineGroupsFromHuooVersionString(
        masterPwa, PIPELINE_ID, huooVersionId);

    verify(pwaHuooHistoryViewService).getDiffedHuooSummaryAtTimeOfConsentAndPipeline(
        HUOO_VERSION_ENTITY_ID, masterPwa, PIPELINE_ID);
  }

  @Test
  public void getDiffableOrgRolePipelineGroupsFromHuooVersionString_huooVersionIdIsForMigratedHuoos() {

    var huooVersionId = PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + HUOO_VERSION_ENTITY_ID;
    viewablePipelineHuooVersionService.getDiffableOrgRolePipelineGroupsFromHuooVersionString(
        masterPwa, PIPELINE_ID, huooVersionId);

    verify(pwaHuooHistoryViewService).getOrganisationRoleSummaryForHuooMigratedData(
        masterPwa ,HUOO_VERSION_ENTITY_ID);
  }





}