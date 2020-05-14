package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import org.apache.commons.lang3.StringUtils;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositsService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PermanentDepositController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PermanentDepositsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PermanentDepositsService permanentDepositsService;

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING
  );

  private WebUserAccount webUserAccount;
  private AuthenticatedUserAccount user;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PermanentDepositInformation permanentDepositInformation;

  @Before
  public void setUp() {
    webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of());

    pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);
    pwaApplication.setId(APP_ID);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.DRAFT);

    permanentDepositInformation = new PermanentDepositInformation();
    permanentDepositInformation.setPwaApplicationDetail(pwaApplicationDetail);

    //support app context code
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    // by default has all roles
    when(pwaContactService.getContactRoles(eq(pwaApplication), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));


  }

  @Test
  public void renderPermanentDeposits_authenticatedUser_appTypeSmokeTest() throws Exception {
    PwaApplicationType[] appTypes = {PwaApplicationType.CAT_1_VARIATION, PwaApplicationType.CAT_2_VARIATION, PwaApplicationType.DECOMMISSIONING, PwaApplicationType.DEPOSIT_CONSENT, PwaApplicationType.INITIAL, PwaApplicationType.OPTIONS_VARIATION};
    for (var appType : appTypes) {
      try {
        pwaApplication.setApplicationType(appType);
        var result = mockMvc.perform(
            get(ReverseRouter.route(
                on(PermanentDepositController.class).renderPermanentDeposits(appType, APP_ID, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()));
        if (allowedApplicationTypes.contains(appType)) {
          result.andExpect(status().isOk());
        } else {
          result.andExpect(status().isForbidden());
        }
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type:" + appType + "\n" + e.getMessage(), e);
      }

    }

  }

  @Test
  public void postPermanentDeposits_authenticatedUser_appTypeSmokeTest() throws Exception {

    ControllerTestUtils.failValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.FULL);
    PwaApplicationType[] appTypes = {PwaApplicationType.CAT_1_VARIATION, PwaApplicationType.CAT_2_VARIATION, PwaApplicationType.DECOMMISSIONING, PwaApplicationType.DEPOSIT_CONSENT, PwaApplicationType.INITIAL, PwaApplicationType.OPTIONS_VARIATION};
    for (var appType : appTypes) {
      try {
        pwaApplication.setApplicationType(appType);
        // Expect isOk because endpoint validates. If form can't validate, return same page.
        MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
          add("Complete", "Complete");
        }};
        var result = mockMvc.perform(
            post(ReverseRouter.route(
                on(PermanentDepositController.class).postPermanentDeposits(appType, APP_ID, null, null, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf())
                .params(completeParams));
        if (allowedApplicationTypes.contains(appType)) {
          result.andExpect(status().isOk());
        } else {
          result.andExpect(status().isForbidden());
        }
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type:" + appType + "\n" + e.getMessage(), e);
      }
    }
  }

  @Test
  public void postPermanentDeposits_continue_authenticatedUser_appTypeSmokeTest() throws Exception {

    ControllerTestUtils.passValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.PARTIAL);

    PwaApplicationType[] appTypes = {PwaApplicationType.CAT_1_VARIATION, PwaApplicationType.CAT_2_VARIATION, PwaApplicationType.DECOMMISSIONING, PwaApplicationType.DEPOSIT_CONSENT, PwaApplicationType.INITIAL, PwaApplicationType.OPTIONS_VARIATION};
    for (var appType : appTypes) {
      try {
        pwaApplication.setApplicationType(appType);
        // Expect isOk because endpoint validates. If form can't validate, return same page.

        // Expect redirection because endpoint ignores validation.
        MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
          add("Save and complete later", "Save and complete later");
        }};
        var result = mockMvc.perform(
            post(ReverseRouter.route(
                on(PermanentDepositController.class).postPermanentDeposits(appType, APP_ID, null, null, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf())
                .params(continueParams));
        if (allowedApplicationTypes.contains(appType)) {
          result.andExpect(status().is3xxRedirection());
        } else {
          result.andExpect(status().isForbidden());
        }
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type:" + appType + "\n" + e.getMessage(), e);
      }
    }
  }

  @Test
  public void renderPermanentDeposits_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDeposits(PwaApplicationType.INITIAL, null, null, null))))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postPermanentDeposits_complete_unauthenticated() throws Exception {
    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(PermanentDepositController.class)
                .postPermanentDeposits(PwaApplicationType.INITIAL, null, null, null, null, null)))
            .params(completeParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postPermanentDeposits_continue_unauthenticated() throws Exception {
    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(PermanentDepositController.class)
                .postPermanentDeposits(PwaApplicationType.INITIAL, null, null, null, null, null)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderPermanentDeposits_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/permanentdeposits/permanentDeposits"));
  }

  @Test
  public void postPermanentDeposits__continue_validForm() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
    }};

    ControllerTestUtils.passValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());
    verify(permanentDepositsService, times(1)).getPermanentDepositData(pwaApplicationDetail);
    verify(permanentDepositsService, times(1)).saveEntityUsingForm(any(), any(), any());
  }

  @Test
  public void postPermanentDeposits__continue_formValidationFailed() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "Save and complete later");
      add("projectOverview", StringUtils.repeat("a", 5000));
    }};

    ControllerTestUtils.failValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
    verify(permanentDepositsService, times(0)).getPermanentDepositData(pwaApplicationDetail);

  }

  @Test
  public void postPermanentDeposits__complete_noData() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    ControllerTestUtils.failValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());

    verify(permanentDepositsService, never()).getPermanentDepositData(pwaApplicationDetail);

  }

  @Test
  public void postPermanentDeposits_complete_valid() throws Exception {

    LocalDate date = LocalDate.now().plusDays(2);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
      add("materialType", MaterialType.ROCK.toString());
    }};

    ControllerTestUtils.passValidationWhenPost(permanentDepositsService, new PermanentDepositsForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(permanentDepositsService, times(1)).getPermanentDepositData(pwaApplicationDetail);
    verify(permanentDepositsService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(permanentDepositsService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());


  }
}