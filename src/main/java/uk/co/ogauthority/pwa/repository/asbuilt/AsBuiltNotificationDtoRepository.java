package uk.co.ogauthority.pwa.repository.asbuilt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;

@Repository
public interface AsBuiltNotificationDtoRepository {

  Page<AsBuiltNotificationWorkareaView> findAllAsBuiltNotificationsForUser(AuthenticatedUserAccount authenticatedUserAccount,
                                                                           Pageable pageable);

}
