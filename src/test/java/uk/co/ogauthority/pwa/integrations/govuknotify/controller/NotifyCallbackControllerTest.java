package uk.co.ogauthority.pwa.integrations.govuknotify.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyCallback;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyCallbackService;

@WebMvcTest(controllers = NotifyCallbackController.class)
@Import(PwaMvcTestConfiguration.class)
class NotifyCallbackControllerTest extends AbstractControllerTest {

  @MockBean
  private NotifyCallbackService notifyCallbackServiceMock;

  @Autowired
  private ObjectMapper objectMapper;

  private NotifyCallback notifyCallback;

  @BeforeEach
  void setup() {
    notifyCallback = new NotifyCallback(
        "be0a4c7d-1657-4b83-8771-2a40e7408d67",
        345235,
        NotifyCallback.NotifyCallbackStatus.DELIVERED,
        "test@test.email.co.uk",
        NotifyCallback.NotifyNotificationType.EMAIL,
        Instant.now(),
        Instant.now(),
        Instant.now()
    );
  }

  @Test
  void notifyCallback_invalidToken() throws Exception {

    when(notifyCallbackServiceMock.isTokenValid(anyString())).thenReturn(false);

    mockMvc.perform(post("/notify/callback")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(notifyCallback))
        .header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void notifyCallback_validToken() throws Exception {

    when(notifyCallbackServiceMock.isTokenValid(anyString())).thenReturn(true);

    mockMvc.perform(post("/notify/callback")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(notifyCallback))
        .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());

    verify(notifyCallbackServiceMock, times(1)).handleCallback(any());

  }
}
