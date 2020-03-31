package uk.co.ogauthority.pwa.service.licence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.repository.licence.PearsBlockRepository;

@Service
public class PearsBlockService {

  private final PearsBlockRepository pearsBlockRepository;

  @Autowired
  public PearsBlockService(PearsBlockRepository pearsBlockRepository) {
    this.pearsBlockRepository = pearsBlockRepository;
  }

}
