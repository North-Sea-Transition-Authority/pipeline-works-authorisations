package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;

@Service
public class HuooIntroductionGeneratorService implements DocumentSectionGenerator {

  private final DocumentInstanceService documentInstanceService;
  private final MailMergeService mailMergeService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  private static final DocumentSection SECTION = DocumentSection.HUOO_INTRO;

  @Autowired
  public HuooIntroductionGeneratorService(DocumentInstanceService documentInstanceService,
                                          MailMergeService mailMergeService,
                                          PadOrganisationRoleService padOrganisationRoleService) {
    this.documentInstanceService = documentInstanceService;
    this.mailMergeService = mailMergeService;
    this.padOrganisationRoleService = padOrganisationRoleService;
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

    var orgRoleNameToTextMap = padOrganisationRoleService.getRoleCountMap(pwaApplicationDetail)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            e -> e.getKey().name(),
            e -> StringDisplayUtils.pluralise(e.getKey().name(), e.getValue()).toUpperCase()));

    return new DocumentSectionData("documents/consents/sections/huooIntro",
        Map.of(
            "introParagraph", introParagraph,
            "docView", docView,
            "sectionType", SECTION,
            "orgRoleNameToTextMap", orgRoleNameToTextMap));

  }

}
