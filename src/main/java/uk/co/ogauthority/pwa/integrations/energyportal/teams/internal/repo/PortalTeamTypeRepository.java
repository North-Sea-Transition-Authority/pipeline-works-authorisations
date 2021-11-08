package uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.repo;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity.PortalTeamType;

public interface PortalTeamTypeRepository extends CrudRepository<PortalTeamType, Integer> {
}
