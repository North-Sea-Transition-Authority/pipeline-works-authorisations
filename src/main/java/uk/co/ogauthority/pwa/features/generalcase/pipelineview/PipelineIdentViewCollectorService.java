package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdent;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentData;

/**
 * Provide template for combining PipelineIdent and PipelineIdentData into IdentViews.
 */
@Service
public class PipelineIdentViewCollectorService {


  public <T extends PipelineIdent, U extends PipelineIdentData> Map<PipelineId, List<IdentView>> getPipelineIdToIdentVewsMap(
      Class<T> pipelineIdentClass,
      Class<U> pipelineIdentDataClass,
      Supplier<List<T>> identListSupplier,
      Function<List<T>, List<U>> supplyIdentDataForIdents) {
    var idents = identListSupplier.get();

    var identDataMap = supplyIdentDataForIdents.apply(idents).stream()
        .collect(Collectors.toMap(PipelineIdentData::getPipelineIdent, Function.identity()));

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
