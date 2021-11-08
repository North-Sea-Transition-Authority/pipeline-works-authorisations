package uk.co.ogauthority.pwa.integrations.govukpay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class GovukPayClientApiV1ImplTest {

  private static final String APPLICATION_BASE_URL = "http://pwa.local";

  private static final String GOV_UK_PAY_BASE_URL = "http://fivium.fake-pay.local";

  private static final String PAYMENTS_ENDPOINT = "/v1/payments";

  private static final String AUTH_HEADER_VALUE = "Bearer : 123";

  private static final String GOV_PAY_ID = "abc123ABC";

  @Mock
  private ApiV1RequestDataMapper apiV1RequestDataMapper;

  @Mock
  GovUkPayConfiguration configuration;

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;

  private GovUkPayCardPaymentClient govukPayClient;

  @Before
  public void setup() {


    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.bindTo(restTemplate).build();

    when(configuration.getConfiguredRestTemplate()).thenReturn(restTemplate);
    when(configuration.getGovukPayBaseUrl()).thenReturn(GOV_UK_PAY_BASE_URL);
    when(configuration.getGovukPayAuthorizationHeaderValue()).thenReturn(AUTH_HEADER_VALUE);
    when(configuration.getPaymentsEndpoint()).thenReturn(PAYMENTS_ENDPOINT);

    govukPayClient = new GovUkPayClientApiV1Impl(
        apiV1RequestDataMapper,
        configuration
    );

  }

  @Test
  public void getCardPaymentJourneyData_whenRequestOk_andValidJsonReturned() throws IOException {
    var expectedApiUrl = GOV_UK_PAY_BASE_URL + PAYMENTS_ENDPOINT + "/" + GOV_PAY_ID;
    var mapperOutput = mock(GovPayPaymentJourneyData.class);
    when(apiV1RequestDataMapper.mapGetPaymentResult(any())).thenReturn(mapperOutput);
    try (InputStream jsonStream = this.getClass().getResourceAsStream("getPaymentData_valid.json")) {

      var jsonResponse = IOUtils.toString(jsonStream);

      mockServer.expect(requestTo(expectedApiUrl))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

      var result = govukPayClient.getCardPaymentJourneyData(GOV_PAY_ID);

      assertThat(result).isEqualTo(mapperOutput);
    }

  }

  @Test(expected = HttpServerErrorException.class)
  public void getCardPaymentJourneyData_whenServerError() throws IOException {
    var expectedApiUrl = GOV_UK_PAY_BASE_URL + PAYMENTS_ENDPOINT + "/" + GOV_PAY_ID;

    try (InputStream jsonStream = this.getClass().getResourceAsStream("paymentErrorResponse.json")) {
      var jsonResponse = IOUtils.toString(jsonStream);

      mockServer.expect(requestTo(expectedApiUrl))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withServerError()
              .contentType(MediaType.APPLICATION_JSON)
              .body(jsonResponse)
          );

      govukPayClient.getCardPaymentJourneyData(GOV_PAY_ID);

    }
  }

  @Test(expected = HttpClientErrorException.class)
  public void getCardPaymentJourneyData_whenNotFound() throws IOException {
    var expectedApiUrl = GOV_UK_PAY_BASE_URL + PAYMENTS_ENDPOINT + "/" + GOV_PAY_ID;

    try (InputStream jsonStream = this.getClass().getResourceAsStream("paymentErrorResponse.json")) {
      var jsonResponse = IOUtils.toString(jsonStream);

      mockServer.expect(requestTo(expectedApiUrl))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withStatus(HttpStatus.NOT_FOUND)
              .contentType(MediaType.APPLICATION_JSON)
              .body(jsonResponse)
          );

      govukPayClient.getCardPaymentJourneyData(GOV_PAY_ID);

    }
  }

  @Test(expected = HttpClientErrorException.class)
  public void getCardPaymentJourneyData_whenUnauthorised() throws IOException {
    var expectedApiUrl = GOV_UK_PAY_BASE_URL + PAYMENTS_ENDPOINT + "/" + GOV_PAY_ID;


    mockServer.expect(requestTo(expectedApiUrl))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.FORBIDDEN)
        );

    govukPayClient.getCardPaymentJourneyData(GOV_PAY_ID);

  }

  @Test(expected = HttpClientErrorException.class)
  public void getCardPaymentJourneyData_whenTooManyRequests() throws IOException {
    var expectedApiUrl = GOV_UK_PAY_BASE_URL + PAYMENTS_ENDPOINT + "/" + GOV_PAY_ID;


    try (InputStream jsonStream = this.getClass().getResourceAsStream("errorResponse.json")) {
      var jsonResponse = IOUtils.toString(jsonStream);

      mockServer.expect(requestTo(expectedApiUrl))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
              .contentType(MediaType.APPLICATION_JSON)
              .body(jsonResponse)
          );

      govukPayClient.getCardPaymentJourneyData(GOV_PAY_ID);

    }
  }

}
