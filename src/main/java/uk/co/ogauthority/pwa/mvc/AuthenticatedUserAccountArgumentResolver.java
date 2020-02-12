package uk.co.ogauthority.pwa.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.util.SecurityUtils;

public class AuthenticatedUserAccountArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(AuthenticatedUserAccount.class);
  }

  @Override
  public AuthenticatedUserAccount resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    return SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .orElseThrow(() -> new RuntimeException("Failed to get AuthenticatedUserAccount from current authentication context"));
  }

}
