package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;

@Repository
public interface PadBundleRepository extends CrudRepository<PadBundle, Integer> {

  List<PadBundle> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

}
