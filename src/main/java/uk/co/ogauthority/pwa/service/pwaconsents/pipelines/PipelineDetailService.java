package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static java.util.stream.Collectors.toMap;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pipelines.CountPipelineDetailsForPipelineDto;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PadPipelineDto;

@Service
public class PipelineDetailService {

  private final PipelineDetailRepository pipelineDetailRepository;
  private final Clock clock;
  private final PipelineMappingService pipelineMappingService;
  private final PipelineDetailIdentService pipelineDetailIdentService;

  public PipelineDetailService(PipelineDetailRepository pipelineDetailRepository,
                               @Qualifier("utcClock") Clock clock,
                               PipelineMappingService pipelineMappingService,
                               PipelineDetailIdentService pipelineDetailIdentService) {
    this.pipelineDetailRepository = pipelineDetailRepository;
    this.clock = clock;
    this.pipelineMappingService = pipelineMappingService;
    this.pipelineDetailIdentService = pipelineDetailIdentService;
  }

  // TODO PWA-1046 - this has a misleading name. No "similar" check is done.
  public List<PipelineBundlePairDto> getSimilarPipelineBundleNamesByDetail(PwaApplicationDetail detail) {
    return pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail);
  }

  public List<PipelineOverview> getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(MasterPwa masterPwa,
                                                                                      Set<PipelineStatus> statusFilter,
                                                                                      Instant instant) {
    return pipelineDetailRepository.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(masterPwa, statusFilter, instant);
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

  public List<PipelineDetail> getNonDeletedPipelineDetailsForApplicationMasterPwa(MasterPwa masterPwa) {

    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(
        masterPwa,
        PipelineStatus.historicalStatusSet()
    );
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwaById(PwaApplication pwaApplication,
                                                                                  Set<PipelineId> pipelineIds) {
    //revisit if performance is bad
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        pwaApplication.getMasterPwa()).stream()
        .filter(pd -> pipelineIds.contains(pd.getPipelineId()))
        .collect(Collectors.toList());
  }

  public List<PipelineDetail> getAllPipelineDetailsForPipeline(PipelineId pipelineId) {
    return pipelineDetailRepository.findAllByPipeline_Id(pipelineId.asInt());
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwa(PwaApplication pwaApplication) {
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(pwaApplication.getMasterPwa());
  }

  private List<PipelineDetail> getActivePipelineDetailsForPipelines(Collection<Pipeline> pipelines) {
    return pipelineDetailRepository.findAllByPipelineInAndEndTimestampIsNull(pipelines);
  }

  private void endPipelineDetail(PipelineDetail detail) {
    detail.setTipFlag(false);
    detail.setEndTimestamp(clock.instant());
  }

  public void createNewPipelineDetails(Map<Pipeline, PadPipelineDto> pipelineToPadPipelineDtoMap,
                                       PwaConsent pwaConsent,
                                       ConsentWriterDto consentWriterDto) {

    // set up storage for pipeline details we are ending and new pipeline details mapped to appropriate pad pipeline dto
    var endedDetails = new ArrayList<PipelineDetail>();
    var newPipelineDetailToPadPipelineDtoMap = new HashMap<PipelineDetail, PadPipelineDto>();

    // get current pipeline details for any pipelines that are on our application, store in a map
    var pipelineToCurrentDetailMap =
        getActivePipelineDetailsForPipelines(pipelineToPadPipelineDtoMap.keySet())
            .stream()
            .collect(Collectors.toMap(PipelineDetail::getPipeline, Function.identity()));

    // for each pipeline in our map
    pipelineToPadPipelineDtoMap.forEach((pipeline, padPipelineDto) -> {

      // if there's a current detail for the pipeline, end it
      Optional.ofNullable(pipelineToCurrentDetailMap.get(pipeline))
          .ifPresent(currentDetail -> {
            endPipelineDetail(currentDetail);
            endedDetails.add(currentDetail);
          });

      // create new detail for the pipeline and store
      var newDetail = createNewPipelineDetail(pipeline, padPipelineDto, pwaConsent);
      newPipelineDetailToPadPipelineDtoMap.put(newDetail, padPipelineDto);
      consentWriterDto.storePipeline(pipeline, newDetail);

    });

    // save all ended details and new details we've created
    pipelineDetailRepository.saveAll(endedDetails);
    pipelineDetailRepository.saveAll(newPipelineDetailToPadPipelineDtoMap.keySet());

    // pass new details through to ident service to create idents
    pipelineDetailIdentService.createPipelineDetailIdents(newPipelineDetailToPadPipelineDtoMap);

  }

  private PipelineDetail createNewPipelineDetail(Pipeline pipeline,
                                                 PadPipelineDto padPipelineDto,
                                                 PwaConsent pwaConsent) {

    var detail = new PipelineDetail(pipeline);
    detail.setStartTimestamp(clock.instant());
    detail.setTipFlag(true);
    detail.setPwaConsent(pwaConsent);

    pipelineMappingService.mapPipelineEntities(detail, padPipelineDto.getPadPipeline());

    // if available, set the transfer from link so we know where this pipeline came from
    Optional.ofNullable(padPipelineDto.getTransferredFromPipeline())
        .ifPresent(detail::setTransferredFromPipeline);

    return detail;

  }

  public Map<PipelineId, Long> countPipelineDetailsPerPipeline(Set<Pipeline> pipelines) {

    if (pipelines.isEmpty()) {
      return Map.of();
    }

    var pipelineIdSet = pipelines.stream()
        .map(Pipeline::getPipelineId)
        .collect(Collectors.toSet());

    return pipelineDetailRepository.getCountOfPipelineDetailsForPipelines(pipelineIdSet)
        .stream()
        .collect(Collectors.toMap(
            CountPipelineDetailsForPipelineDto::getPipelineId,
            CountPipelineDetailsForPipelineDto::getCountOfPipelineDetails)
        );
  }

  public List<PipelineDetail> getLatestPipelineDetailsForIds(List<Integer> pipelineIds) {
    return pipelineDetailRepository.findAllByPipeline_IdInAndTipFlagIsTrue(pipelineIds);
  }

  public List<PipelineDetail> getPipelineDetailsBeforePwaConsentCreated(PipelineId pipelineId, Instant pwaConsentCreatedTime) {
    return pipelineDetailRepository.findAllByPipeline_IdAndPwaConsent_consentInstantIsBefore(
        pipelineId.getPipelineIdAsInt(), pwaConsentCreatedTime);
  }

  public PipelineDetail getFirstConsentedPipelineDetail(PipelineId pipelineId) {
    return pipelineDetailRepository.findFirstByPipeline_IdOrderByStartTimestampAsc(pipelineId.getPipelineIdAsInt())
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Could not find PipelineDetail with Pipeline ID: " + pipelineId.getPipelineIdAsInt()));
  }

  public void setTransferredToPipeline(Pipeline pipeline, Pipeline transferredToPipeline) {

    pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())
        .ifPresent(detail -> {
          detail.setTransferredToPipeline(transferredToPipeline);
          pipelineDetailRepository.save(detail);
        });

  }

}
