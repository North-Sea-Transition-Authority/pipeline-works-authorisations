package uk.co.ogauthority.pwa.service.consultations;


import static java.util.stream.Collectors.groupingBy;

import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;

/**
 * Service to provide aggregated views of consultations status counts.
 */
@Service
class ConsultationsStatusViewFactory {

  private final ConsultationRequestRepository consultationRequestRepository;

  public ConsultationsStatusViewFactory(ConsultationRequestRepository consultationRequestRepository) {
    this.consultationRequestRepository = consultationRequestRepository;
  }

  ApplicationConsultationStatusView getApplicationStatusView(PwaApplication pwaApplication) {

    var consultationRequests = consultationRequestRepository
        .findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication);

    var statusCounts = consultationRequests.stream()
        .collect(groupingBy(
            ConsultationRequest::getStatus, Collectors.counting()
        ));

    return new ApplicationConsultationStatusView(statusCounts);
  }
}
