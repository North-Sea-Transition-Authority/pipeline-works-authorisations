package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDocumentUpdateController;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeDocumentReviewRequestEmailProps;
import uk.co.ogauthority.pwa.model.view.banner.BannerLink;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateValidator;

@Service
public class PublicNoticeDocumentUpdateService {


  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateValidator publicNoticeDocumentUpdateValidator;
  private final AppFileService appFileService;
  private final PublicNoticeDocumentRepository publicNoticeDocumentRepository;
  private final PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PersonService personService;
  private final AssignmentService assignmentService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final NotifyService notifyService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Autowired
  public PublicNoticeDocumentUpdateService(
      PublicNoticeService publicNoticeService,
      PublicNoticeDocumentUpdateValidator publicNoticeDocumentUpdateValidator,
      AppFileService appFileService,
      PublicNoticeDocumentRepository publicNoticeDocumentRepository,
      PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository,
      CamundaWorkflowService camundaWorkflowService,
      PersonService personService,
      AssignmentService assignmentService,
      EmailCaseLinkService emailCaseLinkService,
      NotifyService notifyService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeDocumentUpdateValidator = publicNoticeDocumentUpdateValidator;
    this.appFileService = appFileService;
    this.publicNoticeDocumentRepository = publicNoticeDocumentRepository;
    this.publicNoticeDocumentLinkRepository = publicNoticeDocumentLinkRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.personService = personService;
    this.assignmentService = assignmentService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.notifyService = notifyService;
  }




  public boolean publicNoticeDocumentCanBeUpdated(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }

  public Optional<PageBannerView> getPublicNoticeUpdatePageBannerView(PwaApplication pwaApplication) {

    if (!publicNoticeDocumentCanBeUpdated(pwaApplication)) {
      return Optional.empty();
    }

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);

    return Optional.of(new PageBannerView.PageBannerViewBuilder()
        .setHeader("Public notice document update requested")
        .setHeaderCaption("Requested " + DateUtils.formatDateTime(publicNoticeRequest.getResponseTimestamp()))
        .setBannerLink(new BannerLink(
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .renderUpdatePublicNoticeDocument(pwaApplication.getId(), pwaApplication.getApplicationType(), null, null, null)),
            PublicNoticeAction.UPDATE_DOCUMENT.getDisplayText()
        ))
        .build());
  }

  public BindingResult validate(UpdatePublicNoticeDocumentForm form, BindingResult bindingResult) {
    publicNoticeDocumentUpdateValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private void createAndSaveNewPublicNoticeDocumentAndLink(PwaApplication pwaApplication,
                                                           PublicNotice publicNotice,
                                                           UpdatePublicNoticeDocumentForm form) {

    var latestPublicNoticeDocument = publicNoticeService.getLatestPublicNoticeDocument(publicNotice);

    var newPublicNoticeDocument = new PublicNoticeDocument(
        publicNotice, latestPublicNoticeDocument.getVersion() + 1, PublicNoticeDocumentType.IN_PROGRESS_DOCUMENT);
    newPublicNoticeDocument = publicNoticeDocumentRepository.save(newPublicNoticeDocument);
    latestPublicNoticeDocument.setDocumentType(PublicNoticeDocumentType.ARCHIVED);
    publicNoticeDocumentRepository.save(latestPublicNoticeDocument);

    //accessing list at index 0 as validator ensures there is always and only 1 file
    var newPublicNoticeDocumentLink = publicNoticeService.createPublicNoticeDocumentLinkFromForm(
        pwaApplication, form.getUploadedFileWithDescriptionForms().get(0), newPublicNoticeDocument);
    publicNoticeDocumentLinkRepository.save(newPublicNoticeDocumentLink);
  }

  private void sendPublicNoticeDocumentReviewRequestEmail(PwaApplication pwaApplication) {

    var assignedCaseOfficer = assignmentService.getCaseOfficerAssignment(pwaApplication);
    var caseOfficerPerson = personService.getPersonById(assignedCaseOfficer.getAssigneePersonId());

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);
    var emailProps = new PublicNoticeDocumentReviewRequestEmailProps(
        caseOfficerPerson.getFullName(),
        pwaApplication.getAppReference(),
        caseManagementLink);
    notifyService.sendEmail(emailProps, caseOfficerPerson.getEmailAddress());
  }

  @Transactional
  public void updatePublicNoticeDocumentAndTransitionWorkflow(PwaApplication pwaApplication,
                                                              UpdatePublicNoticeDocumentForm form,
                                                              AuthenticatedUserAccount authenticatedUserAccount) {

    appFileService.updateFiles(form, pwaApplication, FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, authenticatedUserAccount);
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    createAndSaveNewPublicNoticeDocumentAndLink(pwaApplication, publicNotice, form);

    publicNotice.setStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW);
    publicNotice = publicNoticeService.savePublicNotice(publicNotice);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.APPLICANT_UPDATE));
    sendPublicNoticeDocumentReviewRequestEmail(pwaApplication);
  }


}
