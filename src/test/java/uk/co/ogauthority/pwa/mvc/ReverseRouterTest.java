package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest(ReverseRouterTest.class) // We need a WebApplicationContext to test the reverse router
@Import(PwaMvcTestConfiguration.class)
public class ReverseRouterTest extends AbstractControllerTest {

  @Before
  public void setUp() {
    Map<String, Object> uriTemplateVariablesMap = new HashMap<>();
    uriTemplateVariablesMap.put("parentId", "request_parent_id");
    uriTemplateVariablesMap.put("childId", "request_child_id");

    MockHttpServletRequest request = new MockHttpServletRequest();
    ServletRequestAttributes attributes = new ServletRequestAttributes(request);
    attributes.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariablesMap,
        RequestAttributes.SCOPE_REQUEST);
    RequestContextHolder.setRequestAttributes(attributes);
  }

  @Test
  public void testReverseRouter_route() {

    String route = ReverseRouter.route(on(TestController.class).testMethod("method_child_id"));
    assertThat(route).isEqualTo("/parent/request_parent_id/child/method_child_id");

    // Variable from request should be overridden by variable in map
    route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"), Collections.singletonMap("parentId", "map_parent_id"));
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");

    // Variable from request should be overridden by variable in map even when request substitution is disabled
    route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"), Collections.singletonMap("parentId", "map_parent_id"),
        false, Collections.emptyMap());
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");

    // Variable from method parameter should NOT be overridden by variable in map
    Map<String, Object> methodParams = new HashMap<>();
    methodParams.put("parentId", "map_parent_id");
    methodParams.put("childId", "map_child_id");
    route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"), methodParams);
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");

    // Should throw exception if we don't allow variables from the request
    assertThatThrownBy(
        () -> ReverseRouter.route(on(TestController.class).testMethod("method_child_id"), Collections.emptyMap(),
            false, Collections.emptyMap()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Map has no value");
  }

  @Test
  public void testReverseRouter_redirect() {
    // Redirect should behave as route does, with "redirect:/" prefix on results
    ModelAndView redirect = ReverseRouter.redirect(on(TestController.class).testMethod("method_child_id"));
    assertThat(redirect.getViewName()).isEqualTo("redirect:/parent/request_parent_id/child/method_child_id");

    redirect = ReverseRouter.redirect(
        on(TestController.class).testMethod("method_child_id"), Collections.singletonMap("parentId", "map_parent_id"));
    assertThat(redirect.getViewName()).isEqualTo("redirect:/parent/map_parent_id/child/method_child_id");

    redirect = ReverseRouter.redirect(
        on(TestController.class).testMethod("method_child_id"), Collections.singletonMap("parentId", "map_parent_id"),
        false);
    assertThat(redirect.getViewName()).isEqualTo("redirect:/parent/map_parent_id/child/method_child_id");
  }

  @RequestMapping("/parent/{parentId}")
  public static class TestController {

    @GetMapping("/child/{childId}")
    public ModelAndView testMethod(@PathVariable String childId) {
      return new ModelAndView();
    }
  }
}