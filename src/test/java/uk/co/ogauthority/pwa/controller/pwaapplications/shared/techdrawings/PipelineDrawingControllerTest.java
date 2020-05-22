package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.techdrawings.PipelineDrawingValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(
    controllers = PipelineDrawingController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class PipelineDrawingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;
  private PwaApplicationEndpointTestBuilder endpointTester;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @MockBean
  private PipelineDrawingValidator pipelineDrawingValidator;

  @MockBean
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(), any());

    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION)
        .setAllowedRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));
  }

  @Test
  public void renderAddDrawing_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .renderAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddDrawing_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .renderAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderAddDrawing_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .renderAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void postAddDrawing_appTypeSmokeTest() {

    var form = new PipelineDrawingForm();
    ControllerTestUtils.passValidationWhenPost(padTechnicalDrawingService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .postAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddDrawing_appStatusSmokeTest() {

    var form = new PipelineDrawingForm();
    ControllerTestUtils.passValidationWhenPost(padTechnicalDrawingService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .postAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddDrawing_contactRoleSmokeTest() {

    var form = new PipelineDrawingForm();
    ControllerTestUtils.passValidationWhenPost(padTechnicalDrawingService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .postAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddDrawing_failValidation() {

    var form = new PipelineDrawingForm();
    ControllerTestUtils.failValidationWhenPost(padTechnicalDrawingService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineDrawingController.class)
                .postAddDrawing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padTechnicalDrawingService, never()).addDrawing(any(), any());

  }

  @Test
  public void postAddDrawing_passValidation() throws Exception {

    var form = new PipelineDrawingForm();
    ControllerTestUtils.passValidationWhenPost(padTechnicalDrawingService, form, ValidationType.FULL);

    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    mockMvc.perform(
        post(ReverseRouter.route(on(PipelineDrawingController.class)
            .postAddDrawing(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padTechnicalDrawingService, times(1)).addDrawing(any(), any());

  }
}