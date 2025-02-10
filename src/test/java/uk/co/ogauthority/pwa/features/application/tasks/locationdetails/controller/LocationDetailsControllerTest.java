package uk.co.ogauthority.pwa.features.application.tasks.locationdetails.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
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
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsForm;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsSafetyZoneValidator;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsValidator;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacilityService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacilityService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LocationDetailsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class LocationDetailsControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadLocationDetailsService padLocationDetailsService;

  @MockBean
  private DevukFacilityService devukFacilityService;

  @MockBean
  private PadFacilityService padFacilityService;

  @SpyBean
  private LocationDetailsValidator locationDetailsValidator;

  @SpyBean
  private LocationDetailsSafetyZoneValidator locationDetailsSafetyZoneValidator;

  @SpyBean
  private TwoFieldDateInputValidator twoFieldDateInputValidator;

  private WebUserAccount wua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail pwaApplicationDetail;

  private static final int APP_ID = 100;
  private PwaApplicationEndpointTestBuilder endpointTester;


  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    doCallRealMethod().when(applicationBreadcrumbService).fromCrossings(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.DEPOSIT_CONSENT)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);

    when(pwaApplicationDetailService.getTipDetailByAppId(eq(APP_ID))).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(pwaApplicationDetailService.getTipDetailByAppId(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
  }

  @Test
  public void postLocationDetails_fullValidationParams_unauthenticatedUser() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(LocationDetailsController.class).renderLocationDetails(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1))))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void postLocationDetails_partialValidationParams_unauthenticatedUser() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.partialValidationPostParams()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderLocationDetails_validAppId() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(LocationDetailsController.class).renderLocationDetails(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/locationDetails"));
  }

  @Test
  public void postLocationDetails_partialSave_noData() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(),
        ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.partialValidationPostParams())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());

  }

  @Test
  public void postLocationDetails_partialSave_invalidForm() throws Exception {

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add(ValidationType.PARTIAL.getButtonText(), ValidationType.PARTIAL.getButtonText());
      add("transportationMethod", StringUtils.repeat('a', 5000));
    }};

    ControllerTestUtils.failValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(),
        ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(continueParams)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void postLocationDetails_partialSave_validForm() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(),
        ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.partialValidationPostParams())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());

  }

  @Test
  public void postLocationDetails_InvalidForm() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(),
        ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.fullValidationPostParams())
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    verify(padLocationDetailsService, never()).getLocationDetailsForDraft(pwaApplicationDetail);

  }

  @Test
  public void postLocationDetails_ValidForm() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
      add("approximateProjectLocationFromShore", "approx");
      add("withinSafetyZone", "NO");
      add("facilitiesOffshore", "true");
      add("transportsMaterialsToShore", "true");
      add("transportationMethod", "method");
    }};

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(),
        ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null,
                null), Map.of("applicationId", 1)))
            .params(params)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());
    verify(padFacilityService, times(1)).setFacilities(any(), any());
  }

  @Test
  public void renderLocationDetails_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(LocationDetailsController.class)
                    .renderLocationDetails(type, null, null, null),
                Map.of("applicationId", applicationDetail.getMasterPwaApplicationId())));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderLocationDetails_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(LocationDetailsController.class)
                    .renderLocationDetails(type, null, null, null),
                Map.of("applicationId", applicationDetail.getMasterPwaApplicationId())));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderLocationDetails_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(LocationDetailsController.class)
                    .renderLocationDetails(type, null, null, null),
                Map.of("applicationId", applicationDetail.getMasterPwaApplicationId())));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

}
