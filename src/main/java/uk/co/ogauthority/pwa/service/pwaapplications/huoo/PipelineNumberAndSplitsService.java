package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSummaryAndSplit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;

@Service
public class PipelineNumberAndSplitsService {



  public List<PipelineNumbersAndSplits> getAllPipelineNumbersAndSplitsRole(
      HuooRole huooRole,
      Supplier<Map<PipelineId, PipelineOverview>> getSplittablePipelines,
      Supplier<Set<PipelineIdentifier>> getSplitPipelines,
      Set<PipelineIdentifier> pipelineIdentifiers) {

    Map<PipelineIdentifier, PipelineSummaryAndSplit> pipelineIdentifierAndSummarySplitMap = new HashMap<>();
    getSplittablePipelines.get().forEach((identifier, pipelineOverview) -> pipelineIdentifierAndSummarySplitMap.put(
            identifier, new PipelineSummaryAndSplit(pipelineOverview, null, null)));

    Set<PipelineIdentifier> splitPipelinesForRole = getSplitPipelines.get();

    // replace entries for whole pipelines where a pipeline has been split
    Set<PipelineId> splitPipelines = new HashSet<>();
    splitPipelinesForRole.forEach(splitPipelineIdentifier -> {
      var splitPipelineOption = PipelineSummaryAndSplit.duplicateOptionForPipelineIdentifier(
          splitPipelineIdentifier,
          huooRole,
          pipelineIdentifierAndSummarySplitMap.get(splitPipelineIdentifier.getPipelineId()).getPipelineOverview());
      splitPipelines.add(splitPipelineIdentifier.getPipelineId());
      pipelineIdentifierAndSummarySplitMap.put(splitPipelineIdentifier, splitPipelineOption);
    });

    // remove records for whole pipelines where splits are now within the map
    splitPipelines.forEach(pipelineIdentifierAndSummarySplitMap::remove);
    List<PipelineNumbersAndSplits> pipelineNumbersAndSplits = new ArrayList<>();
    pipelineIdentifiers.stream().sorted();
    pipelineIdentifiers.forEach((identifier) -> {
      if (identifier != null) {
        pipelineNumbersAndSplits.add(PipelineNumbersAndSplits.from(
            identifier, pipelineIdentifierAndSummarySplitMap.get(identifier.getPipelineId())));
      }
    });



    return pipelineNumbersAndSplits;
  }







}
