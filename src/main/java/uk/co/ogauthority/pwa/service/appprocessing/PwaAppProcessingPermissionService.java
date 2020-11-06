package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

@Service
public class PwaAppProcessingPermissionService {

  private final ApplicationInvolvementService applicationInvolvementService;

  @Autowired
  public PwaAppProcessingPermissionService(ApplicationInvolvementService applicationInvolvementService) {
    this.applicationInvolvementService = applicationInvolvementService;
  }

  public Set<PwaAppProcessingPermission> getProcessingPermissions(PwaApplication application,
                                                                  AuthenticatedUserAccount user) {

    var appInvolvement = applicationInvolvementService.getApplicationInvolvementDto(application, user);

    var userPrivileges = user.getUserPrivileges();
    var genericPermissions = getGenericProcessingPermissions(userPrivileges);

    var appPermissions = PwaAppProcessingPermission.streamAppPermissions()
        .filter(permission -> {

          switch (permission) {
            case UPDATE_APPLICATION:
              return appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER);
            case CASE_MANAGEMENT_CONSULTEE:
              return !appInvolvement.getConsulteeRoles().isEmpty();
            case CASE_MANAGEMENT_INDUSTRY:
              return !appInvolvement.getContactRoles().isEmpty();
            case CASE_MANAGEMENT_OGA:
              return userPrivileges.contains(PwaUserPrivilege.PWA_REGULATOR);
            case ASSIGN_RESPONDER:
              return appInvolvement.hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER);
            case CONSULTATION_RESPONDER:
              return appInvolvement.hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole.RESPONDER)
                  && appInvolvement.isAssignedAtResponderStage();
            case CASE_OFFICER_REVIEW:
            case EDIT_CONSULTATIONS:
            case PUBLIC_NOTICE:
            case WITHDRAW_CONSULTATION:
            case REQUEST_APPLICATION_UPDATE:
            case EDIT_CONSENT_DOCUMENT:
              return userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER) && appInvolvement.isCaseOfficerStageAndUserAssigned();
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

  public Set<PwaAppProcessingPermission> getGenericProcessingPermissions(AuthenticatedUserAccount user) {
    return getGenericProcessingPermissions(user.getUserPrivileges());
  }

  private Set<PwaAppProcessingPermission> getGenericProcessingPermissions(Collection<PwaUserPrivilege> userPrivileges) {

    return PwaAppProcessingPermission.streamGenericPermissions()
        .filter(permission -> {

          switch (permission) {

            case ACCEPT_INITIAL_REVIEW:
            case ASSIGN_CASE_OFFICER:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER);
            case VIEW_ALL_CONSULTATIONS:
            case ADD_CASE_NOTE:
              return userPrivileges.contains(PwaUserPrivilege.PWA_MANAGER)
                  || userPrivileges.contains(PwaUserPrivilege.PWA_CASE_OFFICER);
            default:
              return false;

          }

        })
        .collect(Collectors.toSet());

  }

}
