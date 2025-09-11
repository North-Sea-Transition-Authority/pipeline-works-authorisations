package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;

@Service
// TODO: Remove in PWARE-60
public class PortalTeamAccessor {

  private final EntityManager entityManager;
  private final PersonService personService;

  @Autowired
  public PortalTeamAccessor(EntityManager entityManager, PersonService personService) {
    this.entityManager = entityManager;
    this.personService = personService;
  }

  public List<PwaUserPrivilege> getAllUserPrivilegesForPerson(Integer personId) {
    var person = personService.getPersonById(personId);

    return getAllPortalSystemPrivilegesForPerson(person)
        .stream()
        .map(portalSystemPrivilegeDto -> PwaUserPrivilege.valueOfOrNull(portalSystemPrivilegeDto.getGrantedPrivilege()))
        .filter(Objects::nonNull)
        .toList();
  }

  public List<PortalSystemPrivilegeDto> getAllPortalSystemPrivilegesForPerson(Person person) {
    return entityManager.createQuery("" +
                // Distinct required to remove duplicates caused by using the PortalTeamTypeRole entity as root
                "SELECT DISTINCT new uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalSystemPrivilegeDto( " +
                "  pt.portalTeamType.type, pttr.name, pttrp.privilege" +
                ") " +
                "FROM PortalTeamMember ptm " +
                "JOIN PortalTeam pt ON pt = ptm.portalTeam " +
                "JOIN PortalTeamType ptt ON ptt = pt.portalTeamType " +
                "JOIN PortalTeamTypeRole pttr ON pttr.portalTeamType = ptt " +
                "JOIN PortalTeamMemberRole ptmr ON ptmr.portalTeamMember = ptm AND ptmr.portalTeamTypeRole = pttr " +
                "JOIN PortalTeamTypeRolePriv pttrp ON pttrp.portalTeamTypeRole = pttr " +
                "WHERE ptm.personId = :personId ",
            PortalSystemPrivilegeDto.class)
        .setParameter("personId", person.getId().asInt())
        .getResultList();

  }
}