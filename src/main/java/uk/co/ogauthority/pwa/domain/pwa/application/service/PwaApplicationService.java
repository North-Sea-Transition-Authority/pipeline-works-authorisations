package uk.co.ogauthority.pwa.domain.pwa.application.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

/**
 * Service to retrieve and manage top level PwaApplication data.
 */
@Service
public class PwaApplicationService {

  private final PwaApplicationRepository pwaApplicationRepository;

  @Autowired
  public PwaApplicationService(PwaApplicationRepository pwaApplicationRepository) {
    this.pwaApplicationRepository = pwaApplicationRepository;
  }

  public PwaApplication getApplicationFromId(int applicationId) {
    return pwaApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find application with id " + applicationId));
  }

  public List<PwaApplication> getAllApplicationsForMasterPwa(MasterPwa masterPwa) {
    return pwaApplicationRepository.findAllByMasterPwa(masterPwa);
  }

  @Transactional
  public void updateApplicantOrganisationUnitId(PwaApplication pwaApplication, PortalOrganisationUnit organisationUnit) {
    pwaApplication.setApplicantOrganisationUnitId(OrganisationUnitId.from(organisationUnit));
    pwaApplicationRepository.save(pwaApplication);
  }

}
