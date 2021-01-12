package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Set;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineSchematicsErrorCode;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(
    controllers = TechnicalDrawingsController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class TechnicalDrawingsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private TechnicalDrawingSectionService technicalDrawingSectionService;

  @MockBean
  private AdmiraltyChartFileService admiraltyChartFileService;

  @MockBean
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @MockBean
  private UmbilicalCrossSectionService umbilicalCrossSectionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  private TechnicalDrawingsController technicalDrawingsController;

  @Before
  public void setup() {

    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(), any());

    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);

    technicalDrawingsController = new TechnicalDrawingsController(
        applicationBreadcrumbService,
        admiraltyChartFileService,
        pwaApplicationRedirectService,
        technicalDrawingSectionService,
        padTechnicalDrawingService,
        padFileService,
        umbilicalCrossSectionService);

  }

  @Test
  public void renderOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(TechnicalDrawingsController.class)
                .renderOverview(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(TechnicalDrawingsController.class)
                .renderOverview(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderOverview_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(TechnicalDrawingsController.class)
                .renderOverview(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void postHuooSummary_Invalid() throws Exception {

    when(pwaContactService.getContactRoles(any(), any())).thenReturn(Set.of(PwaContactRole.PREPARER));

    ControllerTestUtils.failValidationWhenPost(technicalDrawingSectionService, new SummaryForm(), ValidationType.FULL);

    mockMvc.perform(post(ReverseRouter.route(on(TechnicalDrawingsController.class)
        .postOverview(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null,
            null,
            null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk());

  }

  @Test
  public void postHuooSummary_Valid() throws Exception {

    when(pwaContactService.getContactRoles(any(), any())).thenReturn(Set.of(PwaContactRole.PREPARER));

    ControllerTestUtils.passValidationWhenPost(technicalDrawingSectionService, new SummaryForm(), ValidationType.FULL);

    mockMvc.perform(post(ReverseRouter.route(on(TechnicalDrawingsController.class)
        .postOverview(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null,
            null,
            null,
            null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void validateOverview_technicalDrawingError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var errorMessage = technicalDrawingsController.validateOverview(bindingResult);
    assertThat(errorMessage).isEqualTo("All pipelines must be linked to a drawing");
  }

  @Test
  public void validateOverview_admiraltyChartError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var errorMessage = technicalDrawingsController.validateOverview(bindingResult);
    assertThat(errorMessage).isEqualTo("An admiralty chart must be provided");
  }

  @Test
  public void validateOverview_technicalDrawingAndAdmiraltyChartError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode(),
        PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var errorMessage = technicalDrawingsController.validateOverview(bindingResult);
    assertThat(errorMessage).isEqualTo("An admiralty chart must be provided, and all pipelines must be linked to a drawing");
  }


}