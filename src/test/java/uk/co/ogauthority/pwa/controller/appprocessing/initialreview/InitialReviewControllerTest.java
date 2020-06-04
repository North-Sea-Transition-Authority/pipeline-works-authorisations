package uk.co.ogauthority.pwa.controller.appprocessing.initialreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.appprocessing.initialreview.InitialReviewFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = InitialReviewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class, PwaAppProcessingPermissionService.class}))
public class InitialReviewControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private int APP_ID = 1;

  @MockBean
  private WorkflowAssignmentService workflowAssignmentService;

  @MockBean
  private InitialReviewService initialReviewService;

  @MockBean
  private InitialReviewFormValidator validator;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, teamService, pwaApplicationDetailService)
        .setAllowedRegulatorRoles(PwaRegulatorRole.PWA_MANAGER)
        .setAllowedStatuses(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);

  }

  @Test
  public void renderInitialReview_roleSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .renderInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performRegulatorRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderInitialReview_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .renderInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postInitialReview_roleSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(InitialReviewController.class)
                .postInitialReview(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null)));

    endpointTester.performRegulatorRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postInitialReview() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(initialReviewService, times(1)).acceptApplication(pwaApplicationDetail, 5, user);

  }

  @Test
  public void postInitialReview_alreadyPerformed() throws Exception {

    var approvedTimestamp = Instant.now().minusSeconds(60);

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    pwaApplicationDetail.setInitialReviewApprovedByWuaId(1);
    pwaApplicationDetail.setInitialReviewApprovedTimestamp(approvedTimestamp);

    doCallRealMethod().when(initialReviewService).acceptApplication(pwaApplicationDetail, 5, user);

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    assertThat(pwaApplicationDetail.getInitialReviewApprovedTimestamp()).isEqualTo(approvedTimestamp);

  }

  @Test
  public void postInitialReview_validationFail() throws Exception {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    ControllerTestUtils.mockValidatorErrors(validator, List.of("caseOfficerPersonId"));

    mockMvc.perform(post(ReverseRouter.route(on(InitialReviewController.class).postInitialReview(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .param("caseOfficerPersonId", "5")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/appProcessing/initialReview/initialReview"));

  }

}
