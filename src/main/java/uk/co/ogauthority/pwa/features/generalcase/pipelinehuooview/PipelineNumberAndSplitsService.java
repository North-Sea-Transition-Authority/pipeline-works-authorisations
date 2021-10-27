package uk.co.ogauthority.pwa.features.generalcase.pipelinehuooview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineSummaryAndSplit;

/**
 * Contains the logical template for comparing whole pipelines and HUOO pipeline links to determine split pipelines.
 */
@Service
public class PipelineNumberAndSplitsService {

  /**
   * <p>From some supplied map of potentially "split" pipeline Ids to pipeline overviews and some supplied set of
   * PipelineIdentifiers describing split sections of pipelines in the first Map, return a new map from the PipelineIdentifier
   * to the split information.</p>
   *
   * <p>For pipelinesIds in the first map which are not present in the supplied "split" pipelines set, there will be one entry.
   * For pipelinesIds in the first map which are present in the supplied "split" pipelines set, there will only be entries for that pipeline
   * in the result map for the individual sections.</p>
   */
  public Map<PipelineIdentifier, PipelineNumbersAndSplits> getAllPipelineNumbersAndSplitsRole(
      Supplier<Map<PipelineId, PipelineOverview>> getSplittablePipelines,
      Supplier<Set<PipelineIdentifier>> getSplitPipelines) {

    Map<PipelineIdentifier, PipelineSummaryAndSplit> pipelineIdentifierAndSummarySplitMap = new HashMap<>();
    getSplittablePipelines.get().forEach((identifier, pipelineOverview) -> pipelineIdentifierAndSummarySplitMap.put(
            identifier, new PipelineSummaryAndSplit(pipelineOverview, null)));

    Set<PipelineIdentifier> splitPipelinesForRole = getSplitPipelines.get();

    // replace entries for whole pipelines where a pipeline has been split
    Set<PipelineId> splitPipelines = new HashSet<>();
    splitPipelinesForRole.forEach(splitPipelineIdentifier -> {
      var splitPipelineOption = PipelineSummaryAndSplit.duplicateOptionForPipelineIdentifier(
          splitPipelineIdentifier,
          pipelineIdentifierAndSummarySplitMap.get(splitPipelineIdentifier.getPipelineId()).getPipelineOverview());
      splitPipelines.add(splitPipelineIdentifier.getPipelineId());
      pipelineIdentifierAndSummarySplitMap.put(splitPipelineIdentifier, splitPipelineOption);
    });

    // remove records for whole pipelines where splits are now within the map
    splitPipelines.forEach(pipelineIdentifierAndSummarySplitMap::remove);
    return pipelineIdentifierAndSummarySplitMap.entrySet().stream()
        .map(entry -> PipelineNumbersAndSplits.from(entry.getKey(), entry.getValue()))
        .collect(Collectors.toMap(PipelineNumbersAndSplits::getPipelineIdentifier, Function.identity()));
  }







}
