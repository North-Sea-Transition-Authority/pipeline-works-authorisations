package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.testutils.ApplicationSummaryViewTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail detail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  @Before
  public void setup() {
    doCallRealMethod().when(breadcrumbService).fromTaskList(any(), any(), any());

    endpointTester = new PwaApplicationEndpointTestBuilder(
        this.mockMvc,
        pwaContactService,
        pwaApplicationDetailService
    );

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);

    when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    when(applicationSummaryViewService.getApplicationSummaryView(any())).thenReturn(ApplicationSummaryViewTestUtils.getView());

  }

  private void setupReviewEndpointEndpointTester() {
    endpointTester.setAllowedTypes(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.HUOO_VARIATION
    )
        // all app roles should be able to review
        .setAllowedContactRoles(
            PwaContactRole.ACCESS_MANAGER,
            PwaContactRole.PREPARER,
            PwaContactRole.VIEWER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);
  }

  private void setupSubmitEndpointEndpointTester() {
    endpointTester.setAllowedTypes(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.HUOO_VARIATION
    )
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);
  }


  @Test
  public void review_roleChecks() {
    setupReviewEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void review_statusChecks() {
    setupReviewEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void review_typeChecks() {
    setupReviewEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  public void submit_roleChecks() {
    setupSubmitEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void submit_statusChecks() {
    setupSubmitEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  public void submit_typeChecks() {
    setupSubmitEndpointEndpointTester();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(ReviewAndSubmitController.class)
                .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void review_calculatesAppSummary() throws Exception {
    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(get(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().is2xxSuccessful())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation/"));

    verify(applicationSummaryViewService, times(1)).getApplicationSummaryView(detail);

  }


  @Test
  public void submit_doesSubmission_andRedirectsToConfirmation() throws Exception {
    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(post(ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"));

    verify(pwaApplicationSubmissionService, times(1)).submitApplication(user, detail);
  }

}
