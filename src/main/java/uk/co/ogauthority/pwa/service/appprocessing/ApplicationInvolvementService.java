package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * A service to find out how a user is related to an application, e.g. are they part of the contacts team, a consultee,
 * the assigned case officer etc?
 */
@Service
public class ApplicationInvolvementService {

  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final PwaContactService pwaContactService;
  private final ConsultationRequestService consultationRequestService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final UserTypeService userTypeService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final PersonService personService;

  @Autowired
  public ApplicationInvolvementService(ConsulteeGroupTeamService consulteeGroupTeamService,
                                       PwaContactService pwaContactService,
                                       ConsultationRequestService consultationRequestService,
                                       CamundaWorkflowService camundaWorkflowService,
                                       UserTypeService userTypeService,
                                       ConsulteeGroupDetailService consulteeGroupDetailService,
                                       PersonService personService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.pwaContactService = pwaContactService;
    this.consultationRequestService = consultationRequestService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.userTypeService = userTypeService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.personService = personService;
  }

  public ApplicationInvolvementDto getApplicationInvolvementDto(PwaApplication application,
                                                                AuthenticatedUserAccount user) {

    var userType = userTypeService.getUserType(user);

    // INDUSTRY data
    Set<PwaContactRole> appContactRoles = userType == UserType.INDUSTRY
        ? pwaContactService.getContactRoles(application, user.getLinkedPerson())
        : Set.of();

    // CONSULTEE data
    ConsultationInvolvementDto consultationInvolvement = null;
    if (userType == UserType.CONSULTEE) {
      consultationInvolvement = getConsultationInvolvement(application, user);
    }

    // OGA data
    boolean caseOfficerStageAndUserAssigned = false;

    if (userType == UserType.OGA) {
      caseOfficerStageAndUserAssigned = getCaseOfficerPersonId(application)
          .filter(personId -> personId.equals(user.getLinkedPerson().getId()))
          .isPresent();
    }

    return new ApplicationInvolvementDto(
        application,
        appContactRoles,
        consultationInvolvement,
        caseOfficerStageAndUserAssigned);

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

  private ConsultationInvolvementDto getConsultationInvolvement(PwaApplication application,
                                                                AuthenticatedUserAccount user) {

    var consulteeGroupTeamMember = consulteeGroupTeamService.getTeamMemberByPerson(user.getLinkedPerson())
        .orElseThrow(() -> new IllegalStateException(String.format(
            "User with wua id [%s] has CONSULTEE priv but isn't in a consultee group team", user.getWuaId())));

    var consulteeGroupDetail = consulteeGroupDetailService
        .getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroupTeamMember.getConsulteeGroup());

    boolean assignedToResponderStage = false;
    Set<ConsulteeGroupMemberRole> consulteeRoles = Set.of();

    // user has consulted on app if group they are part of has a consultation request for the application
    List<ConsultationRequest> consultationRequests = consultationRequestService.getAllRequestsByApplication(application).stream()
        .filter(r -> Objects.equals(consulteeGroupDetail.getConsulteeGroup(), r.getConsulteeGroup()))
        .collect(Collectors.toList());

    // if they've been consulted at least once, their roles in the team should be acknowledged
    if (!consultationRequests.isEmpty()) {
      consulteeRoles = consulteeGroupTeamMember.getRoles();
    }

    var activeRequest = consultationRequests.stream()
        .filter(consultationRequestService::consultationRequestIsActive)
        .findFirst()
        .orElse(null);

    // if there's an active request, find out whether or not the current user is the assigned responder
    if (activeRequest != null) {

      assignedToResponderStage = camundaWorkflowService
          .getAssignedPersonId(new WorkflowTaskInstance(activeRequest, PwaApplicationConsultationWorkflowTask.RESPONSE))
          .map(personId -> user.getLinkedPerson().getId().equals(personId))
          .orElse(false);

    }

    var historicalRequests = consultationRequests.stream()
        .filter(req -> !Objects.equals(req, activeRequest))
        .collect(Collectors.toList());

    return new ConsultationInvolvementDto(
        consulteeGroupDetail,
        consulteeRoles,
        activeRequest,
        historicalRequests,
        assignedToResponderStage
    );

  }

}
