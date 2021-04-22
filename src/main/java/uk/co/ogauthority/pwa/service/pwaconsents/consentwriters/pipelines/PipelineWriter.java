package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PipelineWriter implements ConsentWriter {

  private final PadPipelineIdentDataService padPipelineIdentDataService;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public PipelineWriter(PadPipelineIdentDataService padPipelineIdentDataService,
                        PipelineDetailService pipelineDetailService) {
    this.padPipelineIdentDataService = padPipelineIdentDataService;
    this.pipelineDetailService = pipelineDetailService;
  }

  @Override
  public int getExecutionOrder() {
    return 15;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return applicationTaskSet.contains(ApplicationTask.PIPELINES);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail,
                                PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    // get all ident data grouped by ident that is linked to our application
    var identToIdentDataSetMap = padPipelineIdentDataService
         .getAllPipelineIdentDataForPwaApplicationDetail(pwaApplicationDetail)
         .stream()
         .collect(Collectors.groupingBy(PadPipelineIdentData::getPadPipelineIdent, Collectors.toSet()));

    // get all pipelines on our app mapped to a list of map entries (ident -> ident data list)
    var padPipelineToIdentMap = identToIdentDataSetMap.entrySet()
        .stream()
        .collect(Collectors.groupingBy(e -> e.getKey().getPadPipeline()));

    var pipelineToPadPipelineDtoMap = new HashMap<Pipeline, PadPipelineDto>();
    padPipelineToIdentMap.forEach((padPipeline, identToIdentDataSetEntryList) -> {

      var dto = new PadPipelineDto();
      dto.setPadPipeline(padPipeline);
      var map = identToIdentDataSetEntryList.stream()
           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      dto.setIdentToIdentDataSetMap(map);

      pipelineToPadPipelineDtoMap.put(padPipeline.getPipeline(), dto);

    });

    pipelineDetailService.createNewPipelineDetails(pipelineToPadPipelineDtoMap, pwaConsent, consentWriterDto);

    return consentWriterDto;

  }

}