package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.TransferParticipantType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PipelineWriter implements ConsentWriter {

  private final PadPipelineIdentDataService padPipelineIdentDataService;
  private final PipelineDetailService pipelineDetailService;
  private final PadPipelineTransferService padPipelineTransferService;

  @Autowired
  public PipelineWriter(PadPipelineIdentDataService padPipelineIdentDataService,
                        PipelineDetailService pipelineDetailService,
                        PadPipelineTransferService padPipelineTransferService) {
    this.padPipelineIdentDataService = padPipelineIdentDataService;
    this.pipelineDetailService = pipelineDetailService;
    this.padPipelineTransferService = padPipelineTransferService;
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

    var padPipelineToIdentMap = getPadPipelineToIdentsMap(pwaApplicationDetail);
    var pipelineToTransferMap = padPipelineTransferService.getPipelineToTransferMap(pwaApplicationDetail);

    var pipelineToPadPipelineDtoMap = new HashMap<Pipeline, PadPipelineDto>();
    padPipelineToIdentMap.forEach((padPipeline, identToIdentDataSetEntryList) -> {

      var dto = new PadPipelineDto();
      dto.setPadPipeline(padPipeline);

      var map = identToIdentDataSetEntryList.stream()
           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      dto.setIdentToIdentDataSetMap(map);

      var pipeline = padPipeline.getPipeline();
      
      if (pipelineToTransferMap.containsKey(pipeline)) {
        
        var padPipelineTransfer = pipelineToTransferMap.get(pipeline);
        
        var transferParticipantType = padPipelineTransfer.getTransferParticipantType(pipeline);
        
        // if the transferred pipeline being consented is the recipient pipeline, set the links appropriately
        // linking the recipient back to the donor and the donor to the new recipient
        if (transferParticipantType == TransferParticipantType.RECIPIENT) {
          var donorPipeline = padPipelineTransfer.getDonorPipeline();
          dto.setTransferredFromPipeline(donorPipeline);
          pipelineDetailService.setTransferredToPipeline(donorPipeline, pipeline);
        }
        
      }

      pipelineToPadPipelineDtoMap.put(pipeline, dto);

    });

    pipelineDetailService.createNewPipelineDetails(pipelineToPadPipelineDtoMap, pwaConsent, consentWriterDto);

    return consentWriterDto;

  }

  private Map<PadPipeline, List<Map.Entry<PadPipelineIdent, Set<PadPipelineIdentData>>>> getPadPipelineToIdentsMap(
      PwaApplicationDetail pwaApplicationDetail) {

    // get all ident data grouped by ident that is linked to our application
    var identToIdentDataSetMap = padPipelineIdentDataService
         .getAllPipelineIdentDataForPwaApplicationDetail(pwaApplicationDetail)
         .stream()
         .collect(Collectors.groupingBy(PadPipelineIdentData::getPadPipelineIdent, Collectors.toSet()));

    // get all pipelines on our app mapped to a list of map entries (ident -> ident data list)
    return identToIdentDataSetMap.entrySet()
        .stream()
        .collect(Collectors.groupingBy(e -> e.getKey().getPadPipeline()));

  }

}