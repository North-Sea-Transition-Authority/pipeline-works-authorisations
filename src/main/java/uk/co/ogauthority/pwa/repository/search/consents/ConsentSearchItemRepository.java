package uk.co.ogauthority.pwa.repository.search.consents;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;

@Repository
public interface ConsentSearchItemRepository extends CrudRepository<ConsentSearchItem, Integer> {

}
