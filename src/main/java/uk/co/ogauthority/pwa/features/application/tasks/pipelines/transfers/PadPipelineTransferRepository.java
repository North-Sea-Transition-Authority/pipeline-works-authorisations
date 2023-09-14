package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineTransferRepository extends CrudRepository<PadPipelineTransfer, Integer> {
  List<PadPipelineTransfer> findByDonorApplicationDetailAndRecipientApplicationDetailIsNull(PwaApplicationDetail donorApplicationDetail);

  List<PadPipelineTransfer> findByRecipientApplicationDetail(PwaApplicationDetail recipientApplicationDetail);

  List<PadPipelineTransfer> findAllByRecipientApplicationDetailIsNull();

  Optional<PadPipelineTransfer> findByDonorPipelineAndRecipientApplicationDetailIsNull(Pipeline donorPipeline);

  Optional<PadPipelineTransfer> findByRecipientPipeline(Pipeline pipeline);

  Optional<PadPipelineTransfer> findByDonorPipeline(Pipeline pipeline);

  List<PadPipelineTransfer> findAllByDonorApplicationDetailIsNullAndRecipientApplicationDetailIsNotNull();

  List<PadPipelineTransfer> findAllByDonorPipeline_IdInOrRecipientPipeline_IdIn(
      List<Integer> donorPipelineIds, List<Integer> recipientPipelineIds);

  Optional<PadPipelineTransfer> findByDonorPipeline_IdOrRecipientPipeline_Id(Integer donorPipelineId, Integer recipientPipelineId);
}
