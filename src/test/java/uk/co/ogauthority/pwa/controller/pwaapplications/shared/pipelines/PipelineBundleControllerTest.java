package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleSummaryView;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelines.AddBundleValidator;
import uk.co.ogauthority.pwa.validators.pipelines.EditBundleValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineBundleController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineBundleControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadBundleService padBundleService;

  @MockBean
  private AddBundleValidator addBundleValidator;

  @MockBean
  private EditBundleValidator editBundleValidator;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private int APP_ID = 1;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    var bundle = new PadBundle();
    bundle.setBundleName("bundle");
    when(padBundleService.getBundleSummaryView(any(), any())).thenReturn(new PadBundleSummaryView(bundle, List.of()));

    when(padBundleService.getBundle(any(), any())).thenReturn(bundle);
  }

  @Test
  public void renderAddBundle_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddBundle_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddBundle_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddBundle_contactSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddBundle_appTypeSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddBundle_appStatusSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddBundle_failedValidation() {

    ControllerTestUtils.mockSmartValidatorErrors(addBundleValidator, List.of("bundleName"));

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postAddBundle(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderEditBundle_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditBundle_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditBundle_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postEditBundle_contactSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditBundle_appTypeSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditBundle_appStatusSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postEditBundle_failedValidation() {

    ControllerTestUtils.mockSmartValidatorErrors(editBundleValidator, List.of("bundleName"));

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postEditBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRemoveBundle_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveBundle_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveBundle_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .renderRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postRemoveBundle_contactSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRemoveBundle_appTypeSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRemoveBundle_appStatusSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineBundleController.class)
                .postRemoveBundle(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

}
