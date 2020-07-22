package uk.co.ogauthority.pwa.controller.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Controller
@RequestMapping
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationController {

  @Autowired
  public ConsultationController() {
  }


  @GetMapping("/consultation")
  public ModelAndView renderConsultation(AuthenticatedUserAccount authenticatedUserAccount) {
    return getConsultationModelAndView();
  }


  private ModelAndView getConsultationModelAndView() {
    return new ModelAndView("consultation/consultation")
        .addObject("requestConsultationsUrl", "");
  }


}