package uk.co.ogauthority.pwa.energyportal.service.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.exceptions.teams.PortalTeamNotFoundException;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalRoleDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.teams.PortalTeam;
import uk.co.ogauthority.pwa.energyportal.model.entity.teams.PortalTeamTypeRole;
import uk.co.ogauthority.pwa.energyportal.model.entity.teams.PortalTeamUsagePurpose;
import uk.co.ogauthority.pwa.energyportal.repository.teams.PortalTeamRepository;


@Service
public class PortalTeamAccessor {

  private final PortalTeamRepository portalTeamRepository;
  private final EntityManager entityManager;

  @Autowired
  public PortalTeamAccessor(PortalTeamRepository portalTeamRepository,
                            EntityManager entityManager) {
    this.portalTeamRepository = portalTeamRepository;
    this.entityManager = entityManager;
  }

  public Optional<PortalTeamDto> findPortalTeamById(int resId) {
    try {
      return Optional.of(entityManager.createQuery("" +
              "SELECT new uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto(" +
              "  pt.resId, pt.name, pt.description, ptt.type, ptu.uref " +
              ") " +
              "FROM PortalTeam pt " +
              "JOIN PortalTeamType ptt ON pt.portalTeamType = ptt " +
              "LEFT JOIN PortalTeamUsage ptu ON ptu.portalTeam = pt" +
              " " +
              "WHERE pt.resId = :resId ",
          PortalTeamDto.class)
          .setParameter("resId", resId)
          .getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }

  }

  /**
   * Given a person and a team Id get person membership details for that team.
   */
  public Optional<PortalTeamMemberDto> getPersonTeamMembership(Person person, int resId) {
    // This implementation is not optimal as it does processing for whole team before filtering to given person.
    // Need to pass optional parameter down the call stack if this is too slow.
    return getPortalTeamMembers(resId)
        .stream()
        .filter(ptm -> person.getId().equals(ptm.getPersonId()))
        .findFirst();
  }

  public List<PortalTeamMemberDto> getPortalTeamMembers(int resId) {
    PortalTeam team = portalTeamRepository.findById(resId)
        .orElseThrow(() -> new PortalTeamNotFoundException("Could not find portal team with resId:" + resId));

    return getPortalTeamMembers(team);

  }

  /**
   * Helper which transforms database results for team member roles into a list of PortalTeamMemberDto.
   */
  private List<PortalTeamMemberDto> getPortalTeamMembers(PortalTeam team) {
    Map<PersonId, Set<PortalRoleDto>> personIdToPortalTeamRoleMap = new HashMap<>();

    // simply converts the list of role results into a map where the personId of the team member is the key and the set of
    // their roles in the team is the value
    for (PortalTeamMemberRoleResult teamMemberRoleResult : getTeamMemberRoleResultsForTeam(team)) {
      PortalRoleDto roleDtoFromResult = convertTeamMemberRoleResultToRoleDto(teamMemberRoleResult);

      if (personIdToPortalTeamRoleMap.containsKey(teamMemberRoleResult.getPersonId())) {
        Set<PortalRoleDto> teamMemberRoles = personIdToPortalTeamRoleMap.get(teamMemberRoleResult.getPersonId());
        // int resId, String name, String title, String description, int displaySequence
        teamMemberRoles.add(roleDtoFromResult);
      } else {
        Set<PortalRoleDto> teamMemberRoles = new HashSet<>();
        teamMemberRoles.add(roleDtoFromResult);
        personIdToPortalTeamRoleMap.put(teamMemberRoleResult.getPersonId(), teamMemberRoles);
      }
    }

    // Using easy to process map, create PortalTeamMemberDto's where the role list of each
    List<PortalTeamMemberDto> teamMemberDtos = new ArrayList<>();
    for (Map.Entry<PersonId, Set<PortalRoleDto>> entry : personIdToPortalTeamRoleMap.entrySet()) {
      teamMemberDtos.add(new PortalTeamMemberDto(entry.getKey(), entry.getValue()));
    }

    return teamMemberDtos;
  }

  private PortalRoleDto convertTeamMemberRoleResultToRoleDto(PortalTeamMemberRoleResult teamMemberRoleResult) {
    return new PortalRoleDto(
        teamMemberRoleResult.getResId(),
        teamMemberRoleResult.getRoleName(),
        teamMemberRoleResult.getRoleTitle(),
        teamMemberRoleResult.getRoleDescription(),
        teamMemberRoleResult.getRoleDisplaySequence()
    );
  }

  /**
   * Helper to make it easier to convert data into API DTOs. This essentially creates one object per role per person in team.
   */
  private List<PortalTeamMemberRoleResult> getTeamMemberRoleResultsForTeam(PortalTeam team) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamMemberRoleResult(" +
            "  pt.resId, " + // team
            "  ptm.personId, " + // person
            "  pttr.name, pttr.title, pttr.description, pttr.displaySeq " + // role details
            ") " +
            "FROM PortalTeamMemberRole ptmr " +
            "JOIN PortalTeamMember ptm ON ptm = ptmr.portalTeamMember " +
            "JOIN PortalTeam pt ON pt = ptm.portalTeam " +
            "JOIN PortalTeamTypeRole pttr ON ptmr.portalTeamTypeRole = pttr " +
            "WHERE ptm.portalTeam = :portalTeam ",
        PortalTeamMemberRoleResult.class)
        .setParameter("portalTeam", team)
        .getResultList();
  }

  public List<PortalTeamDto> getPortalTeamsByPortalTeamType(String portalTeamType) {

    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto(" +
            "  pt.resId, pt.name, pt.description, pt.portalTeamType.type, ptu.uref " +
            ") " +
            "FROM PortalTeam pt " +
            "LEFT JOIN PortalTeamUsage ptu ON ptu.portalTeam = pt " +
            "WHERE pt.portalTeamType.type = :portalTeamType " +
            "AND (ptu.purpose = :usagePurpose OR ptu IS NULL)",
        PortalTeamDto.class)
        .setParameter("portalTeamType", portalTeamType)
        .setParameter("usagePurpose", PortalTeamUsagePurpose.PRIMARY_DATA)
        .getResultList();
  }


  /**
   * Get teams of a given type where some Person is a member and they have a role with matching name in that team.
   */
  public List<PortalTeamDto> getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(Person person, String portalTeamType,
                                                                                   Collection<String> roleNames) {
    List<PortalTeamTypeRole> roles = entityManager.createQuery("" +
            "SELECT pttr " +
            "FROM PortalTeamType ptt " +
            "JOIN PortalTeamTypeRole pttr ON pttr.portalTeamType = ptt " +
            "WHERE ptt.type = :portalTeamType " +
            "AND pttr.name IN :roleNames ",
        PortalTeamTypeRole.class)
        .setParameter("portalTeamType", portalTeamType)
        .setParameter("roleNames", roleNames)
        .getResultList();

    return entityManager.createQuery("" +
            // Distinct required to remove duplicates caused by using the PortalTeamTypeRole entity as root
            "SELECT DISTINCT new uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto( " +
            "  pt.resId, pt.name, pt.description, pt.portalTeamType.type, ptu.uref " +
            ") " +
            "FROM PortalTeamTypeRole pttr " +
            "JOIN PortalTeamType ptt ON ptt = pttr.portalTeamType " +
            "JOIN PortalTeam pt ON pt.portalTeamType = ptt " +
            "JOIN PortalTeamMember ptm ON ptm.portalTeam = pt " +
            "JOIN PortalTeamMemberRole ptmr ON ptmr.portalTeamMember = ptm " +
            "LEFT JOIN PortalTeamUsage ptu ON ptu.portalTeam = pt " +
            "WHERE ptt.type = :portalTeamType " +
            "AND ptm.personId = :personId " +
            "AND (ptu.purpose = :usagePurpose OR ptu IS NULL) " +
            "AND ptmr.portalTeamTypeRole IN :portalTeamTypeRoles",
        PortalTeamDto.class)
        .setParameter("portalTeamType", portalTeamType)
        .setParameter("usagePurpose", PortalTeamUsagePurpose.PRIMARY_DATA)
        .setParameter("portalTeamTypeRoles", roles)
        .setParameter("personId", person.getId().asInt())
        .getResultList();

  }

  /**
   * Remove a given person from a team.
   *
   * @param resId id of team person being removed from
   * @param personToBeRemovedFromTeam Person who is being removed
   * @param actionPerformedBy User who is doing the removing
   */
  @Transactional
  public void removePersonFromTeam(int resId, Person personToBeRemovedFromTeam, WebUserAccount actionPerformedBy) {
    try {
      portalTeamRepository.removeUserFromTeam(resId, personToBeRemovedFromTeam.getId().asInt(), actionPerformedBy.getWuaId());
    } catch (Exception e) {
      String msg = String.format(
          "Error Removing person from team. paramSummary: resId:%s; personId:%s; actingPersonId:%s;",
          resId,
          personToBeRemovedFromTeam.getId(),
          actionPerformedBy.getWuaId()
      );
      throw new RuntimeException(msg, e);
    }
  }


  /**
   * For a team, set a person's roles within that team. Can add person to team if they are not a member already
   *
   * @param resId id of team the person being added to
   * @param person Person who is having their roles set.
   * @param actionPerformedBy User who is doing the adding
   */
  @Transactional
  public void addPersonToTeamWithRoles(int resId, Person person, Collection<String> roleNames, WebUserAccount actionPerformedBy) {
    String roleNameCsv = String.join(",", roleNames);
    try {
      portalTeamRepository.updateUserRoles(resId, person.getId().asInt(), roleNameCsv, actionPerformedBy.getWuaId());
    } catch (Exception e) {
      String msg = String.format("Error adding person to team. paramSummary: resId:%s; personId:%s; roleNameCSV:%s; actingPersonId:%s;",
          resId,
          person.getId(),
          roleNameCsv,
          actionPerformedBy.getWuaId()
      );
      throw new RuntimeException(msg, e);
    }
  }

  /**
   * Simple check to make sure some person has some role within a given team.
   */
  public boolean personIsAMemberOfTeam(int resId, Person person) {
    return !entityManager.createQuery("" +
            "SELECT 1 " +
            "FROM PortalTeamMember ptm " +
            "JOIN PortalTeam pt ON pt = ptm.portalTeam " +
            "WHERE pt.resId = :resId " +
            "AND ptm.personId = :personId",
        Integer.class)
        .setParameter("resId", resId)
        .setParameter("personId", person.getId().asInt())
        .getResultList()
        .isEmpty();
  }

  /**
   * Get a list of all possible roles members of a given team can have.
   */
  public List<PortalRoleDto> getAllPortalRolesForTeam(int resId) {

    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalRoleDto(" +
            "  pt.resId, pttr.name, pttr.title,  pttr.description, pttr.displaySeq " +
            ") " +
            "FROM PortalTeam pt " +
            "JOIN PortalTeamType ptt ON pt.portalTeamType = ptt " +
            "JOIN PortalTeamTypeRole pttr ON pttr.portalTeamType = ptt " +
            "WHERE pt.resId = :resId",
        PortalRoleDto.class)
        .setParameter("resId", resId)
        .getResultList();
  }


  public List<PortalSystemPrivilegeDto> getAllPortalSystemPrivilegesForPerson(Person person) {
    return entityManager.createQuery("" +
            // Distinct required to remove duplicates caused by using the PortalTeamTypeRole entity as root
            "SELECT DISTINCT new uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalSystemPrivilegeDto( " +
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
