package uk.co.ogauthority.pwa.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.notify.NotifyCallbackController;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.model.notify.NotifyCallback;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.notify.NotifyCallbackService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = NotifyCallbackController.class)
public class NotifyCallbackControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  private NotifyCallbackService notifyCallbackServiceMock;

  @Autowired
  private ObjectMapper objectMapper;

  private NotifyCallback notifyCallback;

  @Before
  public void setup() {
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
  public void notifyCallback_invalidToken() throws Exception {

    when(notifyCallbackServiceMock.isTokenValid(anyString())).thenReturn(false);

    mockMvc.perform(post("/notify/callback")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(notifyCallback))
        .header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void notifyCallback_validToken() throws Exception {

    when(notifyCallbackServiceMock.isTokenValid(anyString())).thenReturn(true);

    mockMvc.perform(post("/notify/callback")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(notifyCallback))
        .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());

    verify(notifyCallbackServiceMock, times(1)).handleCallback(any());

  }
}
