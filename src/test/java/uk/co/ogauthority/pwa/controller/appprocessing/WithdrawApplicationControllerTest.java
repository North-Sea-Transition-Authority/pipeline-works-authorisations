package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.mockito.ArgumentMatchers.any;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.application.WithdrawApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.WithdrawPublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = WithdrawApplicationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class WithdrawApplicationControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private WithdrawApplicationService withdrawApplicationService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private  WithdrawPublicNoticeService withdrawPublicNoticeService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 30);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.WITHDRAW_APPLICATION);

    when(withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplicationDetail.getPwaApplication())).thenReturn(false);
  }

  @Test
  void renderWithdrawApplication_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(WithdrawApplicationController.class)
                .renderWithdrawApplication(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postWithdrawApplication_processingPermissionSmokeTest() {

    when(withdrawApplicationService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new WithdrawApplicationForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(WithdrawApplicationController.class)
                .postWithdrawApplication(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postWithdrawApplication() throws Exception {

    when(withdrawApplicationService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new WithdrawApplicationForm(), "form"));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawApplicationController.class)
        .postWithdrawApplication(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(withdrawApplicationService, times(1)).withdrawApplication(any(), any(), any());

  }

  @Test
  void postWithdrawApplication_validationFail() throws Exception {

    var failedBindingResult = new BeanPropertyBindingResult(new WithdrawApplicationForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(withdrawApplicationService.validate(any(), any(), any())).thenReturn(failedBindingResult);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawApplicationController.class)
        .postWithdrawApplication(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("appprocessing/withdrawApplication"));

  }

  @Test
  void postWithdrawApplication_openPublicNoticeExists() throws Exception {

    when(withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(any())).thenReturn(true);

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawApplicationController.class)
        .postWithdrawApplication(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("appprocessing/withdrawApplication"));

  }



}
