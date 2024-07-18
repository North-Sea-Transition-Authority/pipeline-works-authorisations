package uk.co.ogauthority.pwa.mvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public class ReverseRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReverseRouter.class);

  private ReverseRouter() {
  }

  public static String routeWithQueryParamMap(Object methodCall, MultiValueMap<String, String> paramMap) {
    return route(methodCall, Collections.emptyMap(), true, paramMap);
  }

  public static String route(Object methodCall) {
    return route(methodCall, Collections.emptyMap(), true, Collections.emptyMap());
  }

  public static String route(Object methodCall, Map<String, Object> uriVariables) {
    return route(methodCall, uriVariables, true, Collections.emptyMap());
  }

  @SuppressWarnings("unchecked")
  public static String route(Object methodCall,
                             Map<String,Object> uriVariables,
                             boolean expandUriVariablesFromRequest,
                             Map<String, List<String>> paramMap) {
    //Establish URI variables to substitute - explicitly provided should take precedence
    Map<String, Object> allUriVariables = new HashMap<>();
    if (expandUriVariablesFromRequest) {
      RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
      if (requestAttributes == null) {
        LOGGER.info("Cannot expand request parameters when RequestAttributes is null");
      } else {
        Map<String, Object> requestAttributeMap = (Map<String, Object>) requestAttributes.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        if (requestAttributeMap != null) {
          allUriVariables.putAll(requestAttributeMap);
        }
      }
    }

    allUriVariables.putAll(uriVariables);

    //Use a UriComponentsBuilder which is not scoped to the request to get relative URIs (instead of absolute)
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
    builder = MvcUriComponentsBuilder
        .fromMethodCall(builder, methodCall);

    // add query params if any have been provided
    if (!paramMap.isEmpty()) {
      builder = builder
          .queryParams(new LinkedMultiValueMap<>(paramMap));
    }

    return builder
        .buildAndExpand(allUriVariables)
        .encode()
        .toUriString();

  }

  public static ModelAndView redirect(Object methodCall) {
    return redirect(methodCall, Collections.emptyMap());
  }

  public static ModelAndView redirect(Object methodCall, Map<String, Object> uriVariables) {
    return redirect(methodCall, uriVariables, true);
  }

  public static ModelAndView redirect(Object methodCall,
                                      Map<String, Object> uriVariables,
                                      boolean expandUriVariablesFromRequest) {
    return new ModelAndView("redirect:" + route(methodCall, uriVariables, expandUriVariablesFromRequest, Collections.emptyMap()));
  }

  public static ModelAndView redirectWithQueryParamMap(Object methodCall,
                                                       MultiValueMap<String, String> queryParamMap) {
    return new ModelAndView("redirect:" + route(methodCall, Map.of(), true, queryParamMap));
  }

  /**
   * Return an empty BindingResult.
   * Used to avoid passing null into a BindingResult route parameter, which then will cause null warnings to be thrown by IJ if
   * the controller invokes methods on the BindingResult, as it now thinks null is a possible runtime value.
   * @return An empty BindingResult
   */
  public static BindingResult emptyBindingResult() {
    return new BeanPropertyBindingResult(null, "empty");
  }
}