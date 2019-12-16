package uk.co.ogauthority.pipelines.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;

public class UserAccountArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(UserAccount.class);
  }

  @Override
  public UserAccount resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    } else if (authentication.getPrincipal() == null) {
      throw new RuntimeException("Principal was null when trying to resolve controller argument");
    } else if (!(authentication.getPrincipal() instanceof UserAccount)) {
      String error = String.format("Principal was not a UserAccount when trying to resolve controller argument (was a %s)",
          authentication.getPrincipal().getClass());
      throw new RuntimeException(error);
    } else {
      return (UserAccount) authentication.getPrincipal();
    }
  }

}
