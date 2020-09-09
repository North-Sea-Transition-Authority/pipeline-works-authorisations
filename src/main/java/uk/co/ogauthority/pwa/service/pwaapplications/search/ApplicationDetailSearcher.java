package uk.co.ogauthority.pwa.service.pwaapplications.search;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class ApplicationDetailSearcher {

  private final ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository;

  @Autowired
  public ApplicationDetailSearcher(ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository) {
    this.applicationDetailSearchItemRepository = applicationDetailSearchItemRepository;
  }

  public Page<ApplicationDetailSearchItem> searchByStatus(Pageable pageable,
                                                          Set<PwaApplicationStatus> statusFilter) {
    if (statusFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return applicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPadStatusIn(
        pageable,
        statusFilter
    );
  }

  public Page<ApplicationDetailSearchItem> searchByStatusOrApplicationIds(Pageable pageable,
                                                                          Set<PwaApplicationStatus> statusFilter,
                                                                          Set<Integer> pwaApplicationIdFilter) {

    if (statusFilter.isEmpty() && pwaApplicationIdFilter.isEmpty()) {
      return Page.empty(pageable);
    }

    return applicationDetailSearchItemRepository.findAllByPadStatusInOrPwaApplicationIdIn(
        pageable,
        statusFilter,
        pwaApplicationIdFilter
    );

  }

  public Optional<ApplicationDetailSearchItem> searchByApplicationDetailId(Integer pwaApplicationDetailId) {
    return applicationDetailSearchItemRepository.findByPwaApplicationDetailIdEquals(pwaApplicationDetailId);
  }

}
