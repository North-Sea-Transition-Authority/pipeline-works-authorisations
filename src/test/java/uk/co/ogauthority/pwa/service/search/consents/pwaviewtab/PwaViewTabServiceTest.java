package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil.PwaViewTabTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;
import uk.co.ogauthority.pwa.service.search.consents.testutil.PwaContextTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaViewTabServiceTest {

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PwaConsentDtoRepository pwaConsentDtoRepository;

  @Mock
  private AsBuiltViewerService asBuiltViewerService;

  private PwaViewTabService pwaViewTabService;

  private PwaContext pwaContext;


  private final String PIPELINE_REF_ID1 = "PLU001";
  private final String PIPELINE_REF_ID2 = "PL002";
  private final String PIPELINE_REF_ID3 = "PL003";



  @Before
  public void setUp() throws Exception {

    pwaViewTabService = new PwaViewTabService(pipelineDetailService, pwaConsentDtoRepository, asBuiltViewerService);

    pwaContext = PwaContextTestUtil.createPwaContext();

  }


  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineViews_orderedByPipelineNumber() {

    var unOrderedPipelineOverviews = List.of(
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID2, PipelineStatus.DELETED),
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID3, PipelineStatus.PENDING),
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID1, PipelineStatus.IN_SERVICE));

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatus(pwaContext.getMasterPwa(), pipelineStatusFilter))
        .thenReturn(unOrderedPipelineOverviews);

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    assertThat(actualPwaPipelineViews).containsExactly(
        new PwaPipelineView(unOrderedPipelineOverviews.get(2)),
        new PwaPipelineView(unOrderedPipelineOverviews.get(0)),
        new PwaPipelineView(unOrderedPipelineOverviews.get(1)));

  }

  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineViews_containsAsBuiltStatus() {

    var overview = PipelineDetailTestUtil.createPipelineOverviewWithAsBuiltStatus(PIPELINE_REF_ID1,
        PipelineStatus.IN_SERVICE, AsBuiltNotificationStatus.PER_CONSENT);

    var unOrderedPipelineOverviews = List.of(overview);

    var pipelineOverviewWithAsBuiltStatus = PipelineDetailTestUtil
        .createPipelineOverviewWithAsBuiltStatus(PIPELINE_REF_ID1, overview.getPipelineStatus(),
            overview.getAsBuiltNotificationStatus());

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatus(pwaContext.getMasterPwa(), pipelineStatusFilter))
        .thenReturn(unOrderedPipelineOverviews);
    when(asBuiltViewerService.getOverviewsWithAsBuiltStatus(unOrderedPipelineOverviews))
        .thenReturn(List.of(pipelineOverviewWithAsBuiltStatus));

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    assertThat(actualPwaPipelineViews).containsExactly(new PwaPipelineView(pipelineOverviewWithAsBuiltStatus));
  }

  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithPLChars_charsRemoved() {
    var pwaPipelineView = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID2));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("002");
  }

  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithPLUChars_charsRemoved() {
    var pwaPipelineView = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID1));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("001");
  }

  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithWhitespace_whitespaceRemoved() {
    var pwaPipelineView = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("  001"));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("001");
  }


  @Test
  public void getTabContentModelMap_consentTab_modelMapContainsConsentHistoryViews_orderedByConsentDateLatestFirst() {

    var today = LocalDate.now();
    var unOrderedConsentAppDtos = List.of(
        PwaViewTabTestUtil.createConsentApplicationDto(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
        PwaViewTabTestUtil.createConsentApplicationDto(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    when(pwaConsentDtoRepository.getConsentAndApplicationDtos(pwaContext.getMasterPwa())).thenReturn(unOrderedConsentAppDtos);

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.CONSENT_HISTORY);
    var pwaConsentHistoryViews = (List<PwaConsentApplicationDto>) modelMap.get("pwaConsentHistoryViews");
    assertThat(pwaConsentHistoryViews).containsExactly(
        unOrderedConsentAppDtos.get(1), unOrderedConsentAppDtos.get(0));
  }

}