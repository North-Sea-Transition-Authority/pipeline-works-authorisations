package uk.co.ogauthority.pwa.service.documents.templates;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;

@Service
public class DocumentTemplateService {

  private final DocumentTemplateSectionRepository sectionRepository;
  private final DocumentTemplateSectionClauseVersionRepository clauseVersionRepository;
  private final DocumentDtoFactory documentDtoFactory;

  @Autowired
  public DocumentTemplateService(DocumentTemplateSectionRepository sectionRepository,
                                 DocumentTemplateSectionClauseVersionRepository clauseVersionRepository,
                                 DocumentDtoFactory documentDtoFactory) {
    this.sectionRepository = sectionRepository;
    this.clauseVersionRepository = clauseVersionRepository;
    this.documentDtoFactory = documentDtoFactory;
  }

  /**
   * Create an object containing all relevant information about a document template.
   * @param templateMnem to identify document template by
   * @return populated model of document template
   */
  public DocumentTemplateDto populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem templateMnem) {

    List<DocumentTemplateSection> sections = sectionRepository
        .getAllByDocumentTemplate_MnemAndStatusIs(templateMnem, DocumentTemplateSectionStatus.ACTIVE);

    if (sections.isEmpty()) {
      throw new DocumentTemplateException(
          String.format("Expected at least one active section for template mnem [%s], no active sections found.",
          templateMnem.name()));
    }

    Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToCurrentClauseVersionMap = clauseVersionRepository
        .getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrue(sections)
        .stream()
        .collect(Collectors.groupingBy(version -> version.getClause().getDocumentTemplateSection()));

    return documentDtoFactory.create(sectionToCurrentClauseVersionMap);

  }

}
