package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@Service
// TODO: Remove in PWARE-60
public class PortalTeamAccessor {

  private final EntityManager entityManager;

  @Autowired
  public PortalTeamAccessor(EntityManager entityManager) {
    this.entityManager = entityManager;
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