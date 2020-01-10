package uk.co.ogauthority.pipelines.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;
import uk.co.ogauthority.pipelines.model.entity.UserSession;
import uk.co.ogauthority.pipelines.repository.UserSessionRepository;

public class UserSessionServiceTest {

  private UserSessionService userSessionService;

  private UserSession validSession;
  private UserSession expiredSession;
  private UserSession loggedOutSession;

  @Before
  public void setup() {
    Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    UserSessionRepository userSessionRepository = mock(UserSessionRepository.class);

    userSessionService = new UserSessionService(userSessionRepository, Duration.ofHours(1), fixedClock);

    Instant oneMinuteAgo = fixedClock.instant().minus(Duration.ofMinutes(1));
    Instant oneHourAgo = fixedClock.instant().minus(Duration.ofHours(1));

    validSession = createUserSession("VALID", oneMinuteAgo);
    expiredSession = createUserSession("EXPIRED", oneHourAgo);
    loggedOutSession = createUserSession("LOGGED OUT", oneMinuteAgo);
    loggedOutSession.setLogoutTimestamp(oneMinuteAgo);

    when(userSessionRepository.findAndLoadUserAccountById(eq(validSession.getId()))).thenReturn(Optional.of(validSession));
    when(userSessionRepository.findAndLoadUserAccountById(eq(expiredSession.getId()))).thenReturn(Optional.of(expiredSession));
    when(userSessionRepository.findAndLoadUserAccountById(eq(loggedOutSession.getId()))).thenReturn(Optional.of(loggedOutSession));

    when(userSessionRepository.findById(eq(validSession.getId()))).thenReturn(Optional.of(validSession));
    when(userSessionRepository.findById(eq(expiredSession.getId()))).thenReturn(Optional.of(expiredSession));
    when(userSessionRepository.findById(eq(loggedOutSession.getId()))).thenReturn(Optional.of(loggedOutSession));
  }

  @Test
  public void testGetAndValidateSession_withUserAccountLoad() {
    Optional<UserSession> optionalValidSession = userSessionService.getAndValidateSession(validSession.getId(), true);
    assertThat(optionalValidSession).isPresent();
    assertThat(optionalValidSession.map(UserSession::getId).get()).isEqualTo(validSession.getId());

    Optional<UserSession> optionalExpiredSession = userSessionService.getAndValidateSession(expiredSession.getId(), true);
    assertThat(optionalExpiredSession).isEmpty();

    Optional<UserSession> optionalLoggedOutSession = userSessionService.getAndValidateSession(loggedOutSession.getId(), true);
    assertThat(optionalLoggedOutSession).isEmpty();

    Optional<UserSession> missingSession = userSessionService.getAndValidateSession("INVALID", true);
    assertThat(missingSession).isEmpty();
  }

  @Test
  public void testGetAndValidateSession_noUserAccountLoad() {
    Optional<UserSession> optionalValidSession = userSessionService.getAndValidateSession(validSession.getId(), false);
    assertThat(optionalValidSession).isPresent();
    assertThat(optionalValidSession.map(UserSession::getId).get()).isEqualTo(validSession.getId());

    Optional<UserSession> optionalExpiredSession = userSessionService.getAndValidateSession(expiredSession.getId(), false);
    assertThat(optionalExpiredSession).isEmpty();

    Optional<UserSession> optionalLoggedOutSession = userSessionService.getAndValidateSession(loggedOutSession.getId(), false);
    assertThat(optionalLoggedOutSession).isEmpty();

    Optional<UserSession> missingSession = userSessionService.getAndValidateSession("INVALID", true);
    assertThat(missingSession).isEmpty();
  }

  @Test
  public void testIsSessionValid() {
    assertThat(userSessionService.isSessionValid(validSession)).isTrue();
    assertThat(userSessionService.isSessionValid(expiredSession)).isFalse();
    assertThat(userSessionService.isSessionValid(loggedOutSession)).isFalse();
  }

  private UserSession createUserSession(String sessionId, Instant lastAccessTimestamp) {
    UserSession userSession = new UserSession(sessionId);
    UserAccount userAccount = new UserAccount("1");
    userSession.setUserAccount(userAccount);
    userSession.setLastAccessTimestamp(lastAccessTimestamp);
    return userSession;
  }
}