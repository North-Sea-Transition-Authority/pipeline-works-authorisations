package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFastTrackRepository extends CrudRepository<PadFastTrack, Integer> {

  Optional<PadFastTrack> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
