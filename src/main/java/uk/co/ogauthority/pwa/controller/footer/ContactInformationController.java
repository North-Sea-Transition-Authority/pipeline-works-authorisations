package uk.co.ogauthority.pwa.controller.footer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.enums.ServiceContactDetail;

@Controller
@RequestMapping("/contact-information")
public class ContactInformationController {


  @GetMapping()
  public ModelAndView getContactInformation(AuthenticatedUserAccount authenticatedUserAccount) {

    var contactDetails = Arrays.stream(ServiceContactDetail.values())
        .filter(ServiceContactDetail::isShownOnContactPage)
        .sorted(Comparator.comparing(ServiceContactDetail::getDisplayOrder))
        .collect(Collectors.toList());

    return new ModelAndView("footer/contactInformation")
        .addObject("contacts", contactDetails);
  }


}
