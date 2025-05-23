package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.controller;

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
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = PipelineIdentsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PipelineIdentsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final Set<PipelineStatus> allowedPipelineStatuses = Set.of(
      PipelineStatus.IN_SERVICE, PipelineStatus.OUT_OF_USE_ON_SEABED
  );
  private static final Set<PipelineStatus> disallowedPipelineStatuses = Set.of(
      PipelineStatus.RETURNED_TO_SHORE, PipelineStatus.NEVER_LAID
  );

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineIdentService pipelineIdentService;

  @MockBean
  private PipelineIdentFormValidator validator;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;

  private PadPipeline padPipeline;
  private PadPipelineIdent ident;

  @BeforeEach
  void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService,
        padPipelineService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    padPipeline = getDefaultPadPipeline(99, pwaApplicationDetail);

    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

    when(padPipelineService.getById(padPipeline.getId())).thenReturn(padPipeline);
    var padPipelineOverview = new PadPipelineOverview(padPipeline, 0L);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    ident = new PadPipelineIdent();
    ident.setPadPipeline(padPipeline);
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
    identData.setComponentPartsDesc("");
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

    when(pipelineIdentService.getSummaryScreenValidationResult(any()))
        .thenReturn(new SummaryScreenValidationResult(Map.of(), "", "", true, ""));

  }

  @Test
  void renderAddIdent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderAddIdent_previousIdentAvailable() throws Exception {

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
            .with(user(user)))
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
  void renderAddIdent_noPreviousIdent() throws Exception {

    when(pipelineIdentService.getMaxIdent(padPipeline)).thenReturn(Optional.empty());

    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                null,
                null, null)))
            .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
            .andReturn()
            .getModelAndView())
        .getModel()
        .get("form");

    assertThat(identForm.getFromLocation()).isNull();

  }

  @Test
  void renderAddIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  void renderAddIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postAddIdent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddIdent_validationFailed() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromLocation"));

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postAddIdent(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            null,
            null,
            null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
        .andExpect(model().attributeHasErrors("form"));

    verifyNoInteractions(pipelineIdentService);

  }

  @Test
  void postAddIdent_valid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postAddIdent(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            null,
            null,
            null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).addIdent(eq(padPipeline), any());

  }

  @Test
  void postAddIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postAddIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
  }

  @Test
  void renderIdentOverview_permissionSmokeTest() {

    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderIdentOverview_appTypeSmokeTest() {
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
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
  void renderIdentOverview_appStatusSmokeTest() {

    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
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
  void renderIdentOverview_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  void renderIdentOverview_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void renderRemoveIdent_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderRemoveIdent_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderRemoveIdent_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void renderRemoveIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  void renderRemoveIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
                endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
                return ReverseRouter.route(on(PipelineIdentsController.class)
                    .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null));
              });

              endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postRemoveIdent_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postRemoveIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postRemoveIdent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postRemoveIdent_verifyRemove() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postRemoveIdent(APP_ID, PwaApplicationType.INITIAL, padPipeline.getId(), null, 1, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).removeIdent(ident);

  }

  @Test
  void postRemoveIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postRemoveIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postRemoveIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, 1, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
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
  void postIdentOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postIdentOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postIdentOverview_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postIdentOverview_failValidation() {

    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    padPipeline.setPipelineInBundle(false);
    var padPipelineOverview = new PadPipelineOverview(padPipeline);
    when(padPipelineService.getPipelineOverview(any())).thenReturn(padPipelineOverview);
    when(pipelineIdentService.isSectionValid(any())).thenReturn(false);
    when(pipelineIdentService.getSummaryScreenValidationResult(any()))
        .thenReturn(new SummaryScreenValidationResult(Map.of(), "", "", false, ""));

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postIdentOverview(applicationDetail.getMasterPwaApplicationId(), type, padPipeline.getId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postIdentOverview_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postIdentOverview_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postAddIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
  }

  @Test
  void renderEditIdent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderEditIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderEditIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderEditIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  void renderEditIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postEditIdent_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditIdent_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postEditIdent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditIdent_failValidation() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null)));

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      errors.reject("fake", "error");
      return errors;
    }).when(validator).validate(any(), any(), any(Object[].class));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postEditIdent_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postEditIdent_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postEditIdent(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
  }

  @Test
  void renderInsertIdentAbove_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderInsertIdentAbove_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderInsertIdentAbove_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderInsertIdentAbove_previousIdentAvailable() throws Exception {

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
            .with(user(user)))
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
  void renderInsertIdentAbove_noPreviousIdent() throws Exception {

    var identForm = (PipelineIdentForm) Objects.requireNonNull(
        mockMvc.perform(get(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderInsertIdentAbove(
                pwaApplicationDetail.getMasterPwaApplicationId(),
                pwaApplicationDetail.getPwaApplicationType(),
                99,
                1,
                null,
                null, null)))
            .with(user(user)))
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
  void renderInsertIdentAbove_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  void renderInsertIdentAbove_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .renderInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postInsertIdentAbove_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postInsertIdentAbove_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postInsertIdentAbove_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null,
                    null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postInsertIdentAbove_validationFailed() throws Exception {

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
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/addEditIdent"))
        .andExpect(model().attributeHasErrors("form"));

    verify(pipelineIdentService, never()).addIdentAtPosition(any(), any(), any());

  }

  @Test
  void postInsertIdentAbove_valid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PipelineIdentsController.class)
        .postInsertIdentAbove(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            99,
            1,
            null,
            null,
            null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(pipelineIdentService, times(1)).addIdentAtPosition(eq(padPipeline), any(), eq(1));

  }

  @Test
  void postInsertIdentAbove_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  void postInsertIdentAbove_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            endpointTester.getPadPipeline().setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelineIdentsController.class)
                .postInsertIdentAbove(applicationDetail.getMasterPwaApplicationId(), type, 99, 1, null, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
  }

}
