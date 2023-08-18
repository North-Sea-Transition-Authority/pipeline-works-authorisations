package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineTransferRepository extends CrudRepository<PadPipelineTransfer, Integer> {
  Optional<PadPipelineTransfer> findByDonorApplication(PwaApplicationDetail donorApplication);
}
