package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/consultation")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationController {


  private final ConsultationViewService consultationViewService;

  @Autowired
  public ConsultationController(
      ConsultationViewService consultationViewService) {
    this.consultationViewService = consultationViewService;
  }





  @GetMapping
  public ModelAndView renderConsultation(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount) {
    return getConsultationModelAndView(processingContext.getApplicationDetail());
  }


  private ModelAndView getConsultationModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    return new ModelAndView("consultation/consultation")
        .addObject("requestConsultationsUrl",
            ReverseRouter.route(on(ConsultationRequestController.class).renderRequestConsultation(
                pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("consulteeGroupRequestsViews",
            consultationViewService.getConsultationRequestViews(pwaApplicationDetail.getPwaApplication()));
  }


}