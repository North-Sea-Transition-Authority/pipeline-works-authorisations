package uk.co.ogauthority.pwa.controller.testharness;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessService;
import uk.co.ogauthority.pwa.util.RouteUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

/**
 * Controller to allow dev access to the screen for patching in pwa applications
 * Not for production.
 */
@Controller
@Profile("development")
@RequestMapping("/test-harness")
public class TestHarnessController {

  private final TestHarnessService testHarnessService;
  private final PwaTeamService pwaTeamService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public TestHarnessController(TestHarnessService testHarnessService,
                               PwaTeamService pwaTeamService,
                               ControllerHelperService controllerHelperService) {
    this.testHarnessService = testHarnessService;
    this.pwaTeamService = pwaTeamService;
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
          testHarnessService.scheduleGenerateApplicationJob(form);
          return RouteUtils.redirectWorkArea();
        });

  }


  private ModelAndView getGenerateApplicationModelAndView() {

    var applicationTypeMap = PwaApplicationType.stream()
        .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationType::getDisplayName));

    var applicationStatusMap = PwaApplicationStatus.stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationStatus::getDisplayName));

    var caseOfficerCandidates = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER)
        .stream()
        .sorted(Comparator.comparing(Person::getFullName))
        .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
            Person::getFullName));


    return new ModelAndView("testHarness/generateApplication")
        .addObject("cancelUrl", RouteUtils.routeWorkArea())
        .addObject("applicationTypeMap", applicationTypeMap)
        .addObject("applicationStatusMap", applicationStatusMap)
        .addObject("caseOfficerCandidates", caseOfficerCandidates)
        .addObject("applicantUsersMap", testHarnessService.getApplicantsSelectorMap());
  }




}
