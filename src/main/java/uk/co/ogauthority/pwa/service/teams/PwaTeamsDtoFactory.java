package uk.co.ogauthority.pwa.service.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalRoleDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;

/**
 * Converts output from PortalTeams Service layer into Objects useful for the application.
 */
@Service
class PwaTeamsDtoFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaTeamsDtoFactory.class);

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PersonRepository personRepository;

  @Autowired
  public PwaTeamsDtoFactory(PortalOrganisationsAccessor portalOrganisationsAccessor, PersonRepository personRepository) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.personRepository = personRepository;
  }

  /**
   * Given a PortalTeamDto, create the appropriate PWA team type.
   */
  PwaTeam createPwaTeam(PortalTeamDto portalTeamDto) {
    PwaTeamType teamType = PwaTeamType.findByPortalTeamType(portalTeamDto.getType());
    switch (teamType) {
      case REGULATOR:
        return createRegulatorTeam(portalTeamDto);
      case ORGANISATION:
        return createOrganisationTeam(portalTeamDto);
      default:
        throw new PwaTeamFactoryException("portalTeamType not supported by factory. Type: " + portalTeamDto.getType());
    }
  }

  /**
   * Given a portal team, convert to a regulator team if possible.
   */
  PwaRegulatorTeam createRegulatorTeam(PortalTeamDto portalTeam) {
    checkPortalTeamsAllOfExpectedType(Collections.singleton(portalTeam), PwaTeamType.REGULATOR);

    return new PwaRegulatorTeam(
        portalTeam.getResId(),
        portalTeam.getName(),
        portalTeam.getDescription()
    );
  }

  /**
   * Create a PwaOrganisationTeam from a single PortalTeamDto.
   */
  PwaOrganisationTeam createOrganisationTeam(PortalTeamDto portalTeamDto) {
    List<PwaOrganisationTeam> orgTeams = createOrganisationTeamList(Collections.singleton(portalTeamDto));
    if (orgTeams.isEmpty() || orgTeams.get(0) == null) {
      throw new PwaTeamFactoryException("Organisation PwaTeam not created! " + portalTeamDto.toString());
    }
    return orgTeams.get(0);
  }

  /**
   * Given a collection of PortalTeamDtos, try to efficiently convert them to a list of OrganisationTeams.
   */
  List<PwaOrganisationTeam> createOrganisationTeamList(Collection<PortalTeamDto> portalTeams) {

    checkPortalTeamsAllOfExpectedType(portalTeams, PwaTeamType.ORGANISATION);

    // get the organisation urefs from the team scopes
    List<String> organisationTeamPrimaryScopes = portalTeams.stream()
        .map(pt -> pt.getScope().getPrimaryScope())
        .distinct()
        .collect(Collectors.toList());

    // allow easy lookup of portal org grps based on the team scope
    Map<String, PortalOrganisationGroup> teamOrganisationMap = portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(
        organisationTeamPrimaryScopes)
        .stream()
        .collect(Collectors.toMap((PortalOrganisationGroup::getUrefValue), (pog -> pog)));

    List<PwaOrganisationTeam> organisationTeamList = new ArrayList<>();
    for (PortalTeamDto portalTeamDto : portalTeams) {

      // we've hit a data problem if we can't find an organisation group for our team.
      try {
        checkPortalOrganisationFoundForTeam(portalTeamDto, teamOrganisationMap);

        organisationTeamList.add(
            new PwaOrganisationTeam(
                portalTeamDto.getResId(),
                teamOrganisationMap.get(portalTeamDto.getScope().getPrimaryScope()).getName(),
                portalTeamDto.getName(),
                teamOrganisationMap.get(portalTeamDto.getScope().getPrimaryScope())
            )
        );
      } catch (PwaTeamFactoryException e) {
        // dont want to brick any calling code if there is some data issue. Log error and do not add the problem team to the result list.
        LOGGER.warn("Failed to convert PortalTeamDto with resId {} to a PwaOrganisationTeam.", portalTeamDto.getResId(), e);
      }

    }

    return organisationTeamList;
  }

  List<PwaTeamMember> createPwaTeamMemberList(Collection<PortalTeamMemberDto> portalTeamMemberDtoList, PwaTeam team) {
    List<PwaTeamMember> pwaTeamMembers = new ArrayList<>();

    Set<Integer> portalTeamMemberPersonIds = portalTeamMemberDtoList
        .stream()
        .map(PortalTeamMemberDto::getPersonId)
        .map(PersonId::asInt)
        .collect(Collectors.toSet());

    Map<PersonId, Person> teamMemberPeople = personRepository.findAllByIdIn(portalTeamMemberPersonIds)
        .stream()
        .collect(Collectors.toMap(Person::getId, Function.identity()));

    for (PortalTeamMemberDto portalTeamMemberDto : portalTeamMemberDtoList) {
      pwaTeamMembers.add(
          createPwaTeamMember(portalTeamMemberDto, teamMemberPeople.get(portalTeamMemberDto.getPersonId()), team));
    }

    return pwaTeamMembers;
  }

  PwaTeamMember createPwaTeamMember(PortalTeamMemberDto portalTeamMemberDto, Person person, PwaTeam team) {
    Set<PwaRole> roles = portalTeamMemberDto.getRoles()
        .stream()
        .map(this::createPwaRole)
        .collect(Collectors.toSet());

    return new PwaTeamMember(team, person, roles);
  }

  PwaRole createPwaRole(PortalRoleDto portalRoleDto) {
    return new PwaRole(
        portalRoleDto.getName(),
        portalRoleDto.getTitle(),
        portalRoleDto.getDescription(),
        portalRoleDto.getDisplaySequence()
    );
  }

  /**
   * Make sure all portal teams are of expected type before trying to process them.
   */
  private void checkPortalTeamsAllOfExpectedType(Collection<PortalTeamDto> portalTeams,
                                                 PwaTeamType expectedTeamType) {
    if (portalTeams.stream().anyMatch(pt -> !pt.getType().equals(expectedTeamType.getPortalTeamType()))) {
      throw new PwaTeamFactoryException("Not all teams were of the expected team type");
    }
  }

  private void checkPortalOrganisationFoundForTeam(PortalTeamDto portalTeamDto,
                                                   Map<String, PortalOrganisationGroup> teamOrganisationMap) {
    if (!teamOrganisationMap.containsKey(portalTeamDto.getScope().getPrimaryScope())) {
      throw new PwaTeamFactoryException(
          "Expected to find organisation matching team scope but did not! " + portalTeamDto.toString()
      );
    }
  }

  /**
   * Consume collection of PortalSystemPrivilegeDtos (which may contains duplicates through membership of multiple teams and roles)
   * and return a list.
   */
  List<PwaUserPrivilege> createPwaUserPrivilegeList(Collection<PortalSystemPrivilegeDto> portalSystemPrivilegeDtos) {

    Set<PwaUserPrivilege> privileges = new HashSet<>();
    for (PortalSystemPrivilegeDto portalSystemPrivDto: portalSystemPrivilegeDtos) {
      try {
        var pwaPriv = PwaUserPrivilege.valueOf(portalSystemPrivDto.getGrantedPrivilege());
        privileges.add(pwaPriv);
      } catch (IllegalArgumentException e) {
        LOGGER.debug("Unknown priv '{}' found when mapping portal privs to the PwaUserPrivilege enum. This priv has been ignored.",
            portalSystemPrivDto.getGrantedPrivilege());
      }
    }

    return new ArrayList<>(privileges);
  }

}
