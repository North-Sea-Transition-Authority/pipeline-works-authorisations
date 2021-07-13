package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;

@Repository
public interface PadVersionLookupRepository extends CrudRepository<PadVersionLookup, Integer> {

  Optional<PadVersionLookup> findByPwaApplicationId(int applicationId);

}