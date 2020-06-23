package uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;

@Controller
@RequestMapping("/consultee-groups")
public class ConsulteeGroupTeamManagementController {

  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public ConsulteeGroupTeamManagementController(ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  @GetMapping
  public ModelAndView renderManageableGroups(AuthenticatedUserAccount user) {

    var manageableGroupViews = consulteeGroupTeamService.getManageableGroupTeamsForUser(user);

    if (manageableGroupViews.isEmpty()) {
      throw new AccessDeniedException(String.format("User with WUA ID [%s] cannot manage any consultee group teams",
          user.getWuaId()));
    }

    return new ModelAndView("pwaApplication/appProcessing/consultations/consultees/manageableGroupTeamList")
        .addObject("consulteeGroupViews", manageableGroupViews);

  }

}
