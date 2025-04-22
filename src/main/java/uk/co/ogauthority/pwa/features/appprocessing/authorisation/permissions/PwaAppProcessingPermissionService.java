package uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class PwaAppProcessingPermissionService {

  private final ApplicationInvolvementService applicationInvolvementService;
  private final HasTeamRoleService hasTeamRoleService;
  private final UserTypeService userTypeService;

  @Autowired
  public PwaAppProcessingPermissionService(ApplicationInvolvementService applicationInvolvementService,
                                           HasTeamRoleService hasTeamRoleService,
                                           UserTypeService userTypeService) {
    this.applicationInvolvementService = applicationInvolvementService;
    this.hasTeamRoleService = hasTeamRoleService;
    this.userTypeService = userTypeService;
  }

  public ProcessingPermissionsDto getProcessingPermissionsDto(PwaApplicationDetail detail,
                                                              AuthenticatedUserAccount user) {

    var appInvolvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    var usersUserTypes = userTypeService.getUserTypes(user);
    var userIsCaseOfficer = hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER));
    var userIsPwaManager = hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER));

    var genericPermissions = getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);

    var applicationTypeisOptionsVariation = PwaApplicationType.OPTIONS_VARIATION.equals(detail.getPwaApplicationType());
    var applicationTypeIsInitialOrCat1Variation =
        Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION).contains(detail.getPwaApplicationType());

    var appPermissions = PwaAppProcessingPermission.streamAppPermissions()
        .filter(permission -> switch (permission) {
          case UPDATE_APPLICATION -> appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER);
          case CASE_MANAGEMENT_CONSULTEE -> !appInvolvement.getConsultationInvolvement()
              .map(ConsultationInvolvementDto::getConsulteeRoles)
              .orElse(Set.of())
              .isEmpty();
          // app contacts can only access in progress apps, once complete only holder team users can access
          case CASE_MANAGEMENT_INDUSTRY ->
              (appInvolvement.isUserInAppContactTeam() && detail.getStatus() != PwaApplicationStatus.COMPLETE)
                  || appInvolvement.isUserInHolderTeam();
          case PAY_FOR_APPLICATION -> (
              PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT.equals(detail.getStatus())
                  && (appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER, PwaContactRole.ACCESS_MANAGER)
                      || appInvolvement.hasAnyOfTheseHolderRoles(Role.FINANCE_ADMIN))
          );
          case VIEW_PAYMENT_DETAILS_IF_EXISTS -> appInvolvement.isUserInAppContactTeam() || appInvolvement.isUserInHolderTeam();
          case APPROVE_OPTIONS_VIEW -> appInvolvement.isUserInAppContactTeam() && applicationTypeisOptionsVariation;
          case CASE_MANAGEMENT_OGA -> usersUserTypes.contains(UserType.OGA);
          case ASSIGN_RESPONDER -> appInvolvement.hasAnyOfTheseConsulteeRoles(
              ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER);
          case CONSULTATION_RESPONDER -> appInvolvement.hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole.RESPONDER)
              && appInvolvement.getConsultationInvolvement()
              .map(ConsultationInvolvementDto::isAssignedToResponderStage)
              .orElse(false);
          case CONSULTEE_ADVICE -> !appInvolvement.getConsultationInvolvement()
              .map(ConsultationInvolvementDto::getHistoricalRequests)
              .orElse(List.of())
              .isEmpty();
          case APPROVE_OPTIONS, CLOSE_OUT_OPTIONS -> userIsCaseOfficer
              && appInvolvement.isUserAssignedCaseOfficer()
              && applicationTypeisOptionsVariation;
          case CHANGE_OPTIONS_APPROVAL_DEADLINE -> userIsPwaManager
              && applicationTypeisOptionsVariation;
          case VIEW_ALL_PUBLIC_NOTICES, OGA_EDIT_PUBLIC_NOTICE -> (userIsPwaManager || userIsCaseOfficer)
              && applicationTypeIsInitialOrCat1Variation;
          case DRAFT_PUBLIC_NOTICE, REQUEST_PUBLIC_NOTICE_UPDATE, WITHDRAW_PUBLIC_NOTICE, FINALISE_PUBLIC_NOTICE ->
              userIsCaseOfficer
                  && appInvolvement.isUserAssignedCaseOfficer()
                  && applicationTypeIsInitialOrCat1Variation;
          case APPROVE_PUBLIC_NOTICE -> userIsPwaManager
              && applicationTypeIsInitialOrCat1Variation;
          case VIEW_PUBLIC_NOTICE, UPDATE_PUBLIC_NOTICE_DOC -> usersUserTypes.contains(UserType.INDUSTRY)
              && applicationTypeIsInitialOrCat1Variation;
          case CASE_OFFICER_REVIEW, CONFIRM_SATISFACTORY_APPLICATION, EDIT_CONSULTATIONS, WITHDRAW_CONSULTATION,
               SEND_CONSENT_FOR_APPROVAL -> userIsCaseOfficer && appInvolvement.isUserAssignedCaseOfficer();
          case EDIT_CONSENT_DOCUMENT -> (userIsCaseOfficer && appInvolvement.isUserAssignedCaseOfficer())
              || userIsPwaManager;
          case VIEW_CONSENT_DOCUMENT ->
              (hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.CONSENT_SEARCH.getRolesByTeamType())
                  || usersUserTypes.contains(UserType.OGA)
                  || appInvolvement.isUserInHolderTeam());
          case REQUEST_APPLICATION_UPDATE, WITHDRAW_APPLICATION ->
              (userIsCaseOfficer && appInvolvement.isUserAssignedCaseOfficer())
                  || (userIsPwaManager && appInvolvement.isPwaManagerStage());
          case CANCEL_PAYMENT -> PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT.equals(detail.getStatus())
              && userIsPwaManager;
          case MANAGE_APPLICATION_CONTACTS -> (
              appInvolvement.hasAnyOfTheseHolderRoles(PwaApplicationPermission.MANAGE_CONTACTS.getHolderTeamRoles())
                  || appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.ACCESS_MANAGER)
          ) && !ApplicationState.COMPLETED.includes(detail.getStatus());
          default -> false;
        })
        .collect(Collectors.toSet());

    // any user with a case management permission can view the app summary
    if (appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA)
        || appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || appPermissions.contains(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE)) {
      appPermissions.add(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);
    }

    return new ProcessingPermissionsDto(appInvolvement, SetUtils.union(genericPermissions, appPermissions));

  }

  public Set<PwaAppProcessingPermission> getGenericProcessingPermissions(boolean userIsCaseOfficer, boolean userIsPwaManager) {

    return PwaAppProcessingPermission.streamGenericPermissions()
        .filter(permission -> switch (permission) {
          case ACCEPT_INITIAL_REVIEW, ASSIGN_CASE_OFFICER, CONSENT_REVIEW -> userIsPwaManager;
          case VIEW_ALL_CONSULTATIONS, ADD_CASE_NOTE -> userIsPwaManager || userIsCaseOfficer;
          case SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY -> userIsPwaManager && !userIsCaseOfficer;
          default -> false;
        })
        .collect(Collectors.toSet());
  }

}
