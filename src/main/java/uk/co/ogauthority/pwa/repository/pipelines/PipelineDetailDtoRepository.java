package uk.co.ogauthority.pwa.repository.pipelines;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PipelineDetailDtoRepository {

  List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PipelineOverview> getAllPipelineOverviewsForMasterPwa(MasterPwa masterPwa);

  List<PipelineOverview> getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
      MasterPwa masterPwa, Set<PipelineStatus> statusFilter, Instant searchInstant);

  List<CountPipelineDetailsForPipelineDto> getCountOfPipelineDetailsForPipelines(Set<PipelineId> pipelineIds);

}
