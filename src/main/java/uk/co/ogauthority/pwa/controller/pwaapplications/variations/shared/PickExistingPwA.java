package uk.co.ogauthority.pwa.controller.pwaapplications.variations.shared;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationTypePathUrl}/pick-pwa")
public class PickExistingPwA {


  @GetMapping
  public ModelAndView renderPickPwA(@PathVariable("applicationTypePathUrl")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {
    throw new RuntimeException("MH:" + pwaApplicationType);

  }


  @PostMapping
  public ModelAndView pickPwA(@PathVariable("applicationTypePathUrl")
                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType) {

    throw new RuntimeException("MH:" + pwaApplicationType);
  }


}
