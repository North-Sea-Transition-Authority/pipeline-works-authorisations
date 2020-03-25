package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadProjectInformationService;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ProjectInformationController.class)
public class ProjectInformationControllerTest extends AbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadProjectInformationService padProjectInformationService;

  @SpyBean
  private ProjectInformationValidator projectInformationValidator;

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
  private PadProjectInformation padProjectInformation;

  @Before
  public void setUp() {
    webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of());

    pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    padProjectInformation = new PadProjectInformation();
    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);

    when(pwaApplicationDetailService.withDraftTipDetail(any(), any(), any())).thenCallRealMethod();
    when(pwaApplicationDetailService.getTipDetailWithStatus(any(), eq(PwaApplicationStatus.DRAFT)))
        .thenReturn(pwaApplicationDetail);
  }

  @Test
  public void authenticated() throws Exception {
    for (var appType : PwaApplicationType.values()) {
      var result = mockMvc.perform(
          get(ReverseRouter.route(
              on(ProjectInformationController.class).renderProjectInformation(appType, 1, null, null)))
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
              on(ProjectInformationController.class).postCompleteProjectInformation(appType, 1, null, null, null)))
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
              on(ProjectInformationController.class).postContinueProjectInformation(appType, 1, null, null, null)))
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
  public void unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().is3xxRedirection());


    MultiValueMap completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(ProjectInformationController.class)
                .postCompleteProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(completeParams))
        .andExpect(status().isForbidden());


    MultiValueMap continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(
            on(ProjectInformationController.class)
                .postContinueProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(continueParams))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderProjectInformation() throws Exception {
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
  public void postContinueProjectInformation_Valid() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postContinueProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());
    verify(padProjectInformationService, times(1)).getPadProjectInformationData(pwaApplicationDetail);
    verify(padProjectInformationService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postContinueProjectInformation_ValidationFailed() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>(){{
      add("Save and complete later", "");
      add("projectOverview", StringUtils.repeat("a", 5000));
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postContinueProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
    verify(padProjectInformationService, times(0)).getPadProjectInformationData(pwaApplicationDetail);
  }

  @Test
  public void postCompleteProjectInformation_NoData() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>(){{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postCompleteProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());

    verify(padProjectInformationService, never()).getPadProjectInformationData(pwaApplicationDetail);

    var captor = ArgumentCaptor.forClass(Errors.class);
    verify(projectInformationValidator).validate(any(), captor.capture());
    assertThat(captor.getValue().getErrorCount()).isGreaterThan(0);
  }

  @Test
  public void postCompleteProjectInformation_ValidData() throws Exception {
    LocalDate date = LocalDate.now().plusDays(2);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>(){{
      add("Complete", "");
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
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(ProjectInformationController.class)
            .postCompleteProjectInformation(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());
    verify(padProjectInformationService, times(1)).getPadProjectInformationData(pwaApplicationDetail);
    verify(padProjectInformationService, times(1)).saveEntityUsingForm(any(), any());

    var captor = ArgumentCaptor.forClass(Errors.class);

    verify(projectInformationValidator).validate(any(), captor.capture());
    assertThat(captor.getValue().getErrorCount()).isEqualTo(0);
  }
}