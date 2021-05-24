package uk.co.ogauthority.pwa.service.documents.templates;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentClauseService;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;

@Service
public class DocumentTemplateService {

  private final DocumentTemplateSectionRepository sectionRepository;
  private final DocumentTemplateSectionClauseVersionRepository clauseVersionRepository;
  private final DocumentDtoFactory documentDtoFactory;
  private final DocumentViewService documentViewService;
  private final DocumentTemplateSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository;
  private final DocumentTemplateSectionClauseRepository documentTemplateSectionClauseRepository;
  private final DocumentClauseService documentClauseService;

  @Autowired
  public DocumentTemplateService(DocumentTemplateSectionRepository sectionRepository,
                                 DocumentTemplateSectionClauseVersionRepository clauseVersionRepository,
                                 DocumentDtoFactory documentDtoFactory,
                                 DocumentViewService documentViewService,
                                 DocumentTemplateSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository,
                                 DocumentTemplateSectionClauseRepository documentTemplateSectionClauseRepository,
                                 DocumentClauseService documentClauseService) {
    this.sectionRepository = sectionRepository;
    this.clauseVersionRepository = clauseVersionRepository;
    this.documentDtoFactory = documentDtoFactory;
    this.documentViewService = documentViewService;
    this.sectionClauseVersionDtoRepository = sectionClauseVersionDtoRepository;
    this.documentTemplateSectionClauseRepository = documentTemplateSectionClauseRepository;
    this.documentClauseService = documentClauseService;
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

    var docSource = new TemplateDocumentSource(documentSpec);

    var sections = documentSpec.getDocumentSectionDisplayOrderMap().keySet();

    var clauseVersionDtos = sectionClauseVersionDtoRepository
        .findAllByDocumentTemplateMnemAndSectionIn(templateMnem, sections)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    return documentViewService.createDocumentView(PwaDocumentType.TEMPLATE, docSource, clauseVersionDtos);

  }

  public DocumentTemplateSectionClauseVersion getTemplateClauseVersionByClauseIdOrThrow(Integer clauseId) {
    return clauseVersionRepository
        .findByDocumentTemplateSectionClause_IdAndTipFlagIsTrue(clauseId)
        .orElseThrow(() -> new DocumentTemplateException(String.format("Couldn't find template clause with ID: [%s]", clauseId)));
  }

  /**
   * Add a new clause on the same level as but one position after the passed-in clause.
   */
  @Transactional
  public void addClauseAfter(DocumentTemplateSectionClauseVersion versionToAddAfter, ClauseForm form, Person creatingPerson) {

    var newVersion = (DocumentTemplateSectionClauseVersion) documentClauseService
        .addClauseAfter(PwaDocumentType.TEMPLATE, versionToAddAfter, form, creatingPerson);

    documentTemplateSectionClauseRepository.save(newVersion.getClause());
    clauseVersionRepository.save(newVersion);

  }

  /**
   * Add a new clause on the same level as but one position before the passed-in clause.
   * The passed-in clause and any clauses following that (on the same level) are re-ordered.
   */
  @Transactional
  public void addClauseBefore(DocumentTemplateSectionClauseVersion versionToAddBefore, ClauseForm form, Person creatingPerson) {

    var section = versionToAddBefore.getClause().getSection();
    var parentClause = versionToAddBefore.getParentDocumentTemplateSectionClause();

    var clauseVersionsToUpdate = documentClauseService.addClauseBefore(
        PwaDocumentType.TEMPLATE,
        versionToAddBefore,
        form,
        creatingPerson,
        () -> clauseVersionRepository
            .findByDocumentTemplateSectionClause_DocumentTemplateSectionAndParentDocumentTemplateSectionClause(section, parentClause)
            .stream()
            .map(SectionClauseVersion.class::cast)
    );

    var castVersionList = clauseVersionsToUpdate.stream()
        .map(DocumentTemplateSectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    var clauses = castVersionList.stream()
        .map(DocumentTemplateSectionClauseVersion::getClause)
        .collect(Collectors.toList());

    documentTemplateSectionClauseRepository.saveAll(clauses);

    clauseVersionRepository.saveAll(castVersionList);

  }

}
