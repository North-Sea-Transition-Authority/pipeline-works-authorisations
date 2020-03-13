package uk.co.ogauthority.pwa.repository.fields;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.fields.DevukField;
import uk.co.ogauthority.pwa.model.entity.fields.PwaApplicationDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PwaFieldRepository extends CrudRepository<PwaApplicationDetailField, Integer> {

  List<PwaApplicationDetailField> getAllByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  PwaApplicationDetailField findByPwaApplicationDetailAndDevukField(
      PwaApplicationDetail pwaApplicationDetail, DevukField devukField);

}
