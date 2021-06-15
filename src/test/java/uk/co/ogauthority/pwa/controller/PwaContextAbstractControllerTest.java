package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Import(PwaContextAbstractControllerTest.AbstractControllerTestConfiguration.class)
public abstract class PwaContextAbstractControllerTest {

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @Autowired
  protected PwaContextService pwaContextService;

  @MockBean
  protected PipelineService pipelineService;

  @MockBean
  protected ConsentSearchService consentSearchService;

  @MockBean
  protected FoxUrlService foxUrlService;

  @MockBean
  protected UserSessionService userSessionService;

  @MockBean
  protected MasterPwaService masterPwaService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  private TopMenuService topMenuService;

  @Autowired
  protected PwaPermissionService pwaPermissionService;

  @SpyBean
  private SearchPwaBreadcrumbService searchPwaBreadcrumbService;

  @SpyBean
  protected ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  protected PwaApplicationRedirectService redirectService;

  @MockBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @SpyBean
  protected UserTypeService userTypeService;

  @MockBean
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  protected ServiceProperties serviceProperties;

  @SpyBean
  protected FooterService footerServices;



  @Before
  public void abstractControllerTestSetup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    when(foxUrlService.getFoxLoginUrl()).thenReturn("testLoginUrl");
    when(foxUrlService.getFoxLogoutUrl()).thenReturn("testLogoutUrl");

    when(userSessionService.getAndValidateSession(any(), anyBoolean())).thenReturn(Optional.of(new UserSession()));

    when(serviceProperties.getCustomerName()).thenReturn("oga");
    when(serviceProperties.getServiceName()).thenReturn("pwa");

  }

  @TestConfiguration
  public static class AbstractControllerTestConfiguration {
    @Bean
    public SystemAreaAccessService systemAreaAccessService() {
      return new SystemAreaAccessService();
    }

    @Bean
    public FileUploadProperties fileUploadProperties() {
      FileUploadProperties fileUploadProperties = new FileUploadProperties();
      fileUploadProperties.setMaxFileSize(1000L);
      fileUploadProperties.setAllowedExtensions(List.of("txt", "xls", "doc"));
      return fileUploadProperties;
    }
  }

}