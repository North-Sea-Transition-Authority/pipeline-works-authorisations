package uk.co.ogauthority.pwa.mvc.argresolvers;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextParams;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.util.ArgumentResolverUtils;

@Component
public class PwaApplicationContextArgumentResolver implements HandlerMethodArgumentResolver {

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

    int applicationId = ArgumentResolverUtils.resolveIdFromRequest(nativeWebRequest, ArgumentResolverUtils.APPLICATION_ID_PARAM);
    var authenticatedUser = ArgumentResolverUtils.getAuthenticatedUser();

    boolean ignoreAllChecks = ArgumentResolverUtils.ignoreChecksAnnotationPresent(methodParameter);

    if (ignoreAllChecks) {
      LOGGER.debug("Ignoring all application context checks");
      return pwaApplicationContextService.validateAndCreate(new PwaApplicationContextParams(applicationId, authenticatedUser));
    }

    Set<PwaApplicationPermission> requiredPermissions = getApplicationPermissionsCheck(methodParameter);
    Set<PwaApplicationStatus> appStatuses = ArgumentResolverUtils.getApplicationStatusCheck(methodParameter);
    Set<PwaApplicationType> applicationTypes = getApplicationTypeCheck(methodParameter);

    // blow up if no annotations used on controller
    if (requiredPermissions.isEmpty() && appStatuses.isEmpty() && applicationTypes.isEmpty()) {
      throw new AccessDeniedException(String.format("This controller has not been secured using annotations: %s",
          methodParameter.getContainingClass().getName()));
    }

    var contextParams = new PwaApplicationContextParams(applicationId, authenticatedUser)
        .requiredAppStatuses(appStatuses)
        .requiredAppTypes(applicationTypes)
        .requiredUserPermissions(requiredPermissions)
        .withPadPipelineId(ArgumentResolverUtils.resolveIdFromRequestOrNull(nativeWebRequest, "padPipelineId"))
        .withFileId(ArgumentResolverUtils.resolveStringFromRequestOrNull(nativeWebRequest, "fileId"));

    return pwaApplicationContextService.validateAndCreate(contextParams);

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
