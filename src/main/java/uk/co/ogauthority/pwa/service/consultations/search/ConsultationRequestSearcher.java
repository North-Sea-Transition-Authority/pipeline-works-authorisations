package uk.co.ogauthority.pwa.service.consultations.search;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.repository.consultations.search.ConsultationRequestSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

@Service
public class ConsultationRequestSearcher {

  private final ConsultationRequestSearchItemRepository consultationRequestSearchItemRepository;

  @Autowired
  public ConsultationRequestSearcher(ConsultationRequestSearchItemRepository consultationRequestSearchItemRepository) {
    this.consultationRequestSearchItemRepository = consultationRequestSearchItemRepository;
  }

  public Page<ConsultationRequestSearchItem> searchByStatusForGroupIdsOrConsultationRequestIds(Pageable pageable,
                                                                                               ConsultationRequestStatus requestStatus,
                                                                                               Set<Integer> cgIdsToGetStatusRequestsFor,
                                                                                               Set<Integer> consultationRequestIdList) {

    if (cgIdsToGetStatusRequestsFor.isEmpty() && consultationRequestIdList.isEmpty()) {
      return Page.empty(pageable);
    }

    return consultationRequestSearchItemRepository.getAllByConsultationRequestStatusIsAndConsulteeGroupIdIsInOrConsultationRequestIdIn(
        pageable,
        requestStatus,
        cgIdsToGetStatusRequestsFor,
        consultationRequestIdList
    );

  }

}
