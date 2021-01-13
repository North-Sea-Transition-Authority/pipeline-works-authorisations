package uk.co.ogauthority.pwa.service.search.consents;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;

@Service
public class ConsentSearchService {

  public static final int MAX_RESULTS_SIZE = 50;

  private final ConsentSearcher consentSearcher;

  @Autowired
  public ConsentSearchService(ConsentSearcher consentSearcher) {
    this.consentSearcher = consentSearcher;
  }

  public List<ConsentSearchResultView> search() {
    return consentSearcher.findAll().stream()
        .sorted(Comparator.comparing(ConsentSearchItem::getPwaId, Comparator.reverseOrder()))
        .limit(MAX_RESULTS_SIZE)
        .map(ConsentSearchResultView::fromSearchItem)
        .collect(Collectors.toList());
  }

  public boolean haveResultsBeenLimited(List<ConsentSearchResultView> searchResults) {
    return searchResults.size() == MAX_RESULTS_SIZE;
  }

}
