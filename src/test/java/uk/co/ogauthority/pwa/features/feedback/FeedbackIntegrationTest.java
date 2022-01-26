package uk.co.ogauthority.pwa.features.feedback;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.features.feedback.FeedbackIntegrationTest.FeedbackTestConfig.mockWebServer;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.config.TechnicalSupportContactProperties;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.feedback.FeedbackController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.enums.ServiceContactDetail;
import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;
import uk.co.ogauthority.pwa.mvc.DefaultExceptionResolver;
import uk.co.ogauthority.pwa.mvc.error.ErrorService;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.feedback.FeedbackValidator;

@TestPropertySource(properties = {
    "pwa.url.base = http://test/",
    "context-path = pwa"
})
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = FeedbackController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {FeedbackService.class, FeedbackValidator.class, FeedbackEmailService.class,
            CaseLinkService.class, PwaApplicationContextService.class,
            TechnicalSupportContactProperties.class,
            DefaultExceptionResolver.class, ErrorService.class}))
//DefaultExceptionResolver and ErrorService has to be included so
//Spring knows how to handle CannotSendFeedbackException in some tests
@Import({FeedbackIntegrationTest.FeedbackTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FeedbackIntegrationTest extends PwaApplicationContextAbstractControllerTest {

  private final static String RATING = ServiceFeedbackRating.NEITHER.name();
  private final static String COMMENT = "testImprovement";
  private final static Instant DATETIME = Instant.parse("2020-04-29T10:15:30Z");
  private final static Integer APPLICATION_ID = 10;
  private final static String CASE_REFERENCE = "APP_REFERENCE/" + APPLICATION_ID;
  private final static String CASE_LINK = String.format("http://test/pwa/pwa-application/options/%s/case-management/TASKS/", APPLICATION_ID);
  private final static Integer APPLICATION_DETAIL_ID = 20;
  private final static String SUPPORT_EMAIl = ServiceContactDetail.TECHNICAL_SUPPORT.getEmailAddress();

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  PwaApplicationDetailRepository pwaApplicationDetailRepository;

  @MockBean
  NotifyService notifyService;

  private AuthenticatedUserAccount user;
  private Person person;
  private MockResponse responseWhenAuthorized;
  private MockResponse responseWhenNotAuthorized;

  @Before
  public void setUp() {
    person = PersonTestUtil.createDefaultPerson();
    var systemWua = new WebUserAccount(1, person);
    user = new AuthenticatedUserAccount(systemWua, Set.of(PwaUserPrivilege.PWA_REGULATOR));

    responseWhenAuthorized = new MockResponse()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .setResponseCode(200)
        .setBody("{\"feedbackId\": 1}");

    responseWhenNotAuthorized = new MockResponse()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .setResponseCode(403)
        .setBody("{\"timestamp\":\"2021-11-04T17:55:25.329+00:00\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access Denied\",\"path\":\"/fmslocal/api/v1/save-feedback\"}");
  }

  @After
  public void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void saveFeedback_authorized() throws Exception {
    mockWebServer.enqueue(responseWhenAuthorized);

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT))
        .andExpect(redirectedUrlTemplate("/work-area"));

    verify(notifyService, never()).sendEmail(any(EmailProperties.class), any());

    var observedRequest = mockWebServer.takeRequest();
    assertThat(observedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
    assertThat(observedRequest.getPath()).isEqualTo("/api/v1/save-feedback");
    assertThat(observedRequest.getHeaders().get("Content-type")).isEqualTo("application/json");
    assertThat(observedRequest.getHeaders().get("Authorization")).isEqualTo("dev");
  }

  @Test
  public void saveFeedback_unauthorized()  throws Exception{
    mockWebServer.enqueue(responseWhenNotAuthorized);

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT))
        .andExpect(redirectedUrlTemplate("/work-area"));

    verify(notifyService, times(1)).sendEmail(any(EmailProperties.class), eq(SUPPORT_EMAIl));
  }

  @Test
  public void saveFeedback_withApplicationDetailId() throws Exception {
    var pwaApplicationDetail = PwaApplicationTestUtil
        .createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION, APPLICATION_ID, APPLICATION_DETAIL_ID);

    when(pwaApplicationDetailRepository.findByIdAndTipFlagIsTrue(APPLICATION_DETAIL_ID))
        .thenReturn(Optional.of(pwaApplicationDetail));

    mockWebServer.enqueue(responseWhenAuthorized);

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT)
            .param("pwaApplicationDetailId", APPLICATION_DETAIL_ID.toString()))
        .andExpect(redirectedUrlTemplate("/work-area"));

    var observedRequest = mockWebServer.takeRequest();
    assertThat(observedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
    assertThat(observedRequest.getPath()).isEqualTo("/api/v1/save-feedback");
    assertThat(observedRequest.getHeaders().get("Content-type")).isEqualTo("application/json");
    assertThat(observedRequest.getHeaders().get("Authorization")).isEqualTo("dev");

    var postedJson = objectMapper.readTree(observedRequest.getBody().readUtf8());
    assertThat(postedJson.get("submitterName").asText()).isEqualTo(person.getFullName());
    assertThat(postedJson.get("submitterEmail").asText()).isEqualTo(person.getEmailAddress());
    assertThat(postedJson.get("serviceRating").asText()).isEqualTo(RATING);
    assertThat(postedJson.get("comment").asText()).isEqualTo(COMMENT);
    assertThat(Instant.parse(postedJson.get("givenDatetime").asText())).isEqualTo(DATETIME);
    assertThat(postedJson.get("serviceName").asText()).isEqualTo("PWA");
    assertThat(postedJson.get("transactionId").asText()).isEqualTo(APPLICATION_ID.toString());
    assertThat(postedJson.get("transactionReference").asText()).isEqualTo(CASE_REFERENCE);
    assertThat(postedJson.get("transactionLink").asText()).isEqualTo(CASE_LINK);
  }

  @Test
  public void saveFeedback_withoutApplicationDetailId() throws Exception {
    mockWebServer.enqueue(responseWhenAuthorized);

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT))
        .andExpect(redirectedUrlTemplate("/work-area"));

    var observedRequest = mockWebServer.takeRequest();
    assertThat(observedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
    assertThat(observedRequest.getPath()).isEqualTo("/api/v1/save-feedback");
    assertThat(observedRequest.getHeaders().get("Content-type")).isEqualTo("application/json");
    assertThat(observedRequest.getHeaders().get("Authorization")).isEqualTo("dev");

    var postedJson = objectMapper.readTree(observedRequest.getBody().readUtf8());
    assertThat(postedJson.get("submitterName").asText()).isEqualTo(person.getFullName());
    assertThat(postedJson.get("submitterEmail").asText()).isEqualTo(person.getEmailAddress());
    assertThat(postedJson.get("serviceRating").asText()).isEqualTo(RATING);
    assertThat(postedJson.get("comment").asText()).isEqualTo(COMMENT);
    assertThat(Instant.parse(postedJson.get("givenDatetime").asText())).isEqualTo(DATETIME);
    assertThat(postedJson.get("serviceName").asText()).isEqualTo("PWA");
    assertThat(postedJson.get("transactionId").isNull()).isTrue();
    assertThat(postedJson.get("transactionReference").isNull()).isTrue();
    assertThat(postedJson.get("transactionLink").isNull()).isTrue();
  }

  @Test
  public void saveFeedback_invalidParameters() throws Exception {
    mockWebServer.enqueue(responseWhenAuthorized);

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", ""))
        .andExpect(status().isOk());

    var observedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
    assertThat(observedRequest).isNull();
  }

  @Test
  public void saveFeedback_serverDown_throwCannotSendFeedbackException() throws Exception {
    mockWebServer.shutdown();

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT))
        .andExpect(redirectedUrlTemplate("/work-area"));

    verify(notifyService, times(1)).sendEmail(any(EmailProperties.class), eq(SUPPORT_EMAIl));
  }

  @Test
  public void saveFeedback_unexpectedRespone() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .setResponseCode(500)
    );

    mockMvc.perform(post("/feedback")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("serviceRating", RATING)
            .param("feedback", COMMENT))
        .andExpect(redirectedUrlTemplate("/work-area"));

    verify(notifyService, times(1)).sendEmail(any(EmailProperties.class), eq(SUPPORT_EMAIl));
  }

  @TestConfiguration
  public static class FeedbackTestConfig {

    protected static MockWebServer mockWebServer;

    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public FeedbackClientService getFeedbackClientService() throws IOException {
      mockWebServer = new MockWebServer();
      mockWebServer.start();
      String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
      return new FeedbackClientService(objectMapper, baseUrl, 20L, "/api/v1/save-feedback", "PWA", "dev");
    }

    @Bean
    public Clock utcClock() {
      return Clock.fixed(DATETIME, ZoneId.of("UTC"));
    }
  }

}
