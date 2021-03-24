package uk.co.ogauthority.pwa.service.users;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

@Service
public class UserTypeService {

  public UserType getPriorityUserType(AuthenticatedUserAccount authenticatedUserAccount) {

    return getUserTypes(authenticatedUserAccount)
        .stream()
        .max(Comparator.comparing(UserType::getPriority))
        .orElseThrow(() -> new IllegalStateException(
                String.format(
                    "User with WUA ID: %s doesn't match a recognised user type.",
                    authenticatedUserAccount.getWuaId()
                )
            )
        );


  }

  public Set<UserType> getUserTypes(AuthenticatedUserAccount authenticatedUserAccount) {
    return UserType.stream()
        .filter(userType -> authenticatedUserAccount.hasPrivilege(userType.getQualifyingPrivilege()))
        .collect(Collectors.toUnmodifiableSet());
  }

}
