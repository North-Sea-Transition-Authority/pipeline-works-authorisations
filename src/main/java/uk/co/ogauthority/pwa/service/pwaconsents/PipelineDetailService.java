package uk.co.ogauthority.pwa.service.pwaconsents;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;

@Service
public class PipelineDetailService {

  private final PipelineDetailRepository pipelineDetailRepository;

  public PipelineDetailService(
      PipelineDetailRepository pipelineDetailRepository) {
    this.pipelineDetailRepository = pipelineDetailRepository;
  }

  // TODO PWA-1046 - this has a misleading name. No "similar" check is done.
  public List<PipelineBundlePairDto> getSimilarPipelineBundleNamesByDetail(PwaApplicationDetail detail) {
    return pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail);
  }

  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwa(MasterPwa masterPwa) {
    return pipelineDetailRepository.getAllPipelineOverviewsForMasterPwa(masterPwa);
  }

  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwaAndStatus(MasterPwa masterPwa, Set<PipelineStatus> statusFilter) {
    return pipelineDetailRepository.getAllPipelineOverviewsForMasterPwaAndStatus(masterPwa, statusFilter);
  }

  public Map<PipelineId, PipelineOverview> getAllPipelineOverviewsForMasterPwaMap(MasterPwa masterPwa) {
    return pipelineDetailRepository.getAllPipelineOverviewsForMasterPwa(masterPwa)
        .stream()
        .collect(toMap(PipelineId::from, pipelineOverview -> pipelineOverview));
  }

  public PipelineDetail getLatestByPipelineId(Integer id) {
    return pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find PipelineDetail with Pipeline ID: " + id));
  }

  public PipelineDetail getByPipelineDetailId(Integer id) {
    return pipelineDetailRepository.findById(id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find PipelineDetail with ID: " + id));
  }


  public boolean isPipelineConsented(Pipeline pipeline) {
    return pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId()).isPresent();
  }

  public List<PipelineDetail> getNonDeletedPipelineDetailsForApplicationMasterPwa(
      MasterPwa masterPwa) {

    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(
        masterPwa,
        PipelineStatus.historicalStatusSet()
    );
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwaById(PwaApplication pwaApplication,
                                                                                  Set<PipelineId> pipelineIds) {
    //revisit if performance is bad
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        pwaApplication.getMasterPwa()
    ).stream()
        .filter(pd -> pipelineIds.contains(pd.getPipelineId()))
        .collect(Collectors.toList());
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwa(PwaApplication pwaApplication) {
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        pwaApplication.getMasterPwa()
    );
  }

  public List<PipelineDetail> getAllPipelineDetailsForPipeline(PipelineId pipelineId) {
    return pipelineDetailRepository.findAllByPipeline_Id(pipelineId.asInt());
  }

}
