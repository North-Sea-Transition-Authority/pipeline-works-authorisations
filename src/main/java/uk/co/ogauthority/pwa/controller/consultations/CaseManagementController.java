package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class CaseManagementController {

  private final PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @Autowired
  public CaseManagementController(
      PwaAppProcessingPermissionService pwaAppProcessingPermissionService) {
    this.pwaAppProcessingPermissionService = pwaAppProcessingPermissionService;
  }


  @GetMapping
  public ModelAndView renderCaseManagement(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount) {
    return getCaseManagementModelAndView(processingContext, authenticatedUserAccount);
  }


  private ModelAndView getCaseManagementModelAndView(PwaAppProcessingContext appProcessingContext, AuthenticatedUserAccount userAccount) {

    var detail = appProcessingContext.getApplicationDetail();

    return new ModelAndView("consultation/caseManagement")
        .addObject("consultationUrl",
            ReverseRouter.route(on(ConsultationController.class).renderConsultation(
                detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)))
        .addObject("assignCaseOfficerUrl",
            ReverseRouter.route(on(AssignCaseOfficerController.class).renderAssignCaseOfficer(
                detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null, null)))
        .addObject("hasAssignCaseOfficerPermission", pwaAppProcessingPermissionService.getProcessingPermissions(userAccount).contains(
            PwaAppProcessingPermission.ASSIGN_CASE_OFFICER))
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView());
  }


}