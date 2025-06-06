package uk.co.ogauthority.pwa.service.documents.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.repository.documents.templates.DocumentTemplateSectionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentClauseService;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.SectionClauseVersionDtoTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentTemplateServiceTest {

  @Mock
  private DocumentTemplateSectionRepository templateSectionRepository;

  @Mock
  private DocumentTemplateSectionClauseVersionRepository templateSectionClauseVersionRepository;

  @Mock
  private DocumentDtoFactory documentDtoFactory;

  @Mock
  private Clock clock;

  @Mock
  private DocumentViewService documentViewService;

  @Mock
  private DocumentTemplateSectionClauseVersionDtoRepository documentTemplateSectionClauseVersionDtoRepository;

  @Mock
  private DocumentTemplateSectionClauseRepository documentTemplateSectionClauseRepository;

  @Mock
  private DocumentClauseService documentClauseService;

  @Captor
  private ArgumentCaptor<List<DocumentTemplateSectionClauseVersion>> clauseVersionsCaptor;

  private DocumentTemplateService documentTemplateService;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @BeforeEach
  void setUp() {

    var inst = Instant.now();
    when(clock.instant()).thenReturn(inst);

    documentTemplateService = new DocumentTemplateService(templateSectionRepository,
        templateSectionClauseVersionRepository,
        documentDtoFactory,
        documentViewService,
        documentTemplateSectionClauseVersionDtoRepository,
        documentTemplateSectionClauseRepository,
        documentClauseService);

  }

  @Test
  void populateDocumentDtoFromTemplateMnem() {

    var template = new DocumentTemplate();

    var docSpec = DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT;

    var sectionNames = docSpec.getSectionNames();

    Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToClauseListMap = DocumentDtoTestUtils.createArgMap(template, clock);

    var sections = new ArrayList<>(sectionToClauseListMap.keySet());

    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndNameInAndStatusIs(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, sectionNames, DocumentTemplateSectionStatus.ACTIVE))
        .thenReturn(sections);

    var clauseVersions = sectionToClauseListMap.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    when(templateSectionClauseVersionRepository
        .getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrueAndStatusIs(sections, SectionClauseVersionStatus.ACTIVE))
        .thenReturn(clauseVersions);

    documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, docSpec);

    verify(documentDtoFactory, times(1)).create(sectionToClauseListMap);

  }

  @Test
  void populateDocumentDtoFromTemplateMnem_noSections() {
    var docSpec = DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT;
    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndNameInAndStatusIs(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, docSpec.getSectionNames(), DocumentTemplateSectionStatus.ACTIVE))
          .thenReturn(List.of());
    assertThrows(DocumentTemplateException.class, () ->

      documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, docSpec));

  }

  @Test
  void getDocumentView() {

    var list = SectionClauseVersionDtoTestUtils
        .getTemplateSectionClauseVersionDtoList(1, DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT, clock, person, 1, 3, 3);

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    when(documentTemplateSectionClauseVersionDtoRepository
        .findAllByDocumentTemplateMnemAndSectionIn(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT, DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT.getDocumentSectionDisplayOrderMap().keySet()))
        .thenReturn(list);

    documentTemplateService.getDocumentView(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    var castList = list.stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    verify(documentViewService, times(1))
        .createDocumentView(PwaDocumentType.TEMPLATE, docSource, castList);

  }

  @Test
  void getTemplateClauseVersionByClauseIdOrThrow_resultAvailable() {

    var version = new DocumentTemplateSectionClauseVersion();

    when(templateSectionClauseVersionRepository.findByDocumentTemplateSectionClause_IdAndTipFlagIsTrue(1))
        .thenReturn(Optional.of(version));

    var result = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(1);

    assertThat(result).isEqualTo(version);

  }

  @Test
  void getTemplateClauseVersionByClauseIdOrThrow_noResultAvailable() {
    assertThrows(DocumentTemplateException.class, () ->

      documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(1));

  }

  @Test
  void addClauseAfter() {

    var addAfter = new DocumentTemplateSectionClauseVersion();

    var version = new DocumentTemplateSectionClauseVersion();
    var clause = new DocumentTemplateSectionClause();
    version.setClause(clause);

    when(documentClauseService.addClauseAfter(eq(PwaDocumentType.TEMPLATE), any(), any(), any())).thenReturn(version);

    documentTemplateService.addClauseAfter(addAfter, new ClauseForm(), person);

    verify(documentTemplateSectionClauseRepository, times(1)).save(clause);

    verify(templateSectionClauseVersionRepository, times(1)).save(version);

  }

  @Test
  void addClauseBefore() {

    var addBeforeVersion = new DocumentTemplateSectionClauseVersion();
    var addBeforeClause = new DocumentTemplateSectionClause();
    var addBeforeSection = new DocumentTemplateSection();
    addBeforeClause.setSection(addBeforeSection);
    addBeforeVersion.setClause(addBeforeClause);

    var v1 = new DocumentTemplateSectionClauseVersion();
    var c1 = new DocumentTemplateSectionClause();
    v1.setClause(c1);

    var v2 = new DocumentTemplateSectionClauseVersion();
    var c2 = new DocumentTemplateSectionClause();
    v2.setClause(c2);

    var updatedVersions = List.of(v1, v2);

    var castReturnList = updatedVersions.stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    when(documentClauseService.addClauseBefore(eq(PwaDocumentType.TEMPLATE), any(), any(), any(), any()))
        .thenReturn(castReturnList);

    documentTemplateService.addClauseBefore(addBeforeVersion, new ClauseForm(), person);

    verify(documentTemplateSectionClauseRepository, times(1)).saveAll(List.of(c1, c2));

    verify(templateSectionClauseVersionRepository, times(1)).saveAll(List.of(v1, v2));

  }

  @Test
  void addSubClauseFor() {

    var addSubFor = new DocumentTemplateSectionClauseVersion();

    var version = new DocumentTemplateSectionClauseVersion();
    var clause = new DocumentTemplateSectionClause();
    version.setClause(clause);

    when(documentClauseService.addSubClause(eq(PwaDocumentType.TEMPLATE), any(), any(), any())).thenReturn(version);

    documentTemplateService.addSubClause(addSubFor, new ClauseForm(), person);

    verify(documentTemplateSectionClauseRepository, times(1)).save(clause);

    verify(templateSectionClauseVersionRepository, times(1)).save(version);

  }

  @Test
  void editClause_updatedClauseVersionsSaved() {

    var originalClauseVersion = new DocumentTemplateSectionClauseVersion();
    var newClauseVersion = new DocumentTemplateSectionClauseVersion();

    when(documentClauseService.editClause(eq(PwaDocumentType.TEMPLATE), eq(originalClauseVersion), any(), any()))
        .thenReturn(List.of(originalClauseVersion, newClauseVersion));

    documentTemplateService.editClause(originalClauseVersion, new ClauseForm(), person);

    verify(templateSectionClauseVersionRepository, times(1)).saveAll(eq(List.of(originalClauseVersion, newClauseVersion)));

  }

  @Test
  void removeClause_updatedClausesSaved() {

    var parentClauseVersion = new DocumentTemplateSectionClauseVersion();
    var parentClause = new DocumentTemplateSectionClause();
    var documentTemplateSection = new DocumentTemplateSection();
    parentClause.setDocumentTemplateSection(documentTemplateSection);
    parentClauseVersion.setDocumentTemplateSectionClause(parentClause);

    when(templateSectionClauseVersionRepository.findByDocumentTemplateSectionClause_IdAndTipFlagIsTrue(1))
        .thenReturn(Optional.of(parentClauseVersion));

    var childClauseVersion = new DocumentTemplateSectionClauseVersion();
    var subChildClauseVersion = new DocumentTemplateSectionClauseVersion();

    var updatedList = List.of(parentClauseVersion, childClauseVersion, subChildClauseVersion);

    var castList = updatedList.stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    when(documentClauseService.removeClause(eq(parentClauseVersion), eq(person), any(), any()))
        .thenReturn(castList);

    documentTemplateService.removeClause(1, person);

    verify(templateSectionClauseVersionRepository, times(1)).saveAll(clauseVersionsCaptor.capture());

    assertThat(clauseVersionsCaptor.getValue())
        .hasSize(updatedList.size())
        .containsAll(updatedList);

  }

}
