package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;
  private final PadPipelineService padPipelineService;
  private final PadPipelineTransferClaimValidator padPipelineTransferClaimValidator;

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository,
                                    PadPipelineService padPipelineService,
                                    PadPipelineTransferClaimValidator padPipelineTransferClaimValidator) {
    this.transferRepository = transferRepository;
    this.padPipelineService = padPipelineService;
    this.padPipelineTransferClaimValidator = padPipelineTransferClaimValidator;
  }

  @Transactional
  public void transferOut(PadPipeline transferredPipeline, PwaApplicationDetail applicationDetail) {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(transferredPipeline.getPipeline())
        .setDonorApplicationDetail(applicationDetail);
    transferRepository.save(transfer);
  }

  @Transactional
  public void claimPipeline(PadPipelineTransferClaimForm form, PwaApplicationDetail recipientApplicationDetail) {
    var releasedPipeline = padPipelineService.getById(form.getPadPipelineId());
    var transfer = transferRepository.findPadPipelineTransferByPadPipelineAndRecipientApplicationIsNull(releasedPipeline);
    transfer.setRecipientApplication(recipientApplicationDetail);
    transferRepository.save(transfer);

    padPipelineService.createTransferredPipeline(form, recipientApplicationDetail);
  }

  public Map<String, String> getClaimablePipelinesForForm(PwaResourceType resourceType) {
    return transferRepository.findAllByRecipientApplicationIsNull().stream()
        .filter(padPipelineTransfer -> padPipelineTransfer.getDonorApplication().getResourceType().equals(resourceType))
        .collect(StreamUtils.toLinkedHashMap(
            padPipelineTransfer -> String.valueOf(padPipelineTransfer.getPadPipeline().getPadPipelineId().asInt()),
            padPipelineTransfer -> padPipelineTransfer.getPadPipeline().getPipelineRef())
        );
  }

  public BindingResult validateClaimForm(PadPipelineTransferClaimForm form, BindingResult bindingResult) {
    padPipelineTransferClaimValidator.validate(form, bindingResult);
    return bindingResult;
  }
}
