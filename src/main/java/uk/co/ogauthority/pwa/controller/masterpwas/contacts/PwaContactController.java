package uk.co.ogauthority.pwa.controller.masterpwas.contacts;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.service.masterpwas.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Controller
@RequestMapping("/pwa-application/{applicationId}/contacts")
public class PwaContactController {

  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public PwaContactController(PwaContactService pwaContactService,
                              PwaApplicationDetailService pwaApplicationDetailService,
                              ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  @GetMapping
  public ModelAndView renderContactsScreen(@PathVariable("applicationId") Integer applicationId,
                                           AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var pwaApplication = detail.getPwaApplication();

      List<TeamMemberView> teamMemberViews = pwaContactService.getContactsForPwaApplication(pwaApplication).stream()
          .map(c -> new TeamMemberView(
              c.getPerson(),
              "#",
              "#",
              c.getRoles().stream()
                .map(r -> new TeamRoleView(r.getRoleName(), r.getRoleName(), r.getRoleName(), r.getDisplayOrder()))
                .collect(Collectors.toSet())
          ))
          .collect(Collectors.toList());

      var modelAndView = new ModelAndView("teamManagement/teamMembers")
          .addObject("teamName", pwaApplication.getAppReference() + " contacts")
          .addObject("teamMemberViews", teamMemberViews)
          .addObject("addUserUrl", "#")
          .addObject("showBreadcrumbs", true);

      applicationBreadcrumbService.fromTaskList(pwaApplication, modelAndView, "Application contacts");

      return modelAndView;

    });

  }

}
