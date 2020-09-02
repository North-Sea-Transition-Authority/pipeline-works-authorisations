package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.applicationupdate.RequestApplicationUpdateController;
import uk.co.ogauthority.pwa.controller.consultations.ConsultationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
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
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public CaseManagementController(PwaAppProcessingPermissionService pwaAppProcessingPermissionService,
                                  ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.pwaAppProcessingPermissionService = pwaAppProcessingPermissionService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }


  @GetMapping
  public ModelAndView renderCaseManagement(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount) {
    return getCaseManagementModelAndView(processingContext.getApplicationDetail(), authenticatedUserAccount);
  }


  private ModelAndView getCaseManagementModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                     AuthenticatedUserAccount userAccount) {
    var appId = pwaApplicationDetail.getMasterPwaApplicationId();
    var applicationType = pwaApplicationDetail.getPwaApplicationType();

    var canRequestApplicationUpdate = pwaAppProcessingPermissionService.getProcessingPermissions(userAccount)
        .contains(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE)
        && !applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail);

    return new ModelAndView("appprocessing/caseManagement")
        .addObject("requestUpdateUrl", ReverseRouter.route(on(RequestApplicationUpdateController.class)
            .renderRequestUpdate(appId, applicationType, null, null, null)))
        .addObject("consultationUrl", ReverseRouter.route(on(ConsultationController.class)
            .renderConsultation(appId, applicationType, null, null)))
        .addObject("assignCaseOfficerUrl", ReverseRouter.route(on(AssignCaseOfficerController.class)
            .renderAssignCaseOfficer(appId, applicationType, null, null, null)))
        .addObject("hasAssignCaseOfficerPermission",
            pwaAppProcessingPermissionService.getProcessingPermissions(userAccount).contains(
                PwaAppProcessingPermission.ASSIGN_CASE_OFFICER))
        .addObject("canRequestApplicationUpdate", canRequestApplicationUpdate
            );
  }


}