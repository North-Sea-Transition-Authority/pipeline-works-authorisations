package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PadPipelineDto;

@Service
public class PipelineDetailIdentService {

  private final PipelineDetailIdentRepository pipelineDetailIdentRepository;
  private final PipelineIdentMappingService pipelineIdentMappingService;
  private final PipelineDetailIdentDataService pipelineDetailIdentDataService;

  @Autowired
  public PipelineDetailIdentService(PipelineDetailIdentRepository pipelineDetailIdentRepository,
                                    PipelineIdentMappingService pipelineIdentMappingService,
                                    PipelineDetailIdentDataService pipelineDetailIdentDataService) {
    this.pipelineDetailIdentRepository = pipelineDetailIdentRepository;
    this.pipelineIdentMappingService = pipelineIdentMappingService;
    this.pipelineDetailIdentDataService = pipelineDetailIdentDataService;
  }

  public void createPipelineDetailIdents(Map<PipelineDetail, PadPipelineDto> newPipelineDetailToPadPipelineDtoMap) {

    // setup storage for new pipeline idents mapped to pad pipeline ident data
    var pipelineDetailIdentToPadIdentDataSetMap = new HashMap<PipelineDetailIdent, Set<PadPipelineIdentData>>();

    // for each pipeline (detail) in the map
    newPipelineDetailToPadPipelineDtoMap.forEach((pipelineDetail, padPipelineDto) ->

        // for each ident on the application version of that pipeline
        padPipelineDto.getIdentToIdentDataSetMap().forEach((padPipelineIdent, padPipelineIdentDataSet) -> {

          // create a new ident and store it in a map along with the pad ident data set to be saved later
          var newIdent = createNewPipelineDetailIdent(pipelineDetail, padPipelineIdent);

          pipelineDetailIdentToPadIdentDataSetMap.put(newIdent, padPipelineIdentDataSet);

        })

    );

    // save all of our newly created pipeline detail idents
    pipelineDetailIdentRepository.saveAll(pipelineDetailIdentToPadIdentDataSetMap.keySet());

    // pass newly created idents through to data service to create ident data
    pipelineDetailIdentDataService.createPipelineDetailIdentData(pipelineDetailIdentToPadIdentDataSetMap);

  }

  private PipelineDetailIdent createNewPipelineDetailIdent(PipelineDetail pipelineDetail,
                                                           PadPipelineIdent padPipelineIdent) {

    var ident = new PipelineDetailIdent(pipelineDetail);

    pipelineIdentMappingService.mapIdent(ident, padPipelineIdent);

    return ident;

  }

}
