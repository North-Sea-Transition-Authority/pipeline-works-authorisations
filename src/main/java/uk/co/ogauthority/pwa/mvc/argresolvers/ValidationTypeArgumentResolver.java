package uk.co.ogauthority.pwa.mvc.argresolvers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

/**
 * This class provides a way to transform the results of clicking app form submit buttons (either 'Save and complete later'
 * or 'Save and complete' into the type of validation which should be performed for each.
 * Should only be used when posting a form page that is using the submitButtons macro.
 */
public class ValidationTypeArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(ValidationType.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    // see which validation type's button was clicked
    List<ValidationType> resolvedTypes = Stream.of(ValidationType.values())
        .filter(validationType -> webRequest.getParameter(validationType.getButtonText()) != null)
        .collect(Collectors.toList());

    if (resolvedTypes.isEmpty()) {
      throw new IllegalStateException("Can't resolve ValidationType, unrecognised button text");
    }

    if (resolvedTypes.size() > 1) {

      String typesCsv = resolvedTypes.stream()
          .map(Enum::name)
          .collect(Collectors.joining(","));

      throw new IllegalStateException(String.format("Too many ValidationTypes resolved, expected 1 only: [%s]", typesCsv));

    }

    return resolvedTypes.get(0);

  }

}
