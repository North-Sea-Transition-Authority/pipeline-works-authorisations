package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDocumentUpdateController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.Assignment;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeDocumentReviewRequestEmailProps;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateValidator;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDocumentUpdateServiceTest {

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
  private CaseLinkService caseLinkService;

  @Mock
  private PersonService personService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private AppFileManagementService appFileManagementService;

  @Mock
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocumentLink> publicNoticeDocumentLinkArgumentCaptor;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    publicNoticeDocumentUpdateService = new PublicNoticeDocumentUpdateService(publicNoticeService, validator,
        publicNoticeDocumentRepository, publicNoticeDocumentLinkRepository, camundaWorkflowService,
        personService, assignmentService, caseLinkService, appFileManagementService, emailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanNotBeUpdated() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var pageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(pwaApplication);
    assertThat(pageBannerViewOpt).isEmpty();
  }

  @Test
  void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanBeUpdated() {

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
  void validate_verifyServiceInteractions() {

    var form = new UpdatePublicNoticeDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeDocumentUpdateService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


  @Test
  void updatePublicNoticeDocumentAndTransitionWorkflow_allDcoumentAndLinksAndFilesExistAndAreUpdated() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);

    var publicNoticeAtCaseOfficerReview = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(publicNotice)).thenReturn(publicNoticeAtCaseOfficerReview);

    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(latestPublicNoticeDocument.getVersion() + 1);
    when(publicNoticeDocumentRepository.save(newPublicNoticeDocument)).thenReturn(newPublicNoticeDocument);

    var form = new UpdatePublicNoticeDocumentForm();
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var documentLink = new PublicNoticeDocumentLink(newPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromFileId(
        pwaApplication,
        String.valueOf(form.getUploadedFiles().getFirst().getFileId()), newPublicNoticeDocument))
        .thenReturn(documentLink);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var caseOfficerPerson = PersonTestUtil.createDefaultPerson();
    var caseOfficerAssignment = new Assignment
        (pwaApplication.getBusinessKey(), WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, caseOfficerPerson.getId());
    when(assignmentService.getAssignmentOrError(pwaApplication, WorkflowAssignment.CASE_OFFICER)).thenReturn(caseOfficerAssignment);
    when(personService.getPersonById(caseOfficerAssignment.getAssigneePersonId())).thenReturn(caseOfficerPerson);

    publicNoticeDocumentUpdateService.updatePublicNoticeDocumentAndTransitionWorkflow(pwaApplication, form);

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

    verify(emailService, times(1)).sendEmail(expectedEmailProps, caseOfficerPerson, pwaApplication.getAppReference());

  }



}
