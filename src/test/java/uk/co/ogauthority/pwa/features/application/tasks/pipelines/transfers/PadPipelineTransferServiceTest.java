package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTransferServiceTest {

  @Mock
  PadPipelineTransferRepository padPipelineTransferRepository;

  @Mock
  PadPipelineService padPipelineService;

  @Mock
  PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;

  private PadPipelineTransferService padPipelineTransferService;

  private PadPipeline padPipeline;

  private Pipeline pipeline;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup()  {
    padPipelineTransferService = new PadPipelineTransferService(padPipelineTransferRepository, padPipelineService,
        padPipelineTransferClaimValidator);

    pipeline = new Pipeline();
    pipeline.setId(111);

    padPipeline = new PadPipeline();
    padPipeline.setId(1);
    padPipeline.setPipelineRef("ref");

    var pwaApplication = new PwaApplication();
    pwaApplication.setResourceType(PwaResourceType.PETROLEUM);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setId(1);
    pwaApplicationDetail.setPwaApplication(pwaApplication);
  }

  @Test
  public void transferOut() {
    padPipelineTransferService.transferOut(padPipeline, pwaApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(padPipelineTransferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void claimPipeline() {
    var recipientPwa = new PwaApplicationDetail();
    recipientPwa.setId(2);

    var claimForm = new PadPipelineTransferClaimForm()
        .setPadPipelineId(1)
        .setAssignNewPipelineNumber(false);

    var transfer = new PadPipelineTransfer()
        .setId(1)
        .setPadPipeline(padPipeline)
        .setDonorApplication(pwaApplicationDetail);

    when(padPipelineService.getById(padPipeline.getId())).thenReturn(padPipeline);
    when(padPipelineTransferRepository.findPadPipelineTransferByPadPipelineAndRecipientApplicationIsNull(padPipeline))
        .thenReturn(transfer);

    padPipelineTransferService.claimPipeline(claimForm, recipientPwa);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(padPipelineTransferRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(transfer.setRecipientApplication(recipientPwa));
    verify(padPipelineService).createTransferredPipeline(claimForm, recipientPwa);
  }

  @Test
  public void getClaimablePipelinesForForm() {
    var hydrogenApplication = new PwaApplication();
    hydrogenApplication.setResourceType(PwaResourceType.HYDROGEN);

    var hydrogenApplicationDetail = new PwaApplicationDetail();
    hydrogenApplicationDetail.setId(2);
    hydrogenApplicationDetail.setPwaApplication(hydrogenApplication);

    var hydrogenPipeline = new PadPipeline();
    hydrogenPipeline.setId(2);

    var petroleumTransfer = new PadPipelineTransfer()
        .setPadPipeline(padPipeline)
        .setDonorApplication(pwaApplicationDetail);

    var hydrogenTransfer = new PadPipelineTransfer()
        .setPadPipeline(hydrogenPipeline)
        .setDonorApplication(hydrogenApplicationDetail);

    when(padPipelineTransferRepository.findAllByRecipientApplicationIsNull())
        .thenReturn(List.of(petroleumTransfer, hydrogenTransfer));

    assertThat(padPipelineTransferService.getClaimablePipelinesForForm(PwaResourceType.PETROLEUM))
        .isEqualTo(Map.of("1", "ref"));
  }

  @Test
  public void validateClaimForm() {
    var form = new PadPipelineTransferClaimForm();
    var bindingResult = new BeanPropertyBindingResult(null, "");
    padPipelineTransferService.validateClaimForm(form, bindingResult);
    verify(padPipelineTransferClaimValidator).validate(form, bindingResult);
  }

}
