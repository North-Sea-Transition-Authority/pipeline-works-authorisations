package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaAppProcessingPermissionService {

  private final TeamService teamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final PwaContactService pwaContactService;
  private final ConsultationRequestService consultationRequestService;

  @Autowired
  public PwaAppProcessingPermissionService(TeamService teamService,
                                           ConsulteeGroupTeamService consulteeGroupTeamService,
                                           PwaContactService pwaContactService,
                                           ConsultationRequestService consultationRequestService) {
    this.teamService = teamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.pwaContactService = pwaContactService;
    this.consultationRequestService = consultationRequestService;
  }

  public Set<PwaAppProcessingPermission> getProcessingPermissions(PwaApplication application,
                                                                  AuthenticatedUserAccount user) {

    var genericPermissions = getGenericProcessingPermissions(user);

    var appContactMembership = pwaContactService.getContactRoles(application, user.getLinkedPerson());

    var userPrivileges = user.getUserPrivileges();

    Set<ConsulteeGroup> consulteeGroups = consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson()).stream()
        .map(ConsulteeGroupTeamMember::getConsulteeGroup)
        .collect(Collectors.toSet());

    // user has consulted on app if any group they are part of has a consultation request for the application
    boolean consultedOnApp = consultationRequestService.getAllRequestsByApplication(application).stream()
        .anyMatch(r -> consulteeGroups.contains(r.getConsulteeGroup()));

    var appPermissions = PwaAppProcessingPermission.streamAppPermissions()
        .filter(permission -> {

          switch (permission) {
            case UPDATE_APPLICATION:
              return appContactMembership.contains(PwaContactRole.PREPARER);
            case CASE_MANAGEMENT_CONSULTEE:
              return consultedOnApp;
            case CASE_MANAGEMENT_INDUSTRY:
              return !appContactMembership.isEmpty();
            case CASE_MANAGEMENT_OGA:
              return userPrivileges.contains(PwaUserPrivilege.PWA_REGULATOR);
            default:
              return false;
          }

        })
        .collect(Collectors.toSet());

    // any user with a case management permission can view the app summary
    if (appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA)
        || appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE)) {
      appPermissions.add(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);
    }

    return SetUtils.union(genericPermissions, appPermissions);

  }

  public Set<PwaAppProcessingPermission> getGenericProcessingPermissions(WebUserAccount user) {

    Optional<PwaTeamMember> userRegTeamMembershipOpt = teamService
        .getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson());

    Set<PwaRegulatorRole> roles = userRegTeamMembershipOpt
        .map(regTeamMembership -> regTeamMembership.getRoleSet().stream()
            .map(pwaRole -> PwaRegulatorRole.getValueByPortalTeamRoleName(pwaRole.getName()))
            .collect(Collectors.toSet()))
        .orElse(Set.of());

    Set<ConsulteeGroupMemberRole> consulteeGroupRoles = new HashSet<>();
    consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())
        .forEach(member -> consulteeGroupRoles.addAll(member.getRoles()));

    var orgTeams = teamService.getOrganisationTeamsPersonIsMemberOf(user.getLinkedPerson());

    return PwaAppProcessingPermission.streamGenericPermissions()
        .filter(permission -> {

          switch (permission) {

            case ACCEPT_INITIAL_REVIEW:
            case ASSIGN_CASE_OFFICER:
              return roles.contains(PwaRegulatorRole.PWA_MANAGER);
            case CASE_OFFICER_REVIEW:
            case EDIT_CONSULTATIONS:
            case WITHDRAW_CONSULTATION:
            case REQUEST_APPLICATION_UPDATE:
            case EDIT_CONSENT_DOCUMENT:
              return roles.contains(PwaRegulatorRole.CASE_OFFICER);
            case VIEW_ALL_CONSULTATIONS:
              return roles.contains(PwaRegulatorRole.CASE_OFFICER)
                  || roles.contains(PwaRegulatorRole.PWA_MANAGER);
            case ASSIGN_RESPONDER:
              return consulteeGroupRoles.contains(ConsulteeGroupMemberRole.RECIPIENT)
                  || consulteeGroupRoles.contains(ConsulteeGroupMemberRole.RESPONDER);
            case CONSULTATION_RESPONDER:
              return consulteeGroupRoles.contains(ConsulteeGroupMemberRole.RESPONDER);
            case ADD_CASE_NOTE:
              return roles.contains(PwaRegulatorRole.PWA_MANAGER) || roles.contains(PwaRegulatorRole.CASE_OFFICER);
            default:
              return false;

          }

        })
        .collect(Collectors.toSet());

  }

}
