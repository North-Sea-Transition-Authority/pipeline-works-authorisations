package uk.co.ogauthority.pwa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;

@Configuration
public class FeedbackConfiguration {

  @Bean
  FeedbackClientService feedbackClientService(ObjectMapper objectMapper,
                                              @Value("${fms.url.base}") String baseUrl,
                                              @Value("${fms.http.connectTimeout}") Long duration,
                                              @Value("${fms.url.saveFeedback}") String saveFeedbackPostUrl,
                                              @Value("${fms.service.name}") String serviceName,
                                              @Value("${fms.auth.presharedKey}") String presharedKey) {
    return new FeedbackClientService(objectMapper, baseUrl, duration, saveFeedbackPostUrl, serviceName, presharedKey);
  }

}