package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationTaskService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListEntryFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist.TaskListControllerModelAndViewCreator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

public abstract class TaskListControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  protected TaskListService taskListService;

  @MockBean
  protected TaskListControllerModelAndViewCreator taskListControllerModelAndViewCreator;

  @MockBean
  protected TaskListEntryFactory taskListEntryFactory;

  @MockBean
  protected ApplicationTaskService applicationTaskService;

  @MockBean(name = "contactServiceForTaskListService")
  protected PwaContactService pwaContactService;

  @MockBean
  protected PadPipelineService padPipelineService;

  @MockBean
  protected PadFileService padFileService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  private MasterPwaViewService masterPwaViewService;

  @Mock
  private MasterPwaView masterPwaView;

  @Mock
  private ApplicationFormSectionService applicationFormSectionService;

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Before
  public void taskListControllerTestSetup() {
    when(masterPwaView.getReference()).thenReturn("EXAMPLE_REFERENCE");
    when(masterPwaViewService.getCurrentMasterPwaView(any())).thenReturn(masterPwaView);


//    taskListService = new TaskListService(
//        taskListEntryFactory,
//        applicationTaskService
//    );

    taskListControllerModelAndViewCreator = new TaskListControllerModelAndViewCreator(
        applicationBreadcrumbService,
        taskListEntryFactory,
        masterPwaViewService,
        applicationUpdateRequestViewService,
        approveOptionsService,
        pwaApplicationDetailService);

    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(ModelAndView.class), eq("Task list"));

  }

}
