package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadPipelineCrossingRepository extends CrudRepository<PadPipelineCrossing, Integer> {

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countAllByPwaApplicationDetailAndPipelineFullyOwnedByOrganisation(PwaApplicationDetail pwaApplicationDetail,
                                                                        Boolean fullyOwned);

  List<PadPipelineCrossing> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadPipelineCrossing> getByPwaApplicationDetailAndId(PwaApplicationDetail pwaApplicationDetail, Integer id);

}
