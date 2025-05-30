package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = PublicNoticeApplicantViewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ViewPublicNoticeControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTestBuilder;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private PublicNoticeService publicNoticeService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    endpointTestBuilder = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);


    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        pwaApplicationDetail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION));
    var permissionsDto = new ProcessingPermissionsDto(appInvolvement, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    var publicNotice = PublicNoticeTestUtil.createPublishedPublicNotice(pwaApplicationDetail.getPwaApplication());
    when(publicNoticeService.getLatestPublicNotice(any())).thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createApprovedPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice))
        .thenReturn(publicNoticeRequest);

    var fileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(publicNoticeService.getLatestPublicNoticeDocumentFileView(any())).thenReturn(fileView);

    when(publicNoticeService.canApplicantViewLatestPublicNotice(any())).thenReturn(true);
  }


  @Test
  void renderViewPublicNotice_processingPermissionSmokeTest() {

    endpointTestBuilder.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PublicNoticeApplicantViewController.class)
                .renderViewPublicNotice(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTestBuilder.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderViewPublicNotice_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(PublicNoticeApplicantViewController.class).renderViewPublicNotice(
        pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderViewPublicNotice_noViewablePublicNoticeDocument() throws Exception {

    when(publicNoticeService.canApplicantViewLatestPublicNotice(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(PublicNoticeApplicantViewController.class)
        .renderViewPublicNotice(
            pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(AccessDeniedException.class));
  }








}
