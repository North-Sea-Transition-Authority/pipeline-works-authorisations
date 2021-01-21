package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailViewRepository;

@Service
public class CaseSummaryViewService {

  private final ApplicationDetailViewRepository applicationDetailViewRepository;

  @Autowired
  public CaseSummaryViewService(ApplicationDetailViewRepository applicationDetailViewRepository) {
    this.applicationDetailViewRepository = applicationDetailViewRepository;
  }

  public Optional<CaseSummaryView> getCaseSummaryViewForAppDetail(PwaApplicationDetail detail) {

    return applicationDetailViewRepository.findByPwaApplicationDetailId(detail.getId())
        .map(CaseSummaryView::from);

  }

}
