package uk.co.ogauthority.pwa.controller.publicnotice.finalisepublicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import uk.co.ogauthority.pwa.controller.publicnotice.FinalisePublicNoticeController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.FinalisePublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = FinalisePublicNoticeController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class FinalisePublicNoticeControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTestBuilder;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;
  
  @MockBean
  private FinalisePublicNoticeService finalisePublicNoticeService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    endpointTestBuilder = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateAppInvolvement(
        pwaApplicationDetail.getPwaApplication(),
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION),
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        ConsultationInvolvementDtoTestUtil.emptyConsultationInvolvement());
    var permissionsDto = new ProcessingPermissionsDto(appInvolvement, EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    when(finalisePublicNoticeService.publicNoticeCanBeFinalised(any())).thenReturn(true);
  }


  @Test
  void renderFinalisePublicNotice_appStatusSmokeTest() {

    endpointTestBuilder.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(FinalisePublicNoticeController.class)
                .renderFinalisePublicNotice(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTestBuilder.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderFinalisePublicNotice_processingPermissionSmokeTest() {

    endpointTestBuilder.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(FinalisePublicNoticeController.class)
                .renderFinalisePublicNotice(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTestBuilder.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderFinalisePublicNotice_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(FinalisePublicNoticeController.class).renderFinalisePublicNotice(
        pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderFinalisePublicNotice_noPublicNoticeToFinalise() throws Exception {

    when(finalisePublicNoticeService.publicNoticeCanBeFinalised(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FinalisePublicNoticeController.class)
        .renderFinalisePublicNotice(
            pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(AccessDeniedException.class));
  }


  @Test
  void postFinalisePublicNotice_appStatusSmokeTest() {

    when(finalisePublicNoticeService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new FinalisePublicNoticeForm(), "form"));

    endpointTestBuilder.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(FinalisePublicNoticeController.class)
                .postFinalisePublicNotice(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTestBuilder.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postFinalisePublicNotice_permissionSmokeTest() {

    when(finalisePublicNoticeService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new FinalisePublicNoticeForm(), "form"));

    endpointTestBuilder.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(FinalisePublicNoticeController.class)
                .postFinalisePublicNotice(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTestBuilder.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postFinalisePublicNotice_validationFail() throws Exception {

    var failedBindingResult = new BeanPropertyBindingResult(new FinalisePublicNoticeForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(finalisePublicNoticeService.validate(any(), any())).thenReturn(failedBindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(FinalisePublicNoticeController.class)
        .postFinalisePublicNotice(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("publicNotice/finalisePublicNotice"));
  }

  @Test
  void postFinalisePublicNotice_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(post(ReverseRouter.route(on(FinalisePublicNoticeController.class).postFinalisePublicNotice(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  void postFinalisePublicNotice_noPublicNoticeToFinalise() throws Exception {

    when(finalisePublicNoticeService.publicNoticeCanBeFinalised(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(FinalisePublicNoticeController.class)
        .postFinalisePublicNotice(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(AccessDeniedException.class));
  }





}
