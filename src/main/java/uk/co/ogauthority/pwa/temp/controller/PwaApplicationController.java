package uk.co.ogauthority.pwa.temp.controller;

import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.temp.model.contacts.UserOwnerOperatorView;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;

@Controller
@RequestMapping("/application")
public class PwaApplicationController {

  @GetMapping("/1/admin-details")
  public ModelAndView viewAdministrativeDetails(@ModelAttribute("form") AdministrativeDetailsForm administrativeDetailsForm) {
    return new ModelAndView("pwaApplication/temporary/administrativeDetails")
        .addObject("holderCompanyName", "ROYAL DUTCH SHELL");
  }

  @GetMapping("/1/uoo-contacts")
  public ModelAndView viewUserOwnerOperatorContacts() {
    return new ModelAndView("pwaApplication/temporary/uooContacts")
        .addObject("uooList", makeUserOwnerOperatorViews());
  }

  private UserOwnerOperatorView[] makeUserOwnerOperatorViews() {
    var uooA = new UserOwnerOperatorView(
        135432, "Demo Company", "Fivium Ltd\n15 Adam St\nCharing Cross\nLondon WC2N 6AH\n",
        Set.of("User"));

    var uooB = new UserOwnerOperatorView(
        365478, "Another company", "Fivium Ltd\n15 Adam St\nCharing Cross\nLondon WC2N 6AH\n",
        Set.of("Operator"));

    var uooC = new UserOwnerOperatorView(
        83625, "Third company", "Fivium Ltd\n15 Adam St\nCharing Cross\nLondon WC2N 6AH\n",
        Set.of("Owner"));

    var uooD = new UserOwnerOperatorView(
        114234, "Final company", "Fivium Ltd\n15 Adam St\nCharing Cross\nLondon WC2N 6AH\n",
        Set.of("User", "Owner"));
    return new UserOwnerOperatorView[]{uooA, uooB, uooC, uooD};
  }

}
