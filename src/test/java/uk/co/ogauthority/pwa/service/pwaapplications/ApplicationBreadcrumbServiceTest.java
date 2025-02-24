package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;

@ExtendWith(MockitoExtension.class)
class ApplicationBreadcrumbServiceTest {

  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @BeforeEach
  void setUp() {
    applicationBreadcrumbService = new ApplicationBreadcrumbService(pwaApplicationRedirectService,
        crossingAgreementsTaskListService);
  }

  @Test
  void fromWorkArea() {
    var modelAndView = new ModelAndView();
    applicationBreadcrumbService.fromWorkArea(modelAndView, "My area");
    assertThat(modelAndView.getModel()).containsOnlyKeys("breadcrumbMap", "currentPage");
    assertThat(modelAndView.getModel().get("currentPage")).isEqualTo("My area");
    var breadcrumbMap = (Map<String, String>) modelAndView.getModel().get("breadcrumbMap");
    assertThat(breadcrumbMap).containsValue("Work area");
  }

  @Test
  void fromTaskList() {

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