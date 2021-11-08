package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationMultiResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 A service to create response/assign response to consultation request.
 */
@Service
public class ConsultationResponseService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final ConsultationResponseDataService consultationResponseDataService;
  private final ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;
  private final AppFileService appFileService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CONSULTATION_RESPONSE;

  @Autowired
  public ConsultationResponseService(ConsultationRequestService consultationRequestService,
                                     ConsultationResponseRepository consultationResponseRepository,
                                     CamundaWorkflowService camundaWorkflowService,
                                     @Qualifier("utcClock") Clock clock,
                                     NotifyService notifyService,
                                     ConsulteeGroupDetailService consulteeGroupDetailService,
                                     WorkflowAssignmentService workflowAssignmentService,
                                     EmailCaseLinkService emailCaseLinkService,
                                     ConsultationResponseDataService consultationResponseDataService,
                                     ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository,
                                     AppFileService appFileService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseRepository = consultationResponseRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.consultationResponseDataService = consultationResponseDataService;
    this.consultationResponseFileLinkRepository = consultationResponseFileLinkRepository;
    this.appFileService = appFileService;
  }

  public List<ConsultationResponse> getResponsesByConsultationRequests(List<ConsultationRequest> consultationRequests) {
    return consultationResponseRepository.getAllByConsultationRequestIn(consultationRequests);
  }

  public Optional<ConsultationResponse> getResponseByConsultationRequest(ConsultationRequest consultationRequest) {
    return consultationResponseRepository.findByConsultationRequest(consultationRequest);
  }

  public boolean isUserAssignedResponderForConsultation(WebUserAccount user, ConsultationRequest consultationRequest) {

    Optional<PersonId> assignedResponderPersonId = camundaWorkflowService
        .getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));

    return assignedResponderPersonId
        .map(personId -> Objects.equals(personId, user.getLinkedPerson().getId()))
        .orElse(false);

  }

  private ConsultationResponse createAndSaveResponse(ConsultationRequest consultationRequest,
                                                     WebUserAccount user) {

    ConsultationResponse consultationResponse = new ConsultationResponse();
    consultationResponse.setConsultationRequest(consultationRequest);

    consultationResponse.setResponseTimestamp(Instant.now(clock));
    consultationResponse.setRespondingPersonId(user.getLinkedPerson().getId().asInt());

    return consultationResponseRepository.save(consultationResponse);

  }

  @Transactional
  public void saveResponseAndCompleteWorkflow(ConsultationResponseForm form, ConsultationRequest consultationRequest, WebUserAccount user) {

    var consultationResponse = createAndSaveResponse(consultationRequest, user);
    var responseDataList = consultationResponseDataService.createAndSaveResponseData(consultationResponse, form);

    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    consultationRequestService.saveConsultationRequest(consultationRequest);

    createConsultationResponseFileLinks(form, consultationRequest, consultationResponse, user);

    sendResponseNotificationToCaseOfficer(consultationRequest, responseDataList);

    workflowAssignmentService.clearAssignments(consultationRequest);

  }

  private void createConsultationResponseFileLinks(ConsultationResponseForm form,
                                                   ConsultationRequest consultationRequest,
                                                   ConsultationResponse consultationResponse,
                                                   WebUserAccount user) {
    //keep unlinked files to prevent deleting other user's temporary uploaded files when submitting response
    appFileService.updateFiles(form, consultationRequest.getPwaApplication(), FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, user);

    var fileIds = form.getUploadedFileWithDescriptionForms().stream()
        .map(UploadFileWithDescriptionForm::getUploadedFileId)
        .collect(Collectors.toList());

    if (!fileIds.isEmpty()) {

      var files = appFileService.getFilesByIdIn(consultationRequest.getPwaApplication(), FILE_PURPOSE, fileIds);

      var documentLinks = new ArrayList<ConsultationResponseFileLink>();
      files.forEach(file -> {

        var caseNoteDocumentLink = new ConsultationResponseFileLink(consultationResponse, file);
        documentLinks.add(caseNoteDocumentLink);

      });
      consultationResponseFileLinkRepository.saveAll(documentLinks);
    }

  }

  private void sendResponseNotificationToCaseOfficer(ConsultationRequest consultationRequest,
                                                     List<ConsultationResponseData> responseDataList) {

    var application = consultationRequest.getPwaApplication();

    var caseOfficerPerson = workflowAssignmentService
        .getAssignee(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))
        .orElseThrow(() -> new WorkflowAssignmentException(
            String.format("Expected to find a case officer for application with ID: %s", application.getId())));

    String caseOfficerName = caseOfficerPerson.getFullName();
    String caseOfficerEmail = caseOfficerPerson.getEmailAddress();

    var tipGroupDetail = consulteeGroupDetailService
        .getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup());

    String consulteeGroupName = tipGroupDetail.getName();

    EmailProperties emailProps;
    if (responseDataList.size() > 1) {

      var responses = responseDataList.stream()
          .map(d -> getConsultationResponseEmailText(d,
              String.format("%s\n\n%s",
              d.getResponseGroup().getResponseLabel(),
              d.getResponseType().getRadioInsetText(application.getAppReference()))))
          .collect(Collectors.joining("\n\n"));

      emailProps = new ConsultationMultiResponseReceivedEmailProps(
          caseOfficerName,
          application.getAppReference(),
          consulteeGroupName,
          responses,
          emailCaseLinkService.generateCaseManagementLink(application)
      );

    } else {

      emailProps = new ConsultationResponseReceivedEmailProps(
          caseOfficerName,
          application.getAppReference(),
          consulteeGroupName,
          getConsultationResponseEmailText(responseDataList.get(0), responseDataList.get(0).getResponseType().getLabelText()),
          emailCaseLinkService.generateCaseManagementLink(application)
      );

    }

    notifyService.sendEmail(emailProps, caseOfficerEmail);

  }

  private String getConsultationResponseEmailText(ConsultationResponseData responseData, String defaultEmailText) {
    var emailText = responseData.getResponseType().getEmailText().orElse(defaultEmailText);
    return responseData.getResponseType().includeResponseTextInEmail() ? emailText + responseData.getResponseText() : emailText;
  }


  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {

    boolean responded = processingContext.getActiveConsultationRequest()
        .map(ConsultationRequestDto::getConsultationRequest)
        .flatMap(this::getResponseByConsultationRequest)
        .isPresent();

    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONSULTATION_RESPONDER)
        && !responded;

  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    // given show in task list rules, assumes not responded
    return new TaskListEntry(
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(),
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext),
        TaskTag.from(TaskStatus.NOT_STARTED),
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getDisplayOrder()
    );

  }

  public ConsultationResponse getLatestResponseForRequests(List<ConsultationRequest> requests) {
    return consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(requests);
  }

  public boolean areConsultationResponsesValidForOptionsApproval(PwaApplication pwaApplication) {

    var groupRequestMap = consultationRequestService.getAllRequestsByApplication(pwaApplication).stream()
        .filter(consultationRequest -> consultationRequest.getStatus() == ConsultationRequestStatus.RESPONDED)
        .collect(Collectors.groupingBy(ConsultationRequest::getConsulteeGroup));

    var latestResponsesByGroup = groupRequestMap.keySet().stream()
        .map(consulteeGroup -> getLatestResponseForRequests(groupRequestMap.get(consulteeGroup)))
        .collect(Collectors.toList());

    var responseDataList = consultationResponseDataService.findAllByConsultationResponseIn(latestResponsesByGroup);

    var validResponseOptions = Set.of(ConsultationResponseOption.CONFIRMED,
        ConsultationResponseOption.PROVIDE_ADVICE, ConsultationResponseOption.NO_ADVICE,
        ConsultationResponseOption.EIA_AGREE, ConsultationResponseOption.EIA_NOT_RELEVANT,
        ConsultationResponseOption.HABITATS_AGREE, ConsultationResponseOption.HABITATS_NOT_RELEVANT);

    var invalidEmtResponseOptions = Set.of(ConsultationResponseOption.EIA_DISAGREE, ConsultationResponseOption.HABITATS_DISAGREE);

    var hasApproval = false;
    for (var responseData: responseDataList) {

      if (validResponseOptions.contains(responseData.getResponseType())) {
        hasApproval = true;

      } else if (invalidEmtResponseOptions.contains(responseData.getResponseType())) {
        return false;
      }
    }

    return hasApproval;
  }

  public Optional<ConsultationResponseFileLink> getConsultationResponseFileLink(AppFile appFile) {
    return consultationResponseFileLinkRepository.findByAppFile_PwaApplicationAndAppFile(appFile.getPwaApplication(), appFile);
  }

  @Transactional
  public void deleteConsultationResponseFileLink(ConsultationResponseFileLink consultationResponseFileLink) {
    consultationResponseFileLinkRepository.delete(consultationResponseFileLink);
  }

}
