package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessUserRetrievalService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;

@ExtendWith(MockitoExtension.class)
class ConsultationsGeneratorServiceTest {
  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private AssignResponderService assignResponderService;

  @Mock
  private ConsultationResponseService consultationResponseService;

  @Mock
  private TestHarnessUserRetrievalService testHarnessUserRetrievalService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserAccountService userAccountService;

  @Spy
  @InjectMocks
  private ConsultationsGeneratorService consultationsGeneratorService;

  @Test
  void generateAppProcessingTaskData() {
    TestHarnessAppProcessingProperties props = mock(TestHarnessAppProcessingProperties.class);
    PwaApplication application = mock(PwaApplication.class);
    when(props.getPwaApplication()).thenReturn(application);

    UserTeamRolesView consulteeMember = mock(UserTeamRolesView.class);
    when(consulteeMember.teamScopeId()).thenReturn("1");
    when(consulteeMember.wuaId()).thenReturn(10L);
    when(teamQueryService.getUsersOfTeamTypeWithRoleIn(TeamType.CONSULTEE, Set.of(Role.RECIPIENT)))
        .thenReturn(List.of(consulteeMember));

    ConsulteeGroupDetail consulteeGroupDetail = mock(ConsulteeGroupDetail.class);
    when(consulteeGroupDetail.getId()).thenReturn(1);
    when(consulteeGroupDetail.getName()).thenReturn("Consultee Group");
    ConsulteeGroup consulteeGroup = mock(ConsulteeGroup.class);
    when(consulteeGroup.getId()).thenReturn(1);
    when(consulteeGroupDetail.getConsulteeGroup()).thenReturn(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupIdAndTipFlagIsTrue(anyInt())).thenReturn(consulteeGroupDetail);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(any(ConsulteeGroup.class)))
        .thenReturn(consulteeGroupDetail);

    ConsultationRequest consultationRequest = mock(ConsultationRequest.class);
    when(consultationRequest.getConsulteeGroup()).thenReturn(consulteeGroup);
    when(consultationRequestService.getAllOpenRequestsByApplication(application)).thenReturn(List.of(consultationRequest));

    WebUserAccount assigningUser = mock(WebUserAccount.class);
    when(testHarnessUserRetrievalService.getWebUserAccount(10)).thenReturn(assigningUser);

    WebUserAccount responderUser = mock(WebUserAccount.class);
    var person = new  Person();
    person.setId(1);
    when(responderUser.getLinkedPerson()).thenReturn(person);

    when(teamQueryService.getUsersOfScopedTeamWithRoleIn(eq(TeamType.CONSULTEE), any(), eq(Set.of(Role.RESPONDER))))
        .thenReturn(List.of(mock(UserTeamRolesView.class)));
    when(userAccountService.getWebUserAccount(anyInt())).thenReturn(responderUser);


    consultationsGeneratorService.generateAppProcessingTaskData(props);

    verify(consultationRequestService).saveEntitiesAndStartWorkflow(any(ConsultationRequestForm.class), any(), any());
    verify(assignResponderService).assignResponder(any(AssignResponderForm.class), eq(consultationRequest), eq(assigningUser));
    verify(consultationResponseService).saveResponseAndCompleteWorkflow(any(ConsultationResponseForm.class), eq(consultationRequest), eq(responderUser));
  }

  @Test
  void getLinkedAppProcessingTask_returnsCorrectTask() {
    assertThat(consultationsGeneratorService.getLinkedAppProcessingTask())
        .isEqualTo(PwaAppProcessingTask.CONSULTATIONS);
  }

  @Test
  void getAvailableConsulteeMember_returnsFirstRecipient() {
    UserTeamRolesView mockMember = mock(UserTeamRolesView.class);
    when(teamQueryService.getUsersOfTeamTypeWithRoleIn(TeamType.CONSULTEE, Set.of(Role.RECIPIENT)))
        .thenReturn(List.of(mockMember));

    UserTeamRolesView result = consultationsGeneratorService.getAvailableConsulteeMember();

    assertThat(result).isEqualTo(mockMember);
  }

  @Test
  void getAvailableConsulteeMember_throwsWhenNoRecipient() {
    when(teamQueryService.getUsersOfTeamTypeWithRoleIn(TeamType.CONSULTEE, Set.of(Role.RECIPIENT)))
        .thenReturn(List.of());

    assertThatExceptionOfType(PwaEntityNotFoundException.class)
        .isThrownBy(() -> consultationsGeneratorService.getAvailableConsulteeMember());
  }

  @Test
  void getActiveConsultationRequest_returnsFirstOpenRequest() {
    TestHarnessAppProcessingProperties props = mock(TestHarnessAppProcessingProperties.class);
    var application = mock(PwaApplication.class);
    when(props.getPwaApplication()).thenReturn(application);

    ConsultationRequest consultationRequest = mock(ConsultationRequest.class);
    when(consultationRequestService.getAllOpenRequestsByApplication(application))
        .thenReturn(List.of(consultationRequest));

    ConsultationRequest result = consultationsGeneratorService.getActiveConsultationRequest(props);

    assertThat(result).isEqualTo(consultationRequest);
  }

  @Test
  void getActiveConsultationRequest_throwsWhenNoOpenRequest() {
    TestHarnessAppProcessingProperties props = mock(TestHarnessAppProcessingProperties.class);
    var application = mock(PwaApplication.class);
    when(props.getPwaApplication()).thenReturn(application);

    when(consultationRequestService.getAllOpenRequestsByApplication(application))
        .thenReturn(List.of());

    assertThatExceptionOfType(PwaEntityNotFoundException.class)
        .isThrownBy(() -> consultationsGeneratorService.getActiveConsultationRequest(props));
  }

  @Test
  void createConsultationRequest_savesAndStartsWorkflow() {
    TestHarnessAppProcessingProperties props = mock(TestHarnessAppProcessingProperties.class);
    var applicationDetail = mock(PwaApplicationDetail.class);
    var caseOfficerAua = mock(AuthenticatedUserAccount.class);
    when(props.getPwaApplicationDetail()).thenReturn(applicationDetail);
    when(props.getCaseOfficerAua()).thenReturn(caseOfficerAua);

    ConsulteeGroup consulteeGroup = mock(ConsulteeGroup.class);
    ConsulteeGroupDetail consulteeGroupDetail = mock(ConsulteeGroupDetail.class);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup))
        .thenReturn(consulteeGroupDetail);
    when(consulteeGroupDetail.getId()).thenReturn(1);
    when(consulteeGroupDetail.getName()).thenReturn("GroupName");

    consultationsGeneratorService.createConsultationRequest(props, consulteeGroup);

    verify(consultationRequestService).saveEntitiesAndStartWorkflow(any(ConsultationRequestForm.class),
        any(), any());
  }

  @Test
  void assignResponder_assignsAndReturnsResponderUser() {
    ConsultationRequest consultationRequest = mock(ConsultationRequest.class);
    WebUserAccount assigningUser = mock(WebUserAccount.class);

    ConsulteeGroup consulteeGroup = mock(ConsulteeGroup.class);
    when(consultationRequest.getConsulteeGroup()).thenReturn(consulteeGroup);
    when(consulteeGroup.getId()).thenReturn(123);

    var teamType = TeamType.CONSULTEE;

    UserTeamRolesView teamMemberView = mock(UserTeamRolesView.class);
    when(teamMemberView.wuaId()).thenReturn(456L);

    when(teamQueryService.getUsersOfScopedTeamWithRoleIn(eq(teamType), any(), eq(Set.of(Role.RESPONDER))))
        .thenReturn(List.of(teamMemberView));

    WebUserAccount responderUser = mock(WebUserAccount.class);
    var person = new Person();
    person.setId(1);
    when(responderUser.getLinkedPerson()).thenReturn(person);
    when(userAccountService.getWebUserAccount(456)).thenReturn(responderUser);

    WebUserAccount result = consultationsGeneratorService.assignResponder(consultationRequest, assigningUser);

    verify(assignResponderService).assignResponder(any(AssignResponderForm.class), any(), any());
    assertThat(result).isEqualTo(responderUser);
  }

  @Test
  void respondOnConsultationRequest_savesResponseAndCompletesWorkflow() {
    ConsultationRequest consultationRequest = mock(ConsultationRequest.class);
    WebUserAccount respondingUser = mock(WebUserAccount.class);

    consultationsGeneratorService.respondOnConsultationRequest(consultationRequest, respondingUser);

    verify(consultationResponseService).saveResponseAndCompleteWorkflow(
        any(ConsultationResponseForm.class), eq(consultationRequest), eq(respondingUser));
  }
}