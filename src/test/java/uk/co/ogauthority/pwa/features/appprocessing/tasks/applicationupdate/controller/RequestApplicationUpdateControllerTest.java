package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.controller;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestForm;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestValidator;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@WebMvcTest(controllers = RequestApplicationUpdateController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class RequestApplicationUpdateControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final String REQUEST_REASON_ATTR = "requestReason";
  private static final String REQUEST_REASON_VALID = "requestReason";

  private static final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private static final int APP_ID = 10;
  private static final int APP_DETAIL_ID = 11;
  private static final String APP_REF = "APP_REF";

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private WorkflowAssignmentService workflowAssignmentService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @MockBean
  private ApplicationUpdateRequestValidator applicationUpdateRequestValidator;

  private PwaApplicationDetail pwaApplicationDetail;
  private Person person;
  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    person = new Person(100, "test", "person", "email", "telephone");
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, person),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaApplicationDetail.getPwaApplication().setAppReference(APP_REF);

    when(pwaApplicationDetailService.getLatestDetailForUser(1, user)).thenReturn(Optional.of(pwaApplicationDetail));
    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user)).thenReturn(Optional.of(pwaApplicationDetail));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService,
        pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);

  }

  @Test
  void renderRequestUpdate_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(RequestApplicationUpdateController.class)
                .renderRequestUpdate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderRequestUpdate_modelHasExpectedAttributes() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    var result = mockMvc.perform(
        get(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .renderRequestUpdate(APP_ID, APP_TYPE, null, null, null)
        )).with(user(user))

    ).andExpect(status().is2xxSuccessful())
        .andReturn();

    var model = result.getModelAndView().getModel();
    assertThat(model).contains(entry("appRef", APP_REF));
    assertThat(model).contains(entry("form", new ApplicationUpdateRequestForm()));
    assertThat(model).contains(entry("errorList", List.of()));

    verify(applicationUpdateRequestService, times(1)).applicationHasOpenUpdateRequest(pwaApplicationDetail);
    verify(applicationUpdateRequestService, times(0)).submitApplicationUpdateRequest(any(), any(), any());
  }

  @Test
  void renderRequestUpdate_whenApplicationHasOpenUpdateRequest() throws Exception {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).thenReturn(true);

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    var result = mockMvc.perform(
        get(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .renderRequestUpdate(APP_ID, APP_TYPE, null, null, null)
        )).with(user(user))

    ).andExpect(status().isForbidden())
        .andReturn();

  }

  @Test
  void requestUpdate_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(REQUEST_REASON_ATTR, REQUEST_REASON_VALID)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(RequestApplicationUpdateController.class)
                .requestUpdate(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, Optional.empty())));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void requestUpdate_validationFailsWithNullRequestReason() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    ControllerTestUtils.mockValidatorErrors(applicationUpdateRequestValidator, List.of(REQUEST_REASON_ATTR));

    mockMvc.perform(
        post(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .requestUpdate(APP_ID, APP_TYPE, null, null, null, null, Optional.empty())
        ))
            .with(user(user))
            .with(csrf())
            .param(REQUEST_REASON_ATTR, "")

    ).andExpect(status().is2xxSuccessful())
        .andExpect(model().hasErrors())
        .andReturn();

    verify(applicationUpdateRequestService, times(1)).applicationHasOpenUpdateRequest(pwaApplicationDetail);
    verify(applicationUpdateRequestService, times(0)).submitApplicationUpdateRequest(any(), any(), any());

  }

  @Test
  void requestUpdate_validationFailsWithTooLongRequestReason() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    ControllerTestUtils.mockValidatorErrors(applicationUpdateRequestValidator, List.of(REQUEST_REASON_ATTR));

    mockMvc.perform(
        post(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .requestUpdate(APP_ID, APP_TYPE, null, null, null, null, Optional.empty())
        ))
            .with(user(user))
            .with(csrf())
            .param(REQUEST_REASON_ATTR, ValidatorTestUtils.overMaxDefaultCharLength())

    ).andExpect(status().is2xxSuccessful())
        .andExpect(model().hasErrors())
        .andReturn();

    verify(applicationUpdateRequestService, times(1)).applicationHasOpenUpdateRequest(pwaApplicationDetail);
    verify(applicationUpdateRequestService, times(0)).submitApplicationUpdateRequest(any(), any(), any());

  }

  @Test
  void requestUpdate_redirectsWhenFormValid() throws Exception {

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);
    var form = new ApplicationUpdateRequestForm();
    form.setRequestReason(REQUEST_REASON_VALID);
    form.setDeadlineTimestampStr("01/01/2021");

    mockMvc.perform(
        post(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .requestUpdate(APP_ID, APP_TYPE, null, null, form, null, Optional.empty())
        ))
            .with(user(user))
            .with(csrf())
            .param(REQUEST_REASON_ATTR, REQUEST_REASON_VALID)
            .param("deadlineTimestampStr", form.getDeadlineTimestampStr())

    ).andExpect(status().is3xxRedirection())
        .andExpect(model().attributeHasNoErrors())
        .andReturn();
    verify(applicationUpdateRequestService, times(1)).applicationHasOpenUpdateRequest(pwaApplicationDetail);
    verify(applicationUpdateRequestService, times(1)).submitApplicationUpdateRequest(
        pwaApplicationDetail, user, form);
    verify(analyticsService, times(1)).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.UPDATE_REQUEST_SENT));

  }

  @Test
  void requestUpdate_whenApplicationHasOpenUpdateRequest() throws Exception {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).thenReturn(true);

    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    mockMvc.perform(
        post(ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .requestUpdate(APP_ID, APP_TYPE, null, null, null, null, Optional.empty())
        ))
            .with(user(user))
            .with(csrf())
            .param(REQUEST_REASON_ATTR, REQUEST_REASON_VALID)

    ).andExpect(status().isForbidden());

  }
}