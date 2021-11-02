package uk.co.ogauthority.pwa.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.UserSessionService;

public class FoxSessionFilterTest {

  private static final String VALID_SESSION_ID = "VALID_SESSION";

  private FoxSessionFilter foxSessionFilter;

  private UserSessionService userSessionService;

  private HttpSession httpSession;

  @Before
  public void setup() {
    userSessionService = mock(UserSessionService.class);

    when(userSessionService.getAndValidateSession(eq(VALID_SESSION_ID), anyBoolean()))
        .thenReturn(Optional.of(validSession()));

    foxSessionFilter = new FoxSessionFilter(userSessionService);

    httpSession = mock(HttpSession.class);
  }

  @Test
  public void testCheckAuthentication_whenCookieNull() {
    // No previous session and no cookie: nothing should happen
    SecurityContextHolder.clearContext();
    foxSessionFilter.checkAuthentication(null, httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession).invalidate();

    // Previous session exists and no cookie: context should be cleared
    SecurityContextHolder.getContext().setAuthentication(validUserToken());
    foxSessionFilter.checkAuthentication(null, httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession, times(2)).invalidate();
  }

  @Test
  public void testCheckAuthentication_whenCookieEmpty() {
    // No previous session and no cookie: nothing should happen
    SecurityContextHolder.clearContext();
    foxSessionFilter.checkAuthentication(emptySessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession).invalidate();

    // Previous session exists and no cookie: context should be cleared
    SecurityContextHolder.getContext().setAuthentication(validUserToken());
    foxSessionFilter.checkAuthentication(emptySessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession, times(2)).invalidate();
  }

  @Test
  public void testCheckAuthentication_whenTokensMismatch() {
    // Cached session is expired and cookie contains valid session: cache should be re-authenticated to valid session
    SecurityContextHolder.getContext().setAuthentication(expiredUserToken());
    foxSessionFilter.checkAuthentication(validSessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isInstanceOfSatisfying(AuthenticatedUserToken.class, e -> assertThat(e.getSessionId()).isEqualTo(VALID_SESSION_ID));
    // User account load should have been requested
    verify(userSessionService).getAndValidateSession(VALID_SESSION_ID, true);
    verify(httpSession).invalidate();

    // Cached session is invalid and cookie contains a different invalid session: context should be cleared
    SecurityContextHolder.getContext().setAuthentication(expiredUserToken());
    foxSessionFilter.checkAuthentication(expiredSessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession, times(2)).invalidate();
  }

  @Test
  public void testCheckAuthentication_whenTokensMatch() {
    // Cached session is valid and cookie contains valid session: cached session should remain
    SecurityContextHolder.getContext().setAuthentication(validUserToken());
    foxSessionFilter.checkAuthentication(validSessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isInstanceOfSatisfying(AuthenticatedUserToken.class, e -> assertThat(e.getSessionId()).isEqualTo(VALID_SESSION_ID));
    // User account load should NOT have been requested
    verify(userSessionService).getAndValidateSession(VALID_SESSION_ID, false);
  }

  @Test
  public void testCheckAuthentication_whenNoCachedSession() {
    // Cached session is null and cookie contains valid session: cache should be re-authenticated to valid session
    SecurityContextHolder.getContext().setAuthentication(null);
    foxSessionFilter.checkAuthentication(validSessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isInstanceOfSatisfying(AuthenticatedUserToken.class, e -> assertThat(e.getSessionId()).isEqualTo(VALID_SESSION_ID));
    // User account load should have been requested
    verify(userSessionService).getAndValidateSession(VALID_SESSION_ID, true);
    verify(httpSession).invalidate();

    // Cached session is null and cookie contains invalid session: cache should remain empty
    SecurityContextHolder.getContext().setAuthentication(null);
    foxSessionFilter.checkAuthentication(expiredSessionCookie(), httpSession);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(httpSession, times(2)).invalidate();
  }

  private Cookie emptySessionCookie() {
    return new Cookie(FoxSessionFilter.SESSION_COOKIE_NAME, "");
  }

  private Cookie validSessionCookie() {
    return new Cookie(FoxSessionFilter.SESSION_COOKIE_NAME, VALID_SESSION_ID);
  }

  private Cookie expiredSessionCookie() {
    return new Cookie(FoxSessionFilter.SESSION_COOKIE_NAME, "EXPIRED_COOKIE_SESSION");
  }

  private UserSession validSession() {
    var userSession = new UserSession(VALID_SESSION_ID);
    userSession.setAuthenticatedUserAccount(new AuthenticatedUserAccount(new WebUserAccount(1), List.of()));
    return userSession;
  }

  private AuthenticatedUserToken validUserToken() {
    return AuthenticatedUserToken.create(VALID_SESSION_ID,  new AuthenticatedUserAccount(new WebUserAccount(1), List.of()));
  }

  private AuthenticatedUserToken expiredUserToken() {
    return AuthenticatedUserToken.create("EXPIRED_CACHED_SESSION",  new AuthenticatedUserAccount(new WebUserAccount(1), List.of()));
  }
}