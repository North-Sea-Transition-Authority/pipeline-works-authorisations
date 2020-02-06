package uk.co.ogauthority.pwa.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

public class UserAccountArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(AuthenticatedUserAccount.class);
  }

  @Override
  public AuthenticatedUserAccount resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new RuntimeException("SecurityContext contained no Authentication object when trying to resolve controller argument");
    } else if (authentication.getPrincipal() == null) {
      throw new RuntimeException("Principal was null when trying to resolve controller argument");
    } else if (!(authentication.getPrincipal() instanceof AuthenticatedUserAccount)) {
      String error = String.format("Principal was not a AuthenticatedUserAccount when trying to resolve controller argument (was a %s)",
          authentication.getPrincipal().getClass());
      throw new RuntimeException(error);
    } else {
      return (AuthenticatedUserAccount) authentication.getPrincipal();
    }
  }

}
