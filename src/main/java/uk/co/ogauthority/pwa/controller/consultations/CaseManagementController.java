package uk.co.ogauthority.pwa.controller.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Controller
@RequestMapping
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class CaseManagementController {

  @Autowired
  public CaseManagementController() {
  }


  @GetMapping("/case-management")
  public ModelAndView renderCaseManagement(AuthenticatedUserAccount authenticatedUserAccount) {
    return getCaseManagementModelAndView();
  }


  private ModelAndView getCaseManagementModelAndView() {
    return new ModelAndView("consultation/caseManagement");
  }


}