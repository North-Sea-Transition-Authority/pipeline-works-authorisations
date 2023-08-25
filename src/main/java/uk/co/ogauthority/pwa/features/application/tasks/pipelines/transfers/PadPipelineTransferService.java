package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PadPipelineTransferService {
  private final PadPipelineTransferRepository transferRepository;

  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public PadPipelineTransferService(PadPipelineTransferRepository transferRepository,
                                    PipelineDetailService pipelineDetailService) {
    this.transferRepository = transferRepository;
    this.pipelineDetailService = pipelineDetailService;
  }

  @Transactional
  public void transferOut(PadPipeline transferredPipeline, PwaApplicationDetail applicationDetail) {
    var transfer = new PadPipelineTransfer()
        .setDonorPipeline(transferredPipeline.getPipeline())
        .setDonorApplicationDetail(applicationDetail);
    transferRepository.save(transfer);
  }

  public List<PadPipelineTransfer> findUnclaimedByDonorApplication(PwaApplicationDetail applicationDetail) {
    return transferRepository.findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(applicationDetail);
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
