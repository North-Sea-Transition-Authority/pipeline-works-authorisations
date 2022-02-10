package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCableCrossingRepository extends CrudRepository<PadCableCrossing, Integer> {

  Optional<PadCableCrossing> findByPwaApplicationDetailAndId(PwaApplicationDetail pwaApplicationDetail, Integer id);

  List<PadCableCrossing> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
