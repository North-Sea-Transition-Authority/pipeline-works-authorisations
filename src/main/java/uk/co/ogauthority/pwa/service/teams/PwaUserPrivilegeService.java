package uk.co.ogauthority.pwa.service.teams;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;

@Service
public class PwaUserPrivilegeService {

  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public PwaUserPrivilegeService(ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  /**
   * Get user privileges for a user based on PWA domain objects instead of portal objects.
   */
  public Set<PwaUserPrivilege> getPwaUserPrivilegesForPerson(Person person) {

    var teamMemberList = consulteeGroupTeamService.getTeamMembersByPerson(person);

    var privSet = new HashSet<PwaUserPrivilege>();

    if (!teamMemberList.isEmpty()) {

      boolean isAccessManager = teamMemberList.stream()
          .flatMap(groupTeamMember -> groupTeamMember.getRoles().stream())
          .anyMatch(role -> role.equals(ConsulteeGroupMemberRole.ACCESS_MANAGER));

      if (isAccessManager) {
        privSet.add(PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN);
      }

      privSet.add(PwaUserPrivilege.PWA_WORKAREA);

    }

    return privSet;

  }

}
