package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PearsLicenceTransactionService {

  private final PearsLicenceTransactionRepository applicationRepository;

  @Autowired
  public PearsLicenceTransactionService(PearsLicenceTransactionRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  public List<PearsLicenceTransactions> getApplicationsByName(String name) {
    return applicationRepository.findAllByTransactionReferenceContainingIgnoreCase(name);
  }

  public List<PearsLicenceTransactions> getApplicationsByIds(List<Integer> ids) {
    return applicationRepository.findAllByTransactionIdIn(ids);
  }

}
