package uk.co.ogauthority.pwa.features.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackResultStatus;
import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;

class FeedbackClientServiceTest {

  private static MockWebServer mockWebServer;

  private final static String SUBMITTER_NAME = "testName";
  private final static String SUBMITTER_EMAIL = "test@email.com";
  private final static String SERVICE_RATING = ServiceFeedbackRating.VERY_SATISFIED.name();
  private final static String COMMENT = "testImprovement";
  private final static Integer APPLICATION_ID = 2;
  private final static String CASE_REFERENCE = "PA/10";
  private final static String CASE_LINK = "testLink.com";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private TestFeedback feedback;
  private FeedbackClientService feedbackClientService;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());

    feedbackClientService = new FeedbackClientService(objectMapper, baseUrl, 20L, "/api/v1/save-feedback", "PWA", "dev");
    feedback = new TestFeedback(1,SUBMITTER_NAME, SUBMITTER_EMAIL, SERVICE_RATING,
        COMMENT, Instant.now(), APPLICATION_ID, CASE_REFERENCE, CASE_LINK);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void saveFeedback_authorized() throws JsonProcessingException, CannotSendFeedbackException, InterruptedException {
    mockWebServer.enqueue(new MockResponse()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .setResponseCode(200)
        .setBody("{\"feedbackId\": 1}")
    );

    var actualResponse = feedbackClientService.saveFeedback(feedback);
    assertThat(actualResponse.getStatus()).isEqualTo(FeedbackResultStatus.FEEDBACK_SENT);

    var observedRequest = mockWebServer.takeRequest();
    assertThat(observedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
    assertThat(observedRequest.getPath()).isEqualTo("/api/v1/save-feedback");
    assertThat(observedRequest.getHeaders().get("Content-type")).isEqualTo("application/json");
    assertThat(observedRequest.getHeaders().get("Authorization")).isEqualTo("dev");

    var postedJson = objectMapper.readTree(observedRequest.getBody().readUtf8());
    assertThat(postedJson.get("submitterName").asText()).isEqualTo(feedback.getSubmitterName());
    assertThat(postedJson.get("submitterEmail").asText()).isEqualTo(feedback.getSubmitterEmail());
    assertThat(postedJson.get("serviceRating").asText()).isEqualTo(feedback.getServiceRating());
    assertThat(postedJson.get("comment").asText()).isEqualTo(feedback.getComment());
    assertThat(Instant.ofEpochSecond(postedJson.get("givenDatetime").asLong())).isCloseTo(feedback.getGivenDatetime(), within(1, ChronoUnit.SECONDS));
    assertThat(postedJson.get("transactionId").asText()).isEqualTo(feedback.getTransactionId().toString());
    assertThat(postedJson.get("transactionReference").asText()).isEqualTo(feedback.getTransactionReference());
    assertThat(postedJson.get("transactionLink").asText()).isEqualTo(feedback.getTransactionLink());
  }

  @Test
  void saveFeedback_unauthorized(){
    mockWebServer.enqueue(new MockResponse()
          .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .setResponseCode(403)
          .setBody("{\"timestamp\":\"2021-11-04T17:55:25.329+00:00\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access Denied\",\"path\":\"/fmslocal/api/v1/save-feedback\"}")
      );
    assertThrows(CannotSendFeedbackException.class, () ->

      feedbackClientService.saveFeedback(feedback));
  }

  @Test
  void saveFeedback_responseBodyNotJSon() {
    mockWebServer.enqueue(new MockResponse()
          .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .setResponseCode(500)
          .setBody("unexpectedResponseBody")
      );
    assertThrows(CannotSendFeedbackException.class, () ->

      feedbackClientService.saveFeedback(feedback));
  }

}