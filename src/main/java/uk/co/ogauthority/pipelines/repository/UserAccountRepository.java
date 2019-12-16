package uk.co.ogauthority.pipelines.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;
import uk.co.ogauthority.pipelines.model.entity.UserAccountView;

public interface UserAccountRepository extends CrudRepository<UserAccount, String> {

  List<UserAccountView> findByIdIn(List<String> ids);

}
