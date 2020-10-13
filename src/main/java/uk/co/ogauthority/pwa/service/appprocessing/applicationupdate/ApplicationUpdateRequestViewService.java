package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.applicationupdates.ApplicationUpdateRequestStatus;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;

@Service
public class ApplicationUpdateRequestViewService {

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  @Autowired
  public ApplicationUpdateRequestViewService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
  }

  public Optional<ApplicationUpdateRequestView> getOpenRequestView(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository
        .findByPwaApplicationDetailAndStatus(pwaApplicationDetail, ApplicationUpdateRequestStatus.OPEN);
  }

  public Optional<ApplicationUpdateRequestView> getOpenRequestView(PwaApplication pwaApplication) {
    return applicationUpdateRequestRepository
        .findByPwaApplicationDetail_pwaApplicationAndStatus(pwaApplication, ApplicationUpdateRequestStatus.OPEN);
  }

}
