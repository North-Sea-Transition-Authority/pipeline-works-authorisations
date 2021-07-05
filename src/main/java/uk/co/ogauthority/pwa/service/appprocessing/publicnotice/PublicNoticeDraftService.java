package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Clock;
import java.time.Instant;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class PublicNoticeDraftService {

  private final AppFileService appFileService;
  private final PublicNoticeService publicNoticeService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PwaTeamService pwaTeamService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Autowired
  public PublicNoticeDraftService(
      AppFileService appFileService,
      PublicNoticeService publicNoticeService,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock, NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService,
      PwaTeamService pwaTeamService) {
    this.appFileService = appFileService;
    this.publicNoticeService = publicNoticeService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.pwaTeamService = pwaTeamService;
  }



  /*
  This method assumes either a public notice doesn't already exist,
    or the latest public notice is ended or has a rejected draft.
  A new version of a public notice is created if ended/non existent,
    or a new version of the public notice request of an active public notice will be created if the draft has been rejected
   */
  @Transactional
  public void submitPublicNoticeDraft(PublicNoticeDraftForm form,
                                      PwaApplication pwaApplication,
                                      AuthenticatedUserAccount userAccount) {

    var latestPublicNoticeOpt = publicNoticeService.getLatestPublicNoticeOpt(pwaApplication);
    PublicNoticeRequest publicNoticeRequest;
    PublicNotice publicNotice;
    PublicNoticeDocument publicNoticeDocument;

    boolean isInitialDraft;
    if (latestPublicNoticeOpt.isPresent()
        && publicNoticeService.isPublicNoticeStatusEnded(latestPublicNoticeOpt.get().getStatus())) {
      isInitialDraft = true;
    } else if (latestPublicNoticeOpt.isPresent()
        &&  !publicNoticeService.isPublicNoticeStatusEnded(latestPublicNoticeOpt.get().getStatus())) {
      isInitialDraft = false;
    } else {
      isInitialDraft = true;
    }


    if (!isInitialDraft) {
      publicNotice = latestPublicNoticeOpt.get();
      publicNotice.setStatus(PublicNoticeStatus.MANAGER_APPROVAL);

      var latestPublicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);
      publicNoticeRequest = createPublicNoticeRequestFromForm(
          form, publicNotice, latestPublicNoticeRequest.getVersion() + 1, userAccount.getLinkedPerson());

      var latestPublicNoticeDocument = publicNoticeService.getLatestPublicNoticeDocument(publicNotice);
      latestPublicNoticeDocument.setDocumentType(PublicNoticeDocumentType.ARCHIVED);
      publicNoticeService.savePublicNoticeDocument(latestPublicNoticeDocument);
      publicNoticeDocument = new PublicNoticeDocument(
          publicNotice, latestPublicNoticeDocument.getVersion() + 1, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);

    } else {

      var version = latestPublicNoticeOpt.map(notice -> notice.getVersion() + 1).orElse(1);
      publicNotice = new PublicNotice(pwaApplication, PublicNoticeStatus.MANAGER_APPROVAL, version);
      publicNoticeRequest = createPublicNoticeRequestFromForm(form, publicNotice, 1, userAccount.getLinkedPerson());
      publicNoticeDocument = new PublicNoticeDocument(publicNotice, 1, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    }


    publicNoticeService.savePublicNotice(publicNotice);
    publicNoticeService.savePublicNoticeRequest(publicNoticeRequest);

    appFileService.updateFiles(form, pwaApplication, FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, userAccount);
    publicNoticeDocument = publicNoticeService.savePublicNoticeDocument(publicNoticeDocument);
    try {
      var publicNoticeDocumentLink = publicNoticeService.createPublicNoticeDocumentLinkFromForm(
          pwaApplication, form.getUploadedFileWithDescriptionForms().get(0), publicNoticeDocument);
      publicNoticeService.savePublicNoticeDocumentLink(publicNoticeDocumentLink);
    } catch (IndexOutOfBoundsException e) {
      throw new ActionNotAllowedException(
          "Cannot submit draft as the uploaded document cannot be found within the draft form for application id: " +
              pwaApplication.getId());
    }

    if (!isInitialDraft) {
      camundaWorkflowService.completeTask(new WorkflowTaskInstance(publicNotice,
          PwaApplicationPublicNoticeWorkflowTask.DRAFT));
    } else {
      camundaWorkflowService.startWorkflow(publicNotice);
    }
    sendPublicNoticeApprovalEmails(pwaApplication, form.getReason().getReasonText());

  }


  private void sendPublicNoticeApprovalEmails(PwaApplication pwaApplication, String publicNoticeReason) {

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    var pwaManagers = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER);

    pwaManagers.forEach(pwaManager -> {

      var emailProps = new PublicNoticeApprovalRequestEmailProps(
          pwaManager.getFullName(),
          pwaApplication.getAppReference(),
          publicNoticeReason,
          caseManagementLink);
      notifyService.sendEmail(emailProps, pwaManager.getEmailAddress());
    });
  }


  private PublicNoticeRequest createPublicNoticeRequestFromForm(PublicNoticeDraftForm form,
                                                                PublicNotice publicNotice,
                                                                int version,
                                                                Person person) {
    var publicNoticeRequest = new PublicNoticeRequest();
    publicNoticeRequest.setPublicNotice(publicNotice);
    publicNoticeRequest.setCoverLetterText(form.getCoverLetterText());
    publicNoticeRequest.setStatus(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    publicNoticeRequest.setReason(form.getReason());
    publicNoticeRequest.setReasonDescription(
        PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT.equals(form.getReason()) ? form.getReasonDescription() : null);
    publicNoticeRequest.setVersion(version);
    publicNoticeRequest.setCreatedTimestamp(Instant.now(clock));
    publicNoticeRequest.setCreatedByPersonId(person.getId().asInt());
    return publicNoticeRequest;
  }


}
