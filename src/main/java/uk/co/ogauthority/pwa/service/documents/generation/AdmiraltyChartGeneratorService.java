package uk.co.ogauthority.pwa.service.documents.generation;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class AdmiraltyChartGeneratorService implements DocumentSectionGenerator {

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

}
