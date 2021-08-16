package uk.co.ogauthority.pwa.energyportal.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

public interface WebUserAccountRepository extends CrudRepository<WebUserAccount, Integer> {

  List<WebUserAccount> findAllByEmailAddressAndAccountStatusNotIn(String emailAddress, List<WebUserAccountStatus> accountStatuses);

  List<WebUserAccount> findAllByLoginIdAndAccountStatusNotIn(String loginId, List<WebUserAccountStatus> accountStatuses);

  List<WebUserAccount> findAllByPersonAndAccountStatusIn(Person person, List<WebUserAccountStatus> accountStatuses);
}
