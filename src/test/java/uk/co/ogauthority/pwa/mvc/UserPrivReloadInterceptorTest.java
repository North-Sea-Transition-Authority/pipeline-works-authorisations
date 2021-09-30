package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserToken;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.UserSessionService;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserPrivReloadInterceptorTest {

  @Mock
  private UserSessionService userSessionService;

  @Mock
  private SecurityContext securityContext;

  private UserPrivReloadInterceptor userPrivReloadInterceptor;

  private AuthenticatedUserAccount user;

  @Mock
  private AuthenticatedUserToken existingAuthentication;

  private static final String SESSION_ID = "my-sesh";

  @Before
  public void setup(){

    userPrivReloadInterceptor = new UserPrivReloadInterceptor(userSessionService);
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of(
        PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE, PwaUserPrivilege.PWA_CONSENT_SEARCH));

    SecurityContextHolder.setContext(securityContext);
    when(existingAuthentication.getPrincipal()).thenReturn(user);
    when(existingAuthentication.getSessionId()).thenReturn(SESSION_ID);
    when(securityContext.getAuthentication()).thenReturn(existingAuthentication);

  }

  @Test
  public void prehandle_verifySecurityContextAuthSetupAsExpected() throws Exception {

    ArgumentCaptor<? extends Authentication> newAuthCaptor = ArgumentCaptor.forClass(Authentication.class);
    userPrivReloadInterceptor.preHandle(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new Object());

    verify(userSessionService, times(1)).populateUserPrivileges(user);
    verify(securityContext).setAuthentication(newAuthCaptor.capture());

    assertThat(newAuthCaptor.getValue()).satisfies(auth -> {
      assertThat(auth).isInstanceOf(AuthenticatedUserToken.class);
      assertThat(auth.isAuthenticated()).isTrue();
      assertThat(((AuthenticatedUserToken) auth).getSessionId()).isEqualTo(SESSION_ID);
    });

  }

}
