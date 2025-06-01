package uk.co.ogauthority.pwa.service.teams;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;

@Service
public class TeamService {

  private final PortalTeamAccessor portalTeamAccessor;
  private final PwaTeamsDtoFactory pwaTeamsDtoFactory;

  @Autowired
  public TeamService(PortalTeamAccessor portalTeamAccessor,
                     PwaTeamsDtoFactory pwaTeamsDtoFactory) {
    this.portalTeamAccessor = portalTeamAccessor;
    this.pwaTeamsDtoFactory = pwaTeamsDtoFactory;
  }

  public Set<PwaUserPrivilege> getAllUserPrivilegesForPerson(Person person) {
    // get privs available to the user through res type role membership
    return pwaTeamsDtoFactory.createPwaUserPrivilegeSet(portalTeamAccessor.getAllPortalSystemPrivilegesForPerson(person));
  }

}
