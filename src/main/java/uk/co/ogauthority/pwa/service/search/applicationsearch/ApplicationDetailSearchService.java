package uk.co.ogauthority.pwa.service.search.applicationsearch;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailViewRepository;

/**
 * Service which consumes search criteria and uses that to filter applications results for all purposes.
 */
@Service
public class ApplicationDetailSearchService {

  private final ApplicationDetailViewRepository applicationDetailViewRepository;

  @Autowired
  public ApplicationDetailSearchService(ApplicationDetailViewRepository applicationDetailViewRepository) {
    this.applicationDetailViewRepository = applicationDetailViewRepository;
  }

  public List<ApplicationDetailItemView> getAllTipApplicationDetails() {
    // copying list allows return type of list to be simple interface, not an ? extends so we can ignore impl type.
    return List.copyOf(applicationDetailViewRepository.findApplicationDetailViewByTipFlagIsTrue());
  }


}
