package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
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
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.LocationDetailsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadLocationDetailsService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LocationDetailsController.class)
public class LocationDetailsControllerTest extends AbstractControllerTest {

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

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.DEPOSIT_CONSENT
  );

  private WebUserAccount wua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, 1);
    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);
    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    when(pwaApplicationContextService.getApplicationContext(any(), any(), any(), any(), any()))
        .thenReturn(new PwaApplicationContext(pwaApplicationDetail, user, Set.of(PwaContactRole.PREPARER)));
  }

  @Test
  public void render_authenticated_validAppType() {

    when(pwaApplicationContextService.getApplicationContext(any(), any(), anySet(), any(), any()))
        .thenReturn(new PwaApplicationContext(pwaApplicationDetail, user, Set.of(PwaContactRole.PREPARER)));

    allowedApplicationTypes.forEach(validAppType -> {

      try {
        mockMvc.perform(
            get(ReverseRouter.route(
                on(LocationDetailsController.class).renderLocationDetails(validAppType, null, null, null),
                Map.of("applicationId", 1)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()))
            .andExpect(status().isOk());
      } catch (Exception e) {
        throw new AssertionError(e);
      }

    });

  }

  @Test
  public void testUnauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(LocationDetailsController.class).renderLocationDetails(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1))))
        .andExpect(status().is3xxRedirection());


    MultiValueMap completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(completeParams))
        .andExpect(status().isForbidden());


    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderLocationDetails() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(LocationDetailsController.class).renderLocationDetails(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/locationDetails"));
  }

  @Test
  public void postContinueLocationDetails() throws Exception {

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
    }};

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(continueParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postContinueLocationDetails_Invalid() throws Exception {

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
      add("transportationMethod", StringUtils.repeat('a', 5000));
    }};

    ControllerTestUtils.failValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(continueParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void postContinueLocationDetails_Valid() throws Exception {

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
    }};

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(continueParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postCompleteLocationDetails_Invalid() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    ControllerTestUtils.failValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(params)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
    verify(padLocationDetailsService, never()).getLocationDetailsForDraft(pwaApplicationDetail);
  }

  @Test
  public void postCompleteLocationDetails_Valid() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
      add("approximateProjectLocationFromShore", "approx");
      add("withinSafetyZone", "NO");
      add("facilitiesOffshore", "true");
      add("transportsMaterialsToShore", "true");
      add("transportationMethod", "method");
    }};

    ControllerTestUtils.passValidationWhenPost(padLocationDetailsService, new LocationDetailsForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(LocationDetailsController.class).postLocationDetails(PwaApplicationType.INITIAL, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(params)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padLocationDetailsService, times(1)).saveEntityUsingForm(any(), any());
    verify(padFacilityService, times(1)).setFacilities(any(), any());
  }
}