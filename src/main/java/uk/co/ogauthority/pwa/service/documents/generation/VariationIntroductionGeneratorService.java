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
public class VariationIntroductionGeneratorService implements DocumentSectionGenerator {

  private final DocumentInstanceService documentInstanceService;
  private final MailMergeService mailMergeService;

  private static final DocumentSection SECTION = DocumentSection.VARIATION_INTRO;

  @Autowired
  public VariationIntroductionGeneratorService(DocumentInstanceService documentInstanceService,
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

    // extract the first clause from the doc to be our intro paragraph, remove from list
    String introParagraph = docView.getSections().get(0).getClauses().get(0).getText();
    docView.getSections().get(0).getClauses().remove(0);

    return new DocumentSectionData("documents/consents/sections/variationIntro",
        Map.of(
            "introParagraph", introParagraph,
            "docView", docView,
            "sectionType", SECTION));

  }

}
