package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.core.StringContains;
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
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationUserRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CreatePaymentAttemptResult;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CreatePaymentAttemptResultTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = IndustryPaymentController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class IndustryPaymentControllerTest extends PwaAppProcessingContextAbstractControllerTest {
  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 30;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.HUOO_VARIATION;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationChargeRequestService applicationChargeRequestService;

  @MockBean
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @MockBean
  private PwaHolderService pwaHolderService;;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private ApplicationChargeRequestReport applicationChargeRequestReport;
  private ApplicationPaymentDisplaySummary applicationPaymentDisplaySummary;
  private CreatePaymentAttemptResult attemptSuccessResult;

  private PortalOrganisationGroup orgGroup1, orgGroup2;

  @Before
  public void setUp() throws Exception {
    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    orgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "GROUP1", "1");
    orgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "GROUP2", "2");
    when(pwaHolderService.getPwaHolderOrgGroups(any(MasterPwa.class))).thenReturn(Set.of(orgGroup1, orgGroup2));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createOpenReport(
        100,
        "Summary",
        List.of(ApplicationChargeRequestReportTestUtil.createApplicationChargeItem("Item 1", 100))
    );
    applicationPaymentDisplaySummary = ApplicationPaymentDisplaySummaryTestUtil.getDefaultPaymentDisplaySummary();

    when(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));
    when(applicationPaymentSummariser.summarise(any()))
        .thenReturn(applicationPaymentDisplaySummary);


    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    attemptSuccessResult = CreatePaymentAttemptResultTestUtil.createSuccess();
    when(applicationChargeRequestService.startChargeRequestPaymentAttempt(any(), any()))
        .thenReturn(attemptSuccessResult);

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.PAY_FOR_APPLICATION)
        .setAllowedStatuses(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

  }

  @Test
  public void renderPayForApplicationLanding_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(IndustryPaymentController.class)
                .renderPayForApplicationLanding(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void renderPayForApplicationLanding_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(IndustryPaymentController.class)
                .renderPayForApplicationLanding(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPayForApplicationLanding_whenNoOpenChargeRequest() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.PAY_FOR_APPLICATION));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    when(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(APP_ID, APP_TYPE, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isNotFound());

  }

  @Test
  public void renderPayForApplicationLanding_whenOpenChargeRequest() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.PAY_FOR_APPLICATION));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(APP_ID, APP_TYPE, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
        // testing precise URL values not done. Controller reverse router has access to the annotation driven app type converter (@ApplicationTypeUrl),
        // the test does not use the url form of th app type so a mismatch in actual -> expected urls.
        .andExpect(model().attributeExists("cancelUrl"))
        .andExpect(model().attributeExists("paymentLandingPageUrl"))
        .andExpect(model().attribute("financeRoleName",PwaOrganisationUserRole.FINANCE_ADMIN.getRoleName()))
        .andExpect(model().attribute("appPaymentDisplaySummary",applicationPaymentDisplaySummary))
        .andExpect(model().attribute("pwaHolderOrgNames", List.of(orgGroup1.getName(), orgGroup2.getName())));

  }

  @Test
  public void startPaymentAttempt_appStatusSmokeTest() {
    // if app payment received whilst waiting on the page, clicking Pay should redirect you nicely even though the permission has timed out.
    endpointTester
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.PAY_FOR_APPLICATION, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(IndustryPaymentController.class)
                .startPaymentAttempt(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  public void startPaymentAttempt_processingPermissionSmokeTest() {
    // if app payment received whilst waiting on the page, clicking Pay should redirect you nicely even though the permission has timed out.
    endpointTester
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.PAY_FOR_APPLICATION, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);;

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(IndustryPaymentController.class)
                .startPaymentAttempt(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void startPaymentAttempt_whenPaymentCreated() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.PAY_FOR_APPLICATION));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(IndustryPaymentController.class).startPaymentAttempt(APP_ID, APP_TYPE, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + attemptSuccessResult.getStartExternalJourneyUrl()));

    verify(applicationChargeRequestService).startChargeRequestPaymentAttempt(pwaApplicationDetail.getPwaApplication(), user);

  }

  @Test
  public void startPaymentAttempt_whenChargeAlreadyPaid() throws Exception {
    var permissionsDto = new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.PAY_FOR_APPLICATION));
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);

    var alreadyPaidResult = CreatePaymentAttemptResultTestUtil.createCompletedPaymentExists();
    when(applicationChargeRequestService.startChargeRequestPaymentAttempt(any(), any()))
        .thenReturn(alreadyPaidResult);

    mockMvc.perform(post(ReverseRouter.route(on(IndustryPaymentController.class).startPaymentAttempt(APP_ID, APP_TYPE, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name(new StringContains(true, "case-management")));

    verify(applicationChargeRequestService).startChargeRequestPaymentAttempt(pwaApplicationDetail.getPwaApplication(), user);

  }

}