package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;

/*
 A service to create/withdraw consultation requests from application
 */
@Service
public class ConsultationRequestService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestRepository consultationRequestRepository;
  private final ConsultationRequestValidator consultationRequestValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final TeamManagementService teamManagementService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final NotifyService notifyService;
  private final Clock clock;

  @Autowired
  public ConsultationRequestService(
      ConsulteeGroupDetailService consulteeGroupDetailService,
      ConsultationRequestRepository consultationRequestRepository,
      ConsultationRequestValidator consultationRequestValidator,
      CamundaWorkflowService camundaWorkflowService,
      TeamManagementService teamManagementService,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      NotifyService notifyService,
      @Qualifier("utcClock") Clock clock) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestRepository = consultationRequestRepository;
    this.consultationRequestValidator = consultationRequestValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.teamManagementService = teamManagementService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.notifyService = notifyService;
    this.clock = clock;
  }



  public List<ConsulteeGroupDetail> getConsulteeGroups(AuthenticatedUserAccount user) {
    return consulteeGroupDetailService.getAllConsulteeGroupDetails();
  }

  public void saveConsultationRequest(ConsultationRequest consultationRequest) {
    consultationRequestRepository.save(consultationRequest);
  }



  public void saveEntitiesAndStartWorkflow(ConsultationRequestForm form,
                                           PwaApplicationDetail applicationDetail, AuthenticatedUserAccount user) {
    for (var selectedGroupId: form.getConsulteeGroupSelection().keySet()) {
      var consultationRequest = new ConsultationRequest();
      consultationRequest.setConsulteeGroup(
          consulteeGroupDetailService.getConsulteeGroupDetailById(Integer.parseInt(selectedGroupId)).getConsulteeGroup());
      consultationRequest.setPwaApplication(applicationDetail.getPwaApplication());
      consultationRequest.setStartTimestamp(Instant.now());
      consultationRequest.setStartedByPersonId(user.getLinkedPerson().getId().asInt());
      consultationRequest.setDeadlineDate(
          consultationRequest.getStartTimestamp().plus(Period.ofDays(form.getDaysToRespond())));
      consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

      consultationRequest = consultationRequestRepository.save(consultationRequest);
      camundaWorkflowService.startWorkflow(consultationRequest);
    }
  }

  @Transactional
  public void withdrawConsultationRequest(ConsultationRequest consultationRequest, AuthenticatedUserAccount user) {
    var userWorkflowTask = consultationRequest.getStatus() == ConsultationRequestStatus.ALLOCATION
        ? PwaApplicationConsultationWorkflowTask.ALLOCATION : PwaApplicationConsultationWorkflowTask.RESPONSE;
    var workflowTaskInstance = new WorkflowTaskInstance(consultationRequest, userWorkflowTask);

    var responderPersonOpt = camundaWorkflowService.getAssignedPersonId(workflowTaskInstance);
    Person responderPerson = null;
    if (responderPersonOpt.isPresent()) {
      responderPerson = teamManagementService.getPerson(responderPersonOpt.get().asInt());
    }

    camundaWorkflowService.deleteProcessAndTask(workflowTaskInstance);

    var originalRequestStatus = consultationRequest.getStatus();
    consultationRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);
    consultationRequest.setEndedByPersonId(user.getLinkedPerson().getId().asInt());
    consultationRequest.setEndTimestamp(Instant.now(clock));
    saveConsultationRequest(consultationRequest);

    List<Person> emailRecipients = getEmailRecipients(consultationRequest, originalRequestStatus, responderPerson);
    var consulteeGroupName = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(
        consultationRequest.getConsulteeGroup()).getName();
    emailRecipients.forEach(recipient -> {
      var emailProps = buildAssignedEmailProps(recipient, consultationRequest, consulteeGroupName, user.getLinkedPerson());
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }


  private List<Person> getEmailRecipients(
      ConsultationRequest consultationRequest, ConsultationRequestStatus originalRequestStatus, Person responderPerson) {

    List<Person> emailRecipients =  new ArrayList<>();
    if (originalRequestStatus == ConsultationRequestStatus.AWAITING_RESPONSE) {
      emailRecipients.add(responderPerson);

    } else {
      consulteeGroupTeamService.getTeamMembersForGroup(consultationRequest.getConsulteeGroup()).forEach(
          teamMember -> {
            if (teamMember.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT)) {
              emailRecipients.add(teamMember.getPerson());
            }
          });
    }
    return emailRecipients;
  }

  private ConsultationWithdrawnEmailProps buildAssignedEmailProps(Person recipient,
                                                                  ConsultationRequest consultationRequest,
                                                                  String consulteeGroupName,
                                                                  Person withdrawnByUser) {
    return new ConsultationWithdrawnEmailProps(
        recipient.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupName,
        withdrawnByUser.getFullName());
  }


  public void rebindFormCheckboxes(ConsultationRequestForm form) {
    for (var entry: form.getConsulteeGroupSelection().entrySet()) {
      entry.setValue("true");
    }
  }

  public BindingResult validate(ConsultationRequestForm form, BindingResult bindingResult, PwaApplication pwaApplication) {
    consultationRequestValidator.validate(form, bindingResult,
        new ConsultationRequestValidationHints(this, consulteeGroupDetailService, pwaApplication));
    return bindingResult;
  }


  public boolean isConsultationRequestOpen(ConsulteeGroup consulteeGroup, PwaApplication pwaApplication) {
    return consultationRequestRepository.findByConsulteeGroupAndPwaApplicationAndStatusNotIn(
        consulteeGroup, pwaApplication, List.of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN)).isPresent();
  }

  public boolean canWithDrawConsultationRequest(ConsultationRequest consultationRequest) {
    return !EnumSet.of(
        ConsultationRequestStatus.WITHDRAWN,
        ConsultationRequestStatus.RESPONDED)
        .contains(consultationRequest.getStatus());
  }

  public ConsultationRequest getConsultationRequestById(Integer consultationRequestId) {
    return consultationRequestRepository.findById(consultationRequestId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find consultation request with id: %s",  consultationRequestId)));
  }

  public List<ConsultationRequest> getAllRequestsByApplication(PwaApplication pwaApplication) {
    return consultationRequestRepository.findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication);
  }

  public List<ConsultationRequest> getAllRequestsByAppAndGroupRespondedOnly(PwaApplication pwaApplication, ConsulteeGroup consulteeGroup) {
    return consultationRequestRepository.findByConsulteeGroupAndPwaApplicationAndStatus(
        consulteeGroup, pwaApplication, ConsultationRequestStatus.RESPONDED);
  }

}
