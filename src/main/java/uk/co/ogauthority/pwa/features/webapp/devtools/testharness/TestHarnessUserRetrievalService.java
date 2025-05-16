package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
@Profile("test-harness")
public class TestHarnessUserRetrievalService {

  private final TeamService teamService;
  private final WebUserAccountRepository webUserAccountRepository;


  @Autowired
  public TestHarnessUserRetrievalService(TeamService teamService, WebUserAccountRepository webUserAccountRepository) {
    this.teamService = teamService;
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public WebUserAccount getWebUserAccount(Integer wuaId) {

    return webUserAccountRepository.findAllByWuaIdAndAccountStatusIn(wuaId, List.of(WebUserAccountStatus.ACTIVE, WebUserAccountStatus.NEW))
        .stream()
        .findAny()
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find web user account with id: " + wuaId));
  }

  public AuthenticatedUserAccount createAuthenticatedUserAccount(Integer wuaId) {

    var webUserAccount = getWebUserAccount(wuaId);
    return createAuthenticatedUserAccount(webUserAccount);
  }

  public AuthenticatedUserAccount createAuthenticatedUserAccount(WebUserAccount webUserAccount) {
    var userPrivs = teamService.getAllUserPrivilegesForPerson(webUserAccount.getLinkedPerson());
    return new AuthenticatedUserAccount(webUserAccount, userPrivs);
  }
}
