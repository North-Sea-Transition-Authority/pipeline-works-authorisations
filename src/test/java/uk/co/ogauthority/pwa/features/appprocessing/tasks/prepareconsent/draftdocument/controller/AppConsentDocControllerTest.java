package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.controller;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.PrepareConsentTaskService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.ConsentDocumentService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval.PreSendForApprovalChecksViewTestUtil;
import uk.co.ogauthority.pwa.features.consents.viewconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatusResult;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AppConsentDocController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class AppConsentDocControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private DocumentService documentService;

  @MockBean
  private PrepareConsentTaskService prepareConsentTaskService;

  @MockBean
  private ConsentDocumentService consentDocumentService;

  @MockBean
  private ConsentReviewService consentReviewService;

  @MockBean
  private TemplateTextService templateTextService;

  @MockBean
  private MailMergeService mailMergeService;

  @MockBean
  private DocgenService docgenService;
  
  @MockBean
  private MarkdownService markdownService;

  @MockBean
  private ConsentFileViewerService consentFileViewerService;

  private PwaApplicationEndpointTestBuilder editDocumentEndpointTester;
  private PwaApplicationEndpointTestBuilder sendForApprovalEndpointTester;

  private AuthenticatedUserAccount user;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(true);
    when(consentDocumentService.getPreSendForApprovalChecksView(any()))
        .thenReturn(PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView());

    editDocumentEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

    sendForApprovalEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
        pwaApplicationDetail.getPwaApplication()), EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(2);
      errors.rejectValue("coverLetterText", "coverLetterText.error", "error message");
      return invocation;
    }).when(consentDocumentService).validateSendConsentFormUsingPreApprovalChecks(any(), any(), any(), any());

    when(consentFileViewerService.getLatestConsultationRequestViewForDocumentType(
        pwaApplicationDetail.getPwaApplication(), ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION)).thenReturn(Optional.empty());

  }

  @Test
  public void renderConsentDocEditor_permissionSmokeTest() {

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderConsentDocEditor(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderConsentDocEditor_statusSmokeTest() {

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderConsentDocEditor(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    editDocumentEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderConsentDocEditor_prepareConsentTaskNotAccessible() throws Exception {

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class).renderConsentDocEditor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postConsentDocEditor_permissionSmokeTest() {

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .postConsentDocEditor(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postConsentDocEditor_statusSmokeTest() {

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .postConsentDocEditor(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postConsentDocEditor_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).postConsentDocEditor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(documentService, times(1)).createDocumentInstance(pwaApplicationDetail.getPwaApplication(), user.getLinkedPerson());

  }

  @Test
  public void postConsentDocEditor_prepareConsentTaskNotAccessible() throws Exception {

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).postConsentDocEditor(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void schedulePreview_permissionSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));
    var run = new DocgenRun();
    run.setId(1);
    when(docgenService.createDocgenRun(any(), any(), any())).thenReturn(run);

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .schedulePreview(applicationDetail.getMasterPwaApplicationId(), type, null, null, Optional.empty())));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void schedulePreview_statusSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));
    var run = new DocgenRun();
    run.setId(1);
    when(docgenService.createDocgenRun(any(), any(), any())).thenReturn(run);

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .schedulePreview(applicationDetail.getMasterPwaApplicationId(), type, null, null, Optional.empty())));

    editDocumentEndpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void schedulePreview_success() throws Exception {

    var instance = new DocumentInstance();
    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(instance));
    var run = new DocgenRun();
    run.setId(1);
    when(docgenService.createDocgenRun(any(), any(), any())).thenReturn(run);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class)
        .schedulePreview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, Optional.empty())))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(docgenService, times(1)).scheduleDocumentGeneration(run);
    verify(analyticsService, times(1)).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.DOCUMENT_PREVIEW));

  }

  @Test
  public void schedulePreview_prepareConsentTaskNotAccessible() throws Exception {

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class)
        .schedulePreview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, Optional.empty())))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderDocumentGenerating_permissionSmokeTest() {

    setupDocRunCheckEndpoint();

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderDocumentGenerating(applicationDetail.getMasterPwaApplicationId(), type, Long.valueOf(
                    applicationDetail.getMasterPwaApplicationId()), null, null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  private void setupDocRunCheckEndpoint() {
    when(docgenService.getDocgenRun(anyLong())).thenAnswer(invocationOnMock -> {
      var run = new DocgenRun();
      run.setId(invocationOnMock.getArgument(0));
      var docInstance = new DocumentInstance();
      var app = new PwaApplication();
      app.setId(Math.toIntExact(invocationOnMock.getArgument(0)));
      docInstance.setPwaApplication(app);
      run.setDocumentInstance(docInstance);
      run.setDocGenType(DocGenType.PREVIEW);
      return run;
    });
  }

  @Test
  public void renderDocumentGenerating_statusSmokeTest() {

    setupDocRunCheckEndpoint();

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderDocumentGenerating(applicationDetail.getMasterPwaApplicationId(), type, Long.valueOf(applicationDetail.getMasterPwaApplicationId()), null, null)));

    editDocumentEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderDocumentGenerating_prepareConsentTaskNotAccessible() throws Exception {

    setupDocRunCheckEndpoint();

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class)
        .renderDocumentGenerating(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1L, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderDocumentGenerating_docgenRunIsNotPreview_error() throws Exception {

    when(docgenService.getDocgenRun(anyLong())).thenAnswer(invocationOnMock -> {
      var run = new DocgenRun();
      run.setId(invocationOnMock.getArgument(0));
      var docInstance = new DocumentInstance();
      var app = new PwaApplication();
      app.setId(Math.toIntExact(invocationOnMock.getArgument(0)));
      docInstance.setPwaApplication(app);
      run.setDocumentInstance(docInstance);
      run.setDocGenType(DocGenType.FULL);
      return run;
    });

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class)
        .renderDocumentGenerating(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1L, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderDocumentGenerating_docgenRunDoesntMatchApp_error() throws Exception {

    when(docgenService.getDocgenRun(anyLong())).thenAnswer(invocationOnMock -> {
      var run = new DocgenRun();
      run.setId(invocationOnMock.getArgument(0));
      var docInstance = new DocumentInstance();
      var app = new PwaApplication();
      app.setId(Math.toIntExact(999999));
      docInstance.setPwaApplication(app);
      run.setDocumentInstance(docInstance);
      run.setDocGenType(DocGenType.PREVIEW);
      return run;
    });

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class)
        .renderDocumentGenerating(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1L, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void getDocgenRunStatus_permissionSmokeTest() {

    setupDocRunCheckEndpoint();

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .getDocgenRunStatus(applicationDetail.getMasterPwaApplicationId(), type, Long.valueOf(
                    applicationDetail.getMasterPwaApplicationId()), null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void getDocgenRunStatus_statusSmokeTest() {

    setupDocRunCheckEndpoint();

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .getDocgenRunStatus(applicationDetail.getMasterPwaApplicationId(), type, Long.valueOf(applicationDetail.getMasterPwaApplicationId()), null)));

    editDocumentEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void getDocgenRunStatus_prepareConsentTaskNotAccessible() throws Exception {

    setupDocRunCheckEndpoint();

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class)
        .getDocgenRunStatus(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1L, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void getDocgenRunStatus_success() throws Exception {

    setupDocRunCheckEndpoint();

    var run = new DocgenRun();
    run.setId(1L);
    run.setStatus(DocgenRunStatus.COMPLETE);
    var docgenStatusResult = new DocgenRunStatusResult(run, "onCompleteUrl");

    when(docgenService.getDocgenRunStatus(anyLong(), any())).thenReturn(docgenStatusResult);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class)
        .getDocgenRunStatus(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), 1L, null)))
        .with(user(user))
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.docgenRunId").value("1"))
        .andExpect(jsonPath("$.status").value("COMPLETE"))
        .andExpect(jsonPath("$.onCompleteUrl").value("onCompleteUrl"));

  }

  @Test
  public void renderReloadDocument_permissionSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderReloadDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderReloadDocument_statusSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    editDocumentEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderReloadDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderReloadDocument_noDocument() throws Exception {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class).renderReloadDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderReloadDocument_prepareConsentTaskNotAccessible() throws Exception {
    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class).renderReloadDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postReloadDocument_permissionSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .postReloadDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postReloadDocument_statusSmokeTest() {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    editDocumentEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .postReloadDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    editDocumentEndpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postReloadDocument_success() throws Exception {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.of(new DocumentInstance()));

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).postReloadDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(documentService, times(1)).reloadDocumentInstance(pwaApplicationDetail.getPwaApplication(), user.getLinkedPerson());

  }

  @Test
  public void postReloadDocument_noDocument() throws Exception {

    when(documentService.getDocumentInstance(any(), any())).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).postReloadDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(documentService, times(0)).reloadDocumentInstance(pwaApplicationDetail.getPwaApplication(), user.getLinkedPerson());

  }

  @Test
  public void postReloadDocument_prepareConsentTaskNotAccessible() throws Exception {

    when(prepareConsentTaskService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).postReloadDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(documentService, times(0)).reloadDocumentInstance(any(), any());

  }

  @Test
  public void renderSendForApproval_statusSmokeTest() {

    sendForApprovalEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderSendForApproval(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    sendForApprovalEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderSendForApproval_permissionsSmokeTest() {

    sendForApprovalEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .renderSendForApproval(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    sendForApprovalEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSendForApproval_sendNotAllowed() throws Exception {

    var failCheck = PreSendForApprovalChecksViewTestUtil.createFailedChecksView();
    when(consentDocumentService.getPreSendForApprovalChecksView(any())).thenReturn(failCheck);

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class).renderSendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderSendForApproval_sendAllowed() throws Exception {

    when(templateTextService.getLatestVersionTextByType(pwaApplicationDetail.getPwaApplicationType().getConsentEmailTemplateTextType())).thenReturn("my cover letter");

    mockMvc.perform(get(ReverseRouter.route(on(AppConsentDocController.class).renderSendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", hasProperty("coverLetterText", is("my cover letter"))));

  }

  @Test
  public void sendForApproval_statusSmokeTest() {

    sendForApprovalEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .sendForApproval(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    sendForApprovalEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void sendForApproval_permissionsSmokeTest() {

    sendForApprovalEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AppConsentDocController.class)
                .sendForApproval(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    sendForApprovalEndpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void sendForApproval_sendNotAllowed() throws Exception {

    var failCheck = PreSendForApprovalChecksViewTestUtil.createFailedChecksView();
    when(consentDocumentService.getPreSendForApprovalChecksView(any())).thenReturn(failCheck);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).sendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consentDocumentService, never()).sendForApproval(any(), any(), any(), any());

  }

  @Test
  public void sendForApproval_sendAllowed_noParallelConsents() throws Exception {

    // dont fail validation
    doAnswer(invocation -> invocation).when(consentDocumentService).validateSendConsentFormUsingPreApprovalChecks(any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).sendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("coverLetterText", "mytext"))
        .andExpect(status().is3xxRedirection());

    verify(consentDocumentService).sendForApproval(pwaApplicationDetail, "mytext", user, List.of());

  }

  @Test
  public void sendForApproval_sendAllowed_withParallelConsents() throws Exception {

    // dont fail validation
    doAnswer(invocation -> invocation).when(consentDocumentService).validateSendConsentFormUsingPreApprovalChecks(any(), any(), any(), any());

    var preSendApprovalView = PreSendForApprovalChecksViewTestUtil.createParallelConsentsChecksView();
    when(consentDocumentService.getPreSendForApprovalChecksView(any()))
        .thenReturn(preSendApprovalView);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).sendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("coverLetterText", "mytext"))
        .andExpect(status().is3xxRedirection());

    verify(consentDocumentService).sendForApproval(pwaApplicationDetail, "mytext", user, preSendApprovalView.getParallelConsentViews());

  }

  @Test
  public void sendForApproval_alreadySent() throws Exception {

    when(consentReviewService.areThereAnyOpenReviews(pwaApplicationDetail)).thenReturn(true);
    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).sendForApproval(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("coverLetterText", "mytext"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/work-area"));

    verify(consentDocumentService, never()).sendForApproval(pwaApplicationDetail, "mytext", user, List.of());

  }

  @Test
  public void previewCoverLetter() throws Exception {

    // dont fail validation
    doAnswer(invocation -> invocation).when(consentDocumentService).validateSendConsentFormUsingPreApprovalChecks(any(), any(), any(), any());

    var preSendApprovalView = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();
    when(consentDocumentService.getPreSendForApprovalChecksView(any()))
        .thenReturn(preSendApprovalView);

    when(markdownService.convertMarkdownToHtml(eq("mytext"), any())).thenReturn("mymarkdownpreview");

    var container = new MailMergeContainer();
    when(mailMergeService.resolveMergeFields(pwaApplicationDetail.getPwaApplication(), DocGenType.PREVIEW)).thenReturn(container);

    mockMvc.perform(post(ReverseRouter.route(on(AppConsentDocController.class).previewCoverLetter(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("coverLetterText", "mytext")
            .param("preview-text-button", "Preview text"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("markdownPreviewHtml", "mymarkdownpreview"));

    verify(consentDocumentService, times(0)).sendForApproval(any(), any(), any(), any());

    verify(markdownService, times(1)).convertMarkdownToHtml("mytext", container);

  }

}
