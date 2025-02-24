package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.AddCarbonStorageAreaFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingView;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.EditCarbonStorageAreaCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.PadCrossedStorageArea;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(
    controllers = CarbonStorageAreaCrossingController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class CarbonStorageAreaCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private CarbonStorageAreaCrossingService storageAreaCrossingService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private CarbonStorageAreaCrossingFileService carbonStorageAreaCrossingFileService;

  @MockBean
  private AddCarbonStorageAreaFormValidator addFormValidator;

  @MockBean
  private EditCarbonStorageAreaCrossingFormValidator editFormValidator;

  @Mock
  private PadCrossedStorageArea crossedStorageArea;

  @Mock
  private PwaApplicationDetail pwaApplicationDetail;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private static final int APP_ID = 100;
  private static final int CROSSING_ID = 1;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setup() {
    doCallRealMethod().when(applicationBreadcrumbService).fromCrossings(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    when(storageAreaCrossingService.getById(any())).thenReturn(crossedStorageArea);
    when(crossedStorageArea.getStorageAreaReference()).thenReturn("44/TEST");

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(storageAreaCrossingService.getCrossedAreaViews(any())).thenReturn(
        List.of(new CarbonStorageCrossingView(CROSSING_ID, "ref", List.of(), true)));

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));
  }

  @Test
  void renderBlockCrossingOverview_appTypeSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CARBON_STORAGE_AREA_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .renderCarbonStorageCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderBlockCrossingOverview_appStatusSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CARBON_STORAGE_AREA_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .renderCarbonStorageCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  public void renderBlockCrossingOverview_appContactRoleSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.CARBON_STORAGE_AREA_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .renderCarbonStorageCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void actionAddAreaCrossing_whenFormValid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .actionAddAreaCrossing(
                    pwaApplicationDetail.getPwaApplicationType(),
                    pwaApplicationDetail.getMasterPwaApplicationId(),
                    null,
                    null
                    , null)
            ))
            .with(user(user))
            .with(csrf())
            .params(getValidAddFormAsMap()))
        .andExpect(status().is3xxRedirection());
    verify(addFormValidator, times(1)).validate(any(), any(), any(Object[].class));
  }

  @Test
  void actionEditAreaCrossing_whenFormValid() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
            .actionEditAreaCrossing(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                73,
                null,
                null,
                null)
        ))
            .with(user(user))
            .with(csrf())
            .params(getValidAddFormAsMap()))
        .andExpect(status().is3xxRedirection());
    verify(editFormValidator, times(1)).validate(any(), any());
  }

  private MultiValueMap<String, String> getValidAddFormAsMap() {
    return new LinkedMultiValueMap<>() {{
      add("storageAreaRef", "10BLOCK");
      add("crossingOwner", "HOLDER");
    }};
  }

  @Test
  void removeCrossing_whenCrossingNotFound() throws Exception {
    when(storageAreaCrossingService.getById(CROSSING_ID))
        .thenThrow(new PwaEntityNotFoundException("BANG"));
    mockMvc.perform(
            post(ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .actionRemoveAreaCrossing(
                    pwaApplicationDetail.getPwaApplicationType(),
                    pwaApplicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isNotFound());

  }

  @Test
  void removeCrossing_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .actionRemoveAreaCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    CROSSING_ID,
                    null)
            )
        );
    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void removeBlockCrossing_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .actionRemoveAreaCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    CROSSING_ID,
                    null)
            )
        );
    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void removeBlockCrossing_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CarbonStorageAreaCrossingController.class)
                .actionRemoveAreaCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    CROSSING_ID,
                    null)
            )
        );

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }
}
