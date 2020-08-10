package uk.co.ogauthority.pwa.repository.submission;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public interface PadPipelineNumberingRepository {

  List<PadPipeline> getNonConsentedPipelines(PwaApplicationDetail pwaApplicationDetail);

  Long getNextPipelineReferenceNumber();

}
