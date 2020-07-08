package uk.co.ogauthority.pwa.service.controllers.typemismatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;

@Controller
@RequestMapping("/test-controller")
public class ControllerHelperServiceTypeMismatchController {

  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ControllerHelperServiceTypeMismatchController(ControllerHelperService controllerHelperService) {
    this.controllerHelperService = controllerHelperService;
  }

  @PostMapping("/type-mismatch-test")
  public ModelAndView post(@ModelAttribute("form") TypeMismatchTestForm form,
                           BindingResult bindingResult) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, new ModelAndView(), ModelAndView::new);

  }

}
