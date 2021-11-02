package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticeCaseOfficerReviewResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateRequestValidator;

@Service
public class PublicNoticeDocumentUpdateRequestService {


  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateRequestValidator publicNoticeDocumentUpdateRequestValidator;
  private final PublicNoticeDocumentRepository publicNoticeDocumentRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PwaContactService pwaContactService;
  private final NotifyService notifyService;

  @Autowired
  public PublicNoticeDocumentUpdateRequestService(
      PublicNoticeService publicNoticeService,
      PublicNoticeDocumentUpdateRequestValidator publicNoticeDocumentUpdateRequestValidator,
      PublicNoticeDocumentRepository publicNoticeDocumentRepository,
      CamundaWorkflowService camundaWorkflowService,
      EmailCaseLinkService emailCaseLinkService,
      PwaContactService pwaContactService,
      NotifyService notifyService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeDocumentUpdateRequestValidator = publicNoticeDocumentUpdateRequestValidator;
    this.publicNoticeDocumentRepository = publicNoticeDocumentRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.pwaContactService = pwaContactService;
    this.notifyService = notifyService;
  }




  public boolean publicNoticeDocumentUpdateCanBeRequested(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  public BindingResult validate(PublicNoticeDocumentUpdateRequestForm form, BindingResult bindingResult) {
    publicNoticeDocumentUpdateRequestValidator.validate(form, bindingResult);
    return bindingResult;
  }


  private void sendPublicNoticeDocumentUpdateRequestEmail(PwaApplication pwaApplication, String comments) {

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);
    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER);

    emailRecipients.forEach(recipient -> {
      var emailProps = new PublicNoticeUpdateRequestEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          comments,
          caseManagementLink);
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });
  }

  @Transactional
  public void updatePublicNoticeDocumentAndTransitionWorkflow(PwaApplication pwaApplication,
                                                              PublicNoticeDocumentUpdateRequestForm form) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var latestPublicNoticeDocument = publicNoticeService.getLatestPublicNoticeDocument(publicNotice);
    latestPublicNoticeDocument.setComments(form.getComments());
    publicNoticeDocumentRepository.save(latestPublicNoticeDocument);

    publicNotice.setStatus(PublicNoticeStatus.APPLICANT_UPDATE);
    publicNotice = publicNoticeService.savePublicNotice(publicNotice);

    camundaWorkflowService.setWorkflowProperty(publicNotice, PublicNoticeCaseOfficerReviewResult.UPDATE_REQUESTED);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));

    sendPublicNoticeDocumentUpdateRequestEmail(pwaApplication, form.getComments());
  }


}
