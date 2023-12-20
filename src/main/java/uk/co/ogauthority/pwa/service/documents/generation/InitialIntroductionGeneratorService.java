package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;

@Service
public class InitialIntroductionGeneratorService implements DocumentSectionGenerator {

  private final DocumentInstanceService documentInstanceService;
  private final PadAreaService padAreaService;
  private final MailMergeService mailMergeService;

  private static final DocumentSection SECTION = DocumentSection.INITIAL_INTRO;

  @Autowired
  public InitialIntroductionGeneratorService(DocumentInstanceService documentInstanceService,
                                             PadAreaService padAreaService,
                                             MailMergeService mailMergeService) {
    this.documentInstanceService = documentInstanceService;
    this.padAreaService = padAreaService;
    this.mailMergeService = mailMergeService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance,
                                                    DocGenType docGenType) {

    var fieldLinksView = padAreaService.getApplicationAreaLinksView(pwaApplicationDetail);

    String fieldOrOther = Optional.ofNullable(fieldLinksView.getPwaLinkedToDescription())
        .orElse(fieldLinksView.getLinkedAreaNames().stream()
            .map(s -> s.getStringWithTag().getValue())
            .sorted()
            .collect(Collectors.joining(", ")) + " FIELD");

    var docView = documentInstanceService.getDocumentView(documentInstance, SECTION);
    mailMergeService.mailMerge(docView, docGenType);

    // extract the first clause from the doc to be our intro paragraph, remove from list
    String introParagraph = docView.getSections().get(0).getClauses().get(0).getText();
    docView.getSections().get(0).getClauses().remove(0);

    return new DocumentSectionData("documents/consents/sections/initialIntro",
        Map.of(
            "introParagraph", introParagraph,
            "docView", docView,
            "sectionType", SECTION,
            "fieldNameOrOther", fieldOrOther));

  }

}
