package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer> {
}
