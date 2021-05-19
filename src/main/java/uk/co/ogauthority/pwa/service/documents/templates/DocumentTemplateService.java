package uk.co.ogauthority.pwa.service.documents.templates;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;

@Service
public class DocumentTemplateService {

  private final DocumentTemplateSectionRepository sectionRepository;
  private final DocumentTemplateSectionClauseVersionRepository clauseVersionRepository;
  private final DocumentDtoFactory documentDtoFactory;
  private final DocumentViewService documentViewService;
  private final DocumentTemplateSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository;

  @Autowired
  public DocumentTemplateService(DocumentTemplateSectionRepository sectionRepository,
                                 DocumentTemplateSectionClauseVersionRepository clauseVersionRepository,
                                 DocumentDtoFactory documentDtoFactory,
                                 DocumentViewService documentViewService,
                                 DocumentTemplateSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository) {
    this.sectionRepository = sectionRepository;
    this.clauseVersionRepository = clauseVersionRepository;
    this.documentDtoFactory = documentDtoFactory;
    this.documentViewService = documentViewService;
    this.sectionClauseVersionDtoRepository = sectionClauseVersionDtoRepository;
  }

  /**
   * Create an object containing all relevant information about a document template.
   * @param templateMnem to identify document template by
   * @param documentSpec defining which sections should be included in the doc
   * @return populated model of document template
   */
  public DocumentTemplateDto populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem templateMnem,
                                                                 DocumentSpec documentSpec) {

    var sectionsInDocSpec = documentSpec.getDocumentSectionDisplayOrderMap().keySet().stream()
        .map(Enum::name)
        .collect(Collectors.toSet());

    List<DocumentTemplateSection> sections = sectionRepository
        .getAllByDocumentTemplate_MnemAndNameInAndStatusIs(templateMnem, sectionsInDocSpec, DocumentTemplateSectionStatus.ACTIVE);

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

  public DocumentView getDocumentView(DocumentSpec documentSpec) {

    var templateMnem = DocumentTemplateMnem.getMnemFromDocumentSpec(documentSpec);

    var docSource = new TemplateDocumentSource(templateMnem, documentSpec);

    var sections = documentSpec.getDocumentSectionDisplayOrderMap().keySet();

    var clauseVersionDtos = sectionClauseVersionDtoRepository
        .findAllByDocumentTemplateMnemAndSectionIn(templateMnem, sections)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    return documentViewService.createDocumentView(PwaDocumentType.TEMPLATE, docSource, clauseVersionDtos);

  }

}
