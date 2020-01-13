package uk.co.ogauthority.pwa.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.UserAccount;
import uk.co.ogauthority.pwa.model.entity.UserAccountView;

public interface UserAccountRepository extends CrudRepository<UserAccount, String> {

  List<UserAccountView> findByIdIn(List<String> ids);

}
