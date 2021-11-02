package uk.co.ogauthority.pwa.service.pwaapplications.shared.applicantorganisation;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@Service
public class ApplicantOrganisationService {

  private final PwaHolderService pwaHolderService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public ApplicantOrganisationService(PwaHolderService pwaHolderService,
                                      PwaHolderTeamService pwaHolderTeamService) {
    this.pwaHolderService = pwaHolderService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public Set<PortalOrganisationUnit> getPotentialApplicantOrganisations(MasterPwa masterPwa, WebUserAccount webUserAccount) {

    var holderOrgUnits = pwaHolderService.getPwaHolderOrgUnits(masterPwa);

    var orgUnitsHolderHasAccessTo = new HashSet<>(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(
        webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR));

    return Sets.intersection(holderOrgUnits, orgUnitsHolderHasAccessTo);

  }

}
