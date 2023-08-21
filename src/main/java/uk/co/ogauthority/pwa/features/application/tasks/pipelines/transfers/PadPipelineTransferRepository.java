package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;

public interface PadPipelineTransferRepository extends CrudRepository<PadPipelineTransfer, Integer> {

  PadPipelineTransfer findPadPipelineTransferByPadPipelineAndRecipientApplicationIsNull(PadPipeline padPipeline);

  List<PadPipelineTransfer> findAllByRecipientApplicationIsNull();

}