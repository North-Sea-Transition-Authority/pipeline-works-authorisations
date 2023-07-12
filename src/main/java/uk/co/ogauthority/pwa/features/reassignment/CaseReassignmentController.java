package uk.co.ogauthority.pwa.features.reassignment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/reassign-cases")
public class CaseReassignmentController {

  private final CaseReassignmentService caseReassignmentService;

  private final WorkflowAssignmentService workflowAssignmentService;

  private final AssignCaseOfficerService assignCaseOfficerService;

  private final PwaApplicationDetailService pwaApplicationDetailService;

  private final ControllerHelperService controllerHelperService;

  @Autowired
  public CaseReassignmentController(CaseReassignmentService caseReassignmentService,
                                    WorkflowAssignmentService workflowAssignmentService,
                                    AssignCaseOfficerService assignCaseOfficerService,
                                    PwaApplicationDetailService pwaApplicationDetailService,
                                    ControllerHelperService controllerHelperService) {
    this.caseReassignmentService = caseReassignmentService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignCaseOfficerService = assignCaseOfficerService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             RedirectAttributes redirectAttributes,
                                             @ModelAttribute("filterForm") CaseReassignmentFilterForm caseReassignmentFilterForm) {
    checkUserPrivilege(authenticatedUserAccount);
    var workItems = caseReassignmentService.findAllReassignableCases();

    var caseOfficerCandidates = workItems.stream()
        .map(item -> Map.entry(String.valueOf(item.getAssignedCaseOfficerPersonId()), item.getAssignedCaseOfficer()))
        .distinct()
        .sorted(Map.Entry.comparingByValue())
        .collect(StreamUtils.toLinkedHashMap(
            Map.Entry::getKey,
            Map.Entry::getValue));

    if (caseReassignmentFilterForm.getCaseOfficerPersonId() != null) {
      workItems = workItems.stream()
          .filter(view -> view.getAssignedCaseOfficerPersonId().equals(caseReassignmentFilterForm.getCaseOfficerPersonId()))
          .collect(Collectors.toList());
    }

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
            ReverseRouter.route(on(CaseReassignmentController.class).renderCaseReassignment(
                httpServletRequest,
                authenticatedUserAccount,
                redirectAttributes,
                new CaseReassignmentFilterForm())))
        .addObject("caseOfficerCandidates", caseOfficerCandidates);
  }

  @PostMapping("/filter")
  public ModelAndView filterCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("filterForm") CaseReassignmentFilterForm caseReassignmentFilterForm,
                                             RedirectAttributes redirectAttributes) {
    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(caseReassignmentFilterForm));

    return ReverseRouter.redirectWithQueryParamMap(on(CaseReassignmentController.class).renderCaseReassignment(
        httpServletRequest,
        authenticatedUserAccount,
        redirectAttributes,
        caseReassignmentFilterForm),paramMap);
  }

  @PostMapping
  public ModelAndView submitCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                             RedirectAttributes redirectAttributes) {
    checkUserPrivilege(authenticatedUserAccount);
    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(caseReassignmentSelectorForm));

    return ReverseRouter.redirectWithQueryParamMap(on(CaseReassignmentController.class).renderSelectNewAssignee(
        httpServletRequest,
        authenticatedUserAccount,
        caseReassignmentSelectorForm,
        null,
        redirectAttributes), paramMap);
  }



  @GetMapping("/select-case-officer")
  public ModelAndView renderSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {
    checkUserPrivilege(authenticatedUserAccount);
    var selectedIds = caseReassignmentSelectorForm.getSelectedApplicationIds()
        .stream()
        .map(Integer::valueOf)
        .collect(Collectors.toList());
    var selectedPwas = caseReassignmentService.findAllCasesByApplicationId(selectedIds);
    return new ModelAndView("reassignment/chooseAssignee")
        .addObject("submitUrl",
            ReverseRouter.route(on(CaseReassignmentController.class).renderSelectNewAssignee(
                        httpServletRequest,
                        authenticatedUserAccount,
                        null,
                        null,
                        redirectAttributes)))
        .addObject("selectedPwas", selectedPwas)
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(null, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)));
  }

  @PostMapping("/select-case-officer")
  public ModelAndView submitSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                              RedirectAttributes redirectAttributes,
                                              BindingResult bindingResult) {
    checkUserPrivilege(authenticatedUserAccount);
    var validatedBindingResult = caseReassignmentService.validateForm(caseReassignmentSelectorForm, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        validatedBindingResult,
        renderFormError(caseReassignmentSelectorForm, validatedBindingResult, authenticatedUserAccount),
        () -> {
          /**
           * TODO: PWA2022-74 This is required as a result of the PwaStringToCollectionConverter
           * The converter forces comma based inputs to be returned as a string instead of an array/list
           * This is necessary for free text entry into search selectors that could include commas.
           * But messes with Spring binding of lists of strings
           */
          var selectedIds = caseReassignmentSelectorForm.getSelectedApplicationIds()
              .stream()
              .map(appIds -> appIds.split(","))
              .flatMap(Arrays::stream)
              .map(Integer::valueOf)
              .collect(Collectors.toList());
          for (var appId : selectedIds) {
            var applicationDetail = pwaApplicationDetailService.getDetailById(appId);

            assignCaseOfficerService.assignCaseOfficer(
                applicationDetail,
                new PersonId(caseReassignmentSelectorForm.getAssignedCaseOfficerPersonId()),
                authenticatedUserAccount);
          }
          FlashUtils.success(
              redirectAttributes,
              "Succesfully reassigned PWAs"
          );
          return ReverseRouter.redirect(on(CaseReassignmentController.class).renderCaseReassignment(
              httpServletRequest,
              authenticatedUserAccount,
              redirectAttributes,
              null));
        });
  }

  private void checkUserPrivilege(AuthenticatedUserAccount authenticatedUser) {
    if (!authenticatedUser.hasPrivilege(PwaUserPrivilege.PWA_MANAGER)) {
      throw new AccessDeniedException("User does not have access to bulk reassignment");
    }
  }

  private ModelAndView renderFormError(CaseReassignmentSelectorForm caseReassignmentSelectorForm,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {
    caseReassignmentSelectorForm.setSelectedApplicationIds(
        caseReassignmentSelectorForm.getSelectedApplicationIds()
            .stream()
            .map(appIds -> appIds.split(","))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList()));

    return renderSelectNewAssignee(
        null,
        user,
        caseReassignmentSelectorForm,
        bindingResult,
        null
    );
  }
}
