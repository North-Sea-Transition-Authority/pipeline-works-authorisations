package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * Used to map permissions for a specific operation on a PWA application to the various roles in different teams
 * that are allowed that permission.
 */
public enum PwaApplicationPermission {
  SUBMIT(
      Set.of(),
      EnumSet.of(Role.APPLICATION_SUBMITTER),
      Set.of(),
      Set.of()
  ),

  EDIT(
      EnumSet.of(PwaContactRole.PREPARER),
      Set.of(),
      Set.of(),
      Set.of()
  ),

  MANAGE_CONTACTS(
      EnumSet.of(PwaContactRole.ACCESS_MANAGER),
      EnumSet.of(Role.APPLICATION_SUBMITTER, Role.APPLICATION_CREATOR),
      Set.of(),
      Set.of()
  ),

  VIEW(
      EnumSet.allOf(PwaContactRole.class),
      TeamType.ORGANISATION.getAllowedRolesAsSet(),
      TeamType.REGULATOR.getAllowedRolesAsSet(),
      TeamType.CONSULTEE.getAllowedRolesAsSet()
  ),

  SET_PIPELINE_REFERENCE(userRolesForApplicationDto -> {
    var hasPreparerRole = userRolesForApplicationDto.getUserContactRoles().contains(PwaContactRole.PREPARER);
    var hasAnyRegulatorRole = !userRolesForApplicationDto.getUserRegulatorRoles().isEmpty();

    // the user is an app contact preparer and has any of the regulator roles.
    return hasPreparerRole && hasAnyRegulatorRole;
  });

  // Capture the roles a user must have one of in order to qualify for the permission
  private final Set<PwaContactRole> contactRoles;
  private final Set<Role> holderTeamRoles;
  private final Set<Role> regulatorRoles;
  private final Set<Role> consulteeRoles;
  // define a function to override default permission behaviour. Allows specification of complex permission checks.
  private final Function<UserRolesForApplicationDto, Boolean> permissionOverrideFunction;



  /**
   * Use this constructor when you simply want the user to have any of the specified roles to qualify for permission.
   */
  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<Role> holderTeamRoles,
                           Set<Role> regulatorRoles,
                           Set<Role> consulteeRoles) {
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
        Set.of(),
        Set.of(),
        Set.of(),
        Set.of(),
        permissionOverrideFunction);
  }

  /**
   * Not designed to used directly. Behaviour is undefined when both an override function and individual role requirements are specified.
   * Use the alternative constructors that determine defaults values for you.
   */
  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<Role> holderTeamRoles,
                           Set<Role> regulatorRoles,
                           Set<Role> consulteeRoles,
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

  public Set<Role> getRegulatorRoles() {
    return regulatorRoles;
  }

  public Set<Role> getConsulteeRoles() {
    return consulteeRoles;
  }

  public boolean getPermissionOverrideFunctionResult(UserRolesForApplicationDto userRolesForApplicationDto) {
    return this.permissionOverrideFunction.apply(userRolesForApplicationDto);
  }

  public static Stream<PwaApplicationPermission> stream() {
    return Arrays.stream(PwaApplicationPermission.values());
  }


}
