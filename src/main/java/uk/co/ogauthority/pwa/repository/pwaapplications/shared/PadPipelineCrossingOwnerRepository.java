package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;

@Repository
public interface PadPipelineCrossingOwnerRepository extends CrudRepository<PadPipelineCrossingOwner, Integer> {
}
