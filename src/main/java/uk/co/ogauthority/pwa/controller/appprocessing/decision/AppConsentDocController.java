package uk.co.ogauthority.pwa.controller.appprocessing.decision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.decision.ConsentDocumentUrlFactory;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/consent-document")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
public class AppConsentDocController {

  private final AppProcessingBreadcrumbService breadcrumbService;

  @Autowired
  public AppConsentDocController(AppProcessingBreadcrumbService breadcrumbService) {
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             PwaAppProcessingContext processingContext,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/decision/consentDocumentEditor")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("docInstanceExists", false)
        .addObject("consentDocumentUrlFactory", new ConsentDocumentUrlFactory(processingContext.getPwaApplication()));

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Consent document");

    return modelAndView;

  }

  @PostMapping
  public ModelAndView postConsentDocEditor(@PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   PwaAppProcessingContext processingContext,
                                   AuthenticatedUserAccount authenticatedUserAccount,
                                   RedirectAttributes redirectAttributes) {

    // todo load document

    FlashUtils.info(redirectAttributes, "Document loaded");
    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

  }

  @GetMapping("/reload")
  public ModelAndView renderReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    return new ModelAndView("pwaApplication/appProcessing/decision/reloadDocumentConfirm")
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("consentDocumentUrlFactory", new ConsentDocumentUrlFactory(processingContext.getPwaApplication()));

  }

  @PostMapping("/reload")
  public ModelAndView postReloadDocument(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    // todo clear instance and load

    FlashUtils.info(redirectAttributes, "Document reloaded");

    return ReverseRouter.redirect(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

  }

}
