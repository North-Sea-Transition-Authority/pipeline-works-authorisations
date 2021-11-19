package uk.co.ogauthority.pwa.controller.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationRequestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ConsultationRequestControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private ConsultationRequestController consultationRequestController;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    consultationRequestController = new ConsultationRequestController(consultationRequestService, null, new AppProcessingBreadcrumbService());

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.EDIT_CONSULTATIONS);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 30);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
        pwaApplicationDetail.getPwaApplication()), EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

  }

  @Test
  public void renderRequestConsultation_getConsulteeGroups_sorted() throws Exception {

    var consulteeGroupDetail1 = ConsulteeGroupTestingUtils.createConsulteeGroup("second consultee", "sc");
    consulteeGroupDetail1.setDisplayOrder(2);
    var consulteeGroupDetail2 = ConsulteeGroupTestingUtils.createConsulteeGroup("third consultee", "tc");
    consulteeGroupDetail2.setDisplayOrder(3);
    var consulteeGroupDetail3 = ConsulteeGroupTestingUtils.createConsulteeGroup("fourth consultee", "foc");
    consulteeGroupDetail3.setDisplayOrder(null);
    var consulteeGroupDetail4 = ConsulteeGroupTestingUtils.createConsulteeGroup("first consultee", "fic");
    consulteeGroupDetail4.setDisplayOrder(1);

    when(consultationRequestService.getAllConsulteeGroups()).thenReturn(
        List.of(consulteeGroupDetail1, consulteeGroupDetail2, consulteeGroupDetail3, consulteeGroupDetail4));

    var modelAndView = Objects.requireNonNull(mockMvc.perform(get(ReverseRouter.route(on(ConsultationRequestController.class).renderRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, new ConsultationRequestForm())))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andReturn()
        .getModelAndView());

    var expectedOrderedConsulteeGroups = List.of(consulteeGroupDetail4, consulteeGroupDetail1, consulteeGroupDetail2,  consulteeGroupDetail3);
    assertThat(modelAndView.getModel().get("consulteeGroups")).isEqualTo(expectedOrderedConsulteeGroups);

  }

  @Test
  public void renderRequestConsultation_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .renderRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRequestConsultation_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .renderRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRequestConsultation_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(get(ReverseRouter.route(on(ConsultationRequestController.class).renderRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postRequestConsultation_appStatusSmokeTest() {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .postRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postRequestConsultation_processingPermissionSmokeTest() {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .postRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRequestConsultation() throws Exception {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
        .postRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("consulteeGroupSelection", "")
        .param("daysToRespond", "28")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consultationRequestService, times(1)).saveEntitiesAndStartWorkflow(any(), eq(pwaApplicationDetail), eq(user));

  }

  @Test
  public void postRequestConsultation_validationFail() throws Exception {

    var failedBindingResult = new BeanPropertyBindingResult(new ConsultationRequestForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(consultationRequestService.validate(any(), any(), any())).thenReturn(failedBindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
        .postRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("consulteeGroupSelection", "")
        .param("daysToRespond", "28")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("consultation/consultationRequest"));

  }

  @Test
  public void postRequestConsultation_noSatisfactoryVersions() throws Exception {

    when(processingPermissionService.getProcessingPermissionsDto(any(), any())).thenReturn(new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class)));

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class).postRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

}
