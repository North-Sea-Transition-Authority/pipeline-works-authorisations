package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositMade;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ProjectInformationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class ProjectInformationControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadProjectInformationService padProjectInformationService;

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.HUOO_VARIATION
  );

  private WebUserAccount webUserAccount;
  private AuthenticatedUserAccount user;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadProjectInformation padProjectInformation;

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

    padProjectInformation = new PadProjectInformation();
    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);

    //support app context code
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    // by default has all roles
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

  }

  @Test
  public void renderProjectInformation_authenticatedUser_appTypeSmokeTest() throws Exception {
    for (var appType : PwaApplicationType.values()) {
      try {
        pwaApplication.setApplicationType(appType);
        var result = mockMvc.perform(
            get(ReverseRouter.route(
                on(ProjectInformationController.class).renderProjectInformation(appType, APP_ID, null, null)))
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
  public void renderProjectInformation_authenticatedUser_validPermanentDepositMadeForAppType() throws Exception {
    for (var appType : PwaApplicationType.values()) {
      try {
        pwaApplication.setApplicationType(appType);
        var result = mockMvc.perform(
            get(ReverseRouter.route(
                on(ProjectInformationController.class).renderProjectInformation(appType, APP_ID, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf()));
        if (allowedApplicationTypes.contains(appType)) {
          if (appType == PwaApplicationType.OPTIONS_VARIATION) {
            result.andExpect(model().attribute("permanentDepositsMadeOptions",
                List.of(PermanentDepositMade.YES, PermanentDepositMade.NONE)));
          } else {
            result.andExpect(model().attribute("permanentDepositsMadeOptions",
                List.of(PermanentDepositMade.THIS_APP, PermanentDepositMade.LATER_APP, PermanentDepositMade.NONE)));          }
        } else {
          result.andExpect(status().isForbidden());
        }
      } catch (AssertionError e) {
        throw new AssertionError("Failed at type:" + appType + "\n" + e.getMessage(), e);
      }
    }
  }

  @Test
  public void postCompleteProjectInformation_authenticatedUser_appTypeSmokeTest() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.FULL);

    for (var appType : PwaApplicationType.values()) {
      try {
        pwaApplication.setApplicationType(appType);
        // Expect isOk because endpoint validates. If form can't validate, return same page.
        var result = mockMvc.perform(
            post(ReverseRouter.route(
                on(ProjectInformationController.class).postProjectInformation(appType, APP_ID, null, null, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf())
                .params(ControllerTestUtils.fullValidationPostParams()));
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
  public void postProjectInformation_continue_authenticatedUser_appTypeSmokeTest() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.PARTIAL);

    for (var appType : PwaApplicationType.values()) {
      try {
        pwaApplication.setApplicationType(appType);
        // Expect isOk because endpoint validates. If form can't validate, return same page.

        // Expect redirection because endpoint ignores validation.
        var result = mockMvc.perform(
            post(ReverseRouter.route(
                on(ProjectInformationController.class).postProjectInformation(appType, APP_ID, null, null, null, null)))
                .with(authenticatedUserAndSession(user))
                .with(csrf())
                .params(ControllerTestUtils.partialValidationPostParams()));
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
  public void renderProjectInformation_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(PwaApplicationType.INITIAL, null, null, null))))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postProjectInformation_complete_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(ProjectInformationController.class)
                .postProjectInformation(PwaApplicationType.INITIAL, null, null, null, null, null)))
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postProjectInformation_continue_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(ProjectInformationController.class)
                .postProjectInformation(PwaApplicationType.INITIAL, null, null, null, null, null)))
            .params(ControllerTestUtils.partialValidationPostParams()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderProjectInformation_serviceInteractions() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/projectInformation"));
    verify(padProjectInformationService, times(1)).mapEntityToForm(any(), any());
  }

  @Test
  public void postProjectInformation__continue_validForm() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(ControllerTestUtils.partialValidationPostParams()))
        .andExpect(status().is3xxRedirection());
    verify(padProjectInformationService, times(1)).getPadProjectInformationData(pwaApplicationDetail);
    verify(padProjectInformationService, times(1)).saveEntityUsingForm(any(), any(), any());
  }

  @Test
  public void postProjectInformation__continue_formValidationFailed() throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add(ValidationType.PARTIAL.getButtonText(), ValidationType.PARTIAL.getButtonText());
      add("projectOverview", StringUtils.repeat("a", 5000));
    }};

    ControllerTestUtils.failValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
    verify(padProjectInformationService, times(0)).getPadProjectInformationData(pwaApplicationDetail);

  }

  @Test
  public void postProjectInformation__complete_noData() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());

    verify(padProjectInformationService, never()).getPadProjectInformationData(pwaApplicationDetail);

  }

  @Test
  public void postProjectInformation_complete_valid() throws Exception {

    LocalDate date = LocalDate.now().plusDays(2);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
      add("projectName", "name");
      add("projectOverview", "overview");
      add("methodOfPipelineDeployment", "pipeline installation method");
      add("proposedStartDay", "" + date.getDayOfMonth());
      add("proposedStartMonth", "" + date.getMonthValue());
      add("proposedStartYear", "" + date.getYear());
      add("mobilisationDay", "" + date.getDayOfMonth());
      add("mobilisationMonth", "" + date.getMonthValue());
      add("mobilisationYear", "" + date.getYear());
      add("earliestCompletionDay", "" + date.getDayOfMonth());
      add("earliestCompletionMonth", "" + date.getMonthValue());
      add("earliestCompletionYear", "" + date.getYear());
      add("latestCompletionDay", "" + date.getDayOfMonth());
      add("latestCompletionMonth", "" + date.getMonthValue());
      add("latestCompletionYear", "" + date.getYear());
      add("usingCampaignApproach", "true");
      add("uploadedFileWithDescriptionForms[0].uploadedFileId", "123" );
      add("uploadedFileWithDescriptionForms[0].uploadedFileDescription", "321" );
      add("uploadedFileWithDescriptionForms[0].uploadedFileInstant", "2020-04-02T16:15:33.166138Z" );
    }};

    ControllerTestUtils.passValidationWhenPost(padProjectInformationService, new ProjectInformationForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(padProjectInformationService, times(1)).getPadProjectInformationData(pwaApplicationDetail);
    verify(padProjectInformationService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(padProjectInformationService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());


  }
}