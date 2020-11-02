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
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationResponseController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ConsultationResponseControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ConsultationResponseService consultationResponseService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConsultationViewService consultationViewService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private AuthenticatedUserAccount user;
  private PwaApplicationDetail pwaApplicationDetail;
  private ConsultationRequest consultationRequest;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.of(PwaUserPrivilege.PWA_CONSULTEE));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    when(pwaApplicationDetailService.getLastSubmittedApplicationDetail(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(pwaApplicationDetail));

    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
    when(consultationRequestService.getConsultationRequestById(any())).thenReturn(consultationRequest);
    when(consultationResponseService.isUserAssignedResponderForConsultation(any(), any())).thenReturn(true);
    when(consultationRequestService.getActiveConsultationRequestByApplicationAndConsulteePerson(any(), any()))
        .thenReturn(Optional.of(new ConsultationRequestDto("nme", consultationRequest)));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setUserPrivileges(PwaUserPrivilege.PWA_CONSULTEE)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CONSULTATION_RESPONDER);

  }

  @Test
  public void renderResponder_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationResponseController.class)
                .renderResponder(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void postResponder_permissionSmokeTest() {

    when(consultationResponseService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationResponseForm(), "form"));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsultationResponseController.class)
                .postResponder(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postResponder() throws Exception {

    when(consultationResponseService.validate(any(), any())).thenReturn(new BeanPropertyBindingResult(new ConsultationResponseForm(), "form"));

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
        .postResponder(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("consultationResponseOption", "")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consultationResponseService, times(1)).saveResponseAndCompleteWorkflow(any(), eq(consultationRequest), eq(user));

  }

  @Test
  public void postResponder_validationFail() throws Exception {

    var failedBindingResult = new BeanPropertyBindingResult(new ConsultationResponseForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(consultationResponseService.validate(any(), any())).thenReturn(failedBindingResult);

    when(pwaAppProcessingPermissionService.getProcessingPermissions(pwaApplicationDetail.getPwaApplication(), user)).thenReturn(EnumSet.allOf(PwaAppProcessingPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(ConsultationResponseController.class)
        .postResponder(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("consultationResponseOption", "")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("consultation/responses/responderForm"));

  }

}
