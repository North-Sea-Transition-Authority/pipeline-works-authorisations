package uk.co.ogauthority.pwa.repository.devuk;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFieldRepository extends CrudRepository<PadField, Integer> {

  List<PadField> getAllByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  PadField findByPwaApplicationDetailAndDevukField(
      PwaApplicationDetail pwaApplicationDetail, DevukField devukField);

}
