package uk.co.ogauthority.pwa.temp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/application/{applicationId}/technical-drawings")
public class TechnicalDrawingsController {

  @GetMapping
  public ModelAndView viewTechnicalDrawings(@PathVariable Integer applicationId) {
    return new ModelAndView("pwaApplication/temporary/technicalDrawings/drawings");
  }

}
