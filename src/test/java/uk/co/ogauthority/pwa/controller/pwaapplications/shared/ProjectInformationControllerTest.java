package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
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

  @MockBean
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
  public void testAuthenticated() throws Exception {
    for(var appType : PwaApplicationType.values()) {
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
  public void testUnauthenticated() {

  }

  @Test
  public void renderProjectInformation() {
  }

  @Test
  public void postContinueProjectInformation() {
  }

  @Test
  public void postCompleteProjectInformation() {
  }
}