package uk.co.ogauthority.pwa.service.consultations;

import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationRequestReceivedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;

/**
 * A service to create consultation requests from application.
 */
@Service
public class ConsultationRequestService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestRepository consultationRequestRepository;
  private final ConsultationRequestValidator consultationRequestValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final TeamManagementService teamManagementService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationsStatusViewFactory consultationsStatusViewFactory;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;

  private static final Set<ConsultationRequestStatus> ENDED_STATUSES =
      Set.of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN);

  @Autowired
  public ConsultationRequestService(
      ConsulteeGroupDetailService consulteeGroupDetailService,
      ConsultationRequestRepository consultationRequestRepository,
      ConsultationRequestValidator consultationRequestValidator,
      CamundaWorkflowService camundaWorkflowService,
      TeamManagementService teamManagementService,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      ConsultationsStatusViewFactory consultationsStatusViewFactory,
      NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestRepository = consultationRequestRepository;
    this.consultationRequestValidator = consultationRequestValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.teamManagementService = teamManagementService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationsStatusViewFactory = consultationsStatusViewFactory;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
  }

  public List<ConsulteeGroupDetail> getAllConsulteeGroups() {
    return consulteeGroupDetailService.getAllConsulteeGroupDetails();
  }

  public void saveConsultationRequest(ConsultationRequest consultationRequest) {
    consultationRequestRepository.save(consultationRequest);
  }

  private void sendConsultationRequestReceivedEmail(ConsultationRequest consultationRequest) {

    List<Person> emailRecipients = getEmailRecipients(consultationRequest, consultationRequest.getStatus(), null);
    var consulteeGroupName = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(
        consultationRequest.getConsulteeGroup()).getName();

    emailRecipients.forEach(recipient -> {
      var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication());
      var emailProps = buildRequestReceivedEmailProps(recipient, consultationRequest, consulteeGroupName, caseManagementLink);
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });

  }

  @Transactional
  public void saveEntitiesAndStartWorkflow(ConsultationRequestForm form,
                                           PwaApplicationDetail applicationDetail,
                                           AuthenticatedUserAccount user) {
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
      sendConsultationRequestReceivedEmail(consultationRequest);
    }
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


  public List<Person> getConsultationRecipients(ConsultationRequest consultationRequest) {

    List<Person> emailRecipients =  new ArrayList<>();
    consulteeGroupTeamService.getTeamMembersForGroup(consultationRequest.getConsulteeGroup()).forEach(
        teamMember -> {
        if (teamMember.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT)) {
          emailRecipients.add(teamMember.getPerson());
        }
      });
    return emailRecipients;
  }

  public Person getAssignedResponderForConsultation(ConsultationRequest consultationRequest) {

    Optional<PersonId> assignedResponderPersonId = camundaWorkflowService
        .getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));

    return assignedResponderPersonId.map(id -> teamManagementService.getPerson(id.asInt())).orElse(null);
  }


  private ConsultationRequestReceivedEmailProps buildRequestReceivedEmailProps(Person recipient,
                                                                               ConsultationRequest consultationRequest,
                                                                               String consulteeGroupName,
                                                                               String caseManagementLink) {

    String dueDateDisplay = DateUtils.formatDate(consultationRequest.getDeadlineDate());

    return new ConsultationRequestReceivedEmailProps(
        recipient.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupName,
        dueDateDisplay,
        caseManagementLink);

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
        consulteeGroup, pwaApplication, ENDED_STATUSES).isPresent();
  }

  public ConsultationRequest getConsultationRequestByIdOrThrow(Integer consultationRequestId) {
    return consultationRequestRepository.findById(consultationRequestId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find consultation request with id: %s",  consultationRequestId)));
  }

  public List<ConsultationRequest> getAllRequestsByApplication(PwaApplication pwaApplication) {
    return consultationRequestRepository.findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication);
  }

  public List<ConsultationRequest> getAllOpenRequestsByApplication(PwaApplication pwaApplication) {
    return consultationRequestRepository.findByPwaApplicationAndStatusNotIn(pwaApplication, ENDED_STATUSES);
  }

  public Map<ConsulteeGroup, ConsulteeGroupDetail> getGroupDetailsForConsulteeGroups(List<ConsultationRequest> consultationRequests) {
    var consulteeGroupDetails = consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(
        consultationRequests.stream().map(ConsultationRequest::getConsulteeGroup).collect(Collectors.toList()));
    return consulteeGroupDetails.stream()
        .collect(Collectors.toMap(ConsulteeGroupDetail::getConsulteeGroup, Function.identity()));
  }

  public List<ConsultationRequest> getAllRequestsByAppAndGroupRespondedOnly(PwaApplication pwaApplication, ConsulteeGroup consulteeGroup) {
    return consultationRequestRepository.findByConsulteeGroupAndPwaApplicationAndStatus(
        consulteeGroup, pwaApplication, ConsultationRequestStatus.RESPONDED);
  }

  public List<ConsultationRequest> getAllRequestsByAppRespondedOnly(PwaApplication pwaApplication) {
    return consultationRequestRepository.findByPwaApplicationAndStatus(pwaApplication, ConsultationRequestStatus.RESPONDED);
  }

  public boolean consultationRequestIsActive(ConsultationRequest consultationRequest) {
    return !ENDED_STATUSES.contains(consultationRequest.getStatus());
  }

  public ApplicationConsultationStatusView getApplicationConsultationStatusView(PwaApplication pwaApplication) {
    return consultationsStatusViewFactory.getApplicationStatusView(pwaApplication);
  }

  static Set<ConsultationRequestStatus> getEndedStatuses() {
    return ENDED_STATUSES;
  }
}
