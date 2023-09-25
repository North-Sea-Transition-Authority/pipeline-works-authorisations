package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.TransferHistoryView;

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

  @Mock
  MasterPwaService masterPwaService;

  PadPipelineTransferService padPipelineTransferService;

  Pipeline pipeline;
  PadPipeline padPipeline;
  PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup()  {
    padPipelineTransferService = new PadPipelineTransferService(
        transferRepository,
        padPipelineService,
        padPipelineTransferClaimValidator,
        pipelineDetailService
    );

    pipeline = new Pipeline();
    pipeline.setId(1);

    padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(1);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setId(1);
  }

  @Test
  public void transferOut() {
    padPipelineTransferService.releasePipeline(padPipeline, pwaApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void claimPipeline() {
    var form = new PadPipelineTransferClaimForm()
        .setPipelineId(1)
        .setAssignNewPipelineNumber(false);

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setId(1);
    pipelineDetail.setPipeline(pipeline);

    var transfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setDonorPipeline(pipeline);

    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);

    var transferredPipeline = new Pipeline();
    transferredPipeline.setId(2);

    var transferredPadPipeline = new PadPipeline();
    transferredPadPipeline.setPipeline(transferredPipeline);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    when(transferRepository.findByDonorPipelineAndRecipientApplicationDetailIsNull(pipeline)).thenReturn(Optional.of(transfer));
    when(padPipelineService.createTransferredPipeline(form, recipientApplicationDetail)).thenReturn(transferredPadPipeline);

    padPipelineTransferService.claimPipeline(form, recipientApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(transferredPipeline);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(recipientApplicationDetail);

//    verify(pipelineDetailService).updateTransferredPipelineDetails(any(), any());
  }

  @Test
  public void findUnclaimedTransfers() {
    var pipelineAppDetail = new PwaApplicationDetail();
    padPipelineTransferService.findUnclaimedByDonorApplication(pipelineAppDetail);
    verify(transferRepository).findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(pipelineAppDetail);
  }

  @Test
  public void checkAndRemoveFromTransfer_nothingToRemove() {
    when(transferRepository.findByRecipientPipeline(pipeline)).thenReturn(Optional.empty());
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.empty());

    padPipelineTransferService.checkAndRemoveFromTransfer(pipeline);

    verify(transferRepository, never()).save(any());
    verify(transferRepository, never()).delete(any());
  }

  @Test
  public void checkAndRemoveFromTransfer_removeRecipient_donorPresent() {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(pipeline)
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setRecipientPipeline(pipeline)
        .setRecipientApplicationDetail(pwaApplicationDetail);

    when(transferRepository.findByRecipientPipeline(pipeline)).thenReturn(Optional.of(transfer));

    padPipelineTransferService.checkAndRemoveFromTransfer(pipeline);

//    verify(pipelineDetailService).clearTransferredPipelineDetails(pipeline.getId(), false);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(null);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(null);
  }

  @Test
  public void checkAndRemoveFromTransfer_removeRecipient_donorNotPresent() {
    var transfer = new PadPipelineTransfer()
        .setRecipientPipeline(pipeline)
        .setRecipientApplicationDetail(pwaApplicationDetail);

    when(transferRepository.findByRecipientPipeline(pipeline)).thenReturn(Optional.of(transfer));

    padPipelineTransferService.checkAndRemoveFromTransfer(pipeline);

//    verify(pipelineDetailService).clearTransferredPipelineDetails(pipeline.getId(), false);
    verify(transferRepository).delete(transfer);
  }

  @Test
  public void checkAndRemoveFromTransfer_removeDonor_recipientPresent() {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(pipeline)
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setRecipientPipeline(pipeline)
        .setRecipientApplicationDetail(pwaApplicationDetail);

    when(transferRepository.findByRecipientPipeline(pipeline)).thenReturn(Optional.empty());
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));

    padPipelineTransferService.checkAndRemoveFromTransfer(pipeline);

//    verify(pipelineDetailService).clearTransferredPipelineDetails(pipeline.getId(), true);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(null);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(null);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void checkAndRemoveFromTransfer_removeDonor_recipientNotPresent() {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(pipeline)
        .setDonorApplicationDetail(pwaApplicationDetail);

    when(transferRepository.findByRecipientPipeline(pipeline)).thenReturn(Optional.empty());
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));

    padPipelineTransferService.checkAndRemoveFromTransfer(pipeline);

//    verify(pipelineDetailService).clearTransferredPipelineDetails(pipeline.getId(), true);
    verify(transferRepository).delete(transfer);
  }

  @Test
  public void getTransferHistoryViews() {
    var recipientMasterPwa = new MasterPwa();
    recipientMasterPwa.setId(2);

    var recipientPwaApplication = new PwaApplication();
    recipientPwaApplication.setId(2);
    recipientPwaApplication.setMasterPwa(recipientMasterPwa);

    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);
    recipientApplicationDetail.setPwaApplication(recipientPwaApplication);

    var recipientPipeline = new Pipeline();
    recipientPipeline.setId(2);

    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(pipeline)
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setRecipientPipeline(recipientPipeline)
        .setRecipientApplicationDetail(recipientApplicationDetail);

    var recipientMasterPwaDetail = new MasterPwaDetail();
    recipientMasterPwaDetail.setMasterPwa(recipientMasterPwa);
    recipientMasterPwaDetail.setReference("2/W/23");

    when(transferRepository.findAllByDonorPipeline_IdInOrRecipientPipeline_IdIn(List.of(1), List.of(1))).thenReturn(List.of(transfer));
    when(masterPwaService.getCurrentDetailOrThrow(recipientMasterPwa)).thenReturn(recipientMasterPwaDetail);

    assertThat(padPipelineTransferService.getTransferHistoryViews(List.of(1))).usingRecursiveComparison().isEqualTo(
        List.of(
            new TransferHistoryView()
                .setOriginalPipelineId(1)
                .setViewUrl(ReverseRouter.route(on(PwaViewController.class).renderViewPwa(2, PwaViewTab.PIPELINES, null, null, false)))
                .setTransfereeConsentReference("2/W/23")
        )
    );
  }
}
