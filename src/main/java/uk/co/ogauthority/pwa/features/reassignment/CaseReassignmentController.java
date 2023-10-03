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
                                             @ModelAttribute("form") CaseReassignmentCasesForm caseReassignmentCasesForm,
                                             BindingResult bindingResult,
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
        .addObject("form", caseReassignmentCasesForm)
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
                null,
                null,
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
        null,
        null,
        caseReassignmentFilterForm),paramMap);
  }

  @PostMapping
  public ModelAndView submitCaseReassignment(HttpServletRequest httpServletRequest,
                                             AuthenticatedUserAccount authenticatedUserAccount,
                                             @ModelAttribute("form") CaseReassignmentCasesForm caseReassignmentCasesForm,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes) {
    checkUserPrivilege(authenticatedUserAccount);

    var validatedBindingResult = caseReassignmentService.validateCasesForm(caseReassignmentCasesForm, bindingResult);

    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(caseReassignmentCasesForm));

    return controllerHelperService.checkErrorsAndRedirect(
        validatedBindingResult,
        renderCaseReassignment(httpServletRequest, authenticatedUserAccount, redirectAttributes, caseReassignmentCasesForm,
            validatedBindingResult, new CaseReassignmentFilterForm()),
        () -> ReverseRouter.redirectWithQueryParamMap(on(CaseReassignmentController.class).renderSelectNewAssignee(
        httpServletRequest,
        authenticatedUserAccount,
        caseReassignmentCasesForm,
        null,
        validatedBindingResult,
        redirectAttributes), paramMap)
    );
  }


  @GetMapping("/select-case-officer")
  public ModelAndView renderSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              CaseReassignmentCasesForm caseReassignmentCasesForm,
                                              @ModelAttribute("form") CaseReassignmentOfficerForm caseReassignmentOfficerForm,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {
    checkUserPrivilege(authenticatedUserAccount);
    var selectedIds = caseReassignmentCasesForm.getSelectedApplicationIds()
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
                        null,
                        redirectAttributes)))
        .addObject("selectedPwas", selectedPwas)
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(null, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)))
        .addObject("selectedCases", caseReassignmentCasesForm);
  }

  @PostMapping("/select-case-officer")
  public ModelAndView submitSelectNewAssignee(HttpServletRequest httpServletRequest,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              CaseReassignmentCasesForm caseReassignmentCasesForm,
                                              @ModelAttribute("form") CaseReassignmentOfficerForm caseReassignmentOfficerForm,
                                              RedirectAttributes redirectAttributes,
                                              BindingResult bindingResult) {
    checkUserPrivilege(authenticatedUserAccount);
    var validatedBindingResult = caseReassignmentService.validateOfficerForm(caseReassignmentOfficerForm, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        validatedBindingResult,
        renderFormError(caseReassignmentCasesForm, validatedBindingResult, authenticatedUserAccount),
        () -> {
          /**
           * TODO: PWA2022-74 This is required as a result of the PwaStringToCollectionConverter
           * The converter forces comma based inputs to be returned as a string instead of an array/list
           * This is necessary for free text entry into search selectors that could include commas.
           * But messes with Spring binding of lists of strings
           */
          var selectedIds = caseReassignmentCasesForm.getSelectedApplicationIds()
              .stream()
              .map(appIds -> appIds.split(","))
              .flatMap(Arrays::stream)
              .map(Integer::valueOf)
              .collect(Collectors.toList());
          for (var appId : selectedIds) {
            var applicationDetail = pwaApplicationDetailService.getTipDetailByAppId(appId);

            assignCaseOfficerService.assignCaseOfficer(
                applicationDetail,
                new PersonId(caseReassignmentOfficerForm.getAssignedCaseOfficerPersonId()),
                authenticatedUserAccount);
          }
          FlashUtils.success(
              redirectAttributes,
              "Successfully reassigned PWAs"
          );
          return ReverseRouter.redirect(on(CaseReassignmentController.class).renderCaseReassignment(
              httpServletRequest,
              authenticatedUserAccount,
              redirectAttributes,
              null,
              null,
              null));
        });
  }

  private void checkUserPrivilege(AuthenticatedUserAccount authenticatedUser) {
    if (!authenticatedUser.hasPrivilege(PwaUserPrivilege.PWA_MANAGER)) {
      throw new AccessDeniedException("User does not have access to bulk reassignment");
    }
  }

  private ModelAndView renderFormError(CaseReassignmentCasesForm caseReassignmentCasesForm,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {
    caseReassignmentCasesForm.setSelectedApplicationIds(
        caseReassignmentCasesForm.getSelectedApplicationIds()
            .stream()
            .map(appIds -> appIds.split(","))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList()));

    return renderSelectNewAssignee(
        null,
        user,
        caseReassignmentCasesForm,
        null,
        bindingResult,
        null
    );
  }
}
