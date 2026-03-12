package uk.co.ogauthority.pwa.user;

import uk.co.ogauthority.pwa.teams.Team;

public interface AllowedDomainService {

  boolean isAllowedDomain(String domain, Team team);
}
