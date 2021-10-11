package uk.co.ogauthority.pwa.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.repository.UserSessionRepository;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class UserSessionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSessionService.class);

  private final UserSessionRepository userSessionRepository;
  private final WebUserAccountRepository webUserAccountRepository;
  private final TeamService teamService;
  private final Duration foxSessionTimeout;
  private final Clock utcSystemClock;

  @Autowired
  public UserSessionService(UserSessionRepository userSessionRepository,
                            WebUserAccountRepository webUserAccountRepository,
                            TeamService teamService,
                            @Value("${app.fox.session-timeout}") Duration foxSessionTimeout,
                            @Qualifier("utcClock") Clock utcSystemClock) {
    this.userSessionRepository = userSessionRepository;
    this.webUserAccountRepository = webUserAccountRepository;
    this.teamService = teamService;
    this.foxSessionTimeout = foxSessionTimeout;
    this.utcSystemClock = utcSystemClock;
  }

  @Transactional(readOnly = true)
  public Optional<UserSession> getAndValidateSession(String sessionId, boolean loadUserAccount) {

    Optional<UserSession> optionalUserSession;

    if (loadUserAccount) {

      Stopwatch stopwatch = Stopwatch.createStarted();

      optionalUserSession = userSessionRepository.findById(sessionId);

      if (optionalUserSession.isPresent()) {

        var userSession = optionalUserSession.get();

        var webUserAccount = webUserAccountRepository.findById(userSession.getWuaId())
            .orElseThrow(() -> new RuntimeException(String.format("wuaId: %s linked to sessionId %s could not be found",
                userSession.getWuaId(),
                userSession.getId())
            ));

        Set<PwaUserPrivilege> privileges = teamService.getAllUserPrivilegesForPerson(webUserAccount.getLinkedPerson());

        var authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, privileges,
            userSession.getProxyUserWuaId().orElse(null));
        userSession.setAuthenticatedUserAccount(authenticatedUserAccount);

      }

      LOGGER.info("Retrieved user account for session id {} in {} ms", sessionId, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } else {
      optionalUserSession = userSessionRepository.findById(sessionId);
      LOGGER.debug("Skip retrieval of user account for session id {}", sessionId);
    }

    return optionalUserSession.filter(this::isSessionValid);
  }

  @VisibleForTesting
  boolean isSessionValid(UserSession userSession) {
    boolean notTimedOut = userSession.getLastAccessTimestamp().plus(foxSessionTimeout).isAfter(utcSystemClock.instant());
    return userSession.getLogoutTimestamp() == null && notTimedOut;
  }

  /**
   * For a given user object, update privileges from datasource.
   */
  public void populateUserPrivileges(AuthenticatedUserAccount user) {
    user.setPrivileges(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()));
  }
}
