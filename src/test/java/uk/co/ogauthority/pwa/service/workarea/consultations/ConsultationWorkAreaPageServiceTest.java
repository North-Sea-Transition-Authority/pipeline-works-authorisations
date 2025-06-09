package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.camunda.external.AssignedTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearcher;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  @Mock
  private ConsultationRequestSearcher consultationRequestSearcher;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ConsultationWorkAreaPageService consultationWorkAreaPageService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      Set.of(PwaUserPrivilege.PWA_ACCESS));

  private ConsulteeGroupDetail groupDetail;

  @BeforeEach
  void setup() {

    groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("test", "t");

  }

  @Test
  void getPageView_zeroResults_userIsNotConsultee() {

    var fakePage = new PageImpl<ConsultationRequestSearchItem>(List.of(), getDefaultWorkAreaViewPageable(REQUESTED_PAGE), 0);
    when(consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(any(),
        eq(ConsultationRequestStatus.ALLOCATION), any(), any())).thenReturn(fakePage);

    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(user.getWuaId(), TeamType.CONSULTEE, Set.of(Role.RECIPIENT))).thenReturn(List.of());

    var workareaPage = consultationWorkAreaPageService.getPageView(user, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isZero();

    verify(consultationRequestSearcher).searchByStatusForGroupIdsOrConsultationRequestIds(
        eq(getDefaultWorkAreaViewPageable(REQUESTED_PAGE)),
        eq(ConsultationRequestStatus.ALLOCATION),
        eq(null),
        eq(Set.of())
    );

  }

  private Pageable getDefaultWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        ConsultationWorkAreaSort.DEADLINE_DATE_ASC.getSort());
  }

  @Test
  void getPageView_unassignedConsultations_userIsRecipient() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);

    var team = new Team(UUID.randomUUID());
    team.setScopeId(String.valueOf(groupDetail.getConsulteeGroupId()));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(user.getWuaId(), TeamType.CONSULTEE, Set.of(Role.RECIPIENT)))
        .thenReturn(List.of(team));

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(), REQUESTED_PAGE);

    verify(consultationRequestSearcher).searchByStatusForGroupIdsOrConsultationRequestIds(
        eq(getDefaultWorkAreaViewPageable(REQUESTED_PAGE)),
        eq(ConsultationRequestStatus.ALLOCATION),
        eq(groupDetail.getConsulteeGroupId()),
        eq(Set.of())
    );

  }

  @Test
  void getPageView_assignedConsultations_userIsResponder() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);
    var assignedTask = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(9999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(consultationRequestSearcher).searchByStatusForGroupIdsOrConsultationRequestIds(
        eq(getDefaultWorkAreaViewPageable(REQUESTED_PAGE)),
        eq(ConsultationRequestStatus.ALLOCATION),
        eq(null),
        eq(Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()))
    );

  }

  @Test
  void getPageView_assignedAndUnassignedConsultations_userIsRecipientAndResponder() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);

    var team = new Team(UUID.randomUUID());
    team.setScopeId(String.valueOf(groupDetail.getConsulteeGroupId()));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(user.getWuaId(), TeamType.CONSULTEE, Set.of(Role.RECIPIENT)))
        .thenReturn(List.of(team));

    var assignedTask = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(9999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(consultationRequestSearcher).searchByStatusForGroupIdsOrConsultationRequestIds(
        eq(getDefaultWorkAreaViewPageable(REQUESTED_PAGE)),
        eq(ConsultationRequestStatus.ALLOCATION),
        eq(groupDetail.getConsulteeGroupId()),
        eq(Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()))
    );

  }

  private void setupFakeConsultationSearchResultPage(List<ConsultationRequestSearchItem> results, int page) {

    var fakePage = new PageImpl<>(
        results,
        getDefaultWorkAreaViewPageable(page),
        results.size());

    when(consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(any(), any(), any(), any())).thenReturn(fakePage);

  }

  private WorkflowTaskInstance getConsultationTaskWorkflowInstance(Integer businessKey, UserWorkflowTask task) {
    return new WorkflowTaskInstance(new GenericWorkflowSubject(businessKey, WorkflowType.PWA_APPLICATION_CONSULTATION), task);
  }
}