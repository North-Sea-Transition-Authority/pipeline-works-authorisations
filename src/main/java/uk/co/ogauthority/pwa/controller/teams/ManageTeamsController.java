package uk.co.ogauthority.pwa.controller.teams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.teams.ManageTeamService;

@Controller
public class ManageTeamsController {

  private final ManageTeamService manageTeamService;

  @Autowired
  public ManageTeamsController(ManageTeamService manageTeamService) {
    this.manageTeamService = manageTeamService;
  }

  @GetMapping("/manage-teams")
  public ModelAndView renderTeamTypes(AuthenticatedUserAccount user) {

    var teamTypeUrlMap = manageTeamService.getManageTeamTypesAndUrlsForUser(user);

    if (teamTypeUrlMap.size() == 1) {
      return new ModelAndView("redirect:" + teamTypeUrlMap.entrySet().iterator().next().getValue());
    }

    if (teamTypeUrlMap.isEmpty()) {
      throw new AccessDeniedException(String.format("User with WUA ID [%s] can't access any team types",
          user.getWuaId()));
    }

    return new ModelAndView("teamManagementOld/teamTypes")
        .addObject("teamTypes", teamTypeUrlMap);

  }

}
