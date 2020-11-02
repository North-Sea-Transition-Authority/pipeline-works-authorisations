package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
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

  @Autowired
  public ApplicationInvolvementService(ConsulteeGroupTeamService consulteeGroupTeamService,
                                       PwaContactService pwaContactService,
                                       ConsultationRequestService consultationRequestService,
                                       CamundaWorkflowService camundaWorkflowService,
                                       UserTypeService userTypeService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.pwaContactService = pwaContactService;
    this.consultationRequestService = consultationRequestService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.userTypeService = userTypeService;
  }

  public ApplicationInvolvementDto getApplicationInvolvementDto(PwaApplication application, AuthenticatedUserAccount user) {

    var userType = userTypeService.getUserType(user);

    // INDUSTRY data
    Set<PwaContactRole> appContactRoles = userType == UserType.INDUSTRY
        ? pwaContactService.getContactRoles(application, user.getLinkedPerson())
        : Set.of();

    // CONSULTEE data
    boolean assignedToResponderStage = false;
    Set<ConsulteeGroupMemberRole> consulteeRoles = Set.of();
    if (userType == UserType.CONSULTEE) {

      var assignedResponderAndGroupRolesPair = getAssignedResponderAndConsulteeRoles(application, user);
      assignedToResponderStage = assignedResponderAndGroupRolesPair.getLeft();
      consulteeRoles = assignedResponderAndGroupRolesPair.getRight();

    }

    // OGA data
    boolean caseOfficerStageAndUserAssigned = false;

    if (userType == UserType.OGA) {
      caseOfficerStageAndUserAssigned = camundaWorkflowService
          .getAssignedPersonId(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))
          .map(personId -> user.getLinkedPerson().getId().equals(personId))
          .orElse(false);
    }

    return new ApplicationInvolvementDto(
        application,
        appContactRoles,
        assignedToResponderStage,
        consulteeRoles,
        caseOfficerStageAndUserAssigned);

  }

  private Pair<Boolean, Set<ConsulteeGroupMemberRole>> getAssignedResponderAndConsulteeRoles(PwaApplication application,
                                                                                             AuthenticatedUserAccount user) {

    var consulteeGroupTeamMemberOpt = consulteeGroupTeamService.getTeamMemberByPerson(user.getLinkedPerson());

    var consulteeGroup = consulteeGroupTeamMemberOpt
        .map(ConsulteeGroupTeamMember::getConsulteeGroup)
        .orElse(null);

    boolean assignedToResponderStage = false;
    Set<ConsulteeGroupMemberRole> consulteeRoles = Set.of();

    if (consulteeGroup != null) {

      // user has consulted on app if group they are part of has a consultation request for the application
      ConsultationRequest consultationRequest = consultationRequestService.getAllRequestsByApplication(application).stream()
          .filter(r -> Objects.equals(consulteeGroup, r.getConsulteeGroup()))
          .findFirst()
          .orElse(null);

      if (consultationRequest != null) {

        assignedToResponderStage = camundaWorkflowService
            .getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE))
            .map(personId -> user.getLinkedPerson().getId().equals(personId))
            .orElse(false);

        consulteeRoles = consulteeGroupTeamMemberOpt.stream()
            .filter(member -> member.getConsulteeGroup().equals(consultationRequest.getConsulteeGroup()))
            .flatMap(member -> member.getRoles().stream())
            .collect(Collectors.toSet());

      }

    }

    return Pair.of(assignedToResponderStage, consulteeRoles);

  }

}
