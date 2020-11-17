package uk.co.ogauthority.pwa.controller.consultations;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 30);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.EDIT_CONSULTATIONS);
  }

  @Test
  public void renderRequestConsultation_getConsulteeGroups_sorted() {

    var processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER),
        null,
        null);

    var consulteeGroupDetail1 = new ConsulteeGroupDetail();
    consulteeGroupDetail1.setName("second consultee");
    consulteeGroupDetail1.setDisplayOrder(2);
    var consulteeGroupDetail2 = new ConsulteeGroupDetail();
    consulteeGroupDetail2.setName("third consultee");
    consulteeGroupDetail2.setDisplayOrder(3);
    var consulteeGroupDetail3 = new ConsulteeGroupDetail();
    consulteeGroupDetail3.setName("fourth consultee");
    consulteeGroupDetail3.setDisplayOrder(null);
    var consulteeGroupDetail4 = new ConsulteeGroupDetail();
    consulteeGroupDetail4.setName("first consultee");
    consulteeGroupDetail4.setDisplayOrder(1);

    when(consultationRequestService.getAllConsulteeGroups()).thenReturn(
        List.of(consulteeGroupDetail1, consulteeGroupDetail2, consulteeGroupDetail3, consulteeGroupDetail4));

    var modelAndView = consultationRequestController.renderRequestConsultation(1, PwaApplicationType.INITIAL, processingContext, user, null);

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
  public void postRequestConsultation_appStatusSmokeTest() {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .postRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postRequestConsultation_processingPermissionSmokeTest() {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationRequestController.class)
                .postRequestConsultation(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRequestConsultation() throws Exception {

    when(consultationRequestService.validate(any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationRequestForm(), "form"));

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
        .postRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
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

    var permissionsDto = new ProcessingPermissionsDto(null, EnumSet.allOf(PwaAppProcessingPermission.class));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationRequestController.class)
        .postRequestConsultation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("consulteeGroupSelection", "")
        .param("daysToRespond", "28")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("consultation/consultationRequest"));

  }

}
