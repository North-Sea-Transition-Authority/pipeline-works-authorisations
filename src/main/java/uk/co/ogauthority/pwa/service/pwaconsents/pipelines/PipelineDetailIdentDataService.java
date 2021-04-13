package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;

@Service
public class PipelineDetailIdentDataService {

  private final PipelineDetailIdentDataRepository identDataRepository;
  private final PipelineIdentDataMappingService pipelineIdentDataMappingService;

  @Autowired
  public PipelineDetailIdentDataService(PipelineDetailIdentDataRepository identDataRepository,
                                        PipelineIdentDataMappingService pipelineIdentDataMappingService) {
    this.identDataRepository = identDataRepository;
    this.pipelineIdentDataMappingService = pipelineIdentDataMappingService;
  }

  public void createPipelineDetailIdentData(
      Map<PipelineDetailIdent, Set<PadPipelineIdentData>> pipelineDetailIdentToPadIdentDataSetMap) {

    // setup storage for the new ident data objects we're creating
    var newIdentData = new ArrayList<PipelineDetailIdentData>();

    // for each ident in the map
    pipelineDetailIdentToPadIdentDataSetMap.forEach(((pipelineDetailIdent, padPipelineIdentDataSet) -> {

      // for each piece of pad ident data linked to that ident
      padPipelineIdentDataSet.forEach(padPipelineIdentData -> {

        // create a new ident data object and store it
        var newData = createPipelineDetailIdentData(pipelineDetailIdent, padPipelineIdentData);
        newIdentData.add(newData);

      });

    }));

    // save all of our newly created ident data objects
    identDataRepository.saveAll(newIdentData);

  }

  private PipelineDetailIdentData createPipelineDetailIdentData(PipelineDetailIdent pipelineDetailIdent,
                                                                PadPipelineIdentData padPipelineIdentData) {

    var identData = new PipelineDetailIdentData(pipelineDetailIdent);

    pipelineIdentDataMappingService.mapPipelineIdentData(identData, padPipelineIdentData);

    return identData;

  }

}
