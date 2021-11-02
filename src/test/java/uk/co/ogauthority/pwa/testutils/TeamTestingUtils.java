package uk.co.ogauthority.pwa.testutils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalRoleDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.model.teams.PwaGlobalTeam;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;

/**
 * Util class to ease testing of Teams service and associated services which use teams package objects.
 */
public class TeamTestingUtils {

  public static PwaRegulatorTeam getRegulatorTeam() {
    return new PwaRegulatorTeam(100, "REGULATOR_TEAM_NAME", "REGULATOR_TEAM_DESCRIPTION");
  }

  public static PwaOrganisationTeam getOrganisationTeam(PortalOrganisationGroup organisationGroup) {
    return new PwaOrganisationTeam(200, "ORG_TEAM_NAME", "ORG_TEAM_DESCRIPTION", organisationGroup);
  }

  public static PwaGlobalTeam getGlobalTeam() {
    return new PwaGlobalTeam(300, "GLOBAL_TEAM_NAME", "GLOBAL_TEAM_DESCRIPTION");
  }

  public static PwaRole getTeamAdminRole() {
    return new PwaRole(
        PwaRole.TEAM_ADMINISTRATOR_ROLE_NAME,
        "ADMIN ROLE_TITLE",
        "ADMIN_ROLE_DESCRIPTION",
        10
    );
  }

  public static PwaRole generatePwaRole(String roleName, int displaySequence) {
    return new PwaRole(
        roleName,
        roleName,
        roleName,
        displaySequence
    );
  }

  public static PortalTeamDto portalTeamDtoFrom(PwaRegulatorTeam team) {
    return new PortalTeamDto(
        team.getId(),
        team.getName(),
        team.getDescription(),
        team.getType().getPortalTeamType(),
        null
    );
  }

  public static PortalTeamDto portalTeamDtoFrom(PwaOrganisationTeam team) {
    return new PortalTeamDto(
        team.getId(),
        team.getName(),
        team.getDescription(),
        team.getType().getPortalTeamType(),
        team.getPortalOrganisationGroup().getUrefValue()
    );
  }

  public static PortalTeamDto portalTeamDtoFrom(PwaGlobalTeam team) {
    return new PortalTeamDto(
        team.getId(),
        team.getName(),
        team.getDescription(),
        team.getType().getPortalTeamType(),
        null
    );
  }

  public static PortalTeamMemberDto createPortalTeamMember(Person person, Set<PortalRoleDto> roles) {
    return new PortalTeamMemberDto(person.getId(), roles);
  }

  public static PortalTeamMemberDto createPortalTeamMember(Person person, PwaTeam team) {
    Set<PortalRoleDto> personRoles = new HashSet<>();
    personRoles.add(
        getTeamAdminRoleDto(team)
    );

    return createPortalTeamMember(person, personRoles);
  }

  public static PortalRoleDto getTeamAdminRoleDto(PwaTeam team) {
    return getTeamAdminRoleDto(team.getId());
  }

  public static PortalRoleDto getTeamAdminRoleDto(int resId) {
    return new PortalRoleDto(resId, PwaRole.TEAM_ADMINISTRATOR_ROLE_NAME, "Team admin", "Team Admin Desc", 10);
  }

  public static PortalOrganisationUnit createOrgUnit() {
    var portalOrganisationGroup = generateOrganisationGroup(100, "ORGANISATION_GROUP", "ORG_GRP");
    return generateOrganisationUnit(1000, "ORGANISATION_UNIT", portalOrganisationGroup);
  }

  private static PortalOrganisationUnit generateOrganisationUnit(int ouId, String name,
                                                                PortalOrganisationGroup portalOrganisationGroup) {
    return PortalOrganisationTestUtils.generateOrganisationUnit(
        ouId, name, portalOrganisationGroup);
  }

  public static PortalOrganisationGroup generateOrganisationGroup(int orgGrpId, String name, String shortName) {

    PortalOrganisationGroup portalOrganisationGroup = new PortalOrganisationGroup();
    try {
      FieldUtils.writeField(portalOrganisationGroup, "orgGrpId", orgGrpId, true);
      FieldUtils.writeField(portalOrganisationGroup, "name", name, true);
      FieldUtils.writeField(portalOrganisationGroup, "shortName", shortName, true);
      FieldUtils.writeField(portalOrganisationGroup, "urefValue", String.valueOf(orgGrpId) + "++REGORGGRP", true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return portalOrganisationGroup;
  }

  public static PwaTeamMember createRegulatorTeamMember(PwaTeam regulatorTeam, Person person, Set<PwaRegulatorRole> regulatorRoles) {

    var pwaRoles = regulatorRoles.stream()
        .map(role -> new PwaRole(role.getPortalTeamRoleName(), "title", "desc", 10))
        .collect(Collectors.toSet());

    return new PwaTeamMember(regulatorTeam, person, pwaRoles);

  }

  public static PwaTeamMember createOrganisationTeamMember(PwaOrganisationTeam orgTeam , Person person, Set<PwaOrganisationRole> pwaOrganisationRoles) {

    var pwaRoles = pwaOrganisationRoles.stream()
        .map(role -> new PwaRole(role.getPortalTeamRoleName(), "title", "desc", 10))
        .collect(Collectors.toSet());

    return new PwaTeamMember(orgTeam, person, pwaRoles);

  }

}

