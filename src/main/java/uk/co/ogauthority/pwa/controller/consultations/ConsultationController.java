package uk.co.ogauthority.pwa.controller.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/consultation")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationController {

  @Autowired
  public ConsultationController() {
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
        .addObject("requestConsultationsUrl", "")
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef());
  }


}