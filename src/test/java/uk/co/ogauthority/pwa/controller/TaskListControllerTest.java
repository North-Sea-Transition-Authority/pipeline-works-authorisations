package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;

import org.junit.Before;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

public abstract class TaskListControllerTest extends AbstractControllerTest {

  @MockBean
  protected ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  protected TaskListService taskListService;

  @Before
  public void taskListControllerTestSetup() {
    taskListService = new TaskListService(pwaApplicationRedirectService, applicationBreadcrumbService);
    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(ModelAndView.class), eq("Task list"));
  }

}
