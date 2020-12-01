package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInvolvementServiceTest {

  private static final PersonId PERSON_ID = new PersonId(10);

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private PersonService personService;

  @Mock
  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  private ApplicationInvolvementService applicationInvolvementService;

  private PwaApplication application;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    applicationInvolvementService = new ApplicationInvolvementService(
        consulteeGroupTeamService,
        pwaContactService,
        consultationRequestService,
        camundaWorkflowService,
        userTypeService,
        consulteeGroupDetailService,
        personService,
        confirmSatisfactoryApplicationService);

    application = new PwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, new Person(1, null, null, null, null)), Set.of());

    when(consultationRequestService.consultationRequestIsActive(any())).thenCallRealMethod();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_isContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(
        Set.of(PwaContactRole.PREPARER));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).containsExactly(PwaContactRole.PREPARER);
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_notContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_assignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);
    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.of(new PersonId(1)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_notAssignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);
    when(camundaWorkflowService.getAssignedPersonId(any())).thenReturn(Optional.empty());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_notConsulted_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            user.getLinkedPerson(),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(camundaWorkflowService, pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

    assertThat(involvement.getConsultationInvolvement()).isPresent();

    var consultationInvolvement = involvement.getConsultationInvolvement().get();

    assertThat(consultationInvolvement.getConsulteeGroupDetail()).isNull();
    assertThat(consultationInvolvement.getConsulteeRoles()).isEmpty();
    assertThat(consultationInvolvement.getActiveRequest()).isNull();
    assertThat(consultationInvolvement.getHistoricalRequests()).isEmpty();
    assertThat(consultationInvolvement.isAssignedToResponderStage()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_wasConsulted_notAssignedResponder_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            user.getLinkedPerson(),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(any())).thenReturn(groupDetail);

    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    request.setStatus(ConsultationRequestStatus.ALLOCATION);

    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request));

    when(camundaWorkflowService.getAssignedPersonId(eq(new WorkflowTaskInstance(request,
        PwaApplicationConsultationWorkflowTask.RESPONSE)))).thenReturn(Optional.of(new PersonId(99)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

    var consultationInvolvementOpt = involvement.getConsultationInvolvement();

    assertThat(consultationInvolvementOpt).isPresent();
    assertThat(consultationInvolvementOpt.get())
        .extracting(
            ConsultationInvolvementDto::getConsulteeGroupDetail,
            ConsultationInvolvementDto::getConsulteeRoles,
            ConsultationInvolvementDto::getActiveRequest,
            ConsultationInvolvementDto::getHistoricalRequests,
            ConsultationInvolvementDto::isAssignedToResponderStage
        )
        .containsExactly(groupDetail, Set.of(ConsulteeGroupMemberRole.RECIPIENT), request, List.of(), false);

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_wasConsulted_isAssignedResponder_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            user.getLinkedPerson(),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(any())).thenReturn(groupDetail);

    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    request.setStatus(ConsultationRequestStatus.ALLOCATION);

    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request));

    when(camundaWorkflowService.getAssignedPersonId(eq(new WorkflowTaskInstance(request,
        PwaApplicationConsultationWorkflowTask.RESPONSE)))).thenReturn(Optional.of(new PersonId(user.getLinkedPerson().getId().asInt())));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    verifyNoInteractions(pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isCaseOfficerStageAndUserAssigned()).isFalse();

    var consultationInvolvementOpt = involvement.getConsultationInvolvement();

    assertThat(consultationInvolvementOpt).isPresent();

    assertThat(consultationInvolvementOpt.get())
        .extracting(
            ConsultationInvolvementDto::getConsulteeGroupDetail,
            ConsultationInvolvementDto::getConsulteeRoles,
            ConsultationInvolvementDto::getActiveRequest,
            ConsultationInvolvementDto::getHistoricalRequests,
            ConsultationInvolvementDto::isAssignedToResponderStage
        ).containsExactly(groupDetail, Set.of(ConsulteeGroupMemberRole.RECIPIENT), request, List.of(), true);

  }

  @Test
  public void getCaseOfficerPersonId_whenAssigned() {
    when(camundaWorkflowService.getAssignedPersonId(
        new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))
    ).thenReturn(Optional.of(PERSON_ID));

    var caseOfficerPersonIdOpt = applicationInvolvementService.getCaseOfficerPersonId(application);

    assertThat(caseOfficerPersonIdOpt).contains(PERSON_ID);

  }

  @Test
  public void getCaseOfficerPersonId_whenNot() {

    var caseOfficerPersonIdOpt = applicationInvolvementService.getCaseOfficerPersonId(application);

    assertThat(caseOfficerPersonIdOpt).isEmpty();

  }

  @Test
  public void atLeastOneSatisfactoryVersion_whenTrue() {

    when(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(application)).thenReturn(true);

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    assertThat(involvement.hasAtLeastOneSatisfactoryVersion()).isTrue();

  }

  @Test
  public void atLeastOneSatisfactoryVersion_whenFalse() {

    when(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(application)).thenReturn(false);

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    assertThat(involvement.hasAtLeastOneSatisfactoryVersion()).isFalse();

  }

}
