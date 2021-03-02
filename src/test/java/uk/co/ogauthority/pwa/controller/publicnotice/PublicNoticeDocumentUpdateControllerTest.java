package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.ViewNotFoundException;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeTestUtil;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PublicNoticeDocumentUpdateController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class PublicNoticeDocumentUpdateControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTestBuilder;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private PublicNoticeService publicNoticeService;

  @MockBean
  private PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    endpointTestBuilder = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);


    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
        pwaApplicationDetail.getPwaApplication()), EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplicationDetail.getPwaApplication());
    when(publicNoticeService.getLatestPublicNotice(any()))
        .thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice))
        .thenReturn(publicNoticeRequest);

    when(publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(any())).thenReturn(true);

    var fileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(publicNoticeDocumentUpdateService.getPublicNoticeDocumentFileView(any()))
        .thenReturn(Optional.of(fileView));
  }


  @Test
  public void renderUpdatePublicNoticeDocument_appStatusSmokeTest() {

    endpointTestBuilder.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .renderUpdatePublicNoticeDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTestBuilder.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderUpdatePublicNoticeDocument_processingPermissionSmokeTest() {

    endpointTestBuilder.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .renderUpdatePublicNoticeDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTestBuilder.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderUpdatePublicNoticeDocument_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class).renderUpdatePublicNoticeDocument(
        pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderUpdatePublicNoticeDocument_noUpdatablePublicNoticeDocument() throws Exception {

    when(publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
        .renderUpdatePublicNoticeDocument(
            pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException() instanceof AccessDeniedException).isTrue());
  }

  @Test
  public void renderUpdatePublicNoticeDocument_noDocumentFileView() throws Exception {

    when(publicNoticeDocumentUpdateService.getPublicNoticeDocumentFileView(any())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
        .renderUpdatePublicNoticeDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException() instanceof ViewNotFoundException).isTrue());
  }


  @Test
  public void postUpdatePublicNoticeDocument_appStatusSmokeTest() {

    when(publicNoticeDocumentUpdateService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new UpdatePublicNoticeDocumentForm(), "form"));

    endpointTestBuilder.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .postUpdatePublicNoticeDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTestBuilder.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postUpdatePublicNoticeDocument_permissionSmokeTest() {

    when(publicNoticeDocumentUpdateService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new UpdatePublicNoticeDocumentForm(), "form"));

    endpointTestBuilder.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .postUpdatePublicNoticeDocument(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTestBuilder.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postUpdatePublicNoticeDocument_validationFail() throws Exception {

    var failedBindingResult = new BeanPropertyBindingResult(new UpdatePublicNoticeDocumentForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(publicNoticeDocumentUpdateService.validate(any(), any())).thenReturn(failedBindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
        .postUpdatePublicNoticeDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("publicNotice/updatePublicNoticeDocument"));
  }

  @Test
  public void postUpdatePublicNoticeDocument_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(post(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class).postUpdatePublicNoticeDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postUpdatePublicNoticeDocument_noUpdatablePublicNoticeDocument() throws Exception {

    when(publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
        .postUpdatePublicNoticeDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException() instanceof AccessDeniedException).isTrue());
  }

  @Test
  public void postUpdatePublicNoticeDocument_noDocumentFileView() throws Exception {

    when(publicNoticeDocumentUpdateService.getPublicNoticeDocumentFileView(any())).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
        .postUpdatePublicNoticeDocument(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException() instanceof ViewNotFoundException).isTrue());
  }





}
