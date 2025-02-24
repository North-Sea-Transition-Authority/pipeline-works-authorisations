package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserToken;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.UserSessionPrivilegesService;

@ExtendWith(MockitoExtension.class)
class UserPrivReloadInterceptorTest {

  @Mock
  private UserSessionPrivilegesService userSessionPrivilegesService;

  @Mock
  private SecurityContext securityContext;

  private UserPrivReloadInterceptor userPrivReloadInterceptor;

  private AuthenticatedUserAccount user;

  @Mock
  private AuthenticatedUserToken existingAuthentication;

  private static final String SESSION_ID = "my-sesh";

  @BeforeEach
  void setup(){

    userPrivReloadInterceptor = new UserPrivReloadInterceptor(userSessionPrivilegesService);
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of(
        PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE, PwaUserPrivilege.PWA_CONSENT_SEARCH));

    SecurityContextHolder.setContext(securityContext);
    when(existingAuthentication.getPrincipal()).thenReturn(user);
    when(existingAuthentication.getSessionId()).thenReturn(SESSION_ID);
    when(securityContext.getAuthentication()).thenReturn(existingAuthentication);

  }

  @Test
  void prehandle_verifySecurityContextAuthSetupAsExpected() throws Exception {

    ArgumentCaptor<? extends Authentication> newAuthCaptor = ArgumentCaptor.forClass(Authentication.class);
    userPrivReloadInterceptor.preHandle(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new Object());

    verify(userSessionPrivilegesService, times(1)).populateUserPrivileges(user);
    verify(securityContext).setAuthentication(newAuthCaptor.capture());

    assertThat(newAuthCaptor.getValue()).satisfies(auth -> {
      assertThat(auth).isInstanceOf(AuthenticatedUserToken.class);
      assertThat(auth.isAuthenticated()).isTrue();
      assertThat(((AuthenticatedUserToken) auth).getSessionId()).isEqualTo(SESSION_ID);
    });

  }

}
