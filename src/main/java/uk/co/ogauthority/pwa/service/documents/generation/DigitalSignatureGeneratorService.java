package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;

@Service
public class DigitalSignatureGeneratorService implements DocumentSectionGenerator {

  private final DocumentInstanceService documentInstanceService;
  private final MailMergeService mailMergeService;

  private static final DocumentSection SECTION = DocumentSection.DIGITAL_SIGNATURE;

  @Autowired
  public DigitalSignatureGeneratorService(DocumentInstanceService documentInstanceService,
                                          MailMergeService mailMergeService) {
    this.documentInstanceService = documentInstanceService;
    this.mailMergeService = mailMergeService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {

    var docView = documentInstanceService.getDocumentView(documentInstance, SECTION);
    mailMergeService.mailMerge(docView, docGenType);

    return new DocumentSectionData(
      "documents/consents/sections/digitalSignature",
      Map.of("docView", docView)
    );

  }

}
