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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentClauseService;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
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
    documentTemplate.setMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

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

    when(documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))
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

    documentInstanceService.clearClauses(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    verify(instanceSectionClauseVersionRepository, times(1)).deleteAll(versionList);

    verify(instanceSectionClauseRepository, times(1)).deleteAll(clauseList);

  }

  @Test
  public void getDocumentInstance() {

    documentInstanceService.getDocumentInstance(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    verify(documentInstanceRepository, times(1))
        .findByPwaApplicationAndDocumentTemplate_Mnem(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

  }

  @Test
  public void getDocumentView() {

    var docInstance = new DocumentInstance();
    docInstance.setPwaApplication(applicationDetail.getPwaApplication());
    docInstance.setDocumentTemplate(documentTemplate);
    docInstance.setId(1);

    var list = SectionClauseVersionDtoTestUtils
        .getInstanceSectionClauseVersionDtoList(1, applicationDetail.getPwaApplicationType().getConsentDocumentSpec() , clock, person, 2, 3, 3);

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

    var list = SectionClauseVersionDtoTestUtils
        .getInstanceSectionClauseVersionDtoList(1, applicationDetail.getPwaApplicationType().getConsentDocumentSpec() , clock, person, 1, 3, 3);

    when(documentInstanceSectionClauseVersionDtoRepository.findAllByDiId_AndSectionNameEquals(docInstance.getId(), DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name()))
        .thenReturn(list);

    documentInstanceService.getDocumentView(docInstance, DocumentSection.INITIAL_TERMS_AND_CONDITIONS);

    var castList = list.stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    verify(documentViewService, times(1))
        .createDocumentView(PwaDocumentType.INSTANCE, applicationDetail.getPwaApplication(), castList);

  }

  @Test
  public void getSectionClauseView() {

    var clauseId = 1;
    var documentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    var documentInstanceSectionClause = new DocumentInstanceSectionClause();
    var documentInstance = new DocumentInstance();
    var documentTemplate = new DocumentTemplate();

    documentTemplate.setMnem(DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    documentInstance.setId(1);
    documentInstance.setDocumentTemplate(documentTemplate);
    documentInstance.setPwaApplication(applicationDetail.getPwaApplication());
    documentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(documentInstanceSectionClause);
    documentInstanceSectionClauseVersion.setStatus(SectionClauseVersionStatus.ACTIVE);
    documentInstanceSectionClause.setDocumentInstance(documentInstance);

    when(instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(clauseId))
        .thenReturn(Optional.of(documentInstanceSectionClauseVersion));

    var sectionName = DocumentSection.SCHEDULE_2.name();
    DocumentInstanceSectionClauseVersionDto dto1 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(sectionName, "some text 1", 1, 1);
    DocumentInstanceSectionClauseVersionDto dto2 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(sectionName, "some text 2", 2, 2);
    dto2.setParentClauseId(dto1.getClauseId());
    DocumentInstanceSectionClauseVersionDto dto3 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(sectionName, "some text 3", 3, 2);
    dto3.setParentClauseId(dto2.getClauseId());
    when(documentInstanceSectionClauseVersionDtoRepository.findAllByDiId(documentInstance.getId()))
        .thenReturn(List.of(dto1, dto2, dto3));

    when(documentViewService.createDocumentView(any(), any(), any())).thenCallRealMethod();

    var sectionClauseVersionView = documentInstanceService.getSectionClauseView(clauseId);

    assertThat(sectionClauseVersionView.getName()).isEqualTo(dto1.getName());
    assertThat(sectionClauseVersionView.getText()).isEqualTo(dto1.getText());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getName()).isEqualTo(dto2.getName());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getText()).isEqualTo(dto2.getText());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getChildClauses().get(0).getName()).isEqualTo(dto3.getName());
    assertThat(sectionClauseVersionView.getChildClauses().get(0).getChildClauses().get(0).getText()).isEqualTo(dto3.getText());
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
  public void addSubClauseFor_clauseIsNotLinkedToTemplateClause() {

    clauseRecord.setDocumentTemplateSection(section);

    documentInstanceService.addSubClause(clauseVersion, buildClauseForm(), person);

    verify(instanceSectionClauseRepository, times(1)).save(singleClauseCaptor.capture());
    verify(instanceSectionClauseVersionRepository, times(1)).save(singleClauseVersionCaptor.capture());

    var newClause = singleClauseCaptor.getValue();
    assertThat(newClause.getDocumentInstance()).isEqualTo(clauseRecord.getDocumentInstance());
    assertThat(newClause.getDocumentTemplateSectionClause()).isEmpty();
    assertThat(newClause.getDocumentTemplateSection()).contains(section);

    var newClauseVersion = singleClauseVersionCaptor.getValue();

    assertThat(newClauseVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newClauseVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newClauseVersion.getDocumentInstanceSectionClause()).isEqualTo(newClause);
    assertThat(newClauseVersion.getLevelOrder()).isEqualTo(1);
    assertThat(newClauseVersion.getVersionNo()).isEqualTo(1);
    assertThat(newClauseVersion.getTipFlag()).isTrue();
    assertThat(newClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newClauseVersion.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(newClauseVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(newClauseVersion.getParentDocumentInstanceSectionClause()).isEqualTo(clauseRecord);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newClauseVersion, List.of("id", "endedByPersonId", "endedTimestamp"));

  }

  @Test
  public void addSubClauseFor_clauseIsLinkedToTemplateClause() {

    clauseRecord.setDocumentTemplateSectionClause(templateClause);

    documentInstanceService.addSubClause(clauseVersion, buildClauseForm(), person);

    verify(instanceSectionClauseRepository, times(1)).save(singleClauseCaptor.capture());
    verify(instanceSectionClauseVersionRepository, times(1)).save(singleClauseVersionCaptor.capture());

    var newClause = singleClauseCaptor.getValue();
    assertThat(newClause.getDocumentInstance()).isEqualTo(clauseRecord.getDocumentInstance());
    assertThat(newClause.getDocumentTemplateSectionClause()).isEmpty();
    assertThat(newClause.getDocumentTemplateSection()).contains(section);

    var newClauseVersion = singleClauseVersionCaptor.getValue();

    assertThat(newClauseVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newClauseVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newClauseVersion.getDocumentInstanceSectionClause()).isEqualTo(newClause);
    assertThat(newClauseVersion.getLevelOrder()).isEqualTo(1);
    assertThat(newClauseVersion.getVersionNo()).isEqualTo(1);
    assertThat(newClauseVersion.getTipFlag()).isTrue();
    assertThat(newClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newClauseVersion.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(newClauseVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(newClauseVersion.getParentDocumentInstanceSectionClause()).isEqualTo(clauseRecord);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newClauseVersion, List.of("id", "endedByPersonId", "endedTimestamp"));

  }

  @Test
  public void editClause_newClauseCreated_originalClauseEnded() {

    var clause = new DocumentInstanceSectionClause();
    var parent = new DocumentInstanceSectionClause();
    var originalClauseVersion = new DocumentInstanceSectionClauseVersion();

    originalClauseVersion.setId(1);
    originalClauseVersion.setDocumentInstanceSectionClause(clause);
    originalClauseVersion.setParentDocumentInstanceSectionClause(parent);
    originalClauseVersion.setStatus(SectionClauseVersionStatus.ACTIVE);
    originalClauseVersion.setTipFlag(true);
    originalClauseVersion.setName("original_name");
    originalClauseVersion.setText("original_text");
    originalClauseVersion.setVersionNo(1);
    originalClauseVersion.setLevelOrder(5);
    originalClauseVersion.setCreatedByPersonId(person.getId());
    originalClauseVersion.setCreatedTimestamp(clock.instant());

    documentInstanceService.editClause(originalClauseVersion, buildClauseForm(), new Person(222, null, null, null, null));

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(clauseVersionsCaptor.capture());

    originalClauseVersion = clauseVersionsCaptor.getValue().stream()
        .filter(v -> v.getId() == 1)
        .findFirst()
        .orElseThrow();

    // initial version updated properly
    assertThat(originalClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.DELETED);
    assertThat(originalClauseVersion.getEndedByPersonId()).isEqualTo(new PersonId(222));
    assertThat(originalClauseVersion.getEndedTimestamp()).isEqualTo(clock.instant());
    assertThat(originalClauseVersion.getTipFlag()).isFalse();

    var newVersion = clauseVersionsCaptor.getValue().stream()
        .filter(v -> v.getId() == null)
        .findFirst()
        .orElseThrow();

    // new version set properly
    assertThat(newVersion.getDocumentInstanceSectionClause()).isEqualTo(clause);
    assertThat(newVersion.getParentDocumentInstanceSectionClause()).isEqualTo(parent);
    assertThat(newVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newVersion.getTipFlag()).isTrue();
    assertThat(newVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newVersion.getVersionNo()).isEqualTo(originalClauseVersion.getVersionNo() + 1);
    assertThat(newVersion.getLevelOrder()).isEqualTo(originalClauseVersion.getLevelOrder());
    assertThat(newVersion.getCreatedByPersonId()).isEqualTo(new PersonId(222));
    assertThat(newVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newVersion, List.of("id", "endedTimestamp", "endedByPersonId"));

  }

  @Test
  public void removeClause() {

    //sub child clause
    var subChildDocumentInstance = new DocumentInstance();
    var subChildDocumentInstanceSectionClause = new DocumentInstanceSectionClause();
    var subChildDocumentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    subChildDocumentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(subChildDocumentInstanceSectionClause);
    subChildDocumentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().setDocumentInstance(subChildDocumentInstance);


    //child clause
    var childDocumentInstance = new DocumentInstance();
    var childDocumentInstanceSectionClause = new DocumentInstanceSectionClause();
    var childDocumentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    childDocumentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(childDocumentInstanceSectionClause);
    childDocumentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().setDocumentInstance(childDocumentInstance);

    when(instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(
        childDocumentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().getDocumentInstance(),
        childDocumentInstanceSectionClauseVersion.getDocumentInstanceSectionClause()
    )).thenReturn(List.of(subChildDocumentInstanceSectionClauseVersion));


    //parent clause
    var clauseId = 1;
    var documentInstance = new DocumentInstance();
    var documentInstanceSectionClause = new DocumentInstanceSectionClause();
    var documentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    documentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(documentInstanceSectionClause);
    documentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().setDocumentInstance(documentInstance);

    when(instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(clauseId))
        .thenReturn(Optional.of(documentInstanceSectionClauseVersion));

    when(instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(
        documentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().getDocumentInstance(),
        documentInstanceSectionClauseVersion.getDocumentInstanceSectionClause()
    )).thenReturn(List.of(childDocumentInstanceSectionClauseVersion));


    //assertions for parent clause

    var person = new Person(1, "name", null, null, null);
    documentInstanceService.removeClause(clauseId, person);

    ArgumentCaptor<DocumentInstanceSectionClauseVersion> docInstanceSectionClauseVersionCaptor = ArgumentCaptor.forClass(DocumentInstanceSectionClauseVersion.class);
    verify(instanceSectionClauseVersionRepository, times(1)).save(docInstanceSectionClauseVersionCaptor.capture());
    assertThat(docInstanceSectionClauseVersionCaptor.getValue().getStatus()).isEqualTo(SectionClauseVersionStatus.DELETED);
    assertThat(docInstanceSectionClauseVersionCaptor.getValue().getEndedByPersonId()).isEqualTo(person.getId());
    assertThat(docInstanceSectionClauseVersionCaptor.getValue().getEndedTimestamp()).isEqualTo(clock.instant());


    //assertions for child clause
    ArgumentCaptor<List<DocumentInstanceSectionClauseVersion>> childDocInstanceSectionClauseVersionCaptor = ArgumentCaptor.forClass(List.class);
    verify(instanceSectionClauseVersionRepository, times(2)).saveAll(childDocInstanceSectionClauseVersionCaptor.capture());
    assertThat(childDocInstanceSectionClauseVersionCaptor.getValue().get(0).getStatus()).isEqualTo(SectionClauseVersionStatus.DELETED);
    assertThat(childDocInstanceSectionClauseVersionCaptor.getValue().get(0).getEndedByPersonId()).isEqualTo(person.getId());
    assertThat(childDocInstanceSectionClauseVersionCaptor.getValue().get(0).getEndedTimestamp()).isEqualTo(clock.instant());


  }

}
