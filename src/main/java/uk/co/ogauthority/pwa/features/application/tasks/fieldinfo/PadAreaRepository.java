package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadAreaRepository extends CrudRepository<PadLinkedArea, Integer> {

  List<PadLinkedArea> getAllByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

  PadLinkedArea findByPwaApplicationDetailAndDevukField(
      PwaApplicationDetail pwaApplicationDetail, DevukField devukField);

}
