package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.pipelinedatautils;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdentData;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;

/**
 * Provide template for combining PipelineIdent and PipelineIdentData into IdentViews.
 */
@Service
public class PipelineIdentViewCollectorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineIdentViewCollectorService.class);
  private static final int IDENT_BATCH_SIZE = 1000;

  public <T extends PipelineIdent, U extends PipelineIdentData> Map<PipelineId, List<IdentView>> getPipelineIdToIdentVewsMap(
      Class<T> pipelineIdentClass,
      Class<U> pipelineIdentDataClass,
      Supplier<List<T>> identListSupplier,
      Function<List<T>, List<U>> supplyIdentDataForIdents) {

    var idents = identListSupplier.get();
    LOGGER.debug("Found {} idents", idents.size());

    // we need to use batches here for the edge case where more than 1000 idents are supplied. This would cause a non-batched
    // method to blow out the oracle IN clause 1000 item limit.
    List<List<T>> identBatchList = Lists.partition(idents, IDENT_BATCH_SIZE);
    LOGGER.debug("Split idents into {} batches", identBatchList.size());

    var identDataMap = new HashMap<PipelineIdent, U>();
    var batchCounter = 1;
    for (List<T> batchIdentList: identBatchList) {
      Map<PipelineIdent, U> batchIdentDataMap = supplyIdentDataForIdents.apply(batchIdentList).stream()
          .collect(
              Collectors.toMap(
                  PipelineIdentData::getPipelineIdent,
                  Function.identity()
              )
          );
      identDataMap.putAll(batchIdentDataMap);

      LOGGER.debug("Ident batch {} processed with size {}", batchCounter, batchIdentList.size());
      batchCounter += 1;
    }

    LOGGER.debug("Found total ident data for {} idents", identDataMap.size());

    // First, group idents by  pipeline ID.
    // Second, map each pipeline ident to an IdentView
    // Finally, Sort the grouped idents by ident number
    return identDataMap.entrySet()
        .stream()
        .collect(
            Collectors.groupingBy(
                entry -> entry.getKey().getPipelineId(),
                Collectors.mapping(
                    entry -> new IdentView(identDataMap.get(entry.getKey())),
                    Collectors.collectingAndThen(
                        toList(),
                        identViews -> identViews.stream()
                            .sorted(Comparator.comparing(IdentView::getIdentNumber))
                            .collect(Collectors.toList())
                    )
                )
            )
        );

  }

}
