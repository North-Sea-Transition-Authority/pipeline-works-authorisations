package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.internal;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;

@Repository
public interface DevukFieldRepository extends CrudRepository<DevukField, Integer> {

  List<DevukField> findAllByStatusNotIn(List<Integer> statusCodes);

}
