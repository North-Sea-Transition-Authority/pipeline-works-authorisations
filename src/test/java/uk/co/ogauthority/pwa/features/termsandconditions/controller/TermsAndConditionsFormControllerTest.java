package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;

@WebMvcTest(TermsAndConditionsFormController.class)
@Import(PwaMvcTestConfiguration.class)
class TermsAndConditionsFormControllerTest extends AbstractControllerTest {

  @MockBean
  TermsAndConditionsService termsAndConditionsService;

  @MockBean
  ControllerHelperService controllerHelperService;

  @MockBean
  MasterPwaService masterPwaService;

  private AuthenticatedUserAccount userAccount;
  private AuthenticatedUserAccount userAccountNoAuth;
  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;

  @BeforeEach
  void setup() {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()),
        EnumSet.of(PwaUserPrivilege.PWA_ACCESS, PwaUserPrivilege.PWA_MANAGER));

    userAccountNoAuth = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()), Set.of());

    masterPwa = MasterPwaTestUtil.create(1);
    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setMasterPwa(masterPwa);
    masterPwaDetail.setReference("1/W/23");
  }

  @Test
  void renderTermsAndConditionsForm_newRecord() throws Exception {
    var availablePwas = Map.of("1/W/23", "1");
    when(termsAndConditionsService.getPwasForSelector()).thenReturn(availablePwas);
    when(termsAndConditionsService.getTermsAndConditionsForm(null)).thenReturn(new TermsAndConditionsForm());

    var mvc = mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsFormController.class)
        .renderNewTermsAndConditionsForm(null, userAccount)))
        .with(user(userAccount)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc.get("cancelUrl")).isEqualTo(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
        .renderTermsAndConditionsManagement(null, null, userAccount)));
    assertThat(mvc.get("pwaSelectorOptions")).isEqualTo(availablePwas);
    assertThat(mvc.get("pageTitle")).isEqualTo("Submit terms and conditions for a PWA");
    assertThat(mvc.get("existingRecord")).isEqualTo(false);
  }

  @Test
  void renderTermsAndConditionsForm_existingRecord() throws Exception {
    var preFilledForm = new TermsAndConditionsForm().setPwaId(1)
        .setVariationTerm(7)
        .setHuooTermOne(3)
        .setHuooTermTwo(6)
        .setHuooTermThree(9)
        .setDepconParagraph(2)
        .setDepconSchedule(8);

    when(termsAndConditionsService.getPwasForSelector()).thenReturn(Map.of());
    when(masterPwaService.getMasterPwaById(1)).thenReturn(masterPwa);
    when(masterPwaService.getCurrentDetailOrThrow(masterPwa)).thenReturn(masterPwaDetail);
    when(termsAndConditionsService.getTermsAndConditionsForm(1)).thenReturn(preFilledForm);

    var mvc = mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .renderEditTermsAndConditionsForm(null, 1, userAccount)))
            .with(user(userAccount)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView()
        .getModel();

    assertThat(mvc.get("cancelUrl")).isEqualTo(ReverseRouter.route(on(TermsAndConditionsManagementController.class)
        .renderTermsAndConditionsManagement(null, null, userAccount)));
    assertThat(mvc.get("pwaSelectorOptions")).isEqualTo(Map.of());
    assertThat(mvc.get("pageTitle")).isEqualTo("Update terms and conditions for PWA 1/W/23");
    assertThat(mvc.get("existingRecord")).isEqualTo(true);
  }

  @Test
  void renderTermsAndConditionsForm_unauthenticated() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .renderNewTermsAndConditionsForm(null, userAccountNoAuth)))
            .with(user(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitTermsAndConditionsVariationForm_post() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .submitTermsAndConditionsForm(null, null, null, userAccount, null)))
            .with(user(userAccount))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void submitTermsAndConditionsVariationForm_post_validationFail() throws Exception {
    var failedBindingResult = new BeanPropertyBindingResult(new TermsAndConditionsForm(), "form");
    failedBindingResult.addError(new ObjectError("fake", "fake"));
    when(termsAndConditionsService.validateForm(any(), any())).thenReturn(failedBindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .submitTermsAndConditionsForm(null, null, null, userAccount, null)))
            .with(user(userAccount))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void submitTermsAndConditionsVariationForm_post_unauthenticated() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .submitTermsAndConditionsForm(null, null, null, userAccountNoAuth, null)))
            .with(user(userAccountNoAuth)))
        .andExpect(status().isForbidden());
  }
}