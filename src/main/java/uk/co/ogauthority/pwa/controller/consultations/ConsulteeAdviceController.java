package uk.co.ogauthority.pwa.controller.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsulteeAdviceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/consultations")
@PwaAppProcessingPermissionCheck(permissions = { PwaAppProcessingPermission.CONSULTEE_ADVICE })
public class ConsulteeAdviceController {

  private final ConsulteeAdviceService consulteeAdviceService;
  private final AppProcessingBreadcrumbService breadcrumbService;

  @Autowired
  public ConsulteeAdviceController(ConsulteeAdviceService consulteeAdviceService,
                                   AppProcessingBreadcrumbService breadcrumbService) {
    this.consulteeAdviceService = consulteeAdviceService;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderConsulteeAdvice(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext) {

    var consulteeAdviceView = consulteeAdviceService.getConsulteeAdviceView(processingContext);

    var modelAndView = new ModelAndView("consultation/consulteeAdvice")
        .addObject("consulteeAdviceView", consulteeAdviceView)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Consultations");

    return modelAndView;

  }

}
