package uk.co.ogauthority.pwa.repository.consultations.search;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public interface ConsultationRequestSearchItemRepository extends CrudRepository<ConsultationRequestSearchItem, Integer> {

  @EntityGraph(attributePaths = "applicationDetailView")
  Page<ConsultationRequestSearchItem> getAllByConsultationRequestStatusIsAndConsulteeGroupIdEqualsOrConsultationRequestIdIn(
      Pageable pageable,
      ConsultationRequestStatus requestStatus,
      Integer cgIdsToFilterStatusBy,
      Collection<Integer> consultationRequestIds
  );

}
