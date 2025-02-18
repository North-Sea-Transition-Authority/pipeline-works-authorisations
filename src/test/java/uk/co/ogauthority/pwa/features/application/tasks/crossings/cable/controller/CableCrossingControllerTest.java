package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = CableCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class CableCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private int APP_ID = 100;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadCableCrossingService padCableCrossingService;

  @MockBean
  private CableCrossingFileService cableCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @BeforeEach
  void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING);

    when(pwaApplicationDetailService.getTipDetailByAppId(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING
        )
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

  @Test
  void renderAddCableCrossing_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(CableCrossingController.class).renderAddCableCrossing(invalidAppType, 1, null, null)))
                    .with(user(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  void renderEditCableCrossing_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(CableCrossingController.class).renderEditCableCrossing(invalidAppType, 1, 1, null, null)))
                    .with(user(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  void renderRemoveCableCrossing_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(CableCrossingController.class).renderRemoveCableCrossing(invalidAppType, 1, 1, null)))
                    .with(user(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  void renderAddCableCrossing_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderAddCableCrossing(PwaApplicationType.INITIAL, 1, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/crossings/addCableCrossing"));
  }

  @Test
  void renderEditCableCrossing_authenticated() throws Exception {

    var cableCrossing = new PadCableCrossing();
    when(padCableCrossingService.getCableCrossing(pwaApplicationDetail, 1)).thenReturn(cableCrossing);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/crossings/editCableCrossing"));
    verify(padCableCrossingService, times(1)).mapCrossingToForm(eq(cableCrossing), any());
  }

  @Test
  void renderRemoveCableCrossing_authenticated() throws Exception {

    var cableCrossing = new PadCableCrossing();
    cableCrossing.setCableName("name");
    cableCrossing.setCableOwner("owner");
    cableCrossing.setLocation("loc");
    when(padCableCrossingService.getCableCrossing(pwaApplicationDetail, 1)).thenReturn(cableCrossing);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderRemoveCableCrossing(PwaApplicationType.INITIAL, 1, 1, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/crossings/removeCableCrossing"));
    verify(padCableCrossingService, times(1)).getCableCrossing(pwaApplicationDetail, 1);
  }

  @Test
  void renderAddCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderAddCableCrossing(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void renderEditCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void postAddCableCrossings_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void postEditCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null,
                null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void postRemoveCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postRemoveCableCrossing(PwaApplicationType.INITIAL, 1, 1, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void postAddCableCrossings_invalid() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
    verify(padCableCrossingService, never()).createCableCrossing(eq(pwaApplicationDetail), any());
  }

  @Test
  void postAddCableCrossings_valid() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("cableName", "abc");
      add("cableOwner", "def");
      add("location", "ghi");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(paramMap))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).createCableCrossing(eq(pwaApplicationDetail), any());
  }

  @Test
  void postEditCableCrossing_invalid() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null,
                null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
    verify(padCableCrossingService, never()).updateCableCrossing(eq(pwaApplicationDetail), eq(1), any());
  }

  @Test
  void postEditCableCrossing_valid() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("cableName", "abc");
      add("cableOwner", "def");
      add("location", "ghi");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class)
                .postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(paramMap))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).updateCableCrossing(eq(pwaApplicationDetail), eq(1), any());
  }

  @Test
  void postRemoveCableCrossing() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postRemoveCableCrossing(PwaApplicationType.INITIAL, 1, 1, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).removeCableCrossing(eq(pwaApplicationDetail), eq(1));
  }

  @Test
  void renderOverview_appTypeSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CABLE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getAllowedTypes().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.CABLE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));

  }

  @Test
  void renderOverview_appStatusSmokeTest() throws Exception {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CABLE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    verify(padFileService, times(endpointTester.getAllowedStatuses().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.CABLE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  void renderOverview_appContactRoleSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CABLE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

    verify(padFileService, times(endpointTester.getAllowedAppPermissions().size())).getUploadedFileViews(any(),
        eq(ApplicationDetailFilePurpose.CABLE_CROSSINGS), eq(ApplicationFileLinkStatus.FULL));
  }

  @Test
  void postOverview_appTypeSmokeTest() {

    when(padCableCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postOverview_appStatusSmokeTest() {

    when(padCableCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  void postOverview_appContactRoleSmokeTest() {

    when(padCableCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CableCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());
  }
}
