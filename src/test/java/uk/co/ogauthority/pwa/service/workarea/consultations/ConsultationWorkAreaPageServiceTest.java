package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearcher;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  @Mock
  private ConsultationRequestSearcher consultationRequestSearcher;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private ConsultationWorkAreaPageService consultationWorkAreaPageService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  private ConsulteeGroupDetail groupDetail;

  @Before
  public void setup() {

    consultationWorkAreaPageService = new ConsultationWorkAreaPageService(consultationRequestSearcher, consulteeGroupTeamService);

    groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("test", "t");

  }

  @Test
  public void getPageView_zeroResults_userIsNotConsultee() {

    var fakePage = new PageImpl<ConsultationRequestSearchItem>(List.of(), getDefaultWorkAreaViewPageable(REQUESTED_PAGE), 0);
    when(consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(any(),
        eq(ConsultationRequestStatus.ALLOCATION), any(), any())).thenReturn(fakePage);

    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of());

    var workareaPage = consultationWorkAreaPageService.getPageView(user, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(consulteeGroupTeamService, times(1)).getTeamMembersByPerson(user.getLinkedPerson());

    verify(consultationRequestSearcher, times(1)).searchByStatusForGroupIdsOrConsultationRequestIds(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        ConsultationRequestStatus.ALLOCATION,
        Set.of(),
        Set.of()
    );

  }

  private Pageable getDefaultWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "deadlineDate"));
  }

  @Test
  public void getPageView_unassignedConsultations_userIsRecipient() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);

    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(
        new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(), REQUESTED_PAGE);

    verify(consultationRequestSearcher, times(1)).searchByStatusForGroupIdsOrConsultationRequestIds(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        ConsultationRequestStatus.ALLOCATION,
        Set.of(groupDetail.getConsulteeGroupId()),
        Set.of()
    );

  }

  @Test
  public void getPageView_assignedConsultations_userIsResponder() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);
    var assignedTask = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(9999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(consultationRequestSearcher, times(1)).searchByStatusForGroupIdsOrConsultationRequestIds(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        ConsultationRequestStatus.ALLOCATION,
        Set.of(),
        Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey())
    );

  }

  @Test
  public void getPageView_assignedAndUnassignedConsultations_userIsRecipientAndResponder() {

    setupFakeConsultationSearchResultPage(List.of(), REQUESTED_PAGE);

    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(
        new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));

    var assignedTask = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(
        getConsultationTaskWorkflowInstance(9999, PwaApplicationConsultationWorkflowTask.RESPONSE), user.getLinkedPerson());

    var workAreaPage = consultationWorkAreaPageService.getPageView(user, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(consultationRequestSearcher, times(1)).searchByStatusForGroupIdsOrConsultationRequestIds(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        ConsultationRequestStatus.ALLOCATION,
        Set.of(groupDetail.getConsulteeGroupId()),
        Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey())
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