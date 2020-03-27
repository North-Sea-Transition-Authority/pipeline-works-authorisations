package uk.co.ogauthority.pwa.mvc;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@Component
public class PwaApplicationContextArgumentResolver implements HandlerMethodArgumentResolver {

  private final String applicationIdParam = "applicationId";
  private final PwaApplicationContextService pwaApplicationContextService;

  @Autowired
  public PwaApplicationContextArgumentResolver(PwaApplicationContextService pwaApplicationContextService) {
    this.pwaApplicationContextService = pwaApplicationContextService;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(PwaApplicationContext.class);
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                NativeWebRequest nativeWebRequest,
                                WebDataBinderFactory webDataBinderFactory) throws Exception {

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) Objects.requireNonNull(nativeWebRequest.getNativeRequest(HttpServletRequest.class))
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    int applicationId;

    try {
      applicationId = Integer.parseInt(pathVariables.get(applicationIdParam));
    } catch (NumberFormatException e) {
      throw new PwaEntityNotFoundException("PWA applications must have numeric IDs");
    }

    Set<PwaApplicationPermission> requiredPermissions = getApplicationPermissionsCheck(methodParameter);
    PwaApplicationStatus appStatus = getApplicationStatusCheck(methodParameter);
    Set<PwaApplicationType> applicationTypes = getApplicationTypeCheck(methodParameter);

    var authenticatedUser = SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .orElseThrow(() -> new RuntimeException("Failed to get AuthenticatedUserAccount from current authentication context"));

    return pwaApplicationContextService.getApplicationContext(
        applicationId,
        authenticatedUser,
        requiredPermissions,
        appStatus,
        applicationTypes);

  }

  private PwaApplicationStatus getApplicationStatusCheck(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getMethodAnnotation(PwaApplicationStatusCheck.class))
        .map(PwaApplicationStatusCheck::status)
        .orElse(null);
  }

  private Set<PwaApplicationPermission> getApplicationPermissionsCheck(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getMethodAnnotation(PwaApplicationPermissionCheck.class))
        .map(a -> Arrays.stream(a.permissions()).collect(Collectors.toSet()))
        .orElse(Set.of());
  }

  private Set<PwaApplicationType> getApplicationTypeCheck(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationTypeCheck.class))
        .map(t -> Arrays.stream(t.types()).collect(Collectors.toSet()))
        .orElse(Set.of());
  }

}
