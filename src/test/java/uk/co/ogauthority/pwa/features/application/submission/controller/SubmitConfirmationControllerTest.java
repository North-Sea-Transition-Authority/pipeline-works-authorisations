package uk.co.ogauthority.pwa.features.application.submission.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpSession;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.submission.ApplicationSubmissionSummary;
import uk.co.ogauthority.pwa.features.application.submission.ApplicationSummaryFactory;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = SubmitConfirmationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class SubmitConfirmationControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 99;

  @MockBean
  private ApplicationSummaryFactory applicationSummaryFactory;

  private PwaApplicationEndpointTestBuilder editEndpointTester;
  private PwaApplicationEndpointTestBuilder submitEndpointTester;

  private PwaApplicationDetail detail;
  private ApplicationSubmissionSummary applicationSubmissionSummary;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  private MockHttpSession session;

  @BeforeEach
  void setup() {

    editEndpointTester = new PwaApplicationEndpointTestBuilder(
        this.mockMvc,
        pwaApplicationPermissionService,
        pwaApplicationDetailService
    ).setAllowedTypes(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.HUOO_VARIATION)
    .setAllowedPermissions(PwaApplicationPermission.EDIT)
    .setAllowedStatuses(EnumSet.allOf(PwaApplicationStatus.class).toArray(PwaApplicationStatus[]::new));

    submitEndpointTester = new PwaApplicationEndpointTestBuilder(
        this.mockMvc,
        pwaApplicationPermissionService,
        pwaApplicationDetailService
    ).setAllowedTypes(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.HUOO_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.SUBMIT)
        .setAllowedStatuses(EnumSet.allOf(PwaApplicationStatus.class).toArray(PwaApplicationStatus[]::new));

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    applicationSubmissionSummary = new ApplicationSubmissionSummary(
        detail.getPwaApplication(),
        detail.isFirstVersion(),
        LocalDateTime.now(),
        "Some User"
    );

    when(pwaApplicationPermissionService.getPermissions(detail, user)).thenReturn(
        EnumSet.allOf(PwaApplicationPermission.class));

    when(applicationSummaryFactory.createSubmissionSummary(any())).thenReturn(applicationSubmissionSummary);

    session = new MockHttpSession();
    session.setAttribute("submitterPersonName", "name");

  }

  @Test
  void confirmSubmission_permissionChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSubmission(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void confirmSubmission_statusChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSubmission(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void confirmSubmission_typeChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSubmission(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void confirmSubmission_hasSubmissionSummaryObject() throws Exception {
    when(pwaApplicationDetailService.getTipDetailByAppId(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(get(ReverseRouter.route(on(SubmitConfirmationController.class)
            .confirmSubmission(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
            .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"))
    .andExpect(model().attribute("submissionSummary", applicationSubmissionSummary));

    verify(applicationSummaryFactory, times(1)).createSubmissionSummary(detail);
  }

  @Test
  void confirmSentToSubmitter_permissionChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSentToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .setSession(session)
        .performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void confirmSentToSubmitter_statusChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSentToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .setSession(session)
        .performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void confirmSentToSubmitter_typeChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmSentToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .setSession(session)
        .performAppTypeChecks(status().isOk(), status().isForbidden());

  }

}
