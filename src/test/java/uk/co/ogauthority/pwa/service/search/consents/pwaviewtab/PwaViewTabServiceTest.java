package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
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

  @Mock
  private MasterPwaService masterPwaService;

  private PwaViewTabService pwaViewTabService;

  private PwaContext pwaContext;

  private final String PIPELINE_REF_ID1 = "PLU001";
  private final String PIPELINE_REF_ID2 = "PL002";
  private final String PIPELINE_REF_ID3 = "PL003";

  private final Instant clockTime = Instant.now();
  private final Clock clock = Clock.fixed(Instant.from(clockTime), ZoneId.systemDefault());

  @Before
  public void setUp() throws Exception {

    pwaViewTabService = new PwaViewTabService(
        pipelineDetailService,
        pwaConsentDtoRepository,
        asBuiltViewerService,
        clock,
        masterPwaService);

    pwaContext = PwaContextTestUtil.createPwaContext();

  }


  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineViews_orderedByPipelineNumber() {

    var unOrderedPipelineOverviews = List.of(
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID2, PipelineStatus.DELETED),
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID3, PipelineStatus.PENDING),
        PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID1, PipelineStatus.IN_SERVICE));

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(pwaContext.getMasterPwa(), pipelineStatusFilter, clock.instant()))
        .thenReturn(unOrderedPipelineOverviews);
    when(asBuiltViewerService.getOverviewsWithAsBuiltStatus(unOrderedPipelineOverviews)).thenReturn(unOrderedPipelineOverviews);

    var detail1 = new PipelineDetail();
    detail1.setId(1);
    var pipe1 = new Pipeline();
    pipe1.setId(unOrderedPipelineOverviews.get(0).getPipelineId());
    detail1.setPipeline(pipe1);
    var detail2 = new PipelineDetail();
    detail2.setId(2);
    var pipe2 = new Pipeline();
    pipe2.setId(unOrderedPipelineOverviews.get(1).getPipelineId());
    detail2.setPipeline(pipe2);
    var detail3 = new PipelineDetail();
    detail3.setId(3);
    var pipe3 = new Pipeline();
    pipe3.setId(unOrderedPipelineOverviews.get(2).getPipelineId());
    detail3.setPipeline(pipe3);
    when(pipelineDetailService.getLatestPipelineDetailsForIds(List.of(pipe1.getId(), pipe2.getId(), pipe3.getId())))
        .thenReturn(List.of(detail1, detail2, detail3));

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    assertThat(actualPwaPipelineViews).containsExactly(
        new PwaPipelineView(unOrderedPipelineOverviews.get(2), null, null, null, null),
        new PwaPipelineView(unOrderedPipelineOverviews.get(0), null, null, null, null),
        new PwaPipelineView(unOrderedPipelineOverviews.get(1), null, null, null, null));

  }

  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineViews_containsAsBuiltStatus_andTransferredFrom() {

    var overview = PipelineDetailTestUtil.createPipelineOverviewWithAsBuiltStatus(PIPELINE_REF_ID1,
        PipelineStatus.IN_SERVICE, AsBuiltNotificationStatus.PER_CONSENT);

    var unOrderedPipelineOverviews = List.of(overview);

    var pipelineOverviewWithAsBuiltStatus = PipelineDetailTestUtil
        .createPipelineOverviewWithAsBuiltStatus(overview, overview.getAsBuiltNotificationStatus());

    var detail = new PipelineDetail();
    var pipe = new Pipeline();
    pipe.setId(overview.getPipelineId());
    var pwa = new MasterPwa();
    pipe.setMasterPwa(pwa);

    var transferredPipe = new Pipeline();
    var transferredPwa = new MasterPwa();
    transferredPwa.setId(1);
    transferredPipe.setId(3);
    transferredPipe.setMasterPwa(transferredPwa);
    var transferredPwaDetail = new MasterPwaDetail();
    transferredPwaDetail.setMasterPwa(transferredPwa);
    transferredPwaDetail.setReference("1/W/31");
    detail.setPipeline(pipe);
    detail.setTransferredFromPipeline(transferredPipe);

    when(pipelineDetailService.getLatestPipelineDetailsForIds(any())).thenReturn(List.of(detail));
    when(masterPwaService.findAllCurrentDetailsIn(List.of(transferredPwa))).thenReturn(List.of(transferredPwaDetail));

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(pwaContext.getMasterPwa(), pipelineStatusFilter, clock.instant()))
        .thenReturn(unOrderedPipelineOverviews);
    when(asBuiltViewerService.getOverviewsWithAsBuiltStatus(unOrderedPipelineOverviews))
        .thenReturn(List.of(pipelineOverviewWithAsBuiltStatus));

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    var transferredPwaUrl = ReverseRouter.route(on(PwaViewController.class).renderViewPwa(transferredPwa.getId(), PwaViewTab.PIPELINES, null, null, false));
    assertThat(actualPwaPipelineViews).containsExactly(new PwaPipelineView(pipelineOverviewWithAsBuiltStatus, transferredPwaDetail.getReference(), transferredPwaUrl, null, null));
  }

  @Test
  public void getTabContentModelMap_consentTab_modelMapContainsConsentHistoryViews_orderedByConsentDateLatestFirst() {

    var today = LocalDate.now();
    var unOrderedConsentAppDtos = List.of(
        PwaViewTabTestUtil.createMigratedConsentApplicationDto(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
        PwaViewTabTestUtil.createMigratedConsentApplicationDto(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    when(pwaConsentDtoRepository.getConsentAndApplicationDtos(pwaContext.getMasterPwa())).thenReturn(unOrderedConsentAppDtos);

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.CONSENT_HISTORY);
    var pwaConsentHistoryViews = (List<PwaConsentApplicationDto>) modelMap.get("pwaConsentHistoryViews");
    assertThat(pwaConsentHistoryViews).containsExactly(
        unOrderedConsentAppDtos.get(1), unOrderedConsentAppDtos.get(0));
  }

  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineView_withTransferTo() {
    var unOrderedPipelineOverviews = List.of(PipelineDetailTestUtil.createPipelineOverview(PIPELINE_REF_ID1, PipelineStatus.IN_SERVICE));

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(pwaContext.getMasterPwa(), pipelineStatusFilter, clock.instant()))
        .thenReturn(unOrderedPipelineOverviews);
    when(asBuiltViewerService.getOverviewsWithAsBuiltStatus(unOrderedPipelineOverviews)).thenReturn(unOrderedPipelineOverviews);

    var detail = new PipelineDetail();
    var pipe = new Pipeline();
    pipe.setId(unOrderedPipelineOverviews.get(0).getPipelineId());
    var transferredPipe = new Pipeline();
    var transferredPwa = new MasterPwa();
    transferredPwa.setId(2);
    transferredPipe.setMasterPwa(transferredPwa);
    var transferredPwaDetail = new MasterPwaDetail();
    transferredPwaDetail.setMasterPwa(transferredPwa);
    transferredPwaDetail.setReference("3/W/12");
    detail.setPipeline(pipe);
    detail.setTransferredToPipeline(transferredPipe);

    when(pipelineDetailService.getLatestPipelineDetailsForIds(List.of(unOrderedPipelineOverviews.get(0).getPipelineId()))).thenReturn(List.of(detail));
    when(masterPwaService.findAllCurrentDetailsIn(List.of(transferredPwa))).thenReturn(List.of(transferredPwaDetail));

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    var transferredPwaUrl = ReverseRouter.route(on(PwaViewController.class).renderViewPwa(transferredPwa.getId(), PwaViewTab.PIPELINES, null, null, false));
    assertThat(actualPwaPipelineViews).containsExactly(new PwaPipelineView(unOrderedPipelineOverviews.get(0), null, null,
        transferredPwaDetail.getReference(), transferredPwaUrl));
  }

  @Test
  public void verifyConsentDocumentDownloadable_allOk() throws IllegalAccessException {

    var docgenRun = new DocgenRun();
    docgenRun.setId(2L);
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setDocGenType(DocGenType.FULL);

    var pwaConsent = new PwaConsent();
    pwaConsent.setId(3);
    pwaConsent.setDocgenRunId(docgenRun.getId());

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), docgenRun);
    FieldUtils.writeField(dto, "consentId", pwaConsent.getId(), true);
    var migratedDto = PwaViewTabTestUtil.createMigratedConsentApplicationDto(Instant.now().minus(5, ChronoUnit.HOURS));
    when(pwaConsentDtoRepository.getConsentAndApplicationDtos(any())).thenReturn(List.of(dto, migratedDto));

    pwaViewTabService.verifyConsentDocumentDownloadable(docgenRun, pwaConsent, PwaContextTestUtil.createPwaContext());

  }

  @Test(expected = AccessDeniedException.class)
  public void verifyConsentDocumentDownloadable_notFullRun_exception() throws IllegalAccessException {

    var docgenRun = new DocgenRun();
    docgenRun.setId(2L);
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setDocGenType(DocGenType.PREVIEW);

    var pwaConsent = new PwaConsent();
    pwaConsent.setDocgenRunId(docgenRun.getId());
    pwaConsent.setId(3);

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), docgenRun);
    FieldUtils.writeField(dto, "consentId", pwaConsent.getId(), true);
    var migratedDto = PwaViewTabTestUtil.createMigratedConsentApplicationDto(Instant.now().minus(5, ChronoUnit.HOURS));
    when(pwaConsentDtoRepository.getConsentAndApplicationDtos(any())).thenReturn(List.of(dto, migratedDto));

    pwaViewTabService.verifyConsentDocumentDownloadable(docgenRun, pwaConsent, PwaContextTestUtil.createPwaContext());

  }

  @Test(expected = AccessDeniedException.class)
  public void verifyConsentDocumentDownloadable_notLinkedToPwa_exception() {

    var docgenRun = new DocgenRun();
    docgenRun.setId(2L);
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setDocGenType(DocGenType.FULL);

    var pwaConsent = new PwaConsent();
    pwaConsent.setDocgenRunId(docgenRun.getId());

    var migratedDto = PwaViewTabTestUtil.createMigratedConsentApplicationDto(Instant.now().minus(5, ChronoUnit.HOURS));
    when(pwaConsentDtoRepository.getConsentAndApplicationDtos(any())).thenReturn(List.of(migratedDto));

    pwaViewTabService.verifyConsentDocumentDownloadable(docgenRun, pwaConsent, PwaContextTestUtil.createPwaContext());

  }

}