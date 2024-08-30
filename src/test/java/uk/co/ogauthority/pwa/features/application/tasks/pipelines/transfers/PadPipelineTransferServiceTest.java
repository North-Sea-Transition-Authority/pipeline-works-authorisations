package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

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

    var recipientApplication = new PwaApplication();
    recipientApplication.setResourceType(PwaResourceType.PETROLEUM);
    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);
    recipientApplicationDetail.setPwaApplication(recipientApplication);

    var transferredPipeline = new Pipeline();
    transferredPipeline.setId(2);

    var transferredPadPipeline = new PadPipeline();
    transferredPadPipeline.setPipeline(transferredPipeline);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));
    when(padPipelineService.createTransferredPipeline(form, recipientApplicationDetail)).thenReturn(transferredPadPipeline);

    padPipelineTransferService.claimPipeline(form, recipientApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(transferredPipeline);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(recipientApplicationDetail);

  }

  @Test
  public void claimPipeline_whenClaimedByRevokedPipelineApplication() {
    var form = new PadPipelineTransferClaimForm()
        .setPipelineId(1)
        .setAssignNewPipelineNumber(false);

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setId(1);
    pipelineDetail.setPipeline(pipeline);

    var revokedRecipientApplicationDetail = new PwaApplicationDetail();
    revokedRecipientApplicationDetail.setStatus(PwaApplicationStatus.DELETED);
    var transfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setDonorPipeline(pipeline)
        .setRecipientApplicationDetail(revokedRecipientApplicationDetail);

    var recipientApplication = new PwaApplication();
    recipientApplication.setResourceType(PwaResourceType.PETROLEUM);
    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);
    recipientApplicationDetail.setPwaApplication(recipientApplication);

    var transferredPipeline = new Pipeline();
    transferredPipeline.setId(2);

    var transferredPadPipeline = new PadPipeline();
    transferredPadPipeline.setPipeline(transferredPipeline);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));
    when(padPipelineService.createTransferredPipeline(form, recipientApplicationDetail)).thenReturn(transferredPadPipeline);

    padPipelineTransferService.claimPipeline(form, recipientApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(transferredPipeline);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(recipientApplicationDetail);
  }

  @Test
  public void claimPipeline_whenClaimedByPipelineApplication() {
    var form = new PadPipelineTransferClaimForm()
        .setPipelineId(1)
        .setAssignNewPipelineNumber(false);

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setId(1);
    pipelineDetail.setPipeline(pipeline);

    var claimedRecipientApplicationDetail = new PwaApplicationDetail();
    claimedRecipientApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);
    var transfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setDonorPipeline(pipeline)
        .setRecipientApplicationDetail(claimedRecipientApplicationDetail);

    var recipientApplication = new PwaApplication();
    recipientApplication.setResourceType(PwaResourceType.PETROLEUM);
    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);
    recipientApplicationDetail.setPwaApplication(recipientApplication);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));

    padPipelineTransferService.claimPipeline(form, recipientApplicationDetail);
    verify(padPipelineService, never()).createTransferredPipeline(any(), any());
    verify(transferRepository, never()).save(any());
  }

  @Test
  public void claimPipeline_CcusResourceType() {
    var form = new PadPipelineTransferClaimForm()
        .setPipelineId(1)
        .setAssignNewPipelineNumber(false)
        .setCompatibleWithTarget(true)
        .setLastIntelligentlyPigged(DateUtils.formatToDatePickerString(LocalDate.now()));

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setId(1);
    pipelineDetail.setPipeline(pipeline);

    var transfer = new PadPipelineTransfer()
        .setDonorApplicationDetail(pwaApplicationDetail)
        .setDonorPipeline(pipeline);

    var recipientApplication = new PwaApplication();
    recipientApplication.setResourceType(PwaResourceType.CCUS);
    var recipientApplicationDetail = new PwaApplicationDetail();
    recipientApplicationDetail.setId(2);
    recipientApplicationDetail.setPwaApplication(recipientApplication);

    var transferredPipeline = new Pipeline();
    transferredPipeline.setId(2);

    var transferredPadPipeline = new PadPipeline();
    transferredPadPipeline.setPipeline(transferredPipeline);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    when(transferRepository.findByDonorPipeline(pipeline)).thenReturn(Optional.of(transfer));
    when(padPipelineService.createTransferredPipeline(form, recipientApplicationDetail)).thenReturn(transferredPadPipeline);

    padPipelineTransferService.claimPipeline(form, recipientApplicationDetail);

    ArgumentCaptor<PadPipelineTransfer> captor = ArgumentCaptor.forClass(PadPipelineTransfer.class);
    verify(transferRepository).save(captor.capture());

    assertThat(captor.getValue().getDonorPipeline()).isEqualTo(pipeline);
    assertThat(captor.getValue().getDonorApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(captor.getValue().getRecipientPipeline()).isEqualTo(transferredPipeline);
    assertThat(captor.getValue().getRecipientApplicationDetail()).isEqualTo(recipientApplicationDetail);

  }

  @Test
  public void findUnclaimedByDonorApplication() {
    var pipelineAppDetail = new PwaApplicationDetail();
    var pipelineTransfer = mock(PadPipelineTransfer.class);

    when(transferRepository.findByDonorApplicationDetail(pipelineAppDetail))
        .thenReturn(List.of(pipelineTransfer));

    var results = padPipelineTransferService.findUnclaimedByDonorApplication(pipelineAppDetail);
    assertThat(results).containsExactly(pipelineTransfer);
  }

  @Test
  public void findUnclaimedByDonorApplication_whenPipelineTransferClaimedByRevokedApplication() {
    var pipelineAppDetail = new PwaApplicationDetail();

    var pipelineTransferWithWithdrawnClaim = mock(PadPipelineTransfer.class);
    var withdrawnApplicationDetail = new PwaApplicationDetail();
    withdrawnApplicationDetail.setStatus(PwaApplicationStatus.WITHDRAWN);
    when(pipelineTransferWithWithdrawnClaim.getRecipientApplicationDetail()).thenReturn(withdrawnApplicationDetail);

    var pipelineTransferWithDeletedClaim = mock(PadPipelineTransfer.class);
    var deletedApplicationDetail = new PwaApplicationDetail();
    deletedApplicationDetail.setStatus(PwaApplicationStatus.DELETED);
    when(pipelineTransferWithDeletedClaim.getRecipientApplicationDetail()).thenReturn(deletedApplicationDetail);

    var pipelineTransferWthValidClaim = mock(PadPipelineTransfer.class);
    var completedApplicationDetail = new PwaApplicationDetail();
    completedApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);
    when(pipelineTransferWthValidClaim.getRecipientApplicationDetail()).thenReturn(completedApplicationDetail);

    when(transferRepository.findByDonorApplicationDetail(pipelineAppDetail))
        .thenReturn(List.of(pipelineTransferWithDeletedClaim, pipelineTransferWithWithdrawnClaim, pipelineTransferWthValidClaim));

    var results = padPipelineTransferService.findUnclaimedByDonorApplication(pipelineAppDetail);
    assertThat(results).containsExactlyInAnyOrder(pipelineTransferWithDeletedClaim, pipelineTransferWithWithdrawnClaim);
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

    verify(transferRepository).delete(transfer);
  }

}
