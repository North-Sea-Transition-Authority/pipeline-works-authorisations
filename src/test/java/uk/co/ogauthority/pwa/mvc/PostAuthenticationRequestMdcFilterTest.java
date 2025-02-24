package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;

@ExtendWith(MockitoExtension.class)
class PostAuthenticationRequestMdcFilterTest {

  @Mock
  private MockFilterChain filterChain;

  @InjectMocks
  private PostAuthenticationRequestMdcFilter postAuthenticationRequestMdcFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    MDC.remove(RequestLogFilter.MDC_WUA_ID);
  }

  @Test
  void doFilterInternal_noUser() throws ServletException, IOException {
    PostAuthenticationRequestMdcFilter spy = spy(postAuthenticationRequestMdcFilter);
    when(spy.isUserLoggedIn()).thenReturn(false);

    spy.doFilterInternal(request, response, filterChain);

    assertThat(MDC.get(RequestLogFilter.MDC_WUA_ID)).isNull();

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_userExistsNoProxy() throws ServletException, IOException {
    var userWithoutProxyWuaId = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

    PostAuthenticationRequestMdcFilter spy = spy(postAuthenticationRequestMdcFilter);
    when(spy.isUserLoggedIn()).thenReturn(true);
    when(spy.getAuthenticatedUserAccount()).thenReturn(Optional.of(userWithoutProxyWuaId));

    spy.doFilterInternal(request, response, filterChain);

    assertThat(MDC.get(RequestLogFilter.MDC_WUA_ID))
        .isEqualTo(String.valueOf(userWithoutProxyWuaId.getWuaId()));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_userExistsWithProxy() throws ServletException, IOException {
    var userWithProxyWuaId = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();
    userWithProxyWuaId.setProxyUserWuaId(1);

    PostAuthenticationRequestMdcFilter spy = spy(postAuthenticationRequestMdcFilter);
    when(spy.isUserLoggedIn()).thenReturn(true);
    when(spy.getAuthenticatedUserAccount()).thenReturn(Optional.of(userWithProxyWuaId));

    spy.doFilterInternal(request, response, filterChain);

    assertThat(MDC.get(RequestLogFilter.MDC_WUA_ID))
        .isEqualTo(userWithProxyWuaId.getProxyUserWuaId().orElse(null).toString());

    verify(filterChain).doFilter(request, response);
  }
}