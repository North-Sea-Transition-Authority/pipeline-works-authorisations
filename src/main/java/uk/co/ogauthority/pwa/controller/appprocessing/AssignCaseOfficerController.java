package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/reassign-case-officer")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.ASSIGN_CASE_OFFICER})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class AssignCaseOfficerController {

  private final AssignCaseOfficerService assignCaseOfficerService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final ControllerHelperService controllerHelperService;


  @Autowired
  public AssignCaseOfficerController(
      AssignCaseOfficerService assignCaseOfficerService,
      WorkflowAssignmentService workflowAssignmentService,
      ControllerHelperService controllerHelperService) {
    this.assignCaseOfficerService = assignCaseOfficerService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.controllerHelperService = controllerHelperService;
  }



  @GetMapping
  public ModelAndView renderAssignCaseOfficer(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             PwaAppProcessingContext processingContext,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("form") AssignCaseOfficerForm form) {
    return getAssignCaseOfficerModelAndView(processingContext);
  }


  @PostMapping
  public ModelAndView postAssignCaseOfficer(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") AssignCaseOfficerForm form,
                                            BindingResult bindingResult) {

    bindingResult = assignCaseOfficerService.validate(form, bindingResult, processingContext.getPwaApplication());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAssignCaseOfficerModelAndView(processingContext), () -> {
          assignCaseOfficerService.assignCaseOfficer(
              form.getCaseOfficerPerson(),  processingContext.getApplicationDetail(), authenticatedUserAccount);
          return ReverseRouter.redirect(on(CaseManagementController.class).renderCaseManagement(
              applicationId, pwaApplicationType, AppProcessingTab.TASKS, null, null));
        });

  }


  private ModelAndView getAssignCaseOfficerModelAndView(PwaAppProcessingContext appProcessingContext) {

    var pwaApplicationDetail = appProcessingContext.getApplicationDetail();

    String cancelUrl = ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            AppProcessingTab.TASKS,
            null,
            null));

    return new ModelAndView("appprocessing/assignCaseOfficer")
        .addObject("errorList", List.of())
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)))
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView());

  }


}