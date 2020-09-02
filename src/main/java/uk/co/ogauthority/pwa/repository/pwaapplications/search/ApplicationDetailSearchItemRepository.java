package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public interface ApplicationDetailSearchItemRepository extends CrudRepository<ApplicationDetailSearchItem, Integer> {


  Page<ApplicationDetailSearchItem> findAllByTipFlagIsTrueAndPwaApplicationIdIn(Pageable pageable,
                                                                                Collection<Integer> pwaApplicationIds);

  Page<ApplicationDetailSearchItem> findAllByTipFlagIsTrueAndPadStatusIn(Pageable pageable,
                                                                         Collection<PwaApplicationStatus> statusFilter);

  Page<ApplicationDetailSearchItem> findAllByTipFlagIsTrueAndPadStatusInOrPwaApplicationIdIn(Pageable pageable,
                                                                                             Collection<PwaApplicationStatus> statusFilter,
                                                                                             Collection<Integer> applicationIdFilter);

  Optional<ApplicationDetailSearchItem> findByPwaApplicationDetailIdEquals(Integer pwaApplicationDetailId);

}
