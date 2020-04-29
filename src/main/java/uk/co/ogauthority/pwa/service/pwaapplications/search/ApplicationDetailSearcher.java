package uk.co.ogauthority.pwa.service.pwaapplications.search;

import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;

@Service
public class ApplicationDetailSearcher {

  private final EntityManager entityManager;
  private final ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository;

  @Autowired
  public ApplicationDetailSearcher(EntityManager entityManager,
                                   ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository) {
    this.entityManager = entityManager;
    this.applicationDetailSearchItemRepository = applicationDetailSearchItemRepository;
  }

  public Page<ApplicationDetailSearchItem> search(Pageable pageable,
                                                  Set<PwaApplicationContactRoleDto> contactFilter) {
    if (contactFilter.isEmpty()) {
      return Page.empty();
    }

    var filterApplicationIds = contactFilter.stream()
        .map(PwaApplicationContactRoleDto::getPwaApplicationId)
        .collect(Collectors.toSet());

    return applicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPwaApplicationIdIn(
        pageable,
        filterApplicationIds
    );
  }
}
