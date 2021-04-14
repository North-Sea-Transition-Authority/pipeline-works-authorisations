package uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface ConsentReviewRepository extends CrudRepository<ConsentReview, Integer> {

  List<ConsentReview> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<ConsentReview> findAllByPwaApplicationDetailIn(Collection<PwaApplicationDetail> details);

}
