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
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTransferServiceTest {

  @Mock
  PadPipelineTransferRepository padPipelineTransferRepository;

  PadPipelineTransferService padPipelineTransferService;

  @Before
  public void setup()  {
    padPipelineTransferService = new PadPipelineTransferService(padPipelineTransferRepository);
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

    padPipelineTransferService.transferOut(padPipeline, pwaApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(padPipelineTransferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void findUnclaimedTransfers() {
    var pipelineDetail = new PwaApplicationDetail();
    padPipelineTransferService.findUnclaimedByDonorApplication(pipelineDetail);
    verify(padPipelineTransferRepository).findByDonorApplicationAndRecipientApplicationIsNull(pipelineDetail);
  }
}
