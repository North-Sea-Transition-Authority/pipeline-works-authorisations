package uk.co.ogauthority.pwa.service.documents.generation;

import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface DocumentSectionGenerator {

  default DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                     DocumentInstance documentInstance,
                                                     DocGenType docGenType) {
    return null;
  }

}
