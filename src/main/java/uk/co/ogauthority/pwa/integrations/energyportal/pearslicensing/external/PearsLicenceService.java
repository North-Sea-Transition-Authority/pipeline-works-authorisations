package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal.PearsLicenceRepository;

@Service
public class PearsLicenceService {

  private final PearsLicenceRepository pearsLicenceRepository;

  @Autowired
  public PearsLicenceService(PearsLicenceRepository pearsLicenceRepository) {
    this.pearsLicenceRepository = pearsLicenceRepository;
  }

  public List<PearsLicence> getLicencesByName(String name) {
    return pearsLicenceRepository.findAllByLicenceNameContainingIgnoreCase(name);
  }

  public PearsLicence getByMasterId(Integer masterId) {
    return pearsLicenceRepository.findByMasterId(masterId)
        .orElseThrow(() -> new EntityNotFoundException(String.format("No licence with PLM_ID of %s", masterId)));
  }

  public List<PearsLicence> getAllLicences() {
    return IterableUtils.toList(pearsLicenceRepository.findAll());
  }

}
