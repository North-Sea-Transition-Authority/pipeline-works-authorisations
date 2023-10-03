package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PearsLicenceApplicationService {

  private final PearsLicenceApplicationRepository applicationRepository;

  @Autowired
  public PearsLicenceApplicationService(PearsLicenceApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  public List<PearsLicenceApplication> getApplicationsByName(String name) {
    return applicationRepository.findAllByTransactionReferenceContainingIgnoreCase(name);
  }

  public List<PearsLicenceApplication> getApplicationsByIds(List<Integer> ids) {
    return applicationRepository.findAllByTransactionIdIn(ids);
  }

}
