package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;

@Repository
public interface PadPipelineCrossingRepository extends CrudRepository<PadPipelineCrossing, Integer> {

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countAllByPwaApplicationDetailAndPipelineFullyOwnedByOrganisation(PwaApplicationDetail pwaApplicationDetail,
                                                                        Boolean fullyOwned);

  List<PadPipelineCrossing> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
