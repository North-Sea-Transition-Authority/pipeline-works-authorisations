package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.testutils.ApplicationSummaryViewTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission.ReviewAndSubmitApplicationFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ReviewAndSubmitController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class ReviewAndSubmitControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 99;

  // mocked redirect service gets injected into spyBean, not mockBean
  @SpyBean
  private ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  private PwaApplicationSubmissionService pwaApplicationSubmissionService;

  @MockBean
  private ApplicationSummaryViewService applicationSummaryViewService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @MockBean
  private ReviewAndSubmitApplicationFormValidator validator;

  @MockBean
  private SendAppToSubmitterService sendAppToSubmitterService;

  @MockBean
  private PersonService personService;

  private PwaApplicationEndpointTestBuilder editEndpointTester;
  private PwaApplicationEndpointTestBuilder viewEndpointTester;
  private PwaApplicationEndpointTestBuilder submitEndpointTester;

  private PwaApplicationDetail detail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  private MockHttpSession session;

  @Before
  public void setup() {
    doCallRealMethod().when(breadcrumbService).fromTaskList(any(), any(), any());

    editEndpointTester = getEndpointTesterWithPermissions(PwaApplicationPermission.EDIT);
    viewEndpointTester = getEndpointTesterWithPermissions(PwaApplicationPermission.VIEW);
    submitEndpointTester = getEndpointTesterWithPermissions(PwaApplicationPermission.SUBMIT);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);

    when(pwaApplicationPermissionService.getPermissions(eq(detail), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(applicationSummaryViewService.getApplicationSummaryView(any())).thenReturn(ApplicationSummaryViewTestUtils.getView());
    when(validator.supports(any())).thenReturn(true);

    session = new MockHttpSession();

    when(personService.getPersonById(user.getLinkedPerson().getId())).thenReturn(user.getLinkedPerson());

  }

  @Test
  public void review_roleChecks() {

    viewEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void review_statusChecks() {

    viewEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void review_typeChecks() {

    viewEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null))
        )
        .performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  public void submit_roleChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null))
        )
        .performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void submit_statusChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null))
        )
        .performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void submit_typeChecks() {

    submitEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null))
        )
        .performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void submit_noValidationErrors_redirectsToConfirmation() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"));

    verify(pwaApplicationSubmissionService, times(1)).submitApplication(user, detail, null);

  }


  @Test
  public void submit_failsValidation_backToReviewScreen() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("madeOnlyRequestedChanges", "madeOnlyRequestedChanges.required", "message");
      return invocation;
    }).when(validator).validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/submission/reviewAndSubmit"));

    verifyNoInteractions(pwaApplicationSubmissionService);

  }

  @Test
  public void submit_hasOpenUpdate_validationPass_formDataPassedToSubmissionService() throws Exception {

    var description = "OTHER DESC";

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null))
        ).param("madeOnlyRequestedChanges", "false")
        .param("otherChangesDescription", description)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"));

    verify(pwaApplicationSubmissionService, times(1)).submitApplication(user, detail, description);
  }

  @Test
  public void submit_hasOpenUpdate_validationPass_doesNotPassDescriptionWhenHidden() throws Exception {

    var description = "OTHER DESC";

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null))
        )
        .param("madeOnlyRequestedChanges", "true")
        .param("otherChangesDescription", description)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"));

    // null passed, not description
    verify(pwaApplicationSubmissionService, times(1)).submitApplication(user, detail, null);
  }

  @Test
  public void sendToSubmitter_roleChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .addRequestParam("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt()))
        .performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void sendToSubmitter_statusChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .addRequestParam("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt()))
        .performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void sendToSubmitter_typeChecks() {

    editEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .addRequestParam("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt()))
        .performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void sendToSubmitter_noValidationErrors_redirectsToSendToSubmitterConfirmation_noUpdateTextProvided() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
        .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt())))
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/sentToSubmitter"));

    verify(sendAppToSubmitterService, times(1)).sendToSubmitter(detail, user.getLinkedPerson(), null, user.getLinkedPerson());

  }

  @Test
  public void sendToSubmitter_noValidationErrors_redirectsToSendToSubmitterConfirmation_updateTextProvided() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
        .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt()))
        .param("madeOnlyRequestedChanges", "false")
        .param("otherChangesDescription", "desc"))
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/sentToSubmitter"));

    verify(sendAppToSubmitterService, times(1)).sendToSubmitter(detail, user.getLinkedPerson(), "desc", user.getLinkedPerson());

  }

  @Test
  public void sendToSubmitter_failsValidation_backToReviewScreen() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("madeOnlyRequestedChanges", "madeOnlyRequestedChanges.required", "message");
      return invocation;
    }).when(validator).validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
        .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("submitterPersonId", String.valueOf(user.getLinkedPerson().getId().asInt())))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/submission/reviewAndSubmit"));

    verifyNoInteractions(sendAppToSubmitterService);

  }

  private PwaApplicationEndpointTestBuilder getEndpointTesterWithPermissions(PwaApplicationPermission... permissions) {

    return new PwaApplicationEndpointTestBuilder(this.mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.OPTIONS_VARIATION,
            PwaApplicationType.HUOO_VARIATION)
        .setAllowedPermissions(permissions)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

}
