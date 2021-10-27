package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Interface used to enhance the default repository so DTOs can be produced easily.
 */
public interface PadPipelineDtoRepository {

  List<PadPipelineSummaryDto> findAllPipelinesAsSummaryDtoByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadPipelineSummaryDto> findPipelineAsSummaryDtoByPadPipeline(PadPipeline padPipeline);

  Long countAllWithNoIdentsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Set<PipelineId> getMasterPipelineIdsOnApplication(PwaApplicationDetail pwaApplicationDetail);

  Integer getMaxTemporaryNumberByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PadPipeline> findApplicationsWherePipelineNumberExistsOnDraftOrLastSubmittedVersion(String pipelineNumber);

}
