package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;

/**
 * Interface used to enhance the default repository so DTOs can be produced easily.
 */
public interface PadPipelineDtoRepository {

  List<PadPipelineSummaryDto> findAllPipelinesAsSummaryDtoByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadPipelineSummaryDto> findPipelineAsSummaryDtoByPadPipeline(PadPipeline padPipeline);

  Long countAllWithNoIdentsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Set<PipelineId> getMasterPipelineIdsOnApplication(PwaApplicationDetail pwaApplicationDetail);

  Long countMasterPipelinesOnApplication(PwaApplicationDetail pwaApplicationDetail);

  Integer getMaxTemporaryNumberByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
