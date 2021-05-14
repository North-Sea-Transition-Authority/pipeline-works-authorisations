package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
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
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.PwaAppAssignmentViewRepository;
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
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

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private PwaAppAssignmentViewRepository pwaAppAssignmentViewRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private AssignmentService assignmentService;

  private ApplicationInvolvementService applicationInvolvementService;

  private PwaApplicationDetail detail;
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
        confirmSatisfactoryApplicationService,
        pwaHolderTeamService,
        pwaAppAssignmentViewRepository,
        pwaApplicationDetailService,
        consentReviewService,
        assignmentService);

    detail = new PwaApplicationDetail();
    application = new PwaApplication();
    detail.setPwaApplication(application);
    user = new AuthenticatedUserAccount(new WebUserAccount(1, new Person(1, null, null, null, null)), Set.of());

    when(consultationRequestService.consultationRequestIsActive(any())).thenCallRealMethod();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_isContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.INDUSTRY));
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(
        Set.of(PwaContactRole.PREPARER));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).containsExactly(PwaContactRole.PREPARER);
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_notContact_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.INDUSTRY));
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_inHolderTeam() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.INDUSTRY));
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, user.getLinkedPerson())).thenReturn(EnumSet.allOf(PwaOrganisationRole.class));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isUserInHolderTeam()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_industryUser_notInHolderTeam() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.INDUSTRY));
    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());
    when(pwaHolderTeamService.getRolesInHolderTeam(detail, user.getLinkedPerson())).thenReturn(EnumSet.noneOf(PwaOrganisationRole.class));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, camundaWorkflowService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isUserInHolderTeam()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_assignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA));
    var assignment = new Assignment(application.getBusinessKey(), application.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, user.getLinkedPerson().getId());
    when(assignmentService.getAssignmentsForPerson(application, user.getLinkedPerson())).thenReturn(List.of(assignment));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isTrue();
    assertThat(involvement.isPwaManagerStage()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_notAssignedCo_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isPwaManagerStage()).isFalse();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_pwaManagerStage_onlyRelevantInteractionsAndDataPopulated() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA));
    when(camundaWorkflowService.getAllActiveWorkflowTasks(application)).thenReturn(
        Set.of(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, pwaContactService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isPwaManagerStage()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_andInSomeIndustryHolderTeam_butNotForApp() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA, UserType.INDUSTRY));
    when(camundaWorkflowService.getAllActiveWorkflowTasks(application)).thenReturn(
        Set.of(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isUserInHolderTeam()).isFalse();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isPwaManagerStage()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_andInSomeIndustryHolderTeamForApp() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA, UserType.INDUSTRY));
    when(camundaWorkflowService.getAllActiveWorkflowTasks(application)).thenReturn(
        Set.of(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    when(pwaHolderTeamService.getRolesInHolderTeam(detail, user.getLinkedPerson())).thenReturn(EnumSet.of(PwaOrganisationRole.APPLICATION_SUBMITTER));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.hasAnyOfTheseHolderRoles(PwaOrganisationRole.APPLICATION_SUBMITTER)).isTrue();
    assertThat(involvement.isUserInHolderTeam()).isTrue();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isPwaManagerStage()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_regulatorUser_andInAppContactRoleForApp() {

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.OGA, UserType.INDUSTRY));
    when(camundaWorkflowService.getAllActiveWorkflowTasks(application)).thenReturn(
        Set.of(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(EnumSet.of(PwaContactRole.PREPARER));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(consulteeGroupTeamService, consultationRequestService, consulteeGroupDetailService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).containsExactly(PwaContactRole.PREPARER);
    assertThat(involvement.isUserInAppContactTeam()).isTrue();
    assertThat(involvement.hasAnyOfTheseHolderRoles(PwaOrganisationRole.values())).isFalse();
    assertThat(involvement.isUserInHolderTeam()).isFalse();
    assertThat(involvement.getConsultationInvolvement()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();
    assertThat(involvement.isPwaManagerStage()).isTrue();

  }

  @Test
  public void getApplicationInvolvementDto_consulteeUser_notConsulted_onlyRelevantInteractionsAndDataPopulated() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.CONSULTEE));
    when(consulteeGroupTeamService.getTeamMemberByPerson(any())).thenReturn(Optional.of(
        new ConsulteeGroupTeamMember(
            groupDetail.getConsulteeGroup(),
            user.getLinkedPerson(),
            Set.of(ConsulteeGroupMemberRole.RECIPIENT))
    ));
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(camundaWorkflowService, pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();

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

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.CONSULTEE));
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

    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(request,
        PwaApplicationConsultationWorkflowTask.RESPONSE))).thenReturn(Optional.of(new PersonId(99)));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();

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

    when(userTypeService.getUserTypes(user)).thenReturn(EnumSet.of(UserType.CONSULTEE));
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

    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(request,
        PwaApplicationConsultationWorkflowTask.RESPONSE))).thenReturn(Optional.of(new PersonId(user.getLinkedPerson().getId().asInt())));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    verifyNoInteractions(pwaContactService);

    assertThat(involvement.getPwaApplication()).isEqualTo(application);
    assertThat(involvement.getContactRoles()).isEmpty();
    assertThat(involvement.isUserAssignedCaseOfficer()).isFalse();

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

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    assertThat(involvement.hasAtLeastOneSatisfactoryVersion()).isTrue();

  }

  @Test
  public void atLeastOneSatisfactoryVersion_whenFalse() {

    when(confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(application)).thenReturn(false);

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    assertThat(involvement.hasAtLeastOneSatisfactoryVersion()).isFalse();

  }

  @Test
  public void getCaseOfficersAssignedToOpenApps() {

    var inProgressApplicationIds = List.of(1, 2, 3);
    when(pwaApplicationDetailService.getInProgressApplicationIds()).thenReturn(inProgressApplicationIds);

    applicationInvolvementService.getCaseOfficersAssignedToInProgressApps();
    verify(pwaAppAssignmentViewRepository, times(1))
        .findAllByAssignmentAndPwaApplicationIdIn(WorkflowAssignment.CASE_OFFICER, inProgressApplicationIds);
  }

  @Test
  public void openConsentReview_whenTrue() {

    var consentReview = new ConsentReview();
    when(consentReviewService.getOpenConsentReview(detail)).thenReturn(Optional.of(consentReview));

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    assertThat(involvement.getOpenConsentReview()).isEqualTo(OpenConsentReview.YES);

  }

  @Test
  public void openConsentReview_whenFalse() {

    when(consentReviewService.getOpenConsentReview(detail)).thenReturn(Optional.empty());

    var involvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    assertThat(involvement.getOpenConsentReview()).isEqualTo(OpenConsentReview.NO);

  }

}
