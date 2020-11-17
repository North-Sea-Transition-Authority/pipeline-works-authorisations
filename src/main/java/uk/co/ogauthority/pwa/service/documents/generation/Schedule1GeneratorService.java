package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class Schedule1GeneratorService implements DocumentSectionGenerator {

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    Map<String, Object> modelMap = Map.of("sectionName", DocumentSection.SCHEDULE_1.getDisplayName());

    return new DocumentSectionData("documents/consents/sections/schedule1", modelMap);

  }

}
