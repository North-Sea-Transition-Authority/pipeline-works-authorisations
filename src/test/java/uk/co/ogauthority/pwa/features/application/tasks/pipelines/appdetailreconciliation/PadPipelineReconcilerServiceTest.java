package uk.co.ogauthority.pwa.features.application.tasks.pipelines.appdetailreconciliation;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineReconcilerServiceTest {

  private final int APP_ID = 1;
  private final int SOURCE_DETAIL_ID = 2;
  private final int RECONCILE_DETAIL_ID = 3;

  private final int PIPELINE_1_ID = 10;
  private final int PIPELINE_2_ID = 20;

  @Mock
  private PadPipelineService padPipelineService;

  private PadPipelineReconcilerService padPipelineReconcilerService;

  private PwaApplicationDetail sourceDetail;
  private PwaApplicationDetail reconcileDetail;

  private Pipeline pipeline1;
  private Pipeline pipeline2;

  private PadPipeline sourcePadPipeline1;
  private PadPipeline sourcePadPipeline2;

  private PadPipeline reconcilePadPipeline1;
  private PadPipeline reconcilePadPipeline2;

  @Before
  public void setup() {

    sourceDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        APP_ID,
        SOURCE_DETAIL_ID);
    reconcileDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        APP_ID,
        RECONCILE_DETAIL_ID);

    pipeline1 = new Pipeline(sourceDetail.getPwaApplication());
    pipeline1.setId(PIPELINE_1_ID);
    pipeline2 = new Pipeline(sourceDetail.getPwaApplication());
    pipeline2.setId(PIPELINE_2_ID);

    sourcePadPipeline1 = new PadPipeline(sourceDetail);
    sourcePadPipeline1.setPipeline(pipeline1);
    sourcePadPipeline2 = new PadPipeline(sourceDetail);
    sourcePadPipeline2.setPipeline(pipeline2);

    reconcilePadPipeline1 = new PadPipeline(reconcileDetail);
    reconcilePadPipeline1.setPipeline(pipeline1);
    reconcilePadPipeline2 = new PadPipeline(reconcileDetail);
    reconcilePadPipeline2.setPipeline(pipeline2);

    padPipelineReconcilerService = new PadPipelineReconcilerService(padPipelineService);

  }

  @Test
  public void reconcileApplicationDetailPadPipelines_whenPipelinesFound() {
    when(padPipelineService.getPipelines(sourceDetail))
        .thenReturn(List.of(sourcePadPipeline1, sourcePadPipeline2));
    when(padPipelineService.getPipelines(reconcileDetail))
        .thenReturn(List.of(reconcilePadPipeline1, reconcilePadPipeline2));

    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(
        sourceDetail,
        reconcileDetail);

    var reconciledPipeline1 = reconciledPadPipelines.findByPipelineIdOrError(pipeline1.getPipelineId());
    assertThat(reconciledPipeline1.getSourceDetailPadPipeline()).isEqualTo(sourcePadPipeline1);
    assertThat(reconciledPipeline1.getReconciledPadPipeline()).isEqualTo(reconcilePadPipeline1);

    var reconciledPipeline2 = reconciledPadPipelines.findByPipelineIdOrError(pipeline2.getPipelineId());
    assertThat(reconciledPipeline2.getSourceDetailPadPipeline()).isEqualTo(sourcePadPipeline2);
    assertThat(reconciledPipeline2.getReconciledPadPipeline()).isEqualTo(reconcilePadPipeline2);

  }

  @Test
  public void reconcileApplicationDetailPadPipelines_whenNoSourcePipelinesFound() {

    when(padPipelineService.getPipelines(reconcileDetail))
        .thenReturn(List.of(reconcilePadPipeline1, reconcilePadPipeline2));

    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(sourceDetail,
        reconcileDetail);

    assertThat(reconciledPadPipelines.countReconciledPipelines()).isEqualTo(0);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void reconcileApplicationDetailPadPipelines_whenCannotReconcileSourcePipelineFound() {

    when(padPipelineService.getPipelines(sourceDetail))
        .thenReturn(List.of(sourcePadPipeline1, sourcePadPipeline2));
    when(padPipelineService.getPipelines(reconcileDetail))
        .thenReturn(List.of(reconcilePadPipeline1));

    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(sourceDetail,
        reconcileDetail);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void reconcileApplicationDetailPadPipelines_whenUnreconciledPipelineIdRequested() {

    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(sourceDetail,
        reconcileDetail);

    reconciledPadPipelines.findByPipelineIdOrError(new PipelineId(9999));


  }
}