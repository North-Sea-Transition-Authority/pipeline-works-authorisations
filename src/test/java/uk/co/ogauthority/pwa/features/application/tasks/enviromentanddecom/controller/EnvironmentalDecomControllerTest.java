package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.controller;

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

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = EnvironmentalDecomController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class EnvironmentalDecomControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  private Person person;
  private WebUserAccount wua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail appDetail;
  private Instant instant;

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.DEPOSIT_CONSENT
  );

  @Before
  public void setUp() {

    person = new Person();
    wua = new WebUserAccount(1, person);
    user = new AuthenticatedUserAccount(wua, List.of());

    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    instant = Instant.now();

    var holderOrg = PortalOrganisationTestUtils.generateOrganisationUnit(1, "HOLDER");

    when(pwaApplicationDetailService.getTipDetailByAppId(anyInt())).thenReturn(appDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

  }

  @Test
  public void render_authenticated_validAppType() {


    allowedApplicationTypes.forEach(validAppType -> {
      appDetail.getPwaApplication().setApplicationType(validAppType);
      try {
        mockMvc.perform(
            get(ReverseRouter.route(
                on(EnvironmentalDecomController.class).renderEnvDecom(validAppType, null, null),
                Map.of("applicationId", 1)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
      } catch (Exception e) {
        throw new AssertionError();
      }

    });

  }

  @Test
  public void render_authenticated_invalidAppType() {


    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          appDetail.getPwaApplication().setApplicationType(invalidAppType);
      try {
        mockMvc.perform(
            get(ReverseRouter.route(
                on(EnvironmentalDecomController.class).renderEnvDecom(invalidAppType, null, null),
                Map.of("applicationId", 1)))
                .with(user(user))
                .with(csrf()))
            .andExpect(status().isForbidden());
      } catch (Exception e) {
        if (!(e instanceof AccessDeniedException)) {
          throw new AssertionError();
        }
      }

    });

  }

  @Test
  public void testUnauthenticated() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, null, null), Map.of("applicationId", 1))))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isForbidden());

    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null), Map.of("applicationId", 1)))
            .params(ControllerTestUtils.partialValidationPostParams()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void testRenderAdminDetails() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, null, null), Map.of("applicationId", 1)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));
  }

  @Test
  public void testPostAdminDetails_partial() throws Exception {

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class)
            .postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null), Map.of("applicationId", 1)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.partialValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.PARTIAL), any());

  }

  @Test
  public void testPostAdminDetails_full_invalid() throws Exception {

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    bindingResult.addError(new ObjectError("fake error", "fake"));
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class)
            .postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null), Map.of("applicationId", 1)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));

    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
    verify(padEnvironmentalDecommissioningService, never()).getEnvDecomData(appDetail);

  }

  @Test
  public void testPostAdminDetails_full_valid() throws Exception {

    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>(){{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
      add("transboundaryEffect", "true");
      add("emtSubmissionDay", "1");
      add("emtSubmissionMonth", "1");
      add("emtSubmissionYear", "2020");
      add("emtHasSubmittedPermits", "true");
      add("permitsSubmitted", "permits");
      add("emtHasOutstandingPermits", "true");
      add("permitsPendingSubmission", "other permits");
      add("dischargeFundsAvailable", "true");
      add("acceptsOpolLiability", "true");
      add("acceptsEolRegulations", "true");
      add("acceptsEolRemoval", "true");
      add("acceptsRemovalProposal", "true");
    }};

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null), Map.of("applicationId", 1)))
            .with(user(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().is3xxRedirection());

    verify(padEnvironmentalDecommissioningService, times(1)).getEnvDecomData(appDetail);
    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());

  }
}
