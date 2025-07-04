package uk.co.ogauthority.pwa.features.generalcase.pipelinehuooview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineNumberAndSplitsService.class);

  /**
   * From some supplied map of potentially "split" pipeline Ids to pipeline overviews and some supplied set of
   * PipelineIdentifiers describing split sections of pipelines in the first Map, return a new map from the PipelineIdentifier
   * to the split information.
   *
   * <p>For pipelinesIds in the first map which are not present in the supplied "split" pipelines set, there will be one entry.
   * For pipelinesIds in the first map which are present in the supplied "split" pipelines set, there will only be entries for that pipeline
   * in the result map for the individual sections.</p>
   */
  public Map<PipelineIdentifier, PipelineNumbersAndSplits> getAllPipelineNumbersAndSplitsRole(
      Supplier<Map<PipelineId, PipelineOverview>> getSplittablePipelines,
      Supplier<Set<PipelineIdentifier>> getSplitPipelines) {

    Map<PipelineIdentifier, PipelineSummaryAndSplit> pipelineIdentifierAndSummarySplitMap = new HashMap<>();

    Map<PipelineId, PipelineOverview> splittablePipelines = getSplittablePipelines.get();

    // add entries for all pipelines, split or not
    splittablePipelines.forEach((identifier, pipelineOverview) ->
        pipelineIdentifierAndSummarySplitMap.put(identifier, new PipelineSummaryAndSplit(pipelineOverview, null)));

    LOGGER.debug("pipelineIdentifierAndSummarySplitMap keyset after full pipes added [{}]",
        pipelineIdentifierAndSummarySplitMap.keySet().stream()
            .map(PipelineIdentifier::toString)
            .collect(Collectors.joining(",")));

    Set<PipelineIdentifier> splitPipelinesForRole = getSplitPipelines.get();

    // add entries where a pipeline has been split
    Set<PipelineId> splitPipelines = new HashSet<>();
    splitPipelinesForRole.forEach(splitPipelineIdentifier -> {

      var splitPipelineOption = PipelineSummaryAndSplit.duplicateOptionForPipelineIdentifier(
          splitPipelineIdentifier,
          pipelineIdentifierAndSummarySplitMap.get(splitPipelineIdentifier.getPipelineId()).getPipelineOverview());

      splitPipelines.add(splitPipelineIdentifier.getPipelineId());

      pipelineIdentifierAndSummarySplitMap.put(splitPipelineIdentifier, splitPipelineOption);

      try {
        LOGGER.debug("Adding split info to map key = {}, value = {}",
            splitPipelineIdentifier,
            splitPipelineOption.getSplitInfo());
      } catch (Exception e) {
        LOGGER.warn("Error logging split info add for {}", splitPipelineIdentifier);
      }

    });

    LOGGER.debug("pipelineIdentifierAndSummarySplitMap keyset after split pipes added [{}]",
        pipelineIdentifierAndSummarySplitMap.keySet().stream()
            .map(PipelineIdentifier::toString)
            .collect(Collectors.joining(",")));

    // remove records for whole pipelines where splits are now within the map
    splitPipelines.forEach(pipelineIdentifierAndSummarySplitMap::remove);

    LOGGER.debug("pipelineIdentifierAndSummarySplitMap keyset after duplicate full pipes removed [{}]",
        pipelineIdentifierAndSummarySplitMap.keySet().stream()
            .map(PipelineIdentifier::toString)
            .collect(Collectors.joining(",")));

    return pipelineIdentifierAndSummarySplitMap.entrySet().stream()
        .map(entry -> PipelineNumbersAndSplits.from(entry.getKey(), entry.getValue()))
        .collect(Collectors.toMap(PipelineNumbersAndSplits::getPipelineIdentifier, Function.identity()));

  }

}