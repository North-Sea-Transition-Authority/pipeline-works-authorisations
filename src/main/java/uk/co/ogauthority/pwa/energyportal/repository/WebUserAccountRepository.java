package uk.co.ogauthority.pwa.energyportal.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public interface WebUserAccountRepository extends CrudRepository<WebUserAccount, Integer> {

  List<WebUserAccount> findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(String emailAddress,
                                                                            List<WebUserAccountStatus> accountStatuses);

  List<WebUserAccount> findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(String loginId, List<WebUserAccountStatus> accountStatuses);

  List<WebUserAccount> findAllByPersonAndAccountStatusIn(Person person, List<WebUserAccountStatus> accountStatuses);
}
