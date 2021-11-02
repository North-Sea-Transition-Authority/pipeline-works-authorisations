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
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.ArgumentResolverUtils;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextParams;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;

@Component
public class PwaContextArgumentResolver implements HandlerMethodArgumentResolver {

  private final PwaContextService pwaContextService;
  private static final Logger LOGGER = LoggerFactory.getLogger(PwaContextArgumentResolver.class);

  @Autowired
  public PwaContextArgumentResolver(PwaContextService pwaContextService) {
    this.pwaContextService = pwaContextService;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(PwaContext.class);
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                NativeWebRequest nativeWebRequest,
                                WebDataBinderFactory webDataBinderFactory) throws Exception {

    int pwaId = ArgumentResolverUtils.resolveIdFromRequest(nativeWebRequest, ArgumentResolverUtils.MASTER_PWA_ID_PARAM);
    var authenticatedUser = ArgumentResolverUtils.getAuthenticatedUser();

    boolean ignoreAllChecks = ArgumentResolverUtils.ignoreChecksAnnotationPresent(methodParameter);

    if (ignoreAllChecks) {
      LOGGER.debug("Ignoring all pwa context checks");
      return pwaContextService.validateAndCreate(new PwaContextParams(pwaId, authenticatedUser));
    }

    Set<PwaPermission> requiredRoles = getPwaPermissionsCheck(methodParameter);

    // blow up if no annotations used on controller
    if (requiredRoles.isEmpty()) {
      throw new AccessDeniedException(String.format("This controller has not been secured using annotations: %s",
          methodParameter.getContainingClass().getName()));
    }

    var contextParams = new PwaContextParams(pwaId, authenticatedUser)
        .requiredPwaPermissions(requiredRoles)
        .withPipelineId(ArgumentResolverUtils.resolveIdFromRequestOrNull(nativeWebRequest, "pipelineId"));

    return pwaContextService.validateAndCreate(contextParams);

  }

  /**
   * Get method level permissions or default to controller level if none specified.
   */
  private Set<PwaPermission> getPwaPermissionsCheck(MethodParameter methodParameter) {

    var methodLevelPermissions = Optional.ofNullable(
        methodParameter.getMethodAnnotation(PwaPermissionCheck.class))
        .map(p -> Arrays.stream(p.permissions())
        .collect(Collectors.toSet()))
        .orElse(Set.of());

    if (!methodLevelPermissions.isEmpty()) {
      return methodLevelPermissions;
    }

    return Optional.ofNullable(
        methodParameter.getContainingClass().getAnnotation(PwaPermissionCheck.class))
        .map(p -> Arrays.stream(p.permissions())
        .collect(Collectors.toSet()))
        .orElse(Set.of());

  }

}
