package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;

public class PipelineWriterTestUtils {

  private PipelineWriterTestUtils() {
    // can't instantiate this
    throw new AssertionError();
  }

  public static Map<Pipeline, PadPipelineDto> createPipelineToPadPipelineDtoMap() {

    // set up pipeline 1 linked to padpipeline1 and padPipeline1Ident1, padPipeline1Ident2 (with data)
    // set up pipeline 2 linked to padpipeline2 and padpipeline2ident (with 2 bits of data)
    var pipeline1 = new Pipeline();
    pipeline1.setId(1);
    var pipeline2 = new Pipeline();
    pipeline2.setId(2);
    var padPipeline1 = new PadPipeline();
    padPipeline1.setId(1);
    padPipeline1.setPipeline(pipeline1);
    var padPipeline2 = new PadPipeline();
    padPipeline2.setId(2);
    padPipeline2.setPipeline(pipeline2);
    var padPipeline1Ident1 = new PadPipelineIdent();
    padPipeline1Ident1.setId(1);
    padPipeline1Ident1.setPadPipeline(padPipeline1);
    padPipeline1Ident1.setIdentNo(1);
    var padPipeline1Ident2 = new PadPipelineIdent();
    padPipeline1Ident2.setId(2);
    padPipeline1Ident2.setPadPipeline(padPipeline1);
    padPipeline1Ident2.setIdentNo(2);
    var padPipeline2Ident = new PadPipelineIdent();
    padPipeline2Ident.setId(3);
    padPipeline2Ident.setPadPipeline(padPipeline2);
    padPipeline2Ident.setIdentNo(1);
    var padPipeline1Ident1Data = new PadPipelineIdentData();
    padPipeline1Ident1Data.setId(1);
    padPipeline1Ident1Data.setPadPipelineIdent(padPipeline1Ident1);
    var padPipeline1Ident2Data = new PadPipelineIdentData();
    padPipeline1Ident2Data.setId(2);
    padPipeline1Ident2Data.setPadPipelineIdent(padPipeline1Ident2);
    var padPipeline2IdentData1 = new PadPipelineIdentData();
    padPipeline2IdentData1.setId(3);
    padPipeline2IdentData1.setPadPipelineIdent(padPipeline2Ident);
    var padPipeline2IdentData2 = new PadPipelineIdentData();
    padPipeline2IdentData2.setId(4);
    padPipeline2IdentData2.setPadPipelineIdent(padPipeline2Ident);

    var map = new HashMap<Pipeline, PadPipelineDto>();
    var pipeline1PadDto = new PadPipelineDto();
    pipeline1PadDto.setPadPipeline(padPipeline1);
    pipeline1PadDto.setIdentToIdentDataSetMap(Map.of(
        padPipeline1Ident1, Set.of(padPipeline1Ident1Data),
        padPipeline1Ident2, Set.of(padPipeline1Ident2Data)
    ));
    map.put(pipeline1, pipeline1PadDto);

    var pipeline2PadDto = new PadPipelineDto();
    pipeline2PadDto.setPadPipeline(padPipeline2);
    pipeline2PadDto.setIdentToIdentDataSetMap(Map.of(padPipeline2Ident, Set.of(padPipeline2IdentData1, padPipeline2IdentData2)));
    map.put(pipeline2, pipeline2PadDto);

    return map;

  }

  public static Map<PipelineDetail, PadPipelineDto> createPipelineDetailToPadPipelineDtoMap() {

    var map = createPipelineToPadPipelineDtoMap();

    // create a new detail for each pipeline in the map
    var pipelineToDetailMap = map.keySet().stream()
        .map(PipelineDetail::new)
        .collect(Collectors.toMap(PipelineDetail::getPipeline, Function.identity()));

    var detailToDtoMap = new HashMap<PipelineDetail, PadPipelineDto>();
    map.forEach((pipeline, padPipelineDto) -> detailToDtoMap.put(pipelineToDetailMap.get(pipeline), padPipelineDto));

    return detailToDtoMap;

  }

  public static Map<PipelineDetailIdent, Set<PadPipelineIdentData>> createIdentToDataSetMap() {

    var map = createPipelineDetailToPadPipelineDtoMap();

    var identToDataSetMap = new HashMap<PipelineDetailIdent, Set<PadPipelineIdentData>>();

    map.forEach((detail, padPipelineDto) -> {

      padPipelineDto.getIdentToIdentDataSetMap().forEach((padIdent, dataSet) -> {

        var detailIdent = new PipelineDetailIdent(detail);

        identToDataSetMap.put(detailIdent, dataSet);

      });

    });

    return identToDataSetMap;

  }

}
