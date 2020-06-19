package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;

/*8 get or resolve pipelines using the PickablePipelineOption class. */
@Service
public class PickablePipelineService {

  private final PipelineService pipelineService;
  private final PadPipelineService padPipelineService;


  @Autowired
  public PickablePipelineService(
      PipelineService pipelineService,
      PadPipelineService padPipelineService) {
    this.padPipelineService = padPipelineService;
    this.pipelineService = pipelineService;
  }

  public Set<Pipeline> getPickedPipelinesFromStrings(Set<String> stringSet) {
    var pickedPipelines = stringSet.stream()
        .map(PickablePipelineId::from)
        .collect(toSet());

    return getPickedPipelines(pickedPipelines);
  }

  /* Get pipeline entities for pickablePipelineIds */
  public Set<Pipeline> getPickedPipelines(Set<PickablePipelineId> pickedPipelineIds) {
    var pipelineIds = reconcilePickablePipelineIds(pickedPipelineIds)
        .stream()
        .map(ReconciledPickablePipeline::getPipelineId)
        .collect(toSet());
    return pipelineService.getPipelinesFromIds(pipelineIds);
  }

  /* Pickable pipelines for the application as a whole are those added by the app, imported for update by the app,
   * or active pipeline linked to the application's master pwa which are not imported into the application. */
  public Set<PickablePipelineOption> getAllPickablePipelinesForApplication(PwaApplicationDetail pwaApplicationDetail) {
    Map<Integer, PickablePipelineOption> padPipelinePickableByPipelineId = padPipelineService
        .getAllPadPipelineSummaryDtosForApplicationDetail(pwaApplicationDetail)
        .stream()
        .collect(toMap(PadPipelineSummaryDto::getPipelineId, PickablePipelineOption::from));

    Set<PickablePipelineOption> padPipelinePickableSet = new HashSet<>(padPipelinePickableByPipelineId.values());

    // consented pickable are those pipelines that have not got a version included in the application.
    // when a version does exist in the application we want the updated details to show.
    Set<PickablePipelineOption> consentedPickable = pipelineService.getActivePipelineDetailsForApplicationMasterPwa(
        pwaApplicationDetail.getPwaApplication()
    )
        .stream()
        .filter(p -> !padPipelinePickableByPipelineId.containsKey(p.getPipelineId()))
        .map(PickablePipelineOption::from)
        .collect(toSet());

    return Sets.union(consentedPickable, padPipelinePickableSet);

  }


  /* Pickable pipelines form the application are where the pipeline has been newly added or imported from consented model for update  */
  public Set<PickablePipelineOption> getPickablePipelinesFromApplication(PwaApplicationDetail pwaApplicationDetail) {

    // for now just filter from the entire set. performance concerns should be minimal. important thing is api exists.
    return getAllPickablePipelinesForApplication(pwaApplicationDetail)
        .stream()
        .filter(ppo -> PickablePipelineType.CONSENTED.equals(ppo.getPickablePipelineType()))
        .collect(toSet());

  }

  private Set<ReconciledPickablePipeline> reconcilePickablePipelineIds(Set<PickablePipelineId> pickablePipelineIds) {

    var consentedReconciledPickablePipelineIds = pickablePipelineIds.stream()
        .filter(PickablePipelineId::isConsentedPipelineId)
        .map(p -> new ReconciledPickablePipeline(p, new PipelineId(p.getIdAsIntOrNull())))
        .collect(toSet());

    var padPipelineIds = pickablePipelineIds.stream()
        .filter(PickablePipelineId::isApplicationPipelineId)
        .map(PickablePipelineId::getIdAsIntOrNull)
        .filter(Objects::nonNull)
        .collect(toSet());

    var reconciledPickablePipelines = new HashSet<ReconciledPickablePipeline>();
    padPipelineService.getPadPipelinesByPadPipelineIds(padPipelineIds)
        .stream()
        .map(padPipeline -> new ReconciledPickablePipeline(PickablePipelineId.from(padPipeline),
            PipelineId.from(padPipeline)))
        .forEach(reconciledPickablePipelines::add);

    reconciledPickablePipelines.addAll(consentedReconciledPickablePipelineIds);
    return reconciledPickablePipelines;


  }

  public Set<ReconciledPickablePipeline> reconcilePickablePipelineOptions(
      Set<PickablePipelineOption> pickablePipelineOptions) {

    var pickablePipelineIds = pickablePipelineOptions.stream()
        .map(PickablePipelineId::from)
        .collect(toSet());

    return reconcilePickablePipelineIds(pickablePipelineIds);
  }

  /* Pickable pipeline form the master pwa are where the consented pipeline has not been imported into the application for update. */
  public Set<PickablePipelineOption> getPickablePipelinesFromApplicationMasterPwa(
      PwaApplicationDetail pwaApplicationDetail) {

    // for now just filter from the entire set. performance concerns should be minimal. important thing is api exists.
    return getAllPickablePipelinesForApplication(pwaApplicationDetail)
        .stream()
        .filter(ppo -> PickablePipelineType.APPLICATION.equals(ppo.getPickablePipelineType()))
        .collect(toSet());

  }

}
