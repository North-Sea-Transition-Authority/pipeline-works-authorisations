package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;

@Repository
public interface PadBundleLinkRepository extends CrudRepository<PadBundleLink, Integer> {

  List<PadBundleLink> getAllByBundle(PadBundle bundle);

}
