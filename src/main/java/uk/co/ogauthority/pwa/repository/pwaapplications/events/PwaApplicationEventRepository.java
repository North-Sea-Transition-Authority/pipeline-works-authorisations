package uk.co.ogauthority.pwa.repository.pwaapplications.events;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEvent;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventType;

@Repository
public interface PwaApplicationEventRepository extends CrudRepository<PwaApplicationEvent, Integer> {

  List<PwaApplicationEvent>
      findPwaApplicationEventsByPwaApplicationAndEventTypeAndEventClearedInstantIsNull(
          PwaApplication pwaApplication,
          PwaApplicationEventType eventType);

}