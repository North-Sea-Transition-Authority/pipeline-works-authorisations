package uk.co.ogauthority.pwa.service.pwaapplications;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;


@Service
public class PwaApplicationHolderService {

  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public PwaApplicationHolderService(PwaHolderTeamService pwaHolderTeamService) {
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public Set<PortalOrganisationGroup> getApplicationHolders(MasterPwa masterPwa) {
    return pwaHolderTeamService.getHolderOrgGroups(masterPwa);
  }

}
