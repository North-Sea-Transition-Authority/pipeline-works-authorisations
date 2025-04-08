package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;

@Profile("test-harness")
@Service
public class TestHarnessOrganisationUnitService {
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public TestHarnessOrganisationUnitService(PwaHolderTeamService pwaHolderTeamService) {
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public PortalOrganisationUnit getFirstOrgUnitUserCanAccessOrThrow(WebUserAccount webUserAccount) {

    return pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
            webUserAccount,
            Set.of(Role.APPLICATION_CREATOR)
        )
        .stream()
        .filter(PortalOrganisationUnit::isActive)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "User with WUA ID: %d does not have access to any organisation units".formatted(webUserAccount.getWuaId())
        ));
  }
}
