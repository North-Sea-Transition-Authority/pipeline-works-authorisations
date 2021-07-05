package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class PwaAppProcessingPermissionService {

  private final ApplicationInvolvementService applicationInvolvementService;

  @Autowired
  public PwaAppProcessingPermissionService(ApplicationInvolvementService applicationInvolvementService) {
    this.applicationInvolvementService = applicationInvolvementService;
  }

  public ProcessingPermissionsDto getProcessingPermissionsDto(PwaApplicationDetail detail,
                                                              AuthenticatedUserAccount user) {

    var appInvolvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    var userPrivileges = user.getUserPrivileges();
    var genericPermissions = getGenericProcessingPermissions(userPrivileges);

    var appPermissions = PwaAppProcessingPermission.streamAppPermissions()
        .filter(permission -> {

          switch (permission) {
            case UPDATE_APPLICATION:
              return appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER);
            case CASE_MANAGEMENT_CONSULTEE:
              return !appInvolvement.getConsultationInvolvement()
                  .map(ConsultationInvolvementDto::getConsulteeRoles)
                  .orElse(Set.of())
                  .isEmpty();
            // app contacts can only access in progress apps, once complete only holder team users can access
            case CASE_MANAGEMENT_INDUSTRY:
              return (!appInvolvement.getContactRoles().isEmpty() && detail.getStatus() != PwaApplicationStatus.COMPLETE)
                  || appInvolvement.isUserInHolderTeam();
            case PAY_FOR_APPLICATION: return (
                PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT.equals(detail.getStatus())
                && (
                    appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER, PwaContactRole.ACCESS_MANAGER)
                    || appInvolvement.hasAnyOfTheseHolderRoles(PwaOrganisationRole.FINANCE_ADMIN)
                )
            );
            case VIEW_PAYMENT_DETAILS_IF_EXISTS:
              return appInvolvement.isUserInAppContactTeam() || appInvolvement.isUserInHolderTeam();

            case APPROVE_OPTIONS_VIEW:
              return !appInvolvement.getContactRoles().isEmpty()
                  && PwaApplicationType.OPTIONS_VARIATION.equals(detail.getPwaApplicationType());
            case CASE_MANAGEMENT_OGA:
              return userPrivileges.contains(PwaUserPrivilege.PWA_REGULATOR);
            case ASSIGN_RESPONDER:
              return appInvolvement.hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole.RECIPIENT,
                  ConsulteeGroupMemberRole.RESPONDER);
            case CONSULTATION_RESPONDER:
              return appInvolvement.hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole.RESPONDER)
                  && appInvolvement.getConsultationInvolvement()
                        .map(ConsultationInvolvementDto::isAssignedToResponderStage)
                        .orElse(false);
            case CONSULTEE_ADVICE:
              return !appInvolvement.getConsultationInvolvement()
                  .map(ConsultationInvolvementDto::getHistoricalRequests)
                  .orElse(List.of())
                  .isEmpty();
            case APPROVE_OPTIONS:
            case CLOSE_OUT_OPTIONS:
              return userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER)
                  && appInvolvement.isUserAssignedCaseOfficer()
                  && PwaApplicationType.OPTIONS_VARIATION.equals(detail.getPwaApplicationType());
            case CHANGE_OPTIONS_APPROVAL_DEADLINE:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER)
                  && PwaApplicationType.OPTIONS_VARIATION.equals(detail.getPwaApplicationType());
            case VIEW_ALL_PUBLIC_NOTICES:
              return (userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER))
                  && (PwaApplicationType.INITIAL.equals(detail.getPwaApplicationType())
                  || PwaApplicationType.CAT_1_VARIATION.equals(detail.getPwaApplicationType()));
            case DRAFT_PUBLIC_NOTICE:
            case REQUEST_PUBLIC_NOTICE_UPDATE:
            case WITHDRAW_PUBLIC_NOTICE:
            case FINALISE_PUBLIC_NOTICE:
              return userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER)
                  && appInvolvement.isUserAssignedCaseOfficer()
                  && (PwaApplicationType.INITIAL.equals(detail.getPwaApplicationType())
                  || PwaApplicationType.CAT_1_VARIATION.equals(detail.getPwaApplicationType()));
            case APPROVE_PUBLIC_NOTICE:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER)
                  && (PwaApplicationType.INITIAL.equals(detail.getPwaApplicationType())
                  || PwaApplicationType.CAT_1_VARIATION.equals(detail.getPwaApplicationType()));
            case VIEW_PUBLIC_NOTICE:
            case UPDATE_PUBLIC_NOTICE_DOC:
              return userPrivileges.contains(PwaUserPrivilege.PWA_INDUSTRY)
                  && (PwaApplicationType.INITIAL.equals(detail.getPwaApplicationType())
                  || PwaApplicationType.CAT_1_VARIATION.equals(detail.getPwaApplicationType()));
            case CASE_OFFICER_REVIEW:
            case CONFIRM_SATISFACTORY_APPLICATION:
            case EDIT_CONSULTATIONS:
            case WITHDRAW_CONSULTATION:
            case SEND_CONSENT_FOR_APPROVAL:
              return userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER) && appInvolvement.isUserAssignedCaseOfficer();
            case EDIT_CONSENT_DOCUMENT:
              return (userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER) && appInvolvement.isUserAssignedCaseOfficer())
                  || userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER);
            case VIEW_CONSENT_DOCUMENT:
              return (userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_CONSENT_SEARCH)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_REGULATOR)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_REG_ORG_MANAGE)
                  || appInvolvement.isUserInHolderTeam());
            case REQUEST_APPLICATION_UPDATE:
            case WITHDRAW_APPLICATION:
              return (userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER) && appInvolvement.isUserAssignedCaseOfficer())
                  || (userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER) && appInvolvement.isPwaManagerStage());
            case CANCEL_PAYMENT:
              return PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT.equals(detail.getStatus())
                  && userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER);
            case MANAGE_APPLICATION_CONTACTS:
              return
                  (
                      appInvolvement.hasAnyOfTheseHolderRoles(PwaApplicationPermission.MANAGE_CONTACTS.getHolderTeamRoles())
                      || appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.ACCESS_MANAGER)
                  ) && !ApplicationState.COMPLETED.includes(detail.getStatus());
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

    return new ProcessingPermissionsDto(appInvolvement, SetUtils.union(genericPermissions, appPermissions));

  }

  public Set<PwaAppProcessingPermission> getGenericProcessingPermissions(AuthenticatedUserAccount user) {
    return getGenericProcessingPermissions(user.getUserPrivileges());
  }

  private Set<PwaAppProcessingPermission> getGenericProcessingPermissions(Collection<PwaUserPrivilege> userPrivileges) {

    return PwaAppProcessingPermission.streamGenericPermissions()
        .filter(permission -> {

          switch (permission) {

            case ACCEPT_INITIAL_REVIEW:
            case ASSIGN_CASE_OFFICER:
            case CONSENT_REVIEW:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER);
            case VIEW_ALL_CONSULTATIONS:
            case ADD_CASE_NOTE:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER);
            case SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER) && !userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER);
            default:
              return false;
          }
        })
        .collect(Collectors.toSet());
  }

}
