package uk.co.ogauthority.pwa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class UserSessionPrivilegesService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSessionPrivilegesService.class);

  private final TeamService teamService;

  @Autowired
  public UserSessionPrivilegesService(TeamService teamService) {
    this.teamService = teamService;
  }

  /**
   * For a given user object, update privileges from datasource.
   */
  public void populateUserPrivileges(AuthenticatedUserAccount user) {
    user.setPrivileges(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()));
  }
}
