package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTransferServiceTest {

  @Mock
  PadPipelineTransferRepository transferRepository;

  @Mock
  PadPipelineService padPipelineService;

  @Mock
  PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;

  @Mock
  PipelineDetailService pipelineDetailService;

  PadPipelineTransferService padPipelineTransferService;

  @Before
  public void setup()  {
    padPipelineTransferService = new PadPipelineTransferService(
        transferRepository,
        padPipelineService,
        padPipelineTransferClaimValidator,
        pipelineDetailService);
  }

  @Test
  public void transferOut() {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(1);

    var pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setId(1);

    padPipelineTransferService.releasePipeline(padPipeline, pwaApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void findUnclaimedTransfers() {
    var pipelineDetail = new PwaApplicationDetail();
    padPipelineTransferService.findUnclaimedByDonorApplication(pipelineDetail);
    verify(transferRepository).findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(pipelineDetail);
  }
}
