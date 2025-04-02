package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetUtils;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Used to map permissions for a specific operation on a PWA application to the various roles in different teams
 * that are allowed that permission.
 */
public enum PwaApplicationPermission {
  SUBMIT(
      EnumSet.noneOf(PwaContactRole.class),
      EnumSet.of(Role.APPLICATION_SUBMITTER),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  EDIT(
      EnumSet.of(PwaContactRole.PREPARER),
      Set.of(),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  MANAGE_CONTACTS(
      EnumSet.of(PwaContactRole.ACCESS_MANAGER),
      EnumSet.of(Role.APPLICATION_SUBMITTER, Role.APPLICATION_CREATOR),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  VIEW(
      EnumSet.allOf(PwaContactRole.class),
      EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles()),
      EnumSet.allOf(PwaRegulatorRole.class),
      EnumSet.allOf(ConsulteeGroupMemberRole.class)),

  SET_PIPELINE_REFERENCE(userRolesForApplicationDto ->
      // the user is an app contact preparer and has any of the regulator roles.
      userRolesForApplicationDto.getUserContactRoles().contains(PwaContactRole.PREPARER)
          && !SetUtils.intersection(
          userRolesForApplicationDto.getUserRegulatorRoles(),
          EnumSet.allOf(PwaRegulatorRole.class)).isEmpty()
  );

  // Capture the roles a user must have one of in order to qualify for the permission
  private final Set<PwaContactRole> contactRoles;
  private final Set<Role> holderTeamRoles;
  private final Set<PwaRegulatorRole> regulatorRoles;
  private final Set<ConsulteeGroupMemberRole> consulteeRoles;
  // define a function to override default permission behaviour. Allows specification of complex permission checks.
  private final Function<UserRolesForApplicationDto, Boolean> permissionOverrideFunction;



  /**
   * Use this constructor when you simply want the user to have any of the specified roles to qualify for permission.
   */
  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<Role> holderTeamRoles,
                           Set<PwaRegulatorRole> regulatorRoles,
                           Set<ConsulteeGroupMemberRole> consulteeRoles) {
    this(
        contactRoles,
        holderTeamRoles,
        regulatorRoles,
        consulteeRoles,
        userRolesForApplicationDto -> false);
  }

  /**
   * Use this constructor when you simply want to implement custom logic base on user roles.
   */
  PwaApplicationPermission(Function<UserRolesForApplicationDto, Boolean> permissionOverrideFunction) {
    this(
        EnumSet.noneOf(PwaContactRole.class),
        Set.of(),
        EnumSet.noneOf(PwaRegulatorRole.class),
        EnumSet.noneOf(ConsulteeGroupMemberRole.class),
        permissionOverrideFunction);
  }

  /**
   * Not designed to used directly. Behaviour is undefined when both an override function and individual role requirements are specified.
   * Use the alternative constructors that determine defaults values for you.
   */
  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<Role> holderTeamRoles,
                           Set<PwaRegulatorRole> regulatorRoles,
                           Set<ConsulteeGroupMemberRole> consulteeRoles,
                           Function<UserRolesForApplicationDto, Boolean> permissionOverrideFunction) {
    this.contactRoles = contactRoles;
    this.holderTeamRoles = holderTeamRoles;
    this.regulatorRoles = regulatorRoles;
    this.consulteeRoles = consulteeRoles;
    this.permissionOverrideFunction = permissionOverrideFunction;
  }

  public Set<PwaContactRole> getContactRoles() {
    return contactRoles;
  }

  public Set<Role> getHolderTeamRoles() {
    return holderTeamRoles;
  }

  public Set<PwaRegulatorRole> getRegulatorRoles() {
    return regulatorRoles;
  }

  public Set<ConsulteeGroupMemberRole> getConsulteeRoles() {
    return consulteeRoles;
  }

  public boolean getPermissionOverrideFunctionResult(UserRolesForApplicationDto userRolesForApplicationDto) {
    return this.permissionOverrideFunction.apply(userRolesForApplicationDto);
  }

  public static Stream<PwaApplicationPermission> stream() {
    return Arrays.stream(PwaApplicationPermission.values());
  }


}
