package uk.co.ogauthority.pwa.service.controllers.typemismatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.auth.HasAnyRoleInterceptor;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.saml.SamlResponseParser;
import uk.co.ogauthority.pwa.config.ExternalApiConfiguration;
import uk.co.ogauthority.pwa.config.SamlProperties;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.WebSecurityConfig;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfigurationProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.webapp.TopMenuService;
import uk.co.ogauthority.pwa.integrations.energyportal.access.EnergyPortalAccessApiConfiguration;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@RunWith(SpringRunner.class)
@Import({
    AbstractControllerTest.AbstractControllerTestConfiguration.class,
    PwaMvcTestConfiguration.class,
    WebSecurityConfig.class,
    SamlProperties.class,
    HasAnyRoleInterceptor.class
})
@EnableConfigurationProperties({ExternalApiConfiguration.class, EnergyPortalAccessApiConfiguration.class})
@ActiveProfiles("test")
@WebMvcTest(
    controllers = ControllerHelperServiceTypeMismatchController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {ControllerHelperService.class}))
class ControllerHelperServiceTypeMismatchTest {

  private MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @MockBean
  protected PwaApplicationDetailService pwaApplicationDetailService;

  @MockBean
  protected PwaApplicationRedirectService pwaApplicationRedirectService;

  @MockBean
  protected PwaContactService pwaContactService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  private TopMenuService topMenuService;

  @Autowired
  private ControllerHelperService controllerHelperService;

  @MockBean
  protected ServiceProperties serviceProperties;

  @SpyBean
  protected FooterService footerServices;

  @MockBean
  protected AnalyticsConfigurationProperties analyticsConfigurationProperties;

  @MockBean
  protected AnalyticsService analyticsService;

  @MockBean
  protected SamlResponseParser samlResponseParser;

  @MockBean
  protected LogoutSuccessHandler logoutSuccessHandler;

  @MockBean
  private TeamManagementService teamManagementService;

  @MockBean
  private TeamQueryService teamQueryService;

  @MockBean
  private HasTeamRoleService hasTeamRoleService;

  private final AuthenticatedUserAccount testUser = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  @BeforeEach
  void setUp() {

    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

  }

  @Test
  void checkErrorsAndRedirect_typeMismatchErrors() throws Exception {

    @SuppressWarnings("unchecked")
    var errorList = (List<ErrorItem>) Objects.requireNonNull(mockMvc.perform(
        post(ReverseRouter.route(on(ControllerHelperServiceTypeMismatchController.class).post(null, null)))
            .with(user(testUser))
            .with(csrf())
            .param("integerField", "invalidInt")
            .param("bigDecimalField", "invalidDec")
            .param("stringField", "validString"))
        .andReturn()
        .getModelAndView())
        .getModel()
        .get("errorList");

    assertThat(errorList)
        .extracting(ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactlyInAnyOrder(
            tuple("integerField", "Enter a whole number"),
            tuple("bigDecimalField", "Enter a number")
        );

  }

  @Test
  void checkErrorsAndRedirect_noTypeMismatchErrors() throws Exception {

    var errorList = Objects.requireNonNull(mockMvc.perform(
        post(ReverseRouter.route(on(ControllerHelperServiceTypeMismatchController.class).post(null, null)))
            .with(user(testUser))
            .with(csrf())
            .param("integerField", "7")
            .param("bigDecimalField", "7.77")
            .param("stringField", "validString"))
        .andReturn()
        .getModelAndView())
        .getModel()
        .get("errorList");

    assertThat(errorList).isNull();

  }

}