package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineIdentsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineIdentsControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineIdentService pipelineIdentService;

  @MockBean
  private PipelineIdentFormValidator validator;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private int APP_ID = 1;

  private PadPipeline padPipeline;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService,
        padPipelineService)
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
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
    padPipeline.setId(99);
    when(padPipelineService.getById(padPipeline.getId())).thenReturn(padPipeline);

    var ident = new PadPipelineIdent();
    ident.setId(1);
    ident.setIdentNo(1);
    ident.setFromLocation("");
    ident.setLength(BigDecimal.ONE);
    ident.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(null, null, null, null),
        new LongitudeCoordinate(null, null, null, null)));
    ident.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(null, null, null, null),
        new LongitudeCoordinate(null, null, null, null)));

    var identData = new PadPipelineIdentData(ident);
    identData.setPadPipelineIdent(ident);
    identData.setComponentPartsDescription("");
    identData.setExternalDiameter(BigDecimal.ZERO);
    identData.setInternalDiameter(BigDecimal.ZERO);
    identData.setWallThickness(BigDecimal.ZERO);
    identData.setInsulationCoatingType("");
    identData.setMaop(BigDecimal.ZERO);
    identData.setProductsToBeConveyed("");
    var identView = new IdentView(identData);
    when(pipelineIdentService.getIdentView(any(), any())).thenReturn(identView);

  }

  @Test
  public void renderAddIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderAddIdent_previousIdentAvailable() throws Exception {

    var prevIdent = new PadPipelineIdent();
    prevIdent.setPadPipeline(padPipeline);
    prevIdent.setToLocation("prevTo");
    when(pipelineIdentService.getMaxIdent(padPipeline)).thenReturn(Optional.of(prevIdent));

    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                null,
                null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isEqualTo(prevIdent.getToLocation());

  }

  @Test
  public void renderAddIdent_noPreviousIdent() throws Exception {

    when(pipelineIdentService.getMaxIdent(padPipeline)).thenReturn(Optional.empty());

    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                null,
                null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isNull();

  }

  @Test
  public void postAddIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddIdent_validationFailed() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromLocation"));

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postAddIdent(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            null,
            null,
            null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
        .andExpect(model().attributeHasErrors("form"));

    verifyNoInteractions(pipelineIdentService);

  }

  @Test
  public void postAddIdent_valid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postAddIdent(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            null,
            null,
            null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).addIdent(eq(padPipeline), any());

  }

  @Test
  public void renderIdentOverview_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderIdentOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderIdentOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRemoveIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postRemoveIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRemoveIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postRemoveIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

}
