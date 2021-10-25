package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public interface PipelineDetailDtoRepository {

  List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PipelineOverview> getAllPipelineOverviewsForMasterPwa(MasterPwa masterPwa);

  List<PipelineOverview> getAllPipelineOverviewsForMasterPwaAndStatus(MasterPwa masterPwa, Set<PipelineStatus> statusFilter);

  List<CountPipelineDetailsForPipelineDto> getCountOfPipelineDetailsForPipelines(Set<PipelineId> pipelineIds);



}
