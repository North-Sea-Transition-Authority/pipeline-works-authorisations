package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSummaryAndSplit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;

@Service
public class PipelineNumberAndSplitsService {



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
