package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDocumentUpdateController;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeDocumentReviewRequestEmailProps;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeDocumentUpdateServiceTest {

  private PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeDocumentUpdateValidator validator;

  @Mock
  private AppFileService appFileService;

  @Mock
  private PublicNoticeDocumentRepository publicNoticeDocumentRepository;

  @Mock
  private PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PersonService personService;

  @Mock
  private AssignmentService assignmentService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocumentLink> publicNoticeDocumentLinkArgumentCaptor;




  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;



  @Before
  public void setUp() {

    publicNoticeDocumentUpdateService = new PublicNoticeDocumentUpdateService(publicNoticeService, validator, appFileService,
        publicNoticeDocumentRepository, publicNoticeDocumentLinkRepository, camundaWorkflowService,
        personService, assignmentService, emailCaseLinkService, notifyService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanNotBeUpdated() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var pageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(pwaApplication);
    assertThat(pageBannerViewOpt).isEmpty();
  }

  @Test
  public void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanBeUpdated() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));

    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createApprovedPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice))
        .thenReturn(publicNoticeRequest);

    var pageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(pwaApplication);
    assertThat(pageBannerViewOpt).isPresent();

    var pageBannerView = pageBannerViewOpt.get();
    assertThat(pageBannerView.getHeader()).isEqualTo("Public notice document update requested");
    assertThat(pageBannerView.getHeaderCaption()).isEqualTo("Requested " + DateUtils.formatDateTime(
        publicNoticeRequest.getResponseTimestamp()));
    assertThat(pageBannerView.getBannerLink().getUrl()).isEqualTo(
        ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
            .renderUpdatePublicNoticeDocument(pwaApplication.getId(), pwaApplication.getApplicationType(), null, null, null)));
    assertThat(pageBannerView.getBannerLink().getText()).isEqualTo(PublicNoticeAction.UPDATE_DOCUMENT.getDisplayText());
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new UpdatePublicNoticeDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeDocumentUpdateService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


  @Test
  public void updatePublicNoticeDocumentAndTransitionWorkflow_allDcoumentAndLinksAndFilesExistAndAreUpdated() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);

    var publicNoticeAtCaseOfficerReview = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(publicNotice)).thenReturn(publicNoticeAtCaseOfficerReview);

    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        "file id", "desc", Instant.now());

    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(latestPublicNoticeDocument.getVersion() + 1);
    when(publicNoticeDocumentRepository.save(newPublicNoticeDocument)).thenReturn(newPublicNoticeDocument);

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var documentLink = new PublicNoticeDocumentLink(newPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromForm(pwaApplication, uploadFileWithDescriptionForm, newPublicNoticeDocument))
        .thenReturn(documentLink);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var caseOfficerPerson = PersonTestUtil.createDefaultPerson();
    var caseOfficerAssignment = new Assignment
        (pwaApplication.getBusinessKey(), WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, caseOfficerPerson.getId());
    when(assignmentService.getAssignmentOrError(pwaApplication, WorkflowAssignment.CASE_OFFICER)).thenReturn(caseOfficerAssignment);
    when(personService.getPersonById(caseOfficerAssignment.getAssigneePersonId())).thenReturn(caseOfficerPerson);

    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFileWithDescriptionForms(List.of(uploadFileWithDescriptionForm));
    publicNoticeDocumentUpdateService.updatePublicNoticeDocumentAndTransitionWorkflow(pwaApplication, form, user);


    //verify documents and links updated/added
    verify(publicNoticeDocumentRepository, times(2)).save(publicNoticeDocumentArgumentCaptor.capture());
    var actualNewPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(0);
    assertThat(actualNewPublicNoticeDocument.getPublicNotice()).isEqualTo(publicNoticeAtCaseOfficerReview);
    assertThat(actualNewPublicNoticeDocument.getVersion()).isEqualTo(newPublicNoticeDocument.getVersion());
    assertThat(actualNewPublicNoticeDocument.getDocumentType()).isEqualTo(newPublicNoticeDocument.getDocumentType());

    var actualExistingPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(1);
    assertThat(actualExistingPublicNoticeDocument.getDocumentType()).isEqualTo(PublicNoticeDocumentType.ARCHIVED);

    verify(publicNoticeDocumentLinkRepository, times(1)).save(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);
    assertThat(actualDocumentLink.getPublicNoticeDocument()).isEqualTo(newPublicNoticeDocument);

    //verify public notice updated and call to progress workflow
    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.CASE_OFFICER_REVIEW);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(
        publicNotice, PwaApplicationPublicNoticeWorkflowTask.APPLICANT_UPDATE));

    //verify email sent
    var expectedEmailProps = new PublicNoticeDocumentReviewRequestEmailProps(
        caseOfficerPerson.getFullName(),
        pwaApplication.getAppReference(),
        caseManagementLink);

    verify(notifyService, times(1)).sendEmail(expectedEmailProps, caseOfficerPerson.getEmailAddress());

  }



}
