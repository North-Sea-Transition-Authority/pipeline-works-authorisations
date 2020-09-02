package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;

@Service
public class ApplicationUpdateRequestService {

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;
  private final Clock clock;

  @Autowired
  public ApplicationUpdateRequestService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository,
                                         @Qualifier("utcClock") Clock clock) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
    this.clock = clock;
  }


  @Transactional
  public void createApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                             Person requestingPerson,
                                             String requestReason) {
    var updateRequest = ApplicationUpdateRequest.createRequest(
        pwaApplicationDetail,
        requestingPerson,
        clock,
        requestReason);

    applicationUpdateRequestRepository.save(updateRequest);

  }

  // TODO PWA-161 implements submission of app updates and therefore should determine how update requests go from opened to closed.
  public boolean applicationDetailHasOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.existsByPwaApplicationDetail(pwaApplicationDetail);
  }
}
