package uk.co.ogauthority.pwa.controller.feedback;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.features.feedback.FeedbackService;
import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.util.FlashUtils;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

  private final FeedbackService feedbackService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public FeedbackController(FeedbackService feedbackService,
                            ControllerHelperService controllerHelperService) {
    this.feedbackService = feedbackService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView getFeedback(@RequestParam(required = false) Optional<Integer> pwaApplicationDetailId,
                                  AuthenticatedUserAccount user,
                                  @ModelAttribute("form") FeedbackForm form) {

    return getFeedbackModelAndView(form);
  }

  @PostMapping
  public ModelAndView submitFeedback(@RequestParam(required = false) Integer pwaApplicationDetailId,
                                     AuthenticatedUserAccount user,
                                     @ModelAttribute("form") FeedbackForm form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {

    bindingResult = feedbackService.validateFeedbackForm(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getFeedbackModelAndView(form),
        () -> {
          if (pwaApplicationDetailId != null) {
            feedbackService.saveFeedback(pwaApplicationDetailId, form, user.getLinkedPerson());
          } else {
            feedbackService.saveFeedback(form, user.getLinkedPerson());
          }
          FlashUtils.success(redirectAttributes, "Your feedback has been submitted");
          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
        }
    );
  }


  private ModelAndView getFeedbackModelAndView(FeedbackForm feedbackForm) {
    return new ModelAndView("/feedback/feedback")
        .addObject("form", feedbackForm)
        .addObject("cancelUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("serviceRatings", ServiceFeedbackRating.getAllAsMap())
        .addObject("feedbackCharacterLimit", String.valueOf(FeedbackService.FEEDBACK_CHARACTER_LIMIT))
        .addObject("showBackLink",feedbackForm.getPwaApplicationDetailId() == null);
  }


}
