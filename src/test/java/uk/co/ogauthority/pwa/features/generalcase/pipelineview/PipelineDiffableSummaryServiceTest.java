package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentViewService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineDiffableSummaryServiceTest {

  private static final int PIPELINE_ID = 1;
  private static final String PAD_PIPELINE_NAME = "PAD_PIPELINE_NAME";

  private static final String PIPELINE_POINT_1 = "POINT_1";
  private static final String PIPELINE_POINT_2 = "POINT_2";
  private static final String PIPELINE_POINT_3 = "POINT_3";
  private static final String PIPELINE_POINT_4 = "POINT_4";
  private final MasterPwa FROM_MASTER_PWA = getMasterPwa();
  private final MasterPwa TO_MASTER_PWA = getMasterPwa();

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineOverview padPipelineOverview;

  @Mock
  private PipelineHeaderView pipelineHeaderView;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PipelineDetailIdentViewService pipelineDetailIdentViewService;

  @Mock
  private PadPipelineTransferService padPipelineTransferService;

  @Mock
  private MasterPwaService masterPwaService;

  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Mock
  private IdentView identStart;

  @Mock
  private IdentView identMid;

  @Mock
  private IdentView identEnd;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @BeforeEach
  void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(padPipelineOverview.getPipelineName()).thenReturn(PAD_PIPELINE_NAME);
    when(padPipelineOverview.getPipelineId()).thenReturn(PIPELINE_ID);
    when(padPipelineOverview.getPadPipelineId()).thenReturn(PIPELINE_ID);
    when(padPipelineOverview.getPipelineStatus()).thenReturn(PipelineStatus.IN_SERVICE);

    IdentViewTestUtil.setupSingleCoreIdentViewMock(identStart, PIPELINE_POINT_1, PIPELINE_POINT_2, 1);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identMid, PIPELINE_POINT_2, PIPELINE_POINT_3, 2);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(identEnd, PIPELINE_POINT_3, PIPELINE_POINT_4, 3);

    pipelineDiffableSummaryService = new PipelineDiffableSummaryService(
        padPipelineService,
        padPipelineIdentService,
        pipelineDetailIdentViewService,
        pipelineDetailService,
        padTechnicalDrawingService,
        padPipelineTransferService,
        masterPwaService);
  }


  @Test
  void getApplicationDetailPipelines_whenNoPipelines() {

    assertThat(pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail)).isEmpty();

  }

  @Test
  void getApplicationDetailPipelines_pipelineTransfer_mapsAsExpected() {
    var pipelineId = new PipelineId(PIPELINE_ID);
    var pipelineDetail = getPipelineDetail(pipelineId, BigDecimal.ONE, PipelineType.PRODUCTION_FLOWLINE);
    var time = Instant.now();

    var fromMasterPwaDetail = getMasterPwaDetail(FROM_MASTER_PWA, "1/W/23");

    var toPipeline = new Pipeline();
    toPipeline.setId(PIPELINE_ID);
    toPipeline.setMasterPwa(TO_MASTER_PWA);
    pipelineDetail.setTransferredToPipeline(toPipeline);

    var fromPipeline = new Pipeline();
    fromPipeline.setMasterPwa(FROM_MASTER_PWA);
    pipelineDetail.setTransferredFromPipeline(fromPipeline);

    var toApplication = new PwaApplication();
    toApplication.setMasterPwa(TO_MASTER_PWA);
    var toApplicationDetail = new PwaApplicationDetail();
    toApplicationDetail.setPwaApplication(toApplication);

    var fromApplication = new PwaApplication();
    fromApplication.setMasterPwa(FROM_MASTER_PWA);
    var fromApplicationDetail = new PwaApplicationDetail();
    fromApplicationDetail.setPwaApplication(fromApplication);


    var padPipelineTransfer = new PadPipelineTransfer();
    padPipelineTransfer.setDonorPipeline(fromPipeline);
    padPipelineTransfer.setDonorApplicationDetail(fromApplicationDetail);
    padPipelineTransfer.setRecipientApplicationDetail(toApplicationDetail);
    padPipelineTransfer.setRecipientPipeline(toPipeline);
    padPipelineTransfer.setCompatibleWithTarget(true);
    padPipelineTransfer.setLastIntelligentlyPigged(time);

    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(fromPipeline);

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));
    when(padPipelineTransferService.getPipelineToTransferMap(pwaApplicationDetail)).thenReturn(
        Map.of(toPipeline, padPipelineTransfer)
    );
    when(masterPwaService.findAllCurrentDetailsIn(any())).thenReturn(List.of(fromMasterPwaDetail));

    var summary = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail).get(0);
    assertThat(summary.getPipelineHeaderView().getTransferredFromRef()).isEqualTo("1/W/23");
    assertThat(summary.getPipelineHeaderView().getTransferredToRef()).isEqualTo(null);
    assertThat(summary.getPipelineHeaderView().getPipelineIntelligentlyPigged()).isEqualTo(DateUtils.formatDate(clock.instant()));
    assertThat(summary.getPipelineHeaderView().getPipelineCompatible()).isTrue();
  }

  @Test
  void getApplicationDetailPipelines_pipelineTransfer_whenNoIntelligentlyPiggedDate_thenMapsAsExpected() {
    var pipelineId = new PipelineId(PIPELINE_ID);
    var pipelineDetail = getPipelineDetail(pipelineId, BigDecimal.ONE, PipelineType.PRODUCTION_FLOWLINE);

    var fromMasterPwaDetail = getMasterPwaDetail(FROM_MASTER_PWA, "1/W/23");

    var toPipeline = new Pipeline();
    toPipeline.setId(PIPELINE_ID);
    toPipeline.setMasterPwa(TO_MASTER_PWA);
    pipelineDetail.setTransferredToPipeline(toPipeline);

    var fromPipeline = new Pipeline();
    fromPipeline.setMasterPwa(FROM_MASTER_PWA);
    pipelineDetail.setTransferredFromPipeline(fromPipeline);

    var toApplication = new PwaApplication();
    toApplication.setMasterPwa(TO_MASTER_PWA);
    var toApplicationDetail = new PwaApplicationDetail();
    toApplicationDetail.setPwaApplication(toApplication);

    var fromApplication = new PwaApplication();
    fromApplication.setMasterPwa(FROM_MASTER_PWA);
    var fromApplicationDetail = new PwaApplicationDetail();
    fromApplicationDetail.setPwaApplication(fromApplication);


    var padPipelineTransfer = new PadPipelineTransfer();
    padPipelineTransfer.setDonorPipeline(fromPipeline);
    padPipelineTransfer.setDonorApplicationDetail(fromApplicationDetail);
    padPipelineTransfer.setRecipientApplicationDetail(toApplicationDetail);
    padPipelineTransfer.setRecipientPipeline(toPipeline);
    padPipelineTransfer.setCompatibleWithTarget(true);
    padPipelineTransfer.setLastIntelligentlyPigged(null);

    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(fromPipeline);

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));
    when(padPipelineTransferService.getPipelineToTransferMap(pwaApplicationDetail)).thenReturn(
        Map.of(toPipeline, padPipelineTransfer)
    );
    when(masterPwaService.findAllCurrentDetailsIn(any())).thenReturn(List.of(fromMasterPwaDetail));

    var summary = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail).get(0);
    assertThat(summary.getPipelineHeaderView().getTransferredFromRef()).isEqualTo("1/W/23");
    assertThat(summary.getPipelineHeaderView().getTransferredToRef()).isEqualTo(null);
    assertThat(summary.getPipelineHeaderView().getPipelineIntelligentlyPigged()).isEqualTo(null);
    assertThat(summary.getPipelineHeaderView().getPipelineCompatible()).isTrue();
  }

  @Test
  void getApplicationDetailPipelines_whenOnePipeline_andZeroIdents() {
    var pipeline = new Pipeline();
    pipeline.setId(1);

    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));

    var summaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail);

    assertThat(summaryList).hasSize(1);
    var summary = summaryList.get(0);

    assertThat(summary.getPipelineId().asInt()).isEqualTo(PIPELINE_ID);
    assertThat(summary.getPipelineHeaderView().getPipelineName()).isEqualTo(PAD_PIPELINE_NAME);
    assertThat(summary.getIdentViews()).hasSize(0);
  }

  @Test
  void getApplicationDetailPipelines_whenOnePipeline_andMultipleIdents_thenMappedAsExpected() {
    var pipeline = new Pipeline();
    pipeline.setId(1);

    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);

    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineOverview));
    when(padPipelineIdentService.getIdentViewsFromOverview(padPipelineOverview))
        .thenReturn(List.of(identStart, identMid, identEnd));

    var summaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail);
    var summary = summaryList.get(0);

    assertThat(summary.getIdentViews()).hasSize(3);
    assertThat(summary.getIdentViews().get(0).getFromLocation()).isEqualTo(PIPELINE_POINT_1);
    assertThat(summary.getIdentViews().get(0).getToLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(0).getConnectedToPrevious()).isFalse();
    assertThat(summary.getIdentViews().get(0).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(1).getFromLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(1).getToLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(1).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(1).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(2).getFromLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(2).getToLocation()).isEqualTo(PIPELINE_POINT_4);
    assertThat(summary.getIdentViews().get(2).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(2).getConnectedToNext()).isFalse();

  }


  @Test
  void getConsentedPipelines_whenOnePipeline_andMultipleIdents_thenMappedAsExpected() {
    var pipelineId = new PipelineId(PIPELINE_ID);
    var pipelineDetail = getPipelineDetail(pipelineId, BigDecimal.ONE, PipelineType.PRODUCTION_FLOWLINE);

    Instant instant = Instant.now();

    var toMasterPwa = new MasterPwa(instant);
    toMasterPwa.setId(2);
    var toMasterPwaDetail = new MasterPwaDetail();
    toMasterPwaDetail.setMasterPwa(toMasterPwa);
    toMasterPwaDetail.setReference("2/W/23");

    var toPipeline = new Pipeline();
    toPipeline.setMasterPwa(toMasterPwa);
    pipelineDetail.setTransferredToPipeline(toPipeline);

    var pwaList = Set.of(toMasterPwa);
    var detailList = List.of(toMasterPwaDetail);
    when(masterPwaService.findAllCurrentDetailsIn(pwaList)).thenReturn(detailList);

    when(pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwaById(
        pwaApplicationDetail.getPwaApplication(),
        Set.of(pipelineId)
    )).thenReturn(List.of(pipelineDetail));

    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(pipelineId))
        .thenReturn(List.of(identStart, identMid, identEnd));

    var summaryList = pipelineDiffableSummaryService.getConsentedPipelines(pwaApplicationDetail.getPwaApplication(), Set.of(pipelineId));

    var summary = summaryList.get(0);

    assertThat(summary.getPipelineHeaderView().getTransferredToRef()).isEqualTo("2/W/23");
    assertThat(summary.getPipelineHeaderView().getTransferredFromRef()).isNull();

    assertThat(summary.getIdentViews()).hasSize(3);
    assertThat(summary.getIdentViews().get(0).getFromLocation()).isEqualTo(PIPELINE_POINT_1);
    assertThat(summary.getIdentViews().get(0).getToLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(0).getConnectedToPrevious()).isFalse();
    assertThat(summary.getIdentViews().get(0).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(1).getFromLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(1).getToLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(1).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(1).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(2).getFromLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(2).getToLocation()).isEqualTo(PIPELINE_POINT_4);
    assertThat(summary.getIdentViews().get(2).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(2).getConnectedToNext()).isFalse();

  }


  @Test
  void getConsentedPipeline_multipleIdents_mappedAsExpected() {
    var pipelineId = new PipelineId(PIPELINE_ID);
    var pipelineDetailId = PIPELINE_ID;
    var pipelineDetail = getPipelineDetail(pipelineId, BigDecimal.ONE, PipelineType.PRODUCTION_FLOWLINE);

    Instant instant = Instant.now();

    var fromMasterPwa = new MasterPwa(instant);
    fromMasterPwa.setId(1);
    var fromMasterPwaDetail = new MasterPwaDetail();
    fromMasterPwaDetail.setMasterPwa(fromMasterPwa);
    fromMasterPwaDetail.setReference("1/W/23");

    var fromPipeline = new Pipeline();
    fromPipeline.setMasterPwa(fromMasterPwa);

    pipelineDetail.setTransferredFromPipeline(fromPipeline);

    when(masterPwaService.getCurrentDetailOrThrow(fromMasterPwa)).thenReturn(fromMasterPwaDetail);
    when(pipelineDetailService.getByPipelineDetailId(pipelineDetailId)).thenReturn(pipelineDetail);

    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelineDetail(pipelineId, pipelineDetailId))
        .thenReturn(List.of(identStart, identMid, identEnd));

    var summary = pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(pipelineId.asInt());

    assertThat(summary.getPipelineHeaderView().getTransferredToRef()).isNull();
    assertThat(summary.getPipelineHeaderView().getTransferredFromRef()).isEqualTo("1/W/23");

    assertThat(summary.getIdentViews()).hasSize(3);
    assertThat(summary.getIdentViews().get(0).getFromLocation()).isEqualTo(PIPELINE_POINT_1);
    assertThat(summary.getIdentViews().get(0).getToLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(0).getConnectedToPrevious()).isFalse();
    assertThat(summary.getIdentViews().get(0).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(1).getFromLocation()).isEqualTo(PIPELINE_POINT_2);
    assertThat(summary.getIdentViews().get(1).getToLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(1).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(1).getConnectedToNext()).isTrue();

    assertThat(summary.getIdentViews().get(2).getFromLocation()).isEqualTo(PIPELINE_POINT_3);
    assertThat(summary.getIdentViews().get(2).getToLocation()).isEqualTo(PIPELINE_POINT_4);
    assertThat(summary.getIdentViews().get(2).getConnectedToPrevious()).isTrue();
    assertThat(summary.getIdentViews().get(2).getConnectedToNext()).isFalse();

  }

  @Test
  void getConsentedPipeline_pipelineTransfer_mapsAsExpected() {
    var pipelineId = new PipelineId(PIPELINE_ID);
    var pipelineDetailId = PIPELINE_ID;
    var pipelineDetail = getPipelineDetail(pipelineId, BigDecimal.ONE, PipelineType.PRODUCTION_FLOWLINE);

    var fromMasterPwaDetail = getMasterPwaDetail(FROM_MASTER_PWA, "1/W/23");
    var toMasterPwaDetail = getMasterPwaDetail(TO_MASTER_PWA, "2/W/23");

    var toPipeline = new Pipeline();
    toPipeline.setMasterPwa(TO_MASTER_PWA);
    pipelineDetail.setTransferredToPipeline(toPipeline);

    var fromPipeline = new Pipeline();
    fromPipeline.setMasterPwa(FROM_MASTER_PWA);
    pipelineDetail.setTransferredFromPipeline(fromPipeline);

    when(masterPwaService.getCurrentDetailOrThrow(FROM_MASTER_PWA)).thenReturn(fromMasterPwaDetail);
    when(masterPwaService.getCurrentDetailOrThrow(TO_MASTER_PWA)).thenReturn(toMasterPwaDetail);

    when(pipelineDetailService.getByPipelineDetailId(pipelineDetailId)).thenReturn(pipelineDetail);

    var summary = pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(pipelineId.asInt());
    assertThat(summary.getPipelineHeaderView().getTransferredFromRef()).isEqualTo(fromMasterPwaDetail.getReference());
    assertThat(summary.getPipelineHeaderView().getTransferredToRef()).isEqualTo(toMasterPwaDetail.getReference());
    assertThat(summary.getPipelineHeaderView().getPipelineIntelligentlyPigged()).isEqualTo(null);
    assertThat(summary.getPipelineHeaderView().getPipelineCompatible()).isEqualTo(null);
  }

  private PipelineDetail getPipelineDetail(PipelineId pipelineId,
                                           BigDecimal maxExternalDiameter,
                                           PipelineType pipelineType){


    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());
    var pipelineDetail = new PipelineDetail(pipeline);
    pipelineDetail.setPipelineType(pipelineType);
    pipelineDetail.setMaxExternalDiameter(maxExternalDiameter);
    pipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    return pipelineDetail;
  }

  private MasterPwa getMasterPwa() {
    var masterPwa = new MasterPwa(Instant.now());
    masterPwa.setId(ThreadLocalRandom.current().nextInt());
    return masterPwa;
  }

  private MasterPwaDetail getMasterPwaDetail(MasterPwa masterPwa, String reference) {
    var masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setMasterPwa(masterPwa);
    masterPwaDetail.setReference(reference);
    return masterPwaDetail;
  }
}
