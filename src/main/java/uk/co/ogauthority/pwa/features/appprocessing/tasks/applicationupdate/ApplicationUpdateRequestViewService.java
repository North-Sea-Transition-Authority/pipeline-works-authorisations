package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;

import java.util.Comparator;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class ApplicationUpdateRequestViewService {

  private final ApplicationUpdateRequestViewRepository applicationUpdateRequestViewRepository;

  @Autowired
  public ApplicationUpdateRequestViewService(ApplicationUpdateRequestViewRepository applicationUpdateRequestViewRepository) {
    this.applicationUpdateRequestViewRepository = applicationUpdateRequestViewRepository;
  }

  public Optional<ApplicationUpdateRequestView> getOpenRequestView(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestViewRepository
        .findByPwaApplicationDetailAndStatus(pwaApplicationDetail, ApplicationUpdateRequestStatus.OPEN);
  }

  public Optional<ApplicationUpdateRequestView> getOpenRequestView(PwaApplication pwaApplication) {
    return applicationUpdateRequestViewRepository
        .findByPwaApplicationDetail_pwaApplicationAndStatus(pwaApplication, ApplicationUpdateRequestStatus.OPEN);
  }

  public Optional<ApplicationUpdateRequestView> getLastRespondedApplicationUpdateView(PwaApplicationDetail pwaApplicationDetail) {

    return applicationUpdateRequestViewRepository
        .findAllByPwaApplicationDetail_pwaApplicationAndStatus(
            pwaApplicationDetail.getPwaApplication(),
            ApplicationUpdateRequestStatus.RESPONDED)
        .stream()
        .filter(applicationUpdateRequestView ->
            pwaApplicationDetail.getVersionNo() > applicationUpdateRequestView.getRequestedOnApplicationVersionNo()
        )
        .max(Comparator.comparing(ApplicationUpdateRequestView::getRequestedOnApplicationVersionNo));
  }

}
