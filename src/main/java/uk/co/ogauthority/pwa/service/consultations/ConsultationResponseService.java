package uk.co.ogauthority.pwa.service.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.controller.consultations.responses.ConsultationResponseFileController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationMultiResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileUploadRestController;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

/**
 A service to create response/assign response to consultation request.
 */
@Service
public class ConsultationResponseService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final CaseLinkService caseLinkService;
  private final ConsultationResponseDataService consultationResponseDataService;
  private final ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;
  private final AppFileService appFileService;
  private final FileManagementService fileManagementService;
  private final AppFileManagementService appFileManagementService;

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CONSULTATION_RESPONSE;
  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CONSULTATION_RESPONSE;
  private final EmailService emailService;

  @Autowired
  public ConsultationResponseService(ConsultationRequestService consultationRequestService,
                                     ConsultationResponseRepository consultationResponseRepository,
                                     CamundaWorkflowService camundaWorkflowService,
                                     @Qualifier("utcClock") Clock clock,
                                     ConsulteeGroupDetailService consulteeGroupDetailService,
                                     WorkflowAssignmentService workflowAssignmentService,
                                     CaseLinkService caseLinkService,
                                     ConsultationResponseDataService consultationResponseDataService,
                                     ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository,
                                     AppFileService appFileService,
                                     FileManagementService fileManagementService,
                                     AppFileManagementService appFileManagementService,
                                     EmailService emailService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseRepository = consultationResponseRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.caseLinkService = caseLinkService;
    this.consultationResponseDataService = consultationResponseDataService;
    this.consultationResponseFileLinkRepository = consultationResponseFileLinkRepository;
    this.appFileService = appFileService;
    this.fileManagementService = fileManagementService;
    this.appFileManagementService = appFileManagementService;
    this.emailService = emailService;
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

  public FileUploadComponentAttributes getFileUploadComponentAttributes(
      List<UploadedFileForm> existingFiles,
      PwaApplication pwaApplication,
      ConsultationRequest request
  ) {
    var controller = ConsultationResponseFileController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, DOCUMENT_TYPE)
        .withUploadUrl(ReverseRouter.route(on(AppFileUploadRestController.class)
            .upload(pwaApplication.getId(), FILE_PURPOSE.name(), null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(pwaApplication.getId(), null, null),
            Map.of("consultationRequestId", request.getId())))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(pwaApplication.getId(), null, null),
            Map.of("consultationRequestId", request.getId())))
        .build();
  }

  private void createConsultationResponseFileLinks(ConsultationResponseForm form,
                                                   ConsultationRequest consultationRequest,
                                                   ConsultationResponse consultationResponse,
                                                   WebUserAccount user) {
    appFileManagementService.saveFiles(form, consultationRequest.getPwaApplication(), DOCUMENT_TYPE);

    var fileIds = form.getUploadedFiles().stream()
        .map(UploadedFileForm::getFileId)
        .map(String::valueOf)
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
          caseLinkService.generateCaseManagementLink(application)
      );

    } else {

      emailProps = new ConsultationResponseReceivedEmailProps(
          caseOfficerName,
          application.getAppReference(),
          consulteeGroupName,
          getConsultationResponseEmailText(responseDataList.get(0), responseDataList.get(0).getResponseType().getLabelText()),
          caseLinkService.generateCaseManagementLink(application)
      );

    }

    emailService.sendEmail(emailProps, caseOfficerPerson, application.getAppReference());

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
