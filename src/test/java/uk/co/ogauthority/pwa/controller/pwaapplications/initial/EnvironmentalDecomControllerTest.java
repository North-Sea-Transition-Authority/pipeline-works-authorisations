package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.mockito.ArgumentMatchers.any;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.validators.EnvDecomValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = EnvironmentalDecomController.class)
public class EnvironmentalDecomControllerTest extends AbstractControllerTest {

  @MockBean
  private EnvDecomValidator envDecomValidator;

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
    appDetail = new PwaApplicationDetail();
    appDetail.setPwaApplication(pwaApplication);
    instant = Instant.now();

    when(pwaApplicationDetailService.withDraftTipDetail(eq(1), eq(user), any())).thenCallRealMethod();
    when(pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT)).thenReturn(appDetail);

    var holderOrg = new PortalOrganisationUnit(1, "HOLDER");
    holderOrganisation = new ApplicationHolderOrganisation(appDetail, holderOrg);

  }

  @Test
  public void testAuthenticated() throws Exception {
    for(var appType : PwaApplicationType.values()) {
      var result = mockMvc.perform(
          get(ReverseRouter.route(
              on(EnvironmentalDecomController.class).renderEnvDecom(appType, 1, null, null)))
              .with(authenticatedUserAndSession(user))
              .with(csrf()));
      if (allowedApplicationTypes.contains(appType)) {
        result.andExpect(status().isOk());
      } else {
        result.andExpect(status().isForbidden());
      }

      // Expect isOk because endpoint validates. If form can't validate, return same page.
      MultiValueMap completeParams = new LinkedMultiValueMap<>() {{
        add("Complete", "");
      }};
      result = mockMvc.perform(
          post(ReverseRouter.route(
              on(EnvironmentalDecomController.class).postCompleteEnvDecom(appType, 1, null, null, null)))
              .with(authenticatedUserAndSession(user))
              .with(csrf())
              .params(completeParams));
      if (allowedApplicationTypes.contains(appType)) {
        result.andExpect(status().isOk());
      } else {
        result.andExpect(status().isForbidden());
      }

      // Expect redirection because endpoint ignores validation.
      MultiValueMap continueParams = new LinkedMultiValueMap<>() {{
        add("Save and complete later", "");
      }};
      result = mockMvc.perform(
          post(ReverseRouter.route(
              on(EnvironmentalDecomController.class).postContinueEnvDecom(appType, 1, null, null, null)))
              .with(authenticatedUserAndSession(user))
              .with(csrf())
              .params(continueParams));
      if (allowedApplicationTypes.contains(appType)) {
        result.andExpect(status().is3xxRedirection());
      } else {
        result.andExpect(status().isForbidden());
      }
    }
  }

  @Test
  public void testUnauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().is3xxRedirection());


    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postCompleteEnvDecom(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(completeParams))
        .andExpect(status().isForbidden());


    MultiValueMap continueParams = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(EnvironmentalDecomController.class).postContinueEnvDecom(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testRenderAdminDetails() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(EnvironmentalDecomController.class).renderEnvDecom(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));
  }

  @Test
  public void testPostCompleteAdminDetails_Invalid() throws Exception {
    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class)
            .postCompleteEnvDecom(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/environmentalAndDecommissioning"));
    verify(padEnvironmentalDecommissioningService, never()).getEnvDecomData(appDetail);
  }

  @Test
  public void testPostCompleteAdminDetails_Valid() throws Exception {
    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
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
    mockMvc.perform(
        post(ReverseRouter.route(on(EnvironmentalDecomController.class).postCompleteEnvDecom(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().is3xxRedirection());
    verify(padEnvironmentalDecommissioningService, times(1)).getEnvDecomData(appDetail);
  }
}