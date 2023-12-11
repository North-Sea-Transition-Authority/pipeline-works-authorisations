package uk.co.ogauthority.pwa.service.documents.instances;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentClauseService;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.SectionClauseVersionDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentInstanceServiceTest {

  @Mock
  private DocumentInstanceRepository documentInstanceRepository;

  @Mock
  private DocumentInstanceSectionClauseRepository instanceSectionClauseRepository;

  @Mock
  private DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository;

  @Mock
  private Clock clock;

  @Mock
  private DocumentInstanceSectionClauseVersionDtoRepository documentInstanceSectionClauseVersionDtoRepository;

  @Mock
  private DocumentViewService documentViewService;

  @Mock
  private DocumentClauseService documentClauseService;

  @Captor
  private ArgumentCaptor<DocumentInstance> docInstanceCaptor;

  @Captor
  private ArgumentCaptor<Collection<DocumentInstanceSectionClause>> clausesCaptor;

  @Captor
  private ArgumentCaptor<List<DocumentInstanceSectionClauseVersion>> clauseVersionsCaptor;

  @Captor
  private ArgumentCaptor<DocumentInstanceSectionClauseVersion> singleClauseVersionCaptor;

  @Captor
  private ArgumentCaptor<DocumentInstanceSectionClause> singleClauseCaptor;

  private DocumentInstanceService documentInstanceService;

  private DocumentTemplate documentTemplate;
  private PwaApplicationDetail applicationDetail;
  private Person person;

  private Instant fixedInstant;

  private DocumentInstanceSectionClause parent;
  private DocumentTemplateSection section;
  private DocumentTemplateSectionClause templateClause;
  private DocumentInstanceSectionClause clauseRecord;
  private DocumentInstanceSectionClauseVersion clauseVersion;

  @Before
  public void setUp() {

    documentTemplate = new DocumentTemplate();
    documentTemplate.setMnem(DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);

    person = new Person(1, null, null, null, null);

    applicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    fixedInstant = Instant.now();
    when(clock.instant()).thenReturn(fixedInstant);

    documentInstanceService = new DocumentInstanceService(
        documentInstanceRepository,
        instanceSectionClauseRepository,
        instanceSectionClauseVersionRepository,
        new SectionClauseCreator(clock),
        clock,
        documentInstanceSectionClauseVersionDtoRepository,
        documentViewService,
        documentClauseService
    );

    parent = new DocumentInstanceSectionClause();

    section = new DocumentTemplateSection();
    section.setName("test section");

    templateClause = new DocumentTemplateSectionClause();
    templateClause.setDocumentTemplateSection(section);

    clauseRecord = new DocumentInstanceSectionClause();
    clauseRecord.setDocumentInstance(new DocumentInstance());

    clauseVersion = new DocumentInstanceSectionClauseVersion();
    clauseVersion.setLevelOrder(3);
    clauseVersion.setParentDocumentInstanceSectionClause(parent);
    clauseVersion.setDocumentInstanceSectionClause(clauseRecord);

  }

  @Test
  public void createFromDocumentDto_whenDocumentHasMultipleSections_andMultipleClausesPerSection() {

    var docDto = DocumentDtoTestUtils.create(documentTemplate, clock, person, 2, 3, 3);

    documentInstanceService.createFromDocumentDto(applicationDetail.getPwaApplication(), docDto, person);

    verify(documentInstanceRepository, times(1)).save(docInstanceCaptor.capture());

    // check created instance
    var instance = docInstanceCaptor.getValue();

    assertThat(instance)
        .extracting(DocumentInstance::getDocumentTemplate, DocumentInstance::getPwaApplication, DocumentInstance::getCreatedTimestamp)
        .containsExactly(documentTemplate, applicationDetail.getPwaApplication(), fixedInstant);

    verify(instanceSectionClauseRepository, times(1)).saveAll(clausesCaptor.capture());

    // map the created clauses to the id of the template clause they were created from
    Map<Integer, DocumentInstanceSectionClause> templateClauseIdToInstanceClauseMap = clausesCaptor.getValue().stream()
        .collect(Collectors.toMap(this::getTemplateClauseIdOrThrow, Function.identity()));

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(clauseVersionsCaptor.capture());

    // map the new instance clauses to their version
    List<DocumentInstanceSectionClauseVersion> clauseVersions = new ArrayList<>(clauseVersionsCaptor.getValue());
    Map<DocumentInstanceSectionClause, DocumentInstanceSectionClauseVersion> instanceClauseToVersionMap = clauseVersions.stream()
        .collect(Collectors.toMap(DocumentInstanceSectionClauseVersion::getDocumentInstanceSectionClause, Function.identity()));

    // check that for each section
    for (SectionDto section: docDto.getSections()) {

      int rootClauseCounter = 0;

      // and each template clause within that section
      for (TemplateSectionClauseVersionDto templateClauseVersion : section.getClauses()) {

        // a corresponding instance clause was created, which we can lookup using the template clause id
        var instanceClause = templateClauseIdToInstanceClauseMap.get(templateClauseVersion.getTemplateClauseRecord().getId());
        assertThat(instanceClause).isNotNull();

        // a version was created for the new instance clause
        var instanceClauseVersion = instanceClauseToVersionMap.get(instanceClause);
        assertThat(instanceClauseVersion).isNotNull();

        // assert that templateClauseVersion and instanceClauseVersion have equal fields
        assertThat(templateClauseVersion.getName()).isEqualTo(instanceClauseVersion.getName());
        assertThat(templateClauseVersion.getText()).isEqualTo(instanceClauseVersion.getText());
        assertThat(templateClauseVersion.getVersionNo()).isEqualTo(instanceClauseVersion.getVersionNo());
        assertThat(templateClauseVersion.getTipFlag()).isEqualTo(instanceClauseVersion.getTipFlag());
        assertThat(templateClauseVersion.getLevelOrder()).isEqualTo(instanceClauseVersion.getLevelOrder());
        assertThat(templateClauseVersion.getStatus()).isEqualTo(instanceClauseVersion.getStatus());
        assertThat(templateClauseVersion.getCreatedTimestamp()).isEqualTo(instanceClauseVersion.getCreatedTimestamp());
        assertThat(templateClauseVersion.getCreatedByPersonId()).isEqualTo(instanceClauseVersion.getCreatedByPersonId());

        // if the template clause has a parent, make sure that the instance parent can be found by using the template parent
        // this proves that the instance parents were created properly and the template model was mirrored correctly.
        if (templateClauseVersion.getParentTemplateClause() != null) {

          var parentTemplateClause = templateClauseVersion.getParentTemplateClause();
          var parentInstanceClause = templateClauseIdToInstanceClauseMap.get(parentTemplateClause.getId());

          assertThat(parentInstanceClause).isNotNull();

        } else {

          // if no parent, increment the root counter so we can check that the number of root clauses for the section is correct
          rootClauseCounter++;

        }

      }

      assertThat(rootClauseCounter).isEqualTo(3);

    }

  }

  private Integer getTemplateClauseIdOrThrow(DocumentInstanceSectionClause instanceSectionClause) {
    return instanceSectionClause.getDocumentTemplateSectionClause()
        .map(DocumentTemplateSectionClause::getId)
        .orElseThrow(() -> new IllegalStateException(String.format(
            "Instance clause with id [%s] doesn't have an associated template clause",
            instanceSectionClause.getId())));
  }

  @Test
  public void clearClauses() {

    var instance = new DocumentInstance(documentTemplate, applicationDetail.getPwaApplication(), fixedInstant);

    when(documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT))
        .thenReturn(Optional.of(instance));

    var clause1 = new DocumentInstanceSectionClause();
    clause1.setId(1);

    var clause2 = new DocumentInstanceSectionClause();
    clause1.setId(2);

    var clauseList = List.of(clause1, clause2);

    when(instanceSectionClauseRepository.findAllByDocumentInstance(instance)).thenReturn(clauseList);

    var clauseVersion1 = new DocumentInstanceSectionClauseVersion();
    clauseVersion1.setId(1);

    var clauseVersion2 = new DocumentInstanceSectionClauseVersion();
    clauseVersion1.setId(2);

    var versionList = List.of(clauseVersion1, clauseVersion2);

    when(instanceSectionClauseVersionRepository.findAllByDocumentInstanceSectionClauseIn(clauseList)).thenReturn(versionList);

    documentInstanceService.clearClauses(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);

    verify(instanceSectionClauseVersionRepository, times(1)).deleteAll(versionList);

    verify(instanceSectionClauseRepository, times(1)).deleteAll(clauseList);

  }

  @Test
  public void getDocumentInstance() {

    documentInstanceService.getDocumentInstance(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);

    verify(documentInstanceRepository, times(1))
        .findByPwaApplicationAndDocumentTemplate_Mnem(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);

  }

  @Test
  public void getDocumentView() {

    var docInstance = new DocumentInstance();
    docInstance.setPwaApplication(applicationDetail.getPwaApplication());
    docInstance.setDocumentTemplate(documentTemplate);
    docInstance.setId(1);
    var docSpec = DocumentSpec.getSpecForApplication(applicationDetail.getPwaApplication());

    var list = SectionClauseVersionDtoTestUtils
        .getInstanceSectionClauseVersionDtoList(1, docSpec , clock, person, 2, 3, 3);

    when(documentInstanceSectionClauseVersionDtoRepository.findAllByDiId(any())).thenReturn(list);

    documentInstanceService.getDocumentView(docInstance);

    var castList = list.stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    verify(documentViewService, times(1))
        .createDocumentView(PwaDocumentType.INSTANCE, applicationDetail.getPwaApplication(), castList);

  }

  @Test
  public void getDocumentView_specificSectionOnly() {

    var docInstance = new DocumentInstance();
    docInstance.setPwaApplication(applicationDetail.getPwaApplication());
    docInstance.setDocumentTemplate(documentTemplate);
    docInstance.setId(1);
    var docSpec = DocumentSpec.getSpecForApplication(applicationDetail.getPwaApplication());

    var list = SectionClauseVersionDtoTestUtils
        .getInstanceSectionClauseVersionDtoList(1, docSpec , clock, person, 1, 3, 3);

    when(documentInstanceSectionClauseVersionDtoRepository.findAllByDiId_AndSectionNameEquals(docInstance.getId(), DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name()))
        .thenReturn(list);

    documentInstanceService.getDocumentView(docInstance, DocumentSection.INITIAL_TERMS_AND_CONDITIONS);

    var castList = list.stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    verify(documentViewService, times(1))
        .createDocumentView(PwaDocumentType.INSTANCE, applicationDetail.getPwaApplication(), castList);

  }

  private ClauseForm buildClauseForm() {
    var form = new ClauseForm();
    form.setName("name");
    form.setText("text");
    return form;
  }

  @Test
  public void addClauseAfter() {

    var docInstance = new DocumentInstance();

    var addAfterVersion = new DocumentInstanceSectionClauseVersion();
    var addAfterClause = new DocumentInstanceSectionClause();
    addAfterClause.setDocumentInstance(docInstance);
    addAfterVersion.setDocumentInstanceSectionClause(addAfterClause);

    var version = new DocumentInstanceSectionClauseVersion();
    var clause = new DocumentInstanceSectionClause();
    version.setClause(clause);

    when(documentClauseService.addClauseAfter(eq(PwaDocumentType.INSTANCE), any(), any(), any())).thenReturn(version);

    documentInstanceService.addClauseAfter(addAfterVersion, new ClauseForm(), person);

    verify(instanceSectionClauseRepository, times(1)).save(clause);

    verify(instanceSectionClauseVersionRepository, times(1)).save(version);

  }

  @Test
  public void addClauseBefore() {

    var docInstance = new DocumentInstance();

    var addBeforeVersion = new DocumentInstanceSectionClauseVersion();
    var addBeforeClause = new DocumentInstanceSectionClause();
    var addBeforeSection = new DocumentTemplateSection();
    addBeforeClause.setSection(addBeforeSection);
    addBeforeClause.setDocumentInstance(docInstance);
    addBeforeVersion.setClause(addBeforeClause);

    var v1 = new DocumentInstanceSectionClauseVersion();
    var c1 = new DocumentInstanceSectionClause();
    v1.setClause(c1);

    var v2 = new DocumentInstanceSectionClauseVersion();
    var c2 = new DocumentInstanceSectionClause();
    c2.setDocumentInstance(docInstance);
    v2.setClause(c2);

    var updatedVersions = List.of(v1, v2);

    var castReturnList = updatedVersions.stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    when(documentClauseService.addClauseBefore(eq(PwaDocumentType.INSTANCE), any(), any(), any(), any()))
        .thenReturn(castReturnList);

    documentInstanceService.addClauseBefore(addBeforeVersion, new ClauseForm(), person);

    verify(instanceSectionClauseRepository, times(1)).save(c1);

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(List.of(v1, v2));

  }

  @Test
  public void addSubClauseFor() {

    var docInstance = new DocumentInstance();

    var addSubVersion = new DocumentInstanceSectionClauseVersion();
    var addSubClause = new DocumentInstanceSectionClause();
    addSubClause.setDocumentInstance(docInstance);
    addSubVersion.setDocumentInstanceSectionClause(addSubClause);

    var version = new DocumentInstanceSectionClauseVersion();
    var clause = new DocumentInstanceSectionClause();
    version.setClause(clause);

    when(documentClauseService.addSubClause(eq(PwaDocumentType.INSTANCE), any(), any(), any())).thenReturn(version);

    documentInstanceService.addSubClause(addSubVersion, new ClauseForm(), person);

    verify(instanceSectionClauseRepository, times(1)).save(clause);

    verify(instanceSectionClauseVersionRepository, times(1)).save(version);

  }

  @Test
  public void editClause_updatedClauseVersionsSaved() {

    var originalClauseVersion = new DocumentInstanceSectionClauseVersion();
    var newClauseVersion = new DocumentInstanceSectionClauseVersion();

    when(documentClauseService.editClause(eq(PwaDocumentType.INSTANCE), eq(originalClauseVersion), any(), any()))
        .thenReturn(List.of(originalClauseVersion, newClauseVersion));

    documentInstanceService.editClause(originalClauseVersion, buildClauseForm(), person);

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(eq(List.of(originalClauseVersion, newClauseVersion)));

  }

  @Test
  public void removeClause_updatedClausesSaved() {

    var parentClauseVersion = new DocumentInstanceSectionClauseVersion();
    var parentClause = new DocumentInstanceSectionClause();
    var documentInstance = new DocumentInstance();
    parentClause.setDocumentInstance(documentInstance);
    parentClauseVersion.setDocumentInstanceSectionClause(parentClause);

    when(instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(1))
        .thenReturn(Optional.of(parentClauseVersion));

    var childClauseVersion = new DocumentInstanceSectionClauseVersion();
    var subChildClauseVersion = new DocumentInstanceSectionClauseVersion();

    var updatedList = List.of(parentClauseVersion, childClauseVersion, subChildClauseVersion);

    var castList = updatedList.stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    when(documentClauseService.removeClause(eq(parentClauseVersion), eq(person), any(), any()))
        .thenReturn(castList);

    documentInstanceService.removeClause(1, person);

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(clauseVersionsCaptor.capture());

    assertThat(clauseVersionsCaptor.getValue())
        .hasSize(updatedList.size())
        .containsAll(updatedList);

  }

}
