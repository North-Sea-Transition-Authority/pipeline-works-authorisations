package uk.co.ogauthority.pwa.service.pwaapplications.generic;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailVersioningServiceTest {

  private final int APP_ID = 1;

  @Mock
  private TaskListService taskListService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ApplicationTaskService applicationTaskService;

  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  private PwaApplicationDetail pwaApplicationDetail;

  private WebUserAccount webUserAccount;

  @Before
  public void setUp() throws Exception {

    webUserAccount = new WebUserAccount(1);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 10);

    pwaApplicationDetailVersioningService = new PwaApplicationDetailVersioningService(
        taskListService,
        pwaApplicationDetailService,
        applicationTaskService);
  }

  @Test
  public void createNewApplicationVersion_serviceInteractions_whenMultipleTasks() {

    var shownTasks = List.of(ApplicationTask.APPLICATION_USERS, ApplicationTask.HUOO);
    when(taskListService.getShownApplicationTasksForDetail(pwaApplicationDetail)).thenReturn(shownTasks);
    var fakeVersionedAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 11);
    when(pwaApplicationDetailService.createNewTipDetail(pwaApplicationDetail, webUserAccount)).thenReturn(fakeVersionedAppDetail);

    var newDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, webUserAccount);

    assertThat(newDetail).isEqualTo(fakeVersionedAppDetail);

    verify(pwaApplicationDetailService, times(1)).createNewTipDetail(pwaApplicationDetail, webUserAccount);
    verify(taskListService, times(1)).getShownApplicationTasksForDetail(pwaApplicationDetail);
    verify(applicationTaskService, times(1))
        .copyApplicationTaskDataToApplicationDetail(ApplicationTask.APPLICATION_USERS, pwaApplicationDetail, fakeVersionedAppDetail);
    verify(applicationTaskService, times(1))
        .copyApplicationTaskDataToApplicationDetail(ApplicationTask.HUOO, pwaApplicationDetail, fakeVersionedAppDetail);
    verifyNoMoreInteractions(pwaApplicationDetailService, taskListService, applicationTaskService);

  }

  @Test
  public void createNewApplicationVersion_serviceInteractions_whenZeroTasks() {

    var fakeVersionedAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 11);
    when(pwaApplicationDetailService.createNewTipDetail(pwaApplicationDetail, webUserAccount)).thenReturn(fakeVersionedAppDetail);

    var newDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, webUserAccount);

    assertThat(newDetail).isEqualTo(fakeVersionedAppDetail);

    verify(pwaApplicationDetailService, times(1)).createNewTipDetail(pwaApplicationDetail, webUserAccount);
    verify(taskListService, times(1)).getShownApplicationTasksForDetail(pwaApplicationDetail);
    verifyNoMoreInteractions(pwaApplicationDetailService, taskListService, applicationTaskService);

  }
}