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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextParams;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.ArgumentResolverUtils;

@Component
public class PwaAppProcessingContextArgumentResolver implements HandlerMethodArgumentResolver {

  private final PwaAppProcessingContextService pwaAppProcessingContextService;
  private static final Logger LOGGER = LoggerFactory.getLogger(PwaAppProcessingContextArgumentResolver.class);

  @Autowired
  public PwaAppProcessingContextArgumentResolver(PwaAppProcessingContextService pwaAppProcessingContextService) {
    this.pwaAppProcessingContextService = pwaAppProcessingContextService;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(PwaAppProcessingContext.class);
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                NativeWebRequest nativeWebRequest,
                                WebDataBinderFactory webDataBinderFactory) throws Exception {

    int applicationId = ArgumentResolverUtils.resolveIdFromRequest(nativeWebRequest, ArgumentResolverUtils.APPLICATION_ID_PARAM);
    var authenticatedUser = ArgumentResolverUtils.getAuthenticatedUser();

    boolean ignoreAllChecks = ArgumentResolverUtils.ignoreChecksAnnotationPresent(methodParameter);

    if (ignoreAllChecks) {
      LOGGER.debug("Ignoring all app processing context checks");
      return pwaAppProcessingContextService.validateAndCreate(new PwaAppProcessingContextParams(applicationId, authenticatedUser));
    }

    Set<PwaAppProcessingPermission> requiredPermissions = getProcessingPermissionsCheck(methodParameter);
    Set<PwaApplicationStatus> appStatuses = ArgumentResolverUtils.getApplicationStatusCheck(methodParameter);

    // blow up if no annotations used on controller
    if (requiredPermissions.isEmpty() && appStatuses.isEmpty()) {
      throw new AccessDeniedException(String.format("This controller has not been secured using annotations: %s",
          methodParameter.getContainingClass().getName()));
    }

    var contextParams = new PwaAppProcessingContextParams(applicationId, authenticatedUser)
        .requiredAppStatuses(appStatuses)
        .requiredProcessingPermissions(requiredPermissions)
        .withFileId(ArgumentResolverUtils.resolveStringFromRequestOrNull(nativeWebRequest, "fileId"));

    return pwaAppProcessingContextService.validateAndCreate(contextParams);

  }

  /**
   * Get method level permissions or default to controller level if none specified.
   */
  private Set<PwaAppProcessingPermission> getProcessingPermissionsCheck(MethodParameter methodParameter) {

    var methodLevelPermissions = Optional.ofNullable(
        methodParameter.getMethodAnnotation(PwaAppProcessingPermissionCheck.class))
        .map(p -> Arrays.stream(p.permissions())
        .collect(Collectors.toSet()))
        .orElse(Set.of());

    if (!methodLevelPermissions.isEmpty()) {
      return methodLevelPermissions;
    }

    return Optional.ofNullable(
        methodParameter.getContainingClass().getAnnotation(PwaAppProcessingPermissionCheck.class))
        .map(p -> Arrays.stream(p.permissions())
        .collect(Collectors.toSet()))
        .orElse(Set.of());

  }

}
