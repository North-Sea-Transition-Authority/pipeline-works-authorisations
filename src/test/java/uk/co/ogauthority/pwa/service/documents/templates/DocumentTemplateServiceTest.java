package uk.co.ogauthority.pwa.service.documents.templates;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.exception.documents.DocumentTemplateException;
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

@RunWith(MockitoJUnitRunner.class)
public class DocumentTemplateServiceTest {

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

  private DocumentTemplateService documentTemplateService;

  private Person person = PersonTestUtil.createDefaultPerson();

  @Before
  public void setUp() {

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
  public void populateDocumentDtoFromTemplateMnem() {

    var template = new DocumentTemplate();

    var docSpec = DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT;

    var sectionNames = docSpec.getSectionNames();

    Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToClauseListMap = DocumentDtoTestUtils.createArgMap(template, clock);

    var sections = new ArrayList<>(sectionToClauseListMap.keySet());

    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndNameInAndStatusIs(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, sectionNames, DocumentTemplateSectionStatus.ACTIVE))
        .thenReturn(sections);

    var clauseVersions = sectionToClauseListMap.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    when(templateSectionClauseVersionRepository.getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrue(sections))
        .thenReturn(clauseVersions);

    documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, docSpec);

    verify(documentDtoFactory, times(1)).create(sectionToClauseListMap);

  }

  @Test(expected = DocumentTemplateException.class)
  public void populateDocumentDtoFromTemplateMnem_noSections() {

    var docSpec = DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT;

    when(templateSectionRepository.getAllByDocumentTemplate_MnemAndNameInAndStatusIs(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, docSpec.getSectionNames(), DocumentTemplateSectionStatus.ACTIVE))
        .thenReturn(List.of());

    documentTemplateService.populateDocumentDtoFromTemplateMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, docSpec);

  }

  @Test
  public void getDocumentView() {

    var list = SectionClauseVersionDtoTestUtils
        .getTemplateSectionClauseVersionDtoList(1, DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT, clock, person, 1, 3, 3);

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    when(documentTemplateSectionClauseVersionDtoRepository
        .findAllByDocumentTemplateMnemAndSectionIn(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT, DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT.getDocumentSectionDisplayOrderMap().keySet()))
        .thenReturn(list);

    documentTemplateService.getDocumentView(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    var castList = list.stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    verify(documentViewService, times(1))
        .createDocumentView(PwaDocumentType.TEMPLATE, docSource, castList);

  }

  @Test
  public void getTemplateClauseVersionByClauseIdOrThrow_resultAvailable() {

    var version = new DocumentTemplateSectionClauseVersion();

    when(templateSectionClauseVersionRepository.findByDocumentTemplateSectionClause_IdAndTipFlagIsTrue(1))
        .thenReturn(Optional.of(version));

    var result = documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(1);

    assertThat(result).isEqualTo(version);

  }

  @Test(expected = DocumentTemplateException.class)
  public void getTemplateClauseVersionByClauseIdOrThrow_noResultAvailable() {

    documentTemplateService.getTemplateClauseVersionByClauseIdOrThrow(1);

  }

  @Test
  public void addClauseAfter() {

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
  public void addClauseBefore() {

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

}
