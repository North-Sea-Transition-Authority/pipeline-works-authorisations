package uk.co.ogauthority.pwa.features.application.authorisation.involvement;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.PwaAppAssignmentViewRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * A service to find out how a user is related to an application, e.g. are they part of the contacts team, a consultee,
 * the assigned case officer etc?
 */
@Service
public class ApplicationInvolvementService {

  private final PwaContactService pwaContactService;
  private final ConsultationRequestService consultationRequestService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final UserTypeService userTypeService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final PersonService personService;
  private final ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PwaAppAssignmentViewRepository pwaAppAssignmentViewRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsentReviewService consentReviewService;
  private final AssignmentService assignmentService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public ApplicationInvolvementService(PwaContactService pwaContactService,
                                       ConsultationRequestService consultationRequestService,
                                       CamundaWorkflowService camundaWorkflowService,
                                       UserTypeService userTypeService,
                                       ConsulteeGroupDetailService consulteeGroupDetailService,
                                       PersonService personService,
                                       ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService,
                                       PwaHolderTeamService pwaHolderTeamService,
                                       PwaAppAssignmentViewRepository pwaAppAssignmentViewRepository,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       ConsentReviewService consentReviewService,
                                       AssignmentService assignmentService,
                                       TeamQueryService teamQueryService) {
    this.pwaContactService = pwaContactService;
    this.consultationRequestService = consultationRequestService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.userTypeService = userTypeService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.personService = personService;
    this.confirmSatisfactoryApplicationService = confirmSatisfactoryApplicationService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.pwaAppAssignmentViewRepository = pwaAppAssignmentViewRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consentReviewService = consentReviewService;
    this.assignmentService = assignmentService;
    this.teamQueryService = teamQueryService;
  }

  public ApplicationInvolvementDto getApplicationInvolvementDto(PwaApplicationDetail detail,
                                                                AuthenticatedUserAccount user) {

    var userTypes = userTypeService.getUserTypes(user);
    var application = detail.getPwaApplication();

    // INDUSTRY data
    Set<PwaContactRole> appContactRoles = Set.of();
    Set<Role> userHolderTeamRoles = Set.of();

    if (userTypes.contains(UserType.INDUSTRY)) {

      userHolderTeamRoles = pwaHolderTeamService.getRolesInHolderTeam(detail, user);

      appContactRoles = pwaContactService.getContactRoles(application, user.getLinkedPerson());

    }

    // CONSULTEE data
    ConsultationInvolvementDto consultationInvolvement = null;
    if (userTypes.contains(UserType.CONSULTEE)) {
      consultationInvolvement = getConsultationInvolvement(application, user)
          .orElseThrow(() -> new IllegalStateException(String.format(
              "Person with id [%s] has CONSULTEE priv but we have no consultation involvement object",
              user.getLinkedPerson().getId().asInt())));
    }

    // OGA data
    var userIsAssignedCaseOfficer = false;
    boolean pwaManagerStage = false;

    if (userTypes.contains(UserType.OGA)) {

      userIsAssignedCaseOfficer = assignmentService.getAssignmentsForPerson(application, user.getLinkedPerson()).stream()
          .anyMatch(ass -> WorkflowAssignment.CASE_OFFICER.equals(ass.getWorkflowAssignment()));

      pwaManagerStage = camundaWorkflowService.getAllActiveWorkflowTasks(application).stream()
          .map(t -> PwaApplicationWorkflowTask.valueOf(t.getTaskName()))
          .anyMatch(t -> t == PwaApplicationWorkflowTask.APPLICATION_REVIEW);

    }

    boolean atLeastOneSatisfactoryVersion = confirmSatisfactoryApplicationService.atLeastOneSatisfactoryVersion(
        application);

    var openConsentReview = consentReviewService.getOpenConsentReview(detail)
        .map(openReview -> OpenConsentReview.YES)
        .orElse(OpenConsentReview.NO);

    var userIsIndustryOnly = !(appContactRoles.isEmpty() && userHolderTeamRoles.isEmpty())
         && !(consultationInvolvement != null || userTypes.contains(UserType.OGA));

    return new ApplicationInvolvementDto(
        application,
        appContactRoles,
        consultationInvolvement,
        userIsAssignedCaseOfficer,
        pwaManagerStage,
        atLeastOneSatisfactoryVersion,
        userHolderTeamRoles,
        userIsIndustryOnly,
        openConsentReview);

  }

  public Optional<PersonId> getCaseOfficerPersonId(PwaApplication pwaApplication) {
    return camundaWorkflowService.getAssignedPersonId(
        new WorkflowTaskInstance(pwaApplication, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)
    );
  }

  public Optional<Person> getCaseOfficerPerson(PwaApplication pwaApplication) {
    return getCaseOfficerPersonId(pwaApplication)
        .map(personService::getPersonById);
  }

  public Optional<ConsultationInvolvementDto> getConsultationInvolvement(PwaApplication application,
                                                                         WebUserAccount webUserAccount) {

    var consulteeGroupTeamMemberOpt =
        teamQueryService.getTeamRolesViewsByUserAndTeamType(webUserAccount.getWuaId(), TeamType.CONSULTEE).stream()
          .findFirst();

    if (consulteeGroupTeamMemberOpt.isEmpty()) {
      return Optional.empty();
    }

    var consulteeGroupTeamMember = consulteeGroupTeamMemberOpt.get();

    var consulteeGroupId = Integer.valueOf(consulteeGroupTeamMember.teamScopeId());
    var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupIdAndTipFlagIsTrue(consulteeGroupId);

    boolean assignedToResponderStage = false;
    Set<Role> consulteeRoles = Set.of();

    // user has consulted on app if group they are part of has a consultation request for the application
    List<ConsultationRequest> consultationRequests = consultationRequestService.getAllRequestsByApplication(application).stream()
        .filter(r -> Objects.equals(consulteeGroupId, r.getConsulteeGroup().getId()))
        .toList();

    // if they've been consulted at least once, their roles in the team should be acknowledged
    if (!consultationRequests.isEmpty()) {
      consulteeRoles = new HashSet<>(consulteeGroupTeamMember.roles());
    }

    var activeRequest = consultationRequests.stream()
        .filter(consultationRequestService::consultationRequestIsActive)
        .findFirst()
        .orElse(null);

    // if there's an active request, find out whether or not the current user is the assigned responder
    if (activeRequest != null) {

      assignedToResponderStage = camundaWorkflowService
          .getAssignedPersonId(
              new WorkflowTaskInstance(activeRequest, PwaApplicationConsultationWorkflowTask.RESPONSE))
          .map(personId -> webUserAccount.getLinkedPerson().getId().equals(personId))
          .orElse(false);

    }

    var historicalRequests = consultationRequests.stream()
        .filter(req -> !Objects.equals(req, activeRequest))
        .collect(Collectors.toList());

    var dto = new ConsultationInvolvementDto(
        consulteeGroupDetail,
        consulteeRoles,
        activeRequest,
        historicalRequests,
        assignedToResponderStage
    );

    return Optional.of(dto);

  }

  /**
   * WARNING: This may return assignment views where the case officer is assigned on more that one app.
   * Currently this is only called by the ApplicationSearchController where duplicate case officers are removed.
   */
  public List<PwaAppAssignmentView> getCaseOfficersAssignedToInProgressApps() {
    var inProgressApplicationIds = pwaApplicationDetailService.getInProgressApplicationIds();
    return pwaAppAssignmentViewRepository.findAllByAssignmentAndPwaApplicationIdIn(
        WorkflowAssignment.CASE_OFFICER, inProgressApplicationIds);
  }

}
