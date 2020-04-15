package uk.co.ogauthority.pwa.mvc;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationNoChecks;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(PwaApplicationContextArgumentResolver.class);

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

    var applicationId = resolveApplicationIdFromRequest(nativeWebRequest);

    var authenticatedUser = SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .orElseThrow(
            () -> new RuntimeException("Failed to get AuthenticatedUserAccount from current authentication context"));

    if (ignoreAllChecks(methodParameter)) {
      LOGGER.debug("Ignoring all application context checks");
      return pwaApplicationContextService.getApplicationContext(applicationId, authenticatedUser);
    }

    Set<PwaApplicationPermission> requiredPermissions = getApplicationPermissionsCheck(methodParameter);
    PwaApplicationStatus appStatus = getApplicationStatusCheck(methodParameter);
    Set<PwaApplicationType> applicationTypes = getApplicationTypeCheck(methodParameter);

    // blow up if no annotations used on controller
    if (requiredPermissions.isEmpty() && appStatus == null && applicationTypes.isEmpty()) {
      throw new AccessDeniedException(String.format("This controller has not been secured using annotations: %s",
          methodParameter.getContainingClass().getName()));
    }

    return pwaApplicationContextService.createAndPerformApplicationContextChecks(
        applicationId,
        authenticatedUser,
        requiredPermissions,
        appStatus,
        applicationTypes);

  }

  private int resolveApplicationIdFromRequest(NativeWebRequest nativeWebRequest) {
    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) Objects.requireNonNull(
        nativeWebRequest.getNativeRequest(HttpServletRequest.class))
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    try {
      return Integer.parseInt(pathVariables.get(applicationIdParam));
    } catch (NumberFormatException e) {
      throw new PwaEntityNotFoundException("PWA applications must have numeric IDs");
    }
  }

  /**
   * Get method level status or default to controller level if none specified.
   */
  private PwaApplicationStatus getApplicationStatusCheck(MethodParameter methodParameter) {

    var methodLevelStatus = Optional.ofNullable(methodParameter.getMethodAnnotation(PwaApplicationStatusCheck.class))
        .map(PwaApplicationStatusCheck::status);

    return methodLevelStatus.orElseGet(
        () -> Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationStatusCheck.class))
            .map(PwaApplicationStatusCheck::status)
            .orElse(null));

  }

  private boolean ignoreAllChecks(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationNoChecks.class))
        .isPresent();
  }

  /**
   * Get method level permissions or default to controller level if none specified.
   */
  private Set<PwaApplicationPermission> getApplicationPermissionsCheck(MethodParameter methodParameter) {

    var methodLevelPermissions = Optional.ofNullable(
        methodParameter.getMethodAnnotation(PwaApplicationPermissionCheck.class))
        .map(a -> Arrays.stream(a.permissions()).collect(Collectors.toSet()))
        .orElse(Set.of());

    if (!methodLevelPermissions.isEmpty()) {
      return methodLevelPermissions;
    }

    return Optional.ofNullable(
        methodParameter.getContainingClass().getAnnotation(PwaApplicationPermissionCheck.class))
        .map(a -> Arrays.stream(a.permissions()).collect(Collectors.toSet()))
        .orElse(Set.of());
  }

  private Set<PwaApplicationType> getApplicationTypeCheck(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationTypeCheck.class))
        .map(t -> Arrays.stream(t.types()).collect(Collectors.toSet()))
        .orElse(Set.of());
  }

}
