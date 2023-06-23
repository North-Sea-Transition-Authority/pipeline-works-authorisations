package uk.co.ogauthority.pwa.features.reassignment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/reassign-cases")
public class CaseReassignmentController {

  private final CaseReasignmentService caseReasignmentService;

  private final WorkflowAssignmentService workflowAssignmentService;

  private final AssignCaseOfficerService assignCaseOfficerService;

  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public CaseReassignmentController(CaseReasignmentService caseReasignmentService,
                                    WorkflowAssignmentService workflowAssignmentService,
                                    AssignCaseOfficerService assignCaseOfficerService,
                                    PwaApplicationDetailService pwaApplicationDetailService) {
    this.caseReasignmentService = caseReasignmentService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignCaseOfficerService = assignCaseOfficerService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }


  @GetMapping
  public ModelAndView renderCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             RedirectAttributes redirectAttributes,
                                             @ModelAttribute("filterForm") CaseReassignmentFilterForm caseReassignmentFilterForm) {
    var workItems = caseReasignmentService.getReassignableWorkAreaItems(
        caseReassignmentFilterForm.getCaseOfficerPersonId());
    return new ModelAndView("reassignment/reassignment")
        .addObject("assignableCases", workItems)
        .addObject("filterForm", caseReassignmentFilterForm)
        .addObject("form", new CaseReassignmentSelectorForm())
        .addObject("filterURL",
            ReverseRouter.route(on(CaseReassignmentController.class).filterCaseReassignment(
                httpServletRequest,
                authenticatedUserAccount,
                caseReassignmentFilterForm,
                redirectAttributes)))
        .addObject("clearURL",
            ReverseRouter.route(on(CaseReassignmentController.class).filterCaseReassignment(
                httpServletRequest,
                authenticatedUserAccount,
                null,
                redirectAttributes)))
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(null, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)));
  }

  @PostMapping("/filter")
  public ModelAndView filterCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("filterForm") CaseReassignmentFilterForm caseReassignmentFilterForm,
                                             RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("filterForm", caseReassignmentFilterForm);
    return ReverseRouter.redirect(on(CaseReassignmentController.class).renderCaseReassignment(
        httpServletRequest,
        authenticatedUserAccount,
        redirectAttributes,
        caseReassignmentFilterForm));
  }

  @PostMapping
  public ModelAndView submitCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                             RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("form", caseReassignmentSelectorForm);
    return ReverseRouter.redirect(on(CaseReassignmentController.class).renderSelectNewAssignee(
        httpServletRequest,
        authenticatedUserAccount,
        caseReassignmentSelectorForm,
        redirectAttributes));
  }



  @GetMapping("/select")
  public ModelAndView renderSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                              RedirectAttributes redirectAttributes) {
    return new ModelAndView("reassignment/chooseAssignee")
        .addObject("form", caseReassignmentSelectorForm)
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(null, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)));
  }

  @PostMapping("/select")
  public ModelAndView submitSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                              RedirectAttributes redirectAttributes) {

    for (var detailId : caseReassignmentSelectorForm.getSelectedCases()) {
      var applicationDetail = pwaApplicationDetailService.getDetailById(detailId);

      assignCaseOfficerService.assignCaseOfficer(
          applicationDetail,
          new PersonId(caseReassignmentSelectorForm.getCaseOfficerAssignee()),
          authenticatedUserAccount);
    }
    return ReverseRouter.redirect(on(CaseReassignmentController.class).renderCaseReassignment(
        httpServletRequest,
        authenticatedUserAccount,
        redirectAttributes,
        null));
  }
}
