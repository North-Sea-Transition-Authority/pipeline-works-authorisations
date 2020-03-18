package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-information")
public class ProjectInformationController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public ProjectInformationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationDetailService pwaApplicationDetailService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getProjectInformationModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/projectInformation");
    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Project information");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderProjectInformation(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") ProjectInformationForm projectInformationForm,
                                               AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      // Load data
      return getProjectInformationModelAndView(detail);
    });
  }

  @PostMapping
  public ModelAndView postProjectInformation(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             @ModelAttribute("form") ProjectInformationForm projectInformationForm,
                                             AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication()));
  }

}
