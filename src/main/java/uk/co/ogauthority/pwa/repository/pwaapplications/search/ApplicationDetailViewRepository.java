package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;

public interface ApplicationDetailViewRepository extends CrudRepository<ApplicationDetailView, Integer> {

  Optional<ApplicationDetailView> findByPwaApplicationDetailId(int appDetailId);

}