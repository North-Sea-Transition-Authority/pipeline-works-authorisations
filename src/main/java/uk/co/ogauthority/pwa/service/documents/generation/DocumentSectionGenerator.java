package uk.co.ogauthority.pwa.service.documents.generation;

import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface DocumentSectionGenerator {

  DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail);

}
