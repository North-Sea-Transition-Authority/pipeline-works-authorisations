package uk.co.ogauthority.pwa.service.teams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalSystemPrivilegeDto;

/**
 * Converts output from PortalTeams Service layer into Objects useful for the application.
 */
@Service
// TODO: Remove in PWARE-60
public class PwaTeamsDtoFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaTeamsDtoFactory.class);

  /**
   * Consume collection of PortalSystemPrivilegeDtos (which may contains duplicates through membership of multiple teams and roles)
   * and return a list.
   */
  public Set<PwaUserPrivilege> createPwaUserPrivilegeSet(Collection<PortalSystemPrivilegeDto> portalSystemPrivilegeDtos) {

    Set<PwaUserPrivilege> privileges = new HashSet<>();

    for (PortalSystemPrivilegeDto portalSystemPrivDto: portalSystemPrivilegeDtos) {
      try {
        var pwaPriv = PwaUserPrivilege.valueOf(portalSystemPrivDto.getGrantedPrivilege());
        privileges.add(pwaPriv);
      } catch (IllegalArgumentException e) {
        LOGGER.debug("Unknown priv '{}' found when mapping portal privs to the PwaUserPrivilege enum. This priv has been ignored.",
            portalSystemPrivDto.getGrantedPrivilege());
      }
    }

    return privileges;

  }

}
