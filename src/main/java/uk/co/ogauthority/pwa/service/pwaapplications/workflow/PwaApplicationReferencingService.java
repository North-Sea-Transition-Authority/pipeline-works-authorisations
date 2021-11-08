package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;

/** Only deals with generating references for applications. */
@Service
public class PwaApplicationReferencingService {

  private final PwaApplicationRepository pwaApplicationRepository;

  @Autowired
  public PwaApplicationReferencingService(
      PwaApplicationRepository pwaApplicationRepository) {
    this.pwaApplicationRepository = pwaApplicationRepository;
  }

  protected String createAppReference() {
    long refSeq = pwaApplicationRepository.getNextRefNum();
    String appRef = "PA/" + refSeq;
    return appRef;
  }


}
