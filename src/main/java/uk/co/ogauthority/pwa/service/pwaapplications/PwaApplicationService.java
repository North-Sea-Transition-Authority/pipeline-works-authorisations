package uk.co.ogauthority.pwa.service.pwaapplications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;

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

}
