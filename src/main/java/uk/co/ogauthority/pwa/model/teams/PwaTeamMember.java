package uk.co.ogauthority.pwa.model.teams;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

public class PwaTeamMember {

  private final PwaTeam team;
  private final Person person;
  private final Set<PwaRole> roleSet;

  public PwaTeamMember(PwaTeam team, Person person, Set<PwaRole> roleSet) {
    this.team = team;
    this.person = person;
    this.roleSet = roleSet;
  }

  public Person getPerson() {
    return person;
  }

  public Set<PwaRole> getRoleSet() {
    return roleSet;
  }

  public PwaTeam getTeam() {
    return team;
  }

  public boolean isTeamAdministrator() {
    return roleSet.stream().anyMatch(PwaRole::isTeamAdministratorRole);
  }
}
