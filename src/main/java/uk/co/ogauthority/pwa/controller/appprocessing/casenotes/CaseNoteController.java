package uk.co.ogauthority.pwa.controller.appprocessing.casenotes;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/case-note")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.ADD_CASE_NOTE)
public class CaseNoteController {

  private final CaseNoteService caseNoteService;
  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public CaseNoteController(CaseNoteService caseNoteService,
                            AppProcessingBreadcrumbService appProcessingBreadcrumbService,
                            ControllerHelperService controllerHelperService) {
    this.caseNoteService = caseNoteService;
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderAddCaseNote(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @ModelAttribute("form") AddCaseNoteForm form,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    return getAddCaseNoteModelAndView(processingContext);

  }

  private ModelAndView getAddCaseNoteModelAndView(PwaAppProcessingContext processingContext) {

    var app = processingContext.getPwaApplication();

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/caseNotes/addCaseNote")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("caseManagementUrl", CaseManagementUtils.routeCaseManagement(app));

    appProcessingBreadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Add case note");

    return modelAndView;

  }

  @PostMapping
  public ModelAndView postAddCaseNote(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      @Valid @ModelAttribute("form") AddCaseNoteForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount authenticatedUserAccount,
                                      RedirectAttributes redirectAttributes) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddCaseNoteModelAndView(processingContext), () -> {

      caseNoteService.createCaseNote(processingContext.getPwaApplication(), form.getNoteText(), authenticatedUserAccount.getLinkedPerson());

      FlashUtils.info(redirectAttributes, "Case note added");

      return CaseManagementUtils.redirectCaseManagement(processingContext.getPwaApplication());

    });

  }

}
