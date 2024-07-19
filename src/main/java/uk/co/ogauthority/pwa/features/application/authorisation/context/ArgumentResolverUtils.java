package uk.co.ogauthority.pwa.features.application.authorisation.context;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.SecurityUtils;

/**
 * <p>Utility class to provide common functions to argument resolvers.</p>
 *
 * <p>TODO split into app context arg resolver util and general case arg resolver util.</p>
 */
public class ArgumentResolverUtils {

  public static final String APPLICATION_ID_PARAM = "applicationId";
  public static final String MASTER_PWA_ID_PARAM = "pwaId";

  private ArgumentResolverUtils() {
    throw new AssertionError();
  }

  private static Map<String, String> getPathVariables(NativeWebRequest nativeWebRequest) {

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) Objects.requireNonNull(
        nativeWebRequest.getNativeRequest(HttpServletRequest.class))
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    return pathVariables;

  }

  /**
   * Return ID parameter in the URI, throw relevant exception if any issues encountered.
   */
  public static Integer resolveIdFromRequest(NativeWebRequest nativeWebRequest, String requestParam) {

    Map<String, String> pathVariables = getPathVariables(nativeWebRequest);

    if (pathVariables.get(requestParam) == null) {
      throw new NullPointerException(String.format("%s not found in URI", requestParam));
    }

    try {
      return Integer.parseInt(pathVariables.get(requestParam));
    } catch (NumberFormatException e) {
      throw new PwaEntityNotFoundException(String.format("PWA requests must have numeric IDs: %s - %s",
          requestParam, pathVariables.get(requestParam)));
    }

  }

  /**
   * If ID parameter exists in the URI, return the integer, otherwise return null.
   */
  public static Integer resolveIdFromRequestOrNull(NativeWebRequest nativeWebRequest, String requestParam) {

    try {
      return resolveIdFromRequest(nativeWebRequest, requestParam);
    } catch (NullPointerException e) {
      return null;
    }

  }

  public static String resolveStringFromRequestOrNull(NativeWebRequest nativeWebRequest, String requestParam) {
    return getPathVariables(nativeWebRequest).get(requestParam);
  }

  /**
   * Get method level status check or default to controller level if none specified.
   */
  public static Set<PwaApplicationStatus> getApplicationStatusCheck(MethodParameter methodParameter) {

    var methodLevelStatuses = Optional.ofNullable(methodParameter.getMethodAnnotation(PwaApplicationStatusCheck.class))
        .map(check -> Arrays.stream(check.statuses()).collect(Collectors.toSet()))
        .orElse(Set.of());

    if (!methodLevelStatuses.isEmpty()) {
      return methodLevelStatuses;
    }

    return Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationStatusCheck.class))
        .map(check -> Arrays.stream(check.statuses()).collect(Collectors.toSet()))
        .orElse(Set.of());

  }

  public static boolean ignoreChecksAnnotationPresent(MethodParameter methodParameter) {
    return Optional.ofNullable(methodParameter.getContainingClass().getAnnotation(PwaApplicationNoChecks.class))
        .isPresent();
  }

  public static AuthenticatedUserAccount getAuthenticatedUser() {
    return SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .orElseThrow(
            () -> new RuntimeException("Failed to get AuthenticatedUserAccount from current authentication context"));
  }

}
