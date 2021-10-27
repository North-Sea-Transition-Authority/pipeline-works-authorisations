package uk.co.ogauthority.pwa.repository.submission;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;

@Repository
public interface PadPipelineSubmissionRepository extends CrudRepository<PadPipeline, Integer>, PadPipelineNumberingRepository {
}
