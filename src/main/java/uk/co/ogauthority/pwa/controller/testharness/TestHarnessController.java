package uk.co.ogauthority.pwa.controller.testharness;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskListController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.testharness.DebugAppFormGenService;
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
  private final DebugAppFormGenService debugAppFormGenService;//Delete - Testing/Debugging Purposes Only

  @Autowired
  public TestHarnessController(TestHarnessService testHarnessService,
                               PwaTeamService pwaTeamService,
                               ControllerHelperService controllerHelperService,
                               DebugAppFormGenService debugAppFormGenService) {
    this.testHarnessService = testHarnessService;
    this.pwaTeamService = pwaTeamService;
    this.controllerHelperService = controllerHelperService;
    this.debugAppFormGenService = debugAppFormGenService;
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



  //Delete - Testing/Debugging Purposes Only
  @GetMapping("/generate-app-form-section/{detailId}/{userId}/{pipelineQuantity}")
  public ModelAndView generateAppFormSection(@PathVariable Integer detailId,
                                             @PathVariable Integer userId,
                                             @PathVariable Integer pipelineQuantity) {

    var detail = debugAppFormGenService.updateAppForm(detailId, userId, pipelineQuantity);
    return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(detail.getPwaApplication().getId(), null));
  }


}
