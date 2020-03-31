package uk.co.ogauthority.pwa.service.licence;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.repository.licence.PearsLicenceRepository;

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
