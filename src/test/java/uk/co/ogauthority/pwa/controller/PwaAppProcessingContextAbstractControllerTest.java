package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.mockito.Mock;
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
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView;
import uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@Import(PwaAppProcessingContextAbstractControllerTest.AbstractControllerTestConfiguration.class)
public abstract class PwaAppProcessingContextAbstractControllerTest {

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @Autowired
  protected PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  protected FoxUrlService foxUrlService;

  @MockBean
  protected UserSessionService userSessionService;

  @MockBean
  protected PwaApplicationDetailService pwaApplicationDetailService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  private TopMenuService topMenuService;

  @Autowired
  protected PwaAppProcessingPermissionService processingPermissionService;

  @MockBean
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected PwaContextService pwaContextService;

  @SpyBean
  protected ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  protected PwaApplicationRedirectService redirectService;

  @MockBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @MockBean
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @SpyBean
  private ControllerHelperService controllerHelperService;

  @SpyBean
  private AppProcessingBreadcrumbService appProcessingBreadcrumbService;

  @MockBean
  private AppFileService appFileService;

  @SpyBean
  protected UserTypeService userTypeService;

  @MockBean
  protected ConsultationRequestService consultationRequestService;

  @MockBean
  protected CaseSummaryViewService caseSummaryViewService;

  @MockBean
  protected ServiceProperties serviceProperties;

  @SpyBean
  protected FooterService footerServices;

  @MockBean
  protected MetricsProvider metricsProvider;

  @MockBean
  private Appender appender;

  private Timer timer;

  @Before
  public void abstractControllerTestSetup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    when(foxUrlService.getFoxLoginUrl()).thenReturn("testLoginUrl");
    when(foxUrlService.getFoxLogoutUrl()).thenReturn("testLogoutUrl");

    when(userSessionService.getAndValidateSession(any(), anyBoolean())).thenReturn(Optional.of(new UserSession()));

    var appDetailView = ApplicationDetailViewTestUtil.createGenericDetailView();
    var caseSummaryView = CaseSummaryView.from(appDetailView);
    when(caseSummaryViewService.getCaseSummaryViewForAppDetail(any())).thenReturn(Optional.of(caseSummaryView));

    when(serviceProperties.getCustomerName()).thenReturn("oga");
    when(serviceProperties.getServiceName()).thenReturn("pwa");

    timer = TimerMetricTestUtils.setupTimerMetric(
        PwaAppProcessingContextService.class, "pwa.appContextTimer", appender);
    when(metricsProvider.getAppContextTimer()).thenReturn(timer);
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