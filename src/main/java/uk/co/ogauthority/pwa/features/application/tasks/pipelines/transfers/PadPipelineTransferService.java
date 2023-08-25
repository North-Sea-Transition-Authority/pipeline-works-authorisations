package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository) {
    this.transferRepository = transferRepository;
  }

  @Transactional
  public void transferOut(PadPipeline transferredPipeline, PwaApplicationDetail applicationDetail) {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(transferredPipeline.getPipeline())
        .setDonorApplicationDetail(applicationDetail);
    transferRepository.save(transfer);
  }

  public List<PadPipelineTransfer> findUnclaimedByDonorApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByDonorApplicationAndRecipientApplicationIsNull(applicationDetail);
  }
}
