package uk.co.ogauthority.pwa.features.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AnalyticsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

  private final ClientHttpRequestFactory requestFactory;
  private final AnalyticsConfigurationProperties configuration;
  private final ObjectMapper objectMapper;

  @Autowired
  public AnalyticsService(ClientHttpRequestFactory requestFactory,
                          AnalyticsConfigurationProperties configuration,
                          ObjectMapper objectMapper) {
    this.requestFactory = requestFactory;
    this.configuration = configuration;
    this.objectMapper = objectMapper;
  }

  @Async
  public void sendAnalyticsEvent(
      Optional<String> cookieClientId,
      AnalyticsEventCategory eventCategory
  ) {
    sendEvent(cookieClientId, eventCategory, Map.of());
  }


  @Async
  public void sendAnalyticsEvent(
      Optional<String> cookieClientId,
      AnalyticsEventCategory eventCategory,
      Map<String, String> paramMap
  ) {
    sendEvent(cookieClientId, eventCategory, paramMap);
  }

  private void sendEvent(
      Optional<String> cookieClientId,
      AnalyticsEventCategory eventCategory,
      Map<String, String> paramMap
  ) {

    if (!configuration.getConfig().isEnabled()) {
      return;
    }

    try {

      // can't use randomly generated UUID as GA4 doesn't accept client ids it hasn't seen before
      var clientId = cookieClientId
          .orElse("anonymous_user");

      var restTemplate = new RestTemplateBuilder()
          .setConnectTimeout(Duration.ofSeconds(configuration.getConfig().getConnectionTimeoutSeconds()))
          .setReadTimeout(Duration.ofSeconds(configuration.getConfig().getConnectionTimeoutSeconds()))
          .defaultHeader("User-Agent", configuration.getConfig().getUserAgent())
          .requestFactory(() -> requestFactory)
          .build();

      sendEventForTag(
          restTemplate,
          configuration.getProperties().getAppTag(),
          configuration.getConfig().getAppTagApiSecret(),
          clientId,
          eventCategory,
          paramMap
      );

      sendEventForTag(
          restTemplate,
          configuration.getProperties().getGlobalTag(),
          configuration.getConfig().getGlobalTagApiSecret(),
          clientId,
          eventCategory,
          paramMap
      );

    } catch (Exception e) {
      LOGGER.error("Error sending Google Analytics event. Response was still served to user", e);
    }
  }

  private void sendEventForTag(RestTemplate restTemplate,
                               String trackingId,
                               String apiSecret,
                               String clientId,
                               AnalyticsEventCategory eventCategory,
                               Map<String, String> paramMap) throws JsonProcessingException {

    var payloadString = constructPayloadJsonString(clientId, eventCategory, paramMap);

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    var request = new HttpEntity<>(payloadString, headers);

    var uriTemplate = UriComponentsBuilder.fromHttpUrl(configuration.getConfig().getEndpointUrl())
        .queryParam("measurement_id", trackingId)
        .queryParam("api_secret", apiSecret)
        .build()
        .toUriString();

    var response = restTemplate.postForEntity(uriTemplate, request, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      LOGGER.error(
          "Non 2xx code returned for Google Analytics event send (was {}). Response was still served to user.",
          response.getStatusCodeValue()
      );
    }

  }

  private String constructPayloadJsonString(String clientId,
                                            AnalyticsEventCategory eventCategory,
                                            Map<String, String> paramMap) throws JsonProcessingException {

    var event = new AnalyticsEvent.Builder(eventCategory.name());

    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      
      event = event.withParam(entry.getKey(), entry.getValue());
      
    }

    var payload = new AnalyticsPayload(clientId, List.of(event.build()));
    return objectMapper.writeValueAsString(payload);

  }

}