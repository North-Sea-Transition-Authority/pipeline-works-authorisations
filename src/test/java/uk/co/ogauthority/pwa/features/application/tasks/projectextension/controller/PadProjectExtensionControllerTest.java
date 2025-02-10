package uk.co.ogauthority.pwa.features.application.tasks.projectextension.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.ProjectExtensionForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PadProjectExtensionController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PadProjectExtensionControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  private WebUserAccount webUserAccount;
  private AuthenticatedUserAccount user;

  private Instant startTimestamp = (LocalDateTime.of(2020, 1, 2, 3, 4, 5)
      .toInstant(ZoneOffset.ofTotalSeconds(0)));

  private Instant endTimestamp = (LocalDateTime.of(2022, 1, 2, 3, 4, 5)
      .toInstant(ZoneOffset.ofTotalSeconds(0)));

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadProjectInformation padProjectInformation;

  @MockBean
  PadProjectInformationService padProjectInformationService;

  @MockBean
  PadProjectExtensionService padProjectExtensionService;

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
    padProjectInformation.setProposedStartTimestamp(startTimestamp);
    padProjectInformation.setLatestCompletionTimestamp(endTimestamp);



    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(padProjectInformation);

  }

  @Test
  public void renderProjectInformation_authenticatedUser_appTypeSmokeTest() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(
            PadProjectExtensionController.class)
            .renderProjectExtension(null, APP_ID, PwaApplicationType.INITIAL, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/projectExtension"))
        .andExpect(model().attribute("startDate", DateUtils.formatDate(startTimestamp)))
        .andExpect(model().attribute("endDate", DateUtils.formatDate(endTimestamp)));
  }

  @Test
  public void postProjectInformation_validationPasses_appTypeSmokeTest() throws Exception {
    ControllerTestUtils.passValidationWhenPost(padProjectExtensionService, new ProjectExtensionForm(), ValidationType.FULL);
    mockMvc.perform(
        post(ReverseRouter.route(on(
            PadProjectExtensionController.class)
            .postProjectExtension(null,
                new ProjectExtensionForm(),
                null,
                null,
                APP_ID,
                PwaApplicationType.INITIAL)))
            .with(user(user))
            .with(csrf())
            .param(ValidationType.FULL.getButtonText(), ""))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/1/tasks"));
  }

  @Test
  public void postProjectInformation_validationFail_appTypeSmokeTest() throws Exception {
    ControllerTestUtils.failValidationWhenPost(padProjectExtensionService, new ProjectExtensionForm(), ValidationType.FULL);
    mockMvc.perform(
            post(ReverseRouter.route(on(
                PadProjectExtensionController.class)
                .postProjectExtension(null,
                    new ProjectExtensionForm(),
                    null,
                    null,
                    APP_ID,
                    PwaApplicationType.INITIAL)))
                .with(user(user))
                .with(csrf())
                .param(ValidationType.FULL.getButtonText(), ""))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/projectExtension"))
        .andExpect(model().attribute("startDate", DateUtils.formatDate(startTimestamp)))
        .andExpect(model().attribute("endDate", DateUtils.formatDate(endTimestamp)));
  }
}
