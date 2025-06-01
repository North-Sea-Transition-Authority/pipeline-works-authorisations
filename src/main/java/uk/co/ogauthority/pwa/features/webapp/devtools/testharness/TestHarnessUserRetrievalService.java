package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;
import uk.co.ogauthority.pwa.service.teams.PwaTeamsDtoFactory;

@Service
@Profile("test-harness")
public class TestHarnessUserRetrievalService {

  private final WebUserAccountRepository webUserAccountRepository;
  private final PortalTeamAccessor portalTeamAccessor;
  private final PwaTeamsDtoFactory pwaTeamsDtoFactory;


  @Autowired
  public TestHarnessUserRetrievalService(WebUserAccountRepository webUserAccountRepository, PortalTeamAccessor portalTeamAccessor,
                                         PwaTeamsDtoFactory pwaTeamsDtoFactory) {
    this.webUserAccountRepository = webUserAccountRepository;
    this.portalTeamAccessor = portalTeamAccessor;
    this.pwaTeamsDtoFactory = pwaTeamsDtoFactory;
  }

  public WebUserAccount getWebUserAccount(Integer wuaId) {

    return webUserAccountRepository.findAllByWuaIdAndAccountStatusIn(wuaId, List.of(WebUserAccountStatus.ACTIVE, WebUserAccountStatus.NEW))
        .stream()
        .findAny()
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find web user account with id: " + wuaId));
  }

  // TODO: Remove in PWARE-60
  public AuthenticatedUserAccount createAuthenticatedUserAccount(Integer wuaId) {

    var webUserAccount = getWebUserAccount(wuaId);
    return createAuthenticatedUserAccount(webUserAccount);
  }

  // TODO: Remove in PWARE-60
  public AuthenticatedUserAccount createAuthenticatedUserAccount(WebUserAccount webUserAccount) {
    var userPrivs = getAllUserPrivilegesForPerson(webUserAccount.getLinkedPerson());
    return new AuthenticatedUserAccount(webUserAccount, userPrivs);
  }

  // TODO: Remove in PWARE-60
  public Set<PwaUserPrivilege> getAllUserPrivilegesForPerson(Person person) {
    // get privs available to the user through res type role membership
    return pwaTeamsDtoFactory.createPwaUserPrivilegeSet(portalTeamAccessor.getAllPortalSystemPrivilegesForPerson(person));
  }
}
