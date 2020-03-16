package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.service.pwaapplications.initial.PadDataService;
import uk.co.ogauthority.pwa.service.pwaapplications.initial.validators.PadEnvDecomValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = InitialEnvironmentalDecomController.class)
public class InitialEnvironmentalDecomControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationHolderService applicationHolderService;

  @MockBean
  private PadEnvDecomValidator padEnvDecomValidator;

  @MockBean
  private PadDataService padDataService;

  private Person person;
  private WebUserAccount wua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail appDetail;
  private ApplicationHolderOrganisation holderOrganisation;
  private Instant instant;
  private PadData padData;

  @Before
  public void setUp() {
    person = new Person();
    wua = new WebUserAccount(1, person);
    user = new AuthenticatedUserAccount(wua, List.of());
    appDetail = new PwaApplicationDetail();
    instant = Instant.now();

    when(pwaApplicationDetailService.withDraftTipDetail(eq(1), eq(user), any())).thenCallRealMethod();
    when(pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT)).thenReturn(appDetail);

    var holderOrg = new PortalOrganisationUnit(1, "HOLDER");
    holderOrganisation = new ApplicationHolderOrganisation(appDetail, holderOrg);
    when(applicationHolderService.getHoldersFromApplicationDetail(appDetail)).thenReturn(List.of(holderOrganisation));

    padData = buildPadData();
    when(padDataService.getPadData(appDetail)).thenReturn(padData);

  }

  @Test
  public void authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(InitialEnvironmentalDecomController.class).renderAdminDetails(1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());

    // Expect isOk because endpoint validates. If form can't validate, return same page.
    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(InitialEnvironmentalDecomController.class).postCompleteAdminDetails(1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().isOk());

    // Expect redirection because endpoint ignores validation.
    MultiValueMap continueParams = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(InitialEnvironmentalDecomController.class).postContinueAdminDetails(1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(continueParams))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(InitialEnvironmentalDecomController.class).renderAdminDetails(1, null, null))))
        .andExpect(status().is3xxRedirection());

    // Expect isOk because endpoint validates. If form can't validate, return same page.
    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(InitialEnvironmentalDecomController.class).postCompleteAdminDetails(1, null, null, null)))
            .params(completeParams))
        .andExpect(status().isForbidden());

    // Expect redirection because endpoint ignores validation
    MultiValueMap continueParams = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(InitialEnvironmentalDecomController.class).postContinueAdminDetails(1, null, null, null)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderAdminDetails() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(InitialEnvironmentalDecomController.class).renderAdminDetails(1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(view().name("pwaApplication/initial/environmentalAndDecommissioning"))
        .andReturn()
        .getModelAndView();
    var form = (EnvDecomForm) modelAndView.getModel().get("form");
    var formDate = LocalDate.ofInstant(padData.getEmtSubmissionTimestamp(), ZoneId.systemDefault());
    assertThat(form.getEmtHasOutstandingPermits()).isEqualTo(padData.getEmtHasOutstandingPermits());
    assertThat(form.getEmtHasSubmittedPermits()).isEqualTo(padData.getEmtHasSubmittedPermits());
    assertThat(form.getPermitsSubmitted()).isEqualTo(padData.getPermitsSubmitted());
    assertThat(form.getPermitsPendingSubmission()).isEqualTo(padData.getPermitsPendingSubmission());
    assertThat(form.getAcceptsEolRegulations()).isEqualTo(padData.getAcceptsEolRegulations());
    assertThat(form.getAcceptsEolRemoval()).isEqualTo(padData.getAcceptsEolRemoval());
    assertThat(form.getAcceptsOpolLiability()).isEqualTo(padData.getAcceptsOpolLiability());
    assertThat(form.getAcceptsRemovalProposal()).isEqualTo(padData.getAcceptsRemovalProposal());
    assertThat(form.getDecommissioningPlans()).isEqualTo(padData.getDecommissioningPlans());
    assertThat(form.getDischargeFundsAvailable()).isEqualTo(padData.getDischargeFundsAvailable());
    assertThat(form.getEmtSubmissionDay()).isEqualTo(formDate.getDayOfMonth());
    assertThat(form.getEmtSubmissionMonth()).isEqualTo(formDate.getMonthValue());
    assertThat(form.getEmtSubmissionYear()).isEqualTo(formDate.getYear());
    assertThat(form.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
  }

  @Test
  public void postCompleteAdminDetails_Invalid() throws Exception {
    MultiValueMap completeParams = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(InitialEnvironmentalDecomController.class).postCompleteAdminDetails(1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/initial/environmentalAndDecommissioning"));
  }

  @Test
  public void postCompleteAdminDetails_Valid() throws Exception {
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
        post(ReverseRouter.route(on(InitialEnvironmentalDecomController.class).postCompleteAdminDetails(1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams))
        .andExpect(status().is3xxRedirection());
  }

  private PadData buildPadData() {
    var padData = new PadData();
    padData.setAcceptsEolRegulations(true);
    padData.setAcceptsEolRemoval(true);
    padData.setAcceptsRemovalProposal(true);
    padData.setAcceptsOpolLiability(true);
    padData.setDecommissioningPlans("Decom plan");
    padData.setDischargeFundsAvailable(true);
    padData.setEmtHasOutstandingPermits(true);
    padData.setEmtHasSubmittedPermits(true);
    padData.setPermitsSubmitted("manual list of permits");
    padData.setPermitsPendingSubmission("manual list of permits");
    padData.setEmtSubmissionTimestamp(instant);
    padData.setTransboundaryEffect(true);
    return padData;
  }
}