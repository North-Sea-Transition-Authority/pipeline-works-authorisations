package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.notify.emailproperties.updaterequests.ApplicationUpdateAcceptedEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmSatisfactoryApplicationServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private EmailCaseLinkService emailCaseLinkService;
  @Mock
  private NotifyService notifyService;

  @Before
  public void setUp() {
    confirmSatisfactoryApplicationService = new ConfirmSatisfactoryApplicationService(pwaApplicationDetailService,
        consultationRequestService,  emailCaseLinkService, notifyService);
  }

  @Test
  public void canShowInTaskList_confirmSatisfactoryApplicationPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION), null, null);

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_confirmSatisfactoryApplicationNotCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_COMPLETED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_confirmSatisfactoryApplicationCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void taskAccessible_notSatisfactory_true() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    assertThat(confirmSatisfactoryApplicationService.taskAccessible(processingContext)).isTrue();
    assertThat(confirmSatisfactoryApplicationService.isSatisfactory(detail)).isFalse();

  }

  @Test
  public void taskAccessible_satisfactory_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    assertThat(confirmSatisfactoryApplicationService.taskAccessible(processingContext)).isFalse();
    assertThat(confirmSatisfactoryApplicationService.isSatisfactory(detail)).isTrue();

  }

  @Test
  public void atLeastOneSatisfactoryVersion_true() {

    var firstVersionSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    firstVersionSatisfactory.setConfirmedSatisfactoryTimestamp(Instant.now());

    var secondVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(
        List.of(firstVersionSatisfactory, secondVersionNotSatisfactory));

    assertThat(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(new PwaApplication())).isTrue();

  }

  @Test
  public void atLeastOneSatisfactoryVersion_false() {

    var firstVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var secondVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(
        List.of(firstVersionNotSatisfactory, secondVersionNotSatisfactory));

    assertThat(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(new PwaApplication())).isFalse();

  }

  @Test
  public void confirmSatisfactory_notAlreadyCompleted_success_hasAssignedResponder() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var person = PersonTestUtil.createDefaultPerson();

    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setPwaApplication(detail.getPwaApplication());
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of(consultationRequest));

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("group");
    Map<ConsulteeGroup, ConsulteeGroupDetail> consulteeGroupAndDetailMap = new HashMap<>();
    consulteeGroupAndDetailMap.put(consulteeGroup, groupDetail);
    when(consultationRequestService.getGroupDetailsForConsulteeGroups(List.of(consultationRequest)))
        .thenReturn(consulteeGroupAndDetailMap);

    var assignedResponder = PersonTestUtil.createPersonFrom(new PersonId(2), "myEmail@mail.com");
    when(consultationRequestService.getAssignedResponderForConsultation(consultationRequest))
        .thenReturn(assignedResponder);

    var caseManagementLink = "link";
    when(emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    var emailProps = new ApplicationUpdateAcceptedEmailProps(
        assignedResponder.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person);

    // actual behaviour tested in app detail service unit test
    verify(pwaApplicationDetailService, times(1)).setConfirmedSatisfactoryData(detail, "my reason", person);
    verify(notifyService, times(1)).sendEmail(emailProps, assignedResponder.getEmailAddress());

  }

  @Test
  public void confirmSatisfactory_notAlreadyCompleted_success_noAssignedResponder() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var person = PersonTestUtil.createDefaultPerson();

    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setPwaApplication(detail.getPwaApplication());
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of(consultationRequest));

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("group");
    Map<ConsulteeGroup, ConsulteeGroupDetail> consulteeGroupAndDetailMap = new HashMap<>();
    consulteeGroupAndDetailMap.put(consulteeGroup, groupDetail);
    when(consultationRequestService.getGroupDetailsForConsulteeGroups(List.of(consultationRequest)))
        .thenReturn(consulteeGroupAndDetailMap);

    var consultationRecipient1 = PersonTestUtil.createPersonFrom(new PersonId(1), "myEmail@mail.com");
    var consultationRecipient2 = PersonTestUtil.createPersonFrom(new PersonId(2), "myEmail2@mail.com");
    when(consultationRequestService.getConsultationRecipients(consultationRequest))
        .thenReturn(List.of(consultationRecipient1, consultationRecipient2));

    var caseManagementLink = "link";
    when(emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    var emailPropsRecipient1 = new ApplicationUpdateAcceptedEmailProps(
        consultationRecipient1.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    var emailPropsRecipient2 = new ApplicationUpdateAcceptedEmailProps(
        consultationRecipient2.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person);

    // actual behaviour tested in app detail service unit test
    verify(pwaApplicationDetailService, times(1)).setConfirmedSatisfactoryData(detail, "my reason", person);


    verify(notifyService, times(2)).sendEmail(any(), any());
    verify(notifyService, atLeastOnce()).sendEmail(emailPropsRecipient1, consultationRecipient1.getEmailAddress());
    verify(notifyService, atLeastOnce()).sendEmail(emailPropsRecipient2, consultationRecipient2.getEmailAddress());

  }

  @Test(expected = IllegalStateException.class)
  public void confirmSatisfactory_alreadyCompleted_error() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());

    var person = PersonTestUtil.createDefaultPerson();

    confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person);

  }




}
