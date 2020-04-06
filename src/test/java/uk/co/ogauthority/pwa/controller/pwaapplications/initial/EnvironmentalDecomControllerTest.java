package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
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
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = EnvironmentalDecomController.class)
public class EnvironmentalDecomControllerTest extends AbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  private Person person;
  private WebUserAccount wua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail appDetail;
  private ApplicationHolderOrganisation holderOrganisation;
  private Instant instant;

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.DEPOSIT_CONSENT
  );

  @Before
  public void setUp() {

    person = new Person();
    wua = new WebUserAccount(1, person);
    user = new AuthenticatedUserAccount(wua, List.of());

    var pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);
    pwaApplication.setId(1);
    appDetail = new PwaApplicationDetail();
    appDetail.setPwaApplication(pwaApplication);
    instant = Instant.now();

    var holderOrg = new PortalOrganisationUnit(1, "HOLDER");
    holderOrganisation = new ApplicationHolderOrganisation(appDetail, holderOrg);

    when(pwaApplicationContextService.getApplicationContext(any(), any(), any(), any(), any())).thenReturn(new PwaApplicationContext(appDetail, user, Set.of(PwaContactRole.PREPARER)));

  }

  @Test
  public void render_authenticated_validAppType() {

    when(pwaApplicationContextService.getApplicationContext(any(), any(), anySet(), any(), any()))
        .thenReturn(new PwaApplicationContext(appDetail, user, Set.of(PwaContactRole.PREPARER)));

    allowedApplicationTypes.forEach(validAppType -> {

      try {
        mockMvc.perform(
            get(ReverseRouter.route(
                on(EnvironmentalDecomController.class).renderEnvDecom(validAppType, null, null, null),
                Map.of("applicationId", 1)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()))
        .andExpect(status().isOk());
      } catch (Exception e) {
        throw new AssertionError();
      }

    });

  }

  @Test
  public void render_authenticated_invalidAppType() {

    when(pwaApplicationContextService.getApplicationContext(any(), any(), anySet(), any(), any()))
        .thenThrow(AccessDeniedException.class);

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {

      try {
        mockMvc.perform(
            get(ReverseRouter.route(
                on(EnvironmentalDecomController.class).renderEnvDecom(invalidAppType, null, null, null),
                Map.of("applicationId", 1)))
                .with(authenticatedUserAndSession(user))
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
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, null, null, null), Map.of("applicationId", 1))))
        .andExpect(status().is3xxRedirection());


    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(completeParams))
        .andExpect(status().isForbidden());


    MultiValueMap continueParams = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null, null, null), Map.of("applicationId", 1)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testRenderAdminDetails() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, null, null, null), Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));
  }

  @Test
  public void testPostAdminDetails_partial() throws Exception {

    MultiValueMap<String, String> completeLaterParams = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "Save and complete later");
    }};

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class)
            .postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null, null, null), Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeLaterParams))
        .andExpect(status().is3xxRedirection());

    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.PARTIAL));

  }

  @Test
  public void testPostAdminDetails_full_invalid() throws Exception {

    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "Complete");
    }};

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    bindingResult.addError(new ObjectError("fake error", "fake"));
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class)
            .postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null, null, null), Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));

    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.FULL));
    verify(padEnvironmentalDecommissioningService, never()).getEnvDecomData(appDetail);

  }

  @Test
  public void testPostAdminDetails_full_valid() throws Exception {

    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "Complete");
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
      add("decommissioningPlans", "decom plans");
      add("acceptsEolRegulations", "true");
      add("acceptsEolRemoval", "true");
      add("acceptsRemovalProposal", "true");
    }};

    var bindingResult = new BeanPropertyBindingResult(EnvironmentalDecommissioningForm.class, "form");
    when(padEnvironmentalDecommissioningService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class).postEnvDecom(PwaApplicationType.INITIAL, null, null, null, null, null, null), Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().is3xxRedirection());

    verify(padEnvironmentalDecommissioningService, times(1)).getEnvDecomData(appDetail);
    verify(padEnvironmentalDecommissioningService, times(1)).validate(any(), any(), eq(ValidationType.FULL));

  }
}