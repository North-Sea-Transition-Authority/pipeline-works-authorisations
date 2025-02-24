package uk.co.ogauthority.pwa.controller.feedback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.features.feedback.FeedbackService;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(FeedbackController.class)
@Import(PwaMvcTestConfiguration.class)
class FeedbackControllerTest extends AbstractControllerTest {

  @MockBean
  FeedbackService feedbackService;

  private static final AuthenticatedUserAccount AUTHENTICATED_USER = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  private static final AuthenticatedUserAccount UNAUTHENTICATED_USER = AuthenticatedUserAccountTestUtil.createNoPrivUserAccount(1);

  private static final int APP_DETAIL_ID = 1;

  @Test
  void getFeedback_whenAuthenticatedAndApplicationDetailId_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.of(10), null, null)))
        .with(user(AUTHENTICATED_USER))
    )
        .andExpect(status().isOk());
  }

  @Test
  void getFeedback_whenAuthenticatedAndNoApplicationDetailId_thenOk() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null)))
                .with(user(AUTHENTICATED_USER))
        )
        .andExpect(status().isOk());
  }

  @Test
  void getFeedback_whenUnauthenticatedAndApplicationDetailId_thenForbidden() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.of(10), null, null)))
                .with(user(UNAUTHENTICATED_USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void getFeedback_whenUnauthenticatedAndNoApplicationDetailId_thenForbidden() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null)))
        .with(user(UNAUTHENTICATED_USER))
    )
        .andExpect(status().isForbidden());
  }

  @Test
  void getFeedback_whenUnauthenticated_thenForbidden() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null)))
                .with(user(UNAUTHENTICATED_USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void submitFeedback_whenNoFormErrors_thenRedirect() throws Exception {

    var form = new FeedbackForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(feedbackService.validateFeedbackForm(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.of(APP_DETAIL_ID), null, null)))
            .with(user(AUTHENTICATED_USER))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name(String.format("redirect:%s", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))));

    verify(feedbackService, times(1)).saveFeedback(APP_DETAIL_ID, form, AUTHENTICATED_USER.getLinkedPerson());
  }

  @Test
  void submitFeedback_whenFormErrors_thenRedirectToFeedbackPage() throws Exception {

    var form = new FeedbackForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(feedbackService.validateFeedbackForm(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null)))
            .with(user(AUTHENTICATED_USER))
            .with(csrf())
        )
        .andExpect(status().isOk());

    verify(feedbackService, never()).saveFeedback(any(), any(), any());
  }

  @Test
  void submitFeedback_whenUnauthenticated_thenForbidden() throws Exception {

    var form = new FeedbackForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(feedbackService.validateFeedbackForm(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null)))
                .with(user(UNAUTHENTICATED_USER))
                .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

}