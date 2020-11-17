package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask.APPROVE_OPTIONS;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.appprocessing.options.ApproveOptionsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.service.consultations.ApplicationConsultationStatusViewTestUtil;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApproveOptionsServiceTest {

  private static final PersonId PERSON_ID = new PersonId(1);

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private OptionsApprovalPersister optionsApprovalPersister;

  @Mock
  private OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  @Mock
  private OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  private ApproveOptionsService approveOptionsService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppProcessingContext pwaAppProcessingContext;

  private Person person;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    person = PersonTestUtil.createPersonFrom(PERSON_ID);

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.allOf(PwaAppProcessingPermission.class)
    );

    when(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(false);

    approveOptionsService = new ApproveOptionsService(
        consultationRequestService,
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsCaseManagementEmailService,
        applicationUpdateRequestService);

  }

  @Test
  public void canShowInTaskList_whenHasApproveOptionsPermission() {

    assertThat(approveOptionsService.canShowInTaskList(pwaAppProcessingContext)).isTrue();

  }

  @Test
  public void canShowInTaskList_whenApproveOptionsPermissionMissing() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    assertThat(approveOptionsService.canShowInTaskList(pwaAppProcessingContext)).isFalse();

  }

  @Test
  public void taskAccessible_missingPermission_serviceInteractions() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.noneOf(PwaAppProcessingPermission.class)
    );

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    verifyNoInteractions(consultationRequestService);

  }

  @Test
  public void taskAccessible_hasPermission_serviceInteractions() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(ApplicationConsultationStatusViewTestUtil.noConsultationRequests());

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    verify(consultationRequestService, times(1))
        .getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication());

    verify(applicationUpdateRequestService, times(1))
        .applicationDetailHasOpenUpdateRequest(pwaApplicationDetail);

  }

  @Test
  public void taskAccessible_hasPermission_noConsultationRequest_andNoOpenUpdateRequest() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(ApplicationConsultationStatusViewTestUtil.noConsultationRequests());

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_hasPermission_hasRespondedRequests_hasUnrespondedRequests_noOpenUpdateRequest() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L),
        ImmutablePair.of(ConsultationRequestStatus.AWAITING_RESPONSE, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void taskAccessible_hasPermission_hasRespondedRequests_noUnrespondedRequests_noOpenUpdateRequest() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L),
        ImmutablePair.of(ConsultationRequestStatus.WITHDRAWN, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isTrue();

  }

  @Test
  public void taskAccessible_hasPermission_hasRespondedRequests_noUnrespondedRequests_OpenUpdateRequest() {

    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L),
        ImmutablePair.of(ConsultationRequestStatus.WITHDRAWN, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    when(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(true);

    var taskAccessible = approveOptionsService.taskAccessible(pwaAppProcessingContext);

    assertThat(taskAccessible).isFalse();

  }

  @Test
  public void approveOptions_serviceInteractions() {
    var instant = Instant.MAX;
    approveOptionsService.approveOptions(pwaApplicationDetail, person, instant);

    verify(optionsApprovalPersister, times(1)).createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(), person, instant
    );

    verify(optionsCaseManagementEmailService, times(1)).sendInitialOptionsApprovedEmail(
        pwaApplicationDetail
    );

  }

  @Test
  public void getTaskListEntry_whenRegulator_andOpenConsultations() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.ALLOCATION, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskListEntry = approveOptionsService.getTaskListEntry(
        APPROVE_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(APPROVE_OPTIONS.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(APPROVE_OPTIONS.getTaskName());
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("cannot start yet");

  }

  @Test
  public void getTaskListEntry_whenRegulator_andNoOpenConsultations_andRespondedConsultation_andNotApproved() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.WITHDRAWN, 1L),
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L)
    ));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskListEntry = approveOptionsService.getTaskListEntry(
        APPROVE_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isEqualTo(
        ReverseRouter.route(on(ApproveOptionsController.class).renderApproveOptions(
            pwaAppProcessingContext.getMasterPwaApplicationId(),
            pwaAppProcessingContext.getApplicationType(),
            null, null, null
        ))
    );
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(APPROVE_OPTIONS.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(APPROVE_OPTIONS.getTaskName());
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("not completed");

  }

  @Test
  public void getTaskListEntry_whenRegulator_andOptionsApproved() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS)
    );

    var statusView = ApplicationConsultationStatusViewTestUtil.from(List.of(
        ImmutablePair.of(ConsultationRequestStatus.WITHDRAWN, 1L),
        ImmutablePair.of(ConsultationRequestStatus.RESPONDED, 1L)
    ));

    var approval = new OptionsApplicationApproval();
    when(optionsApplicationApprovalRepository.findByPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(approval));

    when(consultationRequestService.getApplicationConsultationStatusView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(statusView);

    var taskListEntry = approveOptionsService.getTaskListEntry(
        APPROVE_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(APPROVE_OPTIONS.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(APPROVE_OPTIONS.getTaskName());
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("completed");
  }

  @Test
  public void getTaskListEntry_whenIndustry_andOptionsApproved() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW)
    );

    var approval = new OptionsApplicationApproval();
    when(optionsApplicationApprovalRepository.findByPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(approval));

    var taskListEntry = approveOptionsService.getTaskListEntry(
        APPROVE_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(APPROVE_OPTIONS.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(APPROVE_OPTIONS.getTaskName());
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("completed");
  }

  public void getTaskListEntry_whenIndustry_andOptionsNotApproved() {
    pwaAppProcessingContext = PwaAppProcessingContextTestUtil.withPermissions(
        pwaApplicationDetail,
        EnumSet.of(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW)
    );

    var approval = new OptionsApplicationApproval();
    when(optionsApplicationApprovalRepository.findByPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(approval));

    var taskListEntry = approveOptionsService.getTaskListEntry(
        APPROVE_OPTIONS,
        pwaAppProcessingContext
    );

    assertThat(taskListEntry.getRoute()).isNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(APPROVE_OPTIONS.getDisplayOrder());
    assertThat(taskListEntry.getTaskName()).isEqualTo(APPROVE_OPTIONS.getTaskName());
    assertThat(taskListEntry.getTaskTag().getTagText()).isEqualToIgnoringCase("not completed");
  }
}