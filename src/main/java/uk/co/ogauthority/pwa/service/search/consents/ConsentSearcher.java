package uk.co.ogauthority.pwa.service.search.consents;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.repository.search.consents.ConsentSearchItemRepository;

@Service
public class ConsentSearcher {

  private final ConsentSearchItemRepository consentSearchItemRepository;

  @Autowired
  public ConsentSearcher(ConsentSearchItemRepository consentSearchItemRepository) {
    this.consentSearchItemRepository = consentSearchItemRepository;
  }

  public List<ConsentSearchItem> findAll() {
    return (List<ConsentSearchItem>) consentSearchItemRepository.findAll();
  }

}
