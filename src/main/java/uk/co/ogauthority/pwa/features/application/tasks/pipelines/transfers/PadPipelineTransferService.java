package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;
  private final PadPipelineService padPipelineService;
  private final PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository,
                                    PadPipelineService padPipelineService,
                                    PadPipelineTransferClaimValidator padPipelineTransferClaimValidator,
                                    PipelineDetailService pipelineDetailService) {
    this.transferRepository = transferRepository;
    this.padPipelineService = padPipelineService;
    this.padPipelineTransferClaimValidator = padPipelineTransferClaimValidator;
    this.pipelineDetailService = pipelineDetailService;
  }

  @Transactional
  public void releasePipeline(PadPipeline transferredPipeline, PwaApplicationDetail applicationDetail) {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(transferredPipeline.getPipeline())
        .setDonorApplicationDetail(applicationDetail);
    transferRepository.save(transfer);
  }

  @Transactional
  public void claimPipeline(PadPipelineTransferClaimForm form, PwaApplicationDetail recipientApplicationDetail) {
    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(form.getPipelineId());
    var unclaimedTransfer = findUnclaimedByDonorPipeline(pipelineDetail.getPipeline());
    if (unclaimedTransfer.isPresent()) {
      var claimedPipeline = padPipelineService.createTransferredPipeline(form, recipientApplicationDetail);

      var transfer = unclaimedTransfer.get();
      transfer.setRecipientApplicationDetail(recipientApplicationDetail);
      transfer.setRecipientPipeline(claimedPipeline.getPipeline());
      transferRepository.save(transfer);
    }
  }

  public Map<String, String> getClaimablePipelinesForForm(PwaResourceType resourceType) {
    var pipelineIds = transferRepository.findAllByRecipientApplicationDetailIsNull().stream()
        .filter(padPipelineTransfer -> padPipelineTransfer.getDonorApplicationDetail().getResourceType().equals(resourceType))
        .map(padPipelineTransfer -> padPipelineTransfer.getDonorPipeline().getId())
        .collect(Collectors.toList());

    return pipelineDetailService.getLatestPipelineDetailsForIds(pipelineIds)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            pipelineDetail -> pipelineDetail.getPipelineId().getDiffableString(),
            PipelineDetail::getPipelineNumber)
        );
  }

  public BindingResult validateClaimForm(PadPipelineTransferClaimForm form, BindingResult bindingResult) {
    padPipelineTransferClaimValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public List<PadPipelineTransfer> findUnclaimedByDonorApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(applicationDetail);
  }

  public Optional<PadPipelineTransfer> findUnclaimedByDonorPipeline(Pipeline donorPipeline) {
    return transferRepository.findByDonorPipelineAndRecipientApplicationDetailIsNull(donorPipeline);
  }

  public List<String> getUnclaimedPipelineNumbers(List<PadPipelineTransfer> unclaimedTransfers) {
    var unclaimedPipelines = unclaimedTransfers.stream()
        .map(PadPipelineTransfer::getDonorPipeline)
        .map(Pipeline::getId)
        .collect(Collectors.toList());

    return pipelineDetailService.getLatestPipelineDetailsForIds(unclaimedPipelines).stream()
        .map(PipelineDetail::getPipelineNumber)
        .collect(Collectors.toList());
  }
}
