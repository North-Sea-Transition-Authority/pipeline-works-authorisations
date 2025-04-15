package uk.co.ogauthority.pwa.controller.consultations.responses;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
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
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementControllerTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;

@WebMvcTest(controllers = ConsultationResponseController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ConsultationResponseControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ConsultationResponseService consultationResponseService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConsultationViewService consultationViewService;

  @MockBean
  private ConsultationResponseValidator consultationResponseValidator;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private AuthenticatedUserAccount user;
  private PwaApplicationDetail pwaApplicationDetail;
  private ConsultationRequest consultationRequest;

  private ProcessingPermissionsDto permissionsDto;

  @BeforeEach
  void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.of(PwaUserPrivilege.PWA_ACCESS));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
    when(consultationRequestService.getConsultationRequestByIdOrThrow(any())).thenReturn(consultationRequest);
    when(consultationResponseService.isUserAssignedResponderForConsultation(any(), any())).thenReturn(true);

    permissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        EnumSet.allOf(PwaAppProcessingPermission.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setUserPrivileges(PwaUserPrivilege.PWA_ACCESS)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CONSULTATION_RESPONDER)
        .setConsultationRequest(consultationRequest);

    when(consultationResponseService.getFileUploadComponentAttributes(any(), any(), any()))
        .thenReturn(FileManagementControllerTestUtils.createUploadFileAttributes());
  }

  @Test
  void renderResponder_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationResponseController.class)
                .renderResponder(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postResponder_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationResponseController.class)
                .postResponder(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postResponder() throws Exception {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
        .postResponder(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            1, null, null, null, null)))
        .with(user(user))
        .param("consultationResponseOption", "")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consultationResponseService, times(1)).saveResponseAndCompleteWorkflow(any(), eq(consultationRequest), eq(user));

  }

  @Test
  void postResponder_validationFail() throws Exception {

    ControllerTestUtils.mockValidatorErrors(consultationResponseValidator, List.of("responseDataForms[CONTENT].consultationResponseOption"));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
        .postResponder(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            1, null, null, null, null)))
        .with(user(user))
        .param("consultationResponseOption", "")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("consultation/responses/responderForm"));

  }

}
