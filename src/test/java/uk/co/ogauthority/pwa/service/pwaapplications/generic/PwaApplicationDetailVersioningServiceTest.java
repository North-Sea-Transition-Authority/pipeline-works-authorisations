package uk.co.ogauthority.pwa.service.pwaapplications.generic;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
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
        pwaApplicationDetailService,
        applicationTaskService);
  }

  @Test
  public void createNewApplicationVersion_serviceInteractions_whenMultipleTasksCanBeCopied() {

    var copyableTasks = EnumSet.of(ApplicationTask.APPLICATION_USERS, ApplicationTask.PROJECT_INFORMATION, ApplicationTask.FIELD_INFORMATION);

    copyableTasks.forEach(
        applicationTask -> when(applicationTaskService.taskAllowsCopySectionInformation(applicationTask, pwaApplicationDetail)).thenReturn(true)
    );
    when(applicationTaskService.taskAllowsCopySectionInformation(ApplicationTask.CARBON_STORAGE_INFORMATION, pwaApplicationDetail))
        .thenReturn(true);

    var fakeVersionedAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 11);

    when(pwaApplicationDetailService.createNewTipDetail(pwaApplicationDetail, PwaApplicationStatus.UPDATE_REQUESTED,
        webUserAccount
    )).thenReturn(fakeVersionedAppDetail);

    var newDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, webUserAccount);

    assertThat(newDetail).isEqualTo(fakeVersionedAppDetail);

    verify(pwaApplicationDetailService, times(1))
        .createNewTipDetail(pwaApplicationDetail, PwaApplicationStatus.UPDATE_REQUESTED, webUserAccount);

    // tasks that can be copied are checked then copied
    copyableTasks.forEach( applicationTask -> {
      verify(applicationTaskService, times(1))
          .taskAllowsCopySectionInformation(applicationTask, pwaApplicationDetail);

      verify(applicationTaskService, times(1))
          .copyApplicationTaskDataToApplicationDetail(applicationTask, pwaApplicationDetail, fakeVersionedAppDetail);

    });
    verify(applicationTaskService, never())
        .copyApplicationTaskDataToApplicationDetail(ApplicationTask.CARBON_STORAGE_INFORMATION, pwaApplicationDetail, fakeVersionedAppDetail);

    // tasks that cannot be copied are checked and not copied
    EnumSet.complementOf(copyableTasks).forEach(applicationTask -> {
      verify(applicationTaskService, times(1))
          .taskAllowsCopySectionInformation(applicationTask, pwaApplicationDetail);

      verify(applicationTaskService, times(0))
          .copyApplicationTaskDataToApplicationDetail(applicationTask, pwaApplicationDetail, fakeVersionedAppDetail);

    });

    verifyNoMoreInteractions(pwaApplicationDetailService, taskListService, applicationTaskService);

  }

  @Test
  public void createNewApplicationVersion_serviceInteractions_whenZeroTasksCanBeCopied() {

    var fakeVersionedAppDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 11);
    when(pwaApplicationDetailService.createNewTipDetail(pwaApplicationDetail, PwaApplicationStatus.UPDATE_REQUESTED, webUserAccount))
        .thenReturn(fakeVersionedAppDetail);

    var newDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, webUserAccount);

    assertThat(newDetail).isEqualTo(fakeVersionedAppDetail);

    verify(pwaApplicationDetailService, times(1))
        .createNewTipDetail(pwaApplicationDetail, PwaApplicationStatus.UPDATE_REQUESTED, webUserAccount);

    EnumSet.allOf(ApplicationTask.class).forEach(applicationTask -> {
      verify(applicationTaskService, times(1))
          .taskAllowsCopySectionInformation(applicationTask, pwaApplicationDetail);

    });


    verifyNoMoreInteractions(pwaApplicationDetailService, taskListService, applicationTaskService);

  }
}