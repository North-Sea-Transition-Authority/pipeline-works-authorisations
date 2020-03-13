package uk.co.ogauthority.pwa.service.licence;

import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.licence.PedLicence;
import uk.co.ogauthority.pwa.repository.licence.PedLicenceRepository;

@Service
public class PedLicenceService {

  private final PedLicenceRepository pedLicenceRepository;

  @Autowired
  public PedLicenceService(PedLicenceRepository pedLicenceRepository) {
    this.pedLicenceRepository = pedLicenceRepository;
  }

  public List<PedLicence> getLicencesByName(String name) {
    return pedLicenceRepository.findAllByLicenceNameLikeIgnoreCase(name);
  }

  public List<PedLicence> getAllLicences() {
    return IterableUtils.toList(pedLicenceRepository.findAll());
  }

}
