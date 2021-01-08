package uk.co.ogauthority.pwa.repository.pwaapplications.search;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;

public interface ApplicationDetailViewRepository extends CrudRepository<ApplicationDetailView, Integer> {


  List<ApplicationDetailView> findApplicationDetailViewByTipFlagIsTrue();

}