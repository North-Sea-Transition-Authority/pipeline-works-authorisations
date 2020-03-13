package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationBreadcrumbServiceTest {

  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Before
  public void setUp() {
    applicationBreadcrumbService = new ApplicationBreadcrumbService(pwaApplicationRedirectService);
  }

  @Test
  public void fromWorkArea() {
    var modelAndView = new ModelAndView();
    applicationBreadcrumbService.fromWorkArea(modelAndView, "My area");
    assertThat(modelAndView.getModel()).containsOnlyKeys("breadcrumbMap", "currentPage");
    assertThat(modelAndView.getModel().get("currentPage")).isEqualTo("My area");
    var breadcrumbMap = (Map<String, String>) modelAndView.getModel().get("breadcrumbMap");
    assertThat(breadcrumbMap).containsValue("Work area");
  }

  @Test
  public void fromTaskList() {

    for (var type : PwaApplicationType.values()) {

      var modelAndView = new ModelAndView();
      var pwaApplication = new PwaApplication();
      pwaApplication.setApplicationType(type);

      when(pwaApplicationRedirectService.getTaskListRoute(pwaApplication)).thenReturn("Route");

      applicationBreadcrumbService.fromTaskList(pwaApplication, modelAndView, "My area");
      assertThat(modelAndView.getModel()).containsOnlyKeys("breadcrumbMap", "currentPage");
      assertThat(modelAndView.getModel().get("currentPage")).isEqualTo("My area");

      var breadcrumbMap = (Map<String, String>) modelAndView.getModel().get("breadcrumbMap");
      assertThat(breadcrumbMap).containsValues("Work area", "Task list");
    }
  }
}