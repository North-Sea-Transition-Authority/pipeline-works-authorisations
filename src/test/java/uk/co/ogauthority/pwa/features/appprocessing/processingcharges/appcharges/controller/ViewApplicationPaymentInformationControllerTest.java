package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummary;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.SimplePersonView;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.SimplePersonViewTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@WebMvcTest(controllers = ViewApplicationPaymentInformationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ViewApplicationPaymentInformationControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 30;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.OPTIONS_VARIATION;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ApplicationChargeRequestService applicationChargeRequestService;

  @MockBean
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @MockBean
  private PersonService personService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private ApplicationChargeRequestReport applicationChargeRequestReport;
  private ApplicationPaymentDisplaySummary applicationPaymentDisplaySummary;

  private PersonId requestedByPersonId, paidByPersonId;

  private Instant requestedInstant, paidInstant;

  private SimplePersonView personView;

  @BeforeEach
  void setUp() throws Exception {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);

    requestedByPersonId = new PersonId(1);
    paidByPersonId = new PersonId(2);

    personView = SimplePersonViewTestUtil.createView(paidByPersonId);
    when(personService.getSimplePersonView(any())).thenReturn(personView);

    requestedInstant = LocalDateTime.of(2021, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
    paidInstant = LocalDateTime.of(2021, 2, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createPaidReport(
        100,
        "Summary",
        requestedInstant,
        requestedByPersonId,
        paidInstant,
        paidByPersonId
    );

    applicationPaymentDisplaySummary = ApplicationPaymentDisplaySummaryTestUtil.getDefaultPaymentDisplaySummary();

    when(applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));
    when(applicationPaymentSummariser.summarise(any())).thenReturn(applicationPaymentDisplaySummary);

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService,
        pwaAppProcessingPermissionService)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS);

    // mocks to support non endpointTester tests
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(permissionsDto);


  }

  @Test
  void renderPaymentInformation_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ViewApplicationPaymentInformationController.class)
                .renderPaymentInformation(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderPaymentInformation_noChargeRequestReportFound() throws Exception {
    when(applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(ViewApplicationPaymentInformationController.class)
        .renderPaymentInformation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(user(user))
    )
        .andExpect(status().isNotFound());

  }

  @Test
  void renderPaymentInformation_LatestChargeRequestReportNotPaid() throws Exception {

    applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createOpenReport(
        100,
        "OPEN REPORT",
        List.of()
    );
    when(applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(any()))
        .thenReturn(Optional.of(applicationChargeRequestReport));

    mockMvc.perform(get(ReverseRouter.route(on(ViewApplicationPaymentInformationController.class)
        .renderPaymentInformation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(user(user))
    )
        .andExpect(status().isNotFound());

  }

  @Test
  void renderPaymentInformation_LatestChargeRequestReportPaid() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ViewApplicationPaymentInformationController.class)
        .renderPaymentInformation(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("caseSummaryView"))
        .andExpect(model().attributeExists("appRef"))
        .andExpect(model().attributeExists("appPaymentDisplaySummary"))
        .andExpect(model().attributeExists("pageRef"))
        .andExpect(model().attribute("paidByName", personView.getName()))
        .andExpect(model().attribute("paidByEmail", personView.getEmail()))
        .andExpect(model().attribute("paidInstant", DateUtils.formatDateTime(paidInstant)))
        .andExpect(model().attribute("requestedInstant", DateUtils.formatDateTime(requestedInstant)))
        .andExpect(model().attribute("paymentStatus", applicationChargeRequestReport.getPwaAppChargeRequestStatus().getDispayString()));

    verify(personService).getSimplePersonView(paidByPersonId);

  }
}