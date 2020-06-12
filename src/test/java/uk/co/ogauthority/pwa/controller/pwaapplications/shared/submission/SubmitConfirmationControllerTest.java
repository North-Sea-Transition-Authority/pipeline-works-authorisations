package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.LocalDateTime;
import java.util.EnumSet;
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
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.summary.ApplicationSubmissionSummary;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.summary.ApplicationSummaryFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubmitConfirmationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class SubmitConfirmationControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 99;

  @MockBean
  private ApplicationSummaryFactory applicationSummaryFactory;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail detail;
  private ApplicationSubmissionSummary applicationSubmissionSummary;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  @Before
  public void setup() {

    endpointTester = new PwaApplicationEndpointTestBuilder(
        this.mockMvc,
        pwaContactService,
        pwaApplicationDetailService
    ).setAllowedTypes(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.HUOO_VARIATION
    )
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    applicationSubmissionSummary = new ApplicationSubmissionSummary(
        detail.getPwaApplication(),
        detail.isFirstVersion(),
        LocalDateTime.now(),
        "Some User"
    );

    when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    when(applicationSummaryFactory.createSubmissionSummary(any())).thenReturn(applicationSubmissionSummary);
  }


  @Test
  public void confirmation_roleChecks() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmation(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void confirmation_statusChecks() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmation(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  public void confirmation_typeChecks() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((detail, appType) ->
            ReverseRouter.route(on(SubmitConfirmationController.class)
                .confirmation(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
        .performAppTypeChecks(status().isOk(), status().isForbidden());
  }


  @Test
  public void confirmation_hasSubmissionSummaryObject() throws Exception {
    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    mockMvc.perform(get(ReverseRouter.route(on(SubmitConfirmationController.class)
            .confirmation(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null))
        )
            .with(authenticatedUserAndSession(user))
    )
        .andExpect(status().isOk())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/submission/submitConfirmation"))
    .andExpect(model().attribute("submissionSummary", applicationSubmissionSummary));

    verify(applicationSummaryFactory, times(1)).createSubmissionSummary(detail);
  }

}
