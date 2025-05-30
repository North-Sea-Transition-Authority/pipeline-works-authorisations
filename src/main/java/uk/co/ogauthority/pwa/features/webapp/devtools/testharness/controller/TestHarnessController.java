package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.GenerateVariationApplicationForm;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessUserRetrievalService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.quartzjob.TestHarnessJobCreationService;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.util.RouteUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

/**
 * Controller to allow dev access to the screen for patching in pwa applications
 * Not for production.
 */
@Controller
@Profile("test-harness")
@RequestMapping("/test-harness")
public class TestHarnessController {

  private final TestHarnessService testHarnessService;
  private final TestHarnessJobCreationService testHarnessJobCreationService;
  private final PwaTeamService pwaTeamService;
  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public TestHarnessController(TestHarnessService testHarnessService,
                               TestHarnessJobCreationService testHarnessJobCreationService,
                               PwaTeamService pwaTeamService,
                               PickedPwaRetrievalService pickedPwaRetrievalService,
                               TestHarnessUserRetrievalService testHarnessUserRetrievalService,
                               ControllerHelperService controllerHelperService) {
    this.testHarnessService = testHarnessService;
    this.testHarnessJobCreationService = testHarnessJobCreationService;
    this.pwaTeamService = pwaTeamService;
    this.pickedPwaRetrievalService = pickedPwaRetrievalService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping("/generate-application")
  public ModelAndView renderGenerateApplication(@ModelAttribute("form") GenerateApplicationForm form) {
    return getGenerateApplicationModelAndView();
  }

  @PostMapping("/generate-application")
  public ModelAndView postGenerateApplication(@ModelAttribute("form") GenerateApplicationForm form,
                                              BindingResult bindingResult) {

    var validatedBindingResult = testHarnessService.validateGenerateApplicationForm(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
        getGenerateApplicationModelAndView(), () -> {

          if (form.getApplicationType().getPwaConsentType().equals(PwaConsentType.INITIAL_PWA)) {
            testHarnessJobCreationService.scheduleInitialGenerateApplicationJob(form);
            return RouteUtils.redirectWorkArea();

          } else {
            return ReverseRouter.redirect(on(TestHarnessController.class).renderSelectPwa(
                form.getApplicationType().name(),
                form.getPipelineQuantity(),
                form.getApplicationStatus().name(),
                form.getAssignedCaseOfficerId(),
                form.getApplicantWuaId(),
                form.getResourceType().name(),
                null));
          }
        });

  }

  private ModelAndView getGenerateApplicationModelAndView() {

    var applicationTypeMap = PwaApplicationType.stream()
        .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationType::getDisplayName));

    var resourceTypeMap = Arrays.stream(PwaResourceType.values())
        .sorted(Comparator.comparing(PwaResourceType::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaResourceType::getDisplayName));

    var applicationStatusMap = TestHarnessService.getTestHarnessAppStatuses().stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationStatus::getDisplayName));

    var caseOfficerCandidates = pwaTeamService.getTeamMembersWithRegulatorRole(Role.CASE_OFFICER)
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getFullName))
        .collect(StreamUtils.toLinkedHashMap(member -> String.valueOf(member.wuaId()), TeamMemberView::getFullName));

    var appTypesForPipelines = TestHarnessService.getAppTypesForPipelines()
        .stream().map(PwaApplicationType::getDisplayName).collect(Collectors.joining(", "));

    var appStatusesForCaseOfficer = TestHarnessService.getAppStatusesForCaseOfficer()
        .stream().map(PwaApplicationStatus::getDisplayName).collect(Collectors.joining(", "));

    return new ModelAndView("testHarness/generateApplication")
        .addObject("cancelUrl", RouteUtils.routeWorkArea())
        .addObject("applicationTypeMap", applicationTypeMap)
        .addObject("resourceTypeMap", resourceTypeMap)
        .addObject("applicationStatusMap", applicationStatusMap)
        .addObject("caseOfficerCandidates", caseOfficerCandidates)
        .addObject("applicantUsersMap", testHarnessService.getApplicantsSelectorMap())
        .addObject("appTypesForPipelines", appTypesForPipelines)
        .addObject("appStatusesForCaseOfficer", appStatusesForCaseOfficer);
  }

  @GetMapping("/generate-application/select-pwa")
  public ModelAndView renderSelectPwa(@RequestParam String applicationType,
                                      @RequestParam Integer pipelineQuantity,
                                      @RequestParam String applicationStatus,
                                      @RequestParam Integer assignedCaseOfficerId,
                                      @RequestParam Integer applicantWuaId,
                                      @RequestParam String resourceType,
                                      @ModelAttribute("form") GenerateVariationApplicationForm form) {
    return getSelectPwaModelAndView(form);
  }

  @PostMapping("/generate-application/select-pwa")
  public ModelAndView postSelectPwa(@ModelAttribute("form") GenerateVariationApplicationForm form,
                                    BindingResult bindingResult) {

    var validatedBindingResult = testHarnessService.validateGenerateVariationApplicationForm(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
        getSelectPwaModelAndView(form), () -> {
          testHarnessJobCreationService.scheduleVariationGenerateApplicationJob(form);
          return RouteUtils.redirectWorkArea();
        });

  }

  private ModelAndView getSelectPwaModelAndView(GenerateVariationApplicationForm form) {

    var user = testHarnessUserRetrievalService.getWebUserAccount(form.getApplicantWuaId());
    var pickableOptions = pickedPwaRetrievalService.getPickablePwaOptions(user, PwaResourceType.PETROLEUM);
    var showNonConsentedOptions = !pickableOptions.getNonconsentedPickablePwas().isEmpty()
        && PwaApplicationType.DEPOSIT_CONSENT.equals(form.getApplicationType());


    return new ModelAndView("testHarness/appGenSelectPwa")
        .addObject("cancelUrl", RouteUtils.routeWorkArea())
        .addObject("consentedPwaMap", pickableOptions.getConsentedPickablePwas())
        .addObject("nonConsentedPwaMap", pickableOptions.getNonconsentedPickablePwas())
        .addObject("showNonConsentedOptions", showNonConsentedOptions);
  }

}
