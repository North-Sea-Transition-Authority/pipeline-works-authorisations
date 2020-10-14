package uk.co.ogauthority.pwa.service.users;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

@Service
public class UserTypeService {

  public UserType getUserType(AuthenticatedUserAccount authenticatedUserAccount) {

    var userPrivileges = authenticatedUserAccount.getUserPrivileges();

    if (userPrivileges.contains(PwaUserPrivilege.PWA_INDUSTRY)) {
      return UserType.INDUSTRY;
    }

    if (userPrivileges.contains(PwaUserPrivilege.PWA_REGULATOR)) {
      return UserType.OGA;
    }

    if (userPrivileges.contains(PwaUserPrivilege.PWA_CONSULTEE)) {
      return UserType.CONSULTEE;
    }

    throw new IllegalStateException(
        String.format("User with WUA ID: %s doesn't match a recognised user type.", authenticatedUserAccount.getWuaId()));

  }

}
