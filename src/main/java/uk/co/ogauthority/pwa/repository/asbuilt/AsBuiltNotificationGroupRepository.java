package uk.co.ogauthority.pwa.repository.asbuilt;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

public interface AsBuiltNotificationGroupRepository extends CrudRepository<AsBuiltNotificationGroup, Integer> {

  Optional<AsBuiltNotificationGroup> findByPwaConsent(PwaConsent pwaConsent);

}