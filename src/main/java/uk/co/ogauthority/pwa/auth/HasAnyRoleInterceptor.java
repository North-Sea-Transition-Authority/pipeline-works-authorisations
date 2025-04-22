package uk.co.ogauthority.pwa.auth;

import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@Component
public class HasAnyRoleInterceptor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(HasAnyRoleInterceptor.class);

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasAnyRoles.class,
      HasAnyRole.class,
      HasAnyRoleByGroup.class
  );

  private final HasTeamRoleService hasTeamRoleService;

  @Autowired
  public HasAnyRoleInterceptor(HasTeamRoleService hasTeamRoleService) {
    this.hasTeamRoleService = hasTeamRoleService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      Map<TeamType, Set<Role>> rolesByTeamType = getRolesByTeamType(handlerMethod);

      var user = SecurityUtils.getAuthenticatedUserFromSecurityContext()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
              "Failed to get AuthenticatedUserAccount from current authentication context"));

      var userHasRole = hasTeamRoleService.userHasAnyRoleInTeamTypes(user, rolesByTeamType);

      if (!userHasRole) {

        var errorMessage = getUserHasNoMatchingRoleErrorMessage(rolesByTeamType, user);

        LOGGER.warn(errorMessage);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
      }
    }

    return true;
  }

  private Map<TeamType, Set<Role>> getRolesByTeamType(HandlerMethod handlerMethod) {
    if (hasAnnotation(handlerMethod, HasAnyRoles.class)) {
      Set<HasAnyRole> hasAnyRoleSet = Sets.newHashSet(((HasAnyRoles) getAnnotation(handlerMethod, HasAnyRoles.class)).value());
      return groupRolesByTeamType(hasAnyRoleSet);

    } else if (hasAnnotation(handlerMethod, HasAnyRole.class)) {
      Set<HasAnyRole> hasAnyRoleSet = Sets.newHashSet(((HasAnyRole) getAnnotation(handlerMethod, HasAnyRole.class)));
      return groupRolesByTeamType(hasAnyRoleSet);

    } else if (hasAnnotation(handlerMethod, HasAnyRoleByGroup.class)) {
      return ((HasAnyRoleByGroup) getAnnotation(handlerMethod, HasAnyRoleByGroup.class)).roleGroup().getRolesByTeamType();
    }

    return Map.of();
  }

  private Map<TeamType, Set<Role>> groupRolesByTeamType(Set<HasAnyRole> hasAnyRoleSet) {
    return hasAnyRoleSet.stream()
        .collect(Collectors.groupingBy(
            HasAnyRole::teamType,
            Collectors.mapping(HasAnyRole::roles, Collectors.flatMapping(Arrays::stream, Collectors.toSet()))
        ));
  }

  private String getUserHasNoMatchingRoleErrorMessage(Map<TeamType, Set<Role>> groupedRoles, AuthenticatedUserAccount user) {
    var requiredRoles = String.join(", ",
        groupedRoles.entrySet()
            .stream()
            .flatMap(hasAnyRole -> hasAnyRole.getValue().stream())
            .map(Role::name)
            .distinct()
            .toList());

    return "User with ID %s doesn't have any of the required roles: %s"
        .formatted(user.getWuaId(), requiredRoles);
  }

  public boolean hasAnnotations(HandlerMethod handlerMethod,
                                Set<Class<? extends Annotation>> annotationClasses) {
    return annotationClasses
        .stream()
        .anyMatch(annotationClass -> hasAnnotation(handlerMethod, annotationClass));
  }

  public boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return handlerMethod.hasMethodAnnotation(annotation)
        || handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(annotation);
  }

  public Annotation getAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return Objects.requireNonNullElse(
        handlerMethod.getMethodAnnotation(annotation),
        handlerMethod.getMethod().getDeclaringClass().getAnnotation(annotation)
    );
  }
}
