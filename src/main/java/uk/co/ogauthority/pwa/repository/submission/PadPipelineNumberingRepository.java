package uk.co.ogauthority.pwa.repository.submission;

import java.util.List;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineNumberingRepository {

  List<PadPipeline> getNonConsentedPipelines(PwaApplicationDetail pwaApplicationDetail);

  Long getNextPipelineReferenceNumber();

}
