package uk.co.ogauthority.pwa.service.documents.instances;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.DocumentInstanceTestUtils;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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

  @Captor
  private ArgumentCaptor<DocumentInstance> docInstanceCaptor;

  @Captor
  private ArgumentCaptor<Collection<DocumentInstanceSectionClause>> clausesCaptor;

  @Captor
  private ArgumentCaptor<List<DocumentInstanceSectionClauseVersion>> clauseVersionsCaptor;

  private SectionClauseCreator sectionClauseCreator;

  private DocumentInstanceService documentInstanceService;

  private DocumentTemplate documentTemplate;
  private PwaApplicationDetail applicationDetail;
  private Person person;

  private Instant fixedInstant;

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
        documentInstanceSectionClauseVersionDtoRepository
    );

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
        .collect(Collectors.toMap(c -> c.getDocumentTemplateSectionClause().getId(), Function.identity()));

    verify(instanceSectionClauseVersionRepository, times(1)).saveAll(clauseVersionsCaptor.capture());

    // map the new instance clauses to their version
    Map<DocumentInstanceSectionClause, DocumentInstanceSectionClauseVersion> instanceClauseToVersionMap = clauseVersionsCaptor.getValue().stream()
        .collect(Collectors.toMap(v -> (DocumentInstanceSectionClause) v.getClause(), Function.identity()));

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

    when(instanceSectionClauseVersionRepository.findAllByDocumentInstanceSectionClauseInAndTipFlagIsTrue(clauseList)).thenReturn(versionList);

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
  public void getDocumentView_multipleSections_withMaxNestingLevels() {

    var list = DocumentInstanceTestUtils.getInstanceSectionClauseVersionDtoList(1, clock, person, 2, 3, 3);
    when(documentInstanceSectionClauseVersionDtoRepository.findAllByDiId(any())).thenReturn(list);

    var docInstance = new DocumentInstance();
    docInstance.setPwaApplication(applicationDetail.getPwaApplication());
    docInstance.setDocumentTemplate(documentTemplate);
    when(documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(any(), any())).thenReturn(Optional.of(docInstance));

    var docView = documentInstanceService.getDocumentView(applicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    list.forEach(version -> {

      // check that each clause returned from the query is present in the docview in the expected place (right level etc)
      var docViewClause = findClause(docView, version);
      assertThat(docViewClause).isNotNull();

      // check that the doc view version of the clause has all of the right data and that data is equal to the query clause
      var nullFieldsList = new ArrayList<String>();
      if (version.getParentDiscId() == null) {
        nullFieldsList.add("parentClauseId");
      }
      ObjectTestUtils.assertAllExpectedFieldsHaveValue(docViewClause, nullFieldsList);

      assertThat(docViewClause.getId()).isEqualTo(version.getDiscvId());
      assertThat(docViewClause.getClauseId()).isEqualTo(version.getDiscId());
      assertThat(docViewClause.getLevelNumber()).isEqualTo(version.getLevelNumber());
      assertThat(docViewClause.getLevelOrder()).isEqualTo(version.getLevelOrder());
      assertThat(docViewClause.getName()).isEqualTo(version.getName());
      assertThat(docViewClause.getText()).isEqualTo(version.getText());
      assertThat(docViewClause.getParentClauseId()).isEqualTo(version.getParentDiscId());

      // check that there is a sidebar link for the clause (as long as it is not level 3)
      var sidebarLinkOptional = findSidebarLink(docView, version);

      if (docViewClause.getLevelNumber() == 3) {
        assertThat(sidebarLinkOptional.isEmpty());
      } else {
        assertThat(sidebarLinkOptional).isPresent();

        var sidebarLink = sidebarLinkOptional.get();

        assertThat(sidebarLink).isNotNull();

        assertThat(sidebarLink.getDisplayText()).isEqualTo(docViewClause.getName());
        assertThat(sidebarLink.getIsAnchorLink()).isTrue();
        assertThat(sidebarLink.getLink()).isEqualTo("#clauseId-" + docViewClause.getClauseId());

      }

    });

    assertThat(docView.getSections().size()).isEqualTo(2);

  }

  private SectionClauseVersionView findClause(DocumentView documentView, DocumentInstanceSectionClauseVersionDto versionDto) {

    return documentView.getSections().stream()
        .filter(section -> section.getName().equals(versionDto.getSectionName()))
        .map(section -> {

          switch (versionDto.getLevelNumber()) {
            case 1:
              return section.getClauses().stream()
                  .filter(clause -> Objects.equals(clause.getId(), versionDto.getDiscvId()))
                  .findFirst()
                  .orElseThrow();
            case 2:
              return section.getClauses().stream()
                  .flatMap(clause -> clause.getChildClauses().stream())
                  .filter(childClause -> Objects.equals(childClause.getId(), versionDto.getDiscvId()))
                  .findFirst()
                  .orElseThrow();
            case 3:
              return section.getClauses().stream()
                  .flatMap(clause -> clause.getChildClauses().stream())
                  .flatMap(childClause -> childClause.getChildClauses().stream())
                  .filter(childChildClause -> Objects.equals(childChildClause.getId(), versionDto.getDiscvId()))
                  .findFirst()
                  .orElseThrow();
          }

          return null;

        })
        .findFirst()
        .orElseThrow();

  }

  private Optional<SidebarSectionLink> findSidebarLink(DocumentView documentView, DocumentInstanceSectionClauseVersionDto versionDto) {

    return documentView.getSections().stream()
        .filter(section -> section.getName().equals(versionDto.getSectionName()))
        .flatMap(section -> section.getSidebarSectionLinks().stream())
        .filter(link -> link.getLink().contains(versionDto.getDiscvId().toString()))
        .findFirst();

  }

}
