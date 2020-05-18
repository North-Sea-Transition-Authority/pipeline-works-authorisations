package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskCompletionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

public abstract class TaskListControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  protected TaskListService taskListService;

  @MockBean
  protected TaskCompletionService taskCompletionService;

  @MockBean(name = "contactServiceForTaskListService")
  protected PwaContactService pwaContactService;

  @MockBean
  protected PadPipelineService padPipelineService;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Before
  public void taskListControllerTestSetup() {
    taskListService = new TaskListService(pwaApplicationRedirectService, applicationBreadcrumbService,
        padFastTrackService, taskCompletionService, pwaContactService, permanentDepositService);
    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(ModelAndView.class), eq("Task list"));

  }

}
