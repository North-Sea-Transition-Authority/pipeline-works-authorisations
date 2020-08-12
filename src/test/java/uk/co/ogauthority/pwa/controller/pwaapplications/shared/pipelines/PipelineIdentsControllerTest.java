package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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
  private PadPipelineIdent ident;

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
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    padPipeline = getDefaultPadPipeline(99, pwaApplicationDetail);

    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline.setPipeline(pipeline);

    when(padPipelineService.getById(padPipeline.getId())).thenReturn(padPipeline);
    var padPipelineOverview = new PadPipelineOverview(padPipeline, 0L);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    ident = new PadPipelineIdent();
    ident.setId(1);
    ident.setIdentNo(1);
    ident.setFromLocation("from");
    ident.setToLocation("to");
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
    when(pipelineIdentService.getIdent(any(), any())).thenReturn(ident);

    when(pipelineIdentService.isSectionValid(any())).thenReturn(true);

  }

  @Test
  public void renderAddIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderAddIdent_previousIdentAvailable() throws Exception {

    var prevIdent = new PadPipelineIdent();
    prevIdent.setPadPipeline(padPipeline);
    prevIdent.setToLocation("prevTo");
    prevIdent.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    ));
    when(pipelineIdentService.getMaxIdent(padPipeline)).thenReturn(Optional.of(prevIdent));


    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                null,
                null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isEqualTo(prevIdent.getToLocation());

    var fromCoordinateForm = identForm.getFromCoordinateForm();
    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(fromCoordinateForm.getLatitudeDegrees(), fromCoordinateForm.getLatitudeMinutes(),
            fromCoordinateForm.getLatitudeSeconds(), fromCoordinateForm.getLatitudeDirection()),
        new LongitudeCoordinate(fromCoordinateForm.getLongitudeDegrees(), fromCoordinateForm.getLongitudeMinutes(),
            fromCoordinateForm.getLongitudeSeconds(), fromCoordinateForm.getLongitudeDirection())
    );
    assertThat(fromCoordinatePair).isEqualToComparingFieldByField(prevIdent.getToCoordinates());

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
                null, null)))
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
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

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
            null, null)))
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
            null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).addIdent(eq(padPipeline), any());

  }

  @Test
  public void renderIdentOverview_contactSmokeTest() {

    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderIdentOverview_appTypeSmokeTest() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderIdentOverview_appStatusSmokeTest() {

    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRemoveIdent_contactSmokeTest() {


    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postRemoveIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRemoveIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postRemoveIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postRemoveIdent_verifyRemove() throws Exception {

    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postRemoveIdent(APP_ID, PwaApplicationType.INITIAL, padPipeline.getId(), null, 1, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).removeIdent(ident);

  }

  private PadPipeline getDefaultPadPipeline(int id, PwaApplicationDetail pwaApplicationDetail) {
    var padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
    padPipeline.setId(id);
    padPipeline.setPipelineRef("testref");
    padPipeline.setFromLocation("from");
    padPipeline.setToLocation("to");

    padPipeline.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    ));

    padPipeline.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(2, 2, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(2, 2, BigDecimal.ONE, LongitudeDirection.EAST)
    ));

    padPipeline.setLength(BigDecimal.TEN);
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setComponentPartsDescription("comp parts");
    padPipeline.setProductsToBeConveyed("prods");
    return padPipeline;
  }

  @Test
  public void postIdentOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postIdentOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postIdentOverview_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postIdentOverview_failValidation() {

    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);
    when(pipelineIdentService.isSectionValid(any())).thenReturn(false);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postEditIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postEditIdent_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditIdent_failValidation() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      errors.reject("fake", "error");
      return errors;
    }).when(validator).validate(any(), any(), any());

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderInsertIdentAbove_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderInsertIdentAbove_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderInsertIdentAbove_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderInsertIdentAbove_previousIdentAvailable() throws Exception {

    var prevIdent = new PadPipelineIdent();
    prevIdent.setPadPipeline(padPipeline);
    prevIdent.setToLocation("prevTo");
    prevIdent.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    ));
    when(pipelineIdentService.getIdentByIdentNumber(padPipeline, ident.getIdentNo() - 1)).thenReturn(
        Optional.of(prevIdent));


    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderInsertIdentAbove(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                1,
                null,
                null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isEqualTo(prevIdent.getToLocation());

    var fromCoordinateForm = identForm.getFromCoordinateForm();
    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(fromCoordinateForm.getLatitudeDegrees(), fromCoordinateForm.getLatitudeMinutes(),
            fromCoordinateForm.getLatitudeSeconds(), fromCoordinateForm.getLatitudeDirection()),
        new LongitudeCoordinate(fromCoordinateForm.getLongitudeDegrees(), fromCoordinateForm.getLongitudeMinutes(),
            fromCoordinateForm.getLongitudeSeconds(), fromCoordinateForm.getLongitudeDirection())
    );
    assertThat(fromCoordinatePair).isEqualToComparingFieldByField(prevIdent.getToCoordinates());

  }

  @Test
  public void renderInsertIdentAbove_noPreviousIdent() throws Exception {

    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderInsertIdentAbove(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                1,
                null,
                null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isNull();
    assertThat(identForm.getToLocation()).isNull();

    assertThat(identForm.getFromCoordinateForm()).hasAllNullFieldsOrPropertiesExcept("latitudeDirection");
    assertThat(identForm.getFromCoordinateForm().getLatitudeDirection()).isEqualTo(LatitudeDirection.NORTH);

    assertThat(identForm.getToCoordinateForm()).hasAllNullFieldsOrPropertiesExcept("latitudeDirection");
    assertThat(identForm.getToCoordinateForm().getLatitudeDirection()).isEqualTo(LatitudeDirection.NORTH);

  }

  @Test
  public void postInsertIdentAbove_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postInsertIdentAbove_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postInsertIdentAbove_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postInsertIdentAbove_validationFailed() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromLocation"));

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postInsertIdentAbove(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            1,
            null,
            null,
            null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
        .andExpect(model().attributeHasErrors("form"));

    verify(pipelineIdentService, never()).addIdentAtPosition(any(), any(), any());

  }

  @Test
  public void postInsertIdentAbove_valid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postInsertIdentAbove(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            1,
            null,
            null,
            null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).addIdentAtPosition(eq(padPipeline), any(), eq(1));

  }

}
