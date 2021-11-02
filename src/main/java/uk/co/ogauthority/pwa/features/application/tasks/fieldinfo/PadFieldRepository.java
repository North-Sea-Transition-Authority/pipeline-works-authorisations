package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFieldRepository extends CrudRepository<PadField, Integer> {

  List<PadField> getAllByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  PadField findByPwaApplicationDetailAndDevukField(
      PwaApplicationDetail pwaApplicationDetail, DevukField devukField);

}
