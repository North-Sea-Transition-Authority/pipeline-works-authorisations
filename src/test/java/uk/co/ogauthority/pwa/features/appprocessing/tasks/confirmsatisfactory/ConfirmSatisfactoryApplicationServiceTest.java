package uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests.ApplicationUpdateAcceptedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConfirmSatisfactoryApplicationServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private CaseLinkService caseLinkService;
  @Mock
  private EmailService emailService;

  @Mock
  private PadPipelineTransferService pipelineTransferService;

  @BeforeEach
  void setUp() {
    confirmSatisfactoryApplicationService = new ConfirmSatisfactoryApplicationService(pwaApplicationDetailService,
        consultationRequestService, caseLinkService, pipelineTransferService, emailService);
  }

  @Test
  void canShowInTaskList_confirmSatisfactoryApplicationPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION), null, null,
        Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null,
        Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_showAllTasksPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_hasCaseManagementOgaPermission_appCompleted_true() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA), null, null,
        Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();
  }

  @Test
  void canShowInTaskList_hasCaseManagementOgaPermission_appNotCompleted_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA), null, null,
        Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();
  }

  @Test
  void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null, Set.of());

    boolean canShow = confirmSatisfactoryApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void getTaskListEntry_confirmSatisfactoryApplicationNotCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_confirmSatisfactoryApplicationCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.COMPLETED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_showAllTasksPermission_taskStateLocked() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY), null, null,
        Set.of());

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_invalidAppStatus_taskStateLocked() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(detail);

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);

  }

  @Test
  void getTaskListEntry_validAppStatus_taskStateEditable() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = PwaAppProcessingContextTestUtil.withoutPermissions(detail);

    var taskListEntry = confirmSatisfactoryApplicationService.getTaskListEntry(PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);

  }

  @Test
  void taskAccessible_notSatisfactory_true() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    assertThat(confirmSatisfactoryApplicationService.taskAccessible(processingContext)).isTrue();
    assertThat(confirmSatisfactoryApplicationService.isSatisfactory(detail)).isFalse();

  }

  @Test
  void taskAccessible_satisfactory_false() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    assertThat(confirmSatisfactoryApplicationService.taskAccessible(processingContext)).isFalse();
    assertThat(confirmSatisfactoryApplicationService.isSatisfactory(detail)).isTrue();

  }

  @Test
  void atLeastOneSatisfactoryVersion_true() {

    var firstVersionSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    firstVersionSatisfactory.setConfirmedSatisfactoryTimestamp(Instant.now());

    var secondVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(
        List.of(firstVersionSatisfactory, secondVersionNotSatisfactory));

    assertThat(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(new PwaApplication())).isTrue();

  }

  @Test
  void atLeastOneSatisfactoryVersion_false() {

    var firstVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var secondVersionNotSatisfactory = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(any())).thenReturn(
        List.of(firstVersionNotSatisfactory, secondVersionNotSatisfactory));

    assertThat(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(new PwaApplication())).isFalse();

  }

  @Test
  void confirmSatisfactoryTaskRequired_detailIsFirstVersion_taskNotRequired() {

    var firstVersionDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    assertThat(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(firstVersionDetail)).isFalse();
  }

  @Test
  void confirmSatisfactoryTaskRequired_detailIsNotFirstVersion_detailIsSatisfactory_taskNotRequired() {

    var firstVersionDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    firstVersionDetail.setConfirmedSatisfactoryTimestamp(Instant.now());
    assertThat(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(firstVersionDetail)).isFalse();
  }

  @Test
  void confirmSatisfactoryTaskRequired_detailIsNotFirstVersion_detailIsNotSatisfactory_taskRequired() {

    var firstVersionDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    firstVersionDetail.setVersionNo(2);
    assertThat(confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(firstVersionDetail)).isTrue();
  }

  @Test
  void confirmSatisfactory_notAlreadyCompleted_success_hasAssignedResponder() {

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
    when(caseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    var emailProps = new ApplicationUpdateAcceptedEmailProps(
        assignedResponder.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person);

    // actual behaviour tested in app detail service unit test
    verify(pwaApplicationDetailService, times(1)).setConfirmedSatisfactoryData(detail, "my reason", person);
    verify(emailService, times(1)).sendEmail(emailProps, assignedResponder, detail.getPwaApplication().getAppReference());

  }

  @Test
  void confirmSatisfactory_notAlreadyCompleted_success_noAssignedResponder() {

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
    when(caseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())).thenReturn(caseManagementLink);

    var emailPropsRecipient1 = new ApplicationUpdateAcceptedEmailProps(
        consultationRecipient1.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    var emailPropsRecipient2 = new ApplicationUpdateAcceptedEmailProps(
        consultationRecipient2.getFullName(), detail.getPwaApplicationRef(), groupDetail.getName(), caseManagementLink);

    confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person);

    // actual behaviour tested in app detail service unit test
    verify(pwaApplicationDetailService, times(1)).setConfirmedSatisfactoryData(detail, "my reason", person);


    verify(emailService, times(2)).sendEmail(any(), any(), any());
    verify(emailService, atLeastOnce()).sendEmail(emailPropsRecipient1, consultationRecipient1, detail.getPwaApplication().getAppReference());
    verify(emailService, atLeastOnce()).sendEmail(emailPropsRecipient2, consultationRecipient2, detail.getPwaApplication().getAppReference());

  }

  @Test
  void confirmSatisfactory_alreadyCompleted_error() {
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    var person = PersonTestUtil.createDefaultPerson();
    assertThrows(IllegalStateException.class, () ->

      confirmSatisfactoryApplicationService.confirmSatisfactory(detail, "my reason", person));

  }

  @Test
  void getTaskStatus_NotTransferNotStarted() {
    var detail = new PwaApplicationDetail();
    var applicationContext = new PwaAppProcessingContext(
        detail,
        null,
        null,
        null,
        null,
        null
    );

    var taskStatus = confirmSatisfactoryApplicationService.getTaskStatus(applicationContext);
    assertThat(taskStatus).isEqualTo(TaskStatus.NOT_STARTED);
  }

  @Test
  void getTaskStatus_NotTransferStatisfactory() {
    var detail = new PwaApplicationDetail();
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    var applicationContext = new PwaAppProcessingContext(
        detail,
        null,
        null,
        null,
        null,
        null
    );

    var taskStatus = confirmSatisfactoryApplicationService.getTaskStatus(applicationContext);
    assertThat(taskStatus).isEqualTo(TaskStatus.COMPLETED);
  }

  @Test
  void getTaskStatus_AwaitingTransfer() {
    var detail = new PwaApplicationDetail();

    when(pipelineTransferService.findUnclaimedByDonorApplication(detail)).thenReturn(List.of(new PadPipelineTransfer()));
    var applicationContext = new PwaAppProcessingContext(
        detail,
        null,
        null,
        null,
        null,
        null
    );

    var taskStatus = confirmSatisfactoryApplicationService.getTaskStatus(applicationContext);
    assertThat(taskStatus).isEqualTo(TaskStatus.AWAITING_CLAIM);
  }


}
