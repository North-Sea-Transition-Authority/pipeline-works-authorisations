package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentClauseServiceTest {

  @Mock
  private Clock clock;

  private SectionClauseCreator sectionClauseCreator;

  private final DocumentClauseFactory documentClauseFactory = new DocumentClauseFactory();

  private DocumentClauseService documentClauseService;

  private DocumentInstanceSectionClause parent;
  private DocumentTemplateSection section;
  private DocumentTemplateSectionClause templateClause;
  private DocumentInstanceSectionClause clauseRecord;
  private DocumentInstanceSectionClauseVersion clauseVersion;

  private final Person person = PersonTestUtil.createDefaultPerson();

  private Instant clockTime;

  @Before
  public void setUp() throws Exception {

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

    clockTime = Instant.now();
    when(clock.instant()).thenReturn(clockTime);
    sectionClauseCreator = new SectionClauseCreator(clock);

    documentClauseService = new DocumentClauseService(sectionClauseCreator, documentClauseFactory, clock);

  }

  @Test
  public void addClauseAfter() {

    clauseRecord.setDocumentTemplateSectionClause(templateClause);

    var newClauseVersion = documentClauseService.addClauseAfter(PwaDocumentType.INSTANCE, clauseVersion, buildClauseForm(), person);

    var newClause = newClauseVersion.getClause();
    assertThat(newClause.getSection()).isEqualTo(section);

    assertThat(newClauseVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newClauseVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newClauseVersion.getClause()).isEqualTo(newClause);
    assertThat(newClauseVersion.getParentClause()).contains(parent);
    assertThat(newClauseVersion.getVersionNo()).isEqualTo(1);
    assertThat(newClauseVersion.getTipFlag()).isTrue();
    assertThat(newClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newClauseVersion.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(newClauseVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(newClauseVersion.getLevelOrder()).isEqualTo(clauseVersion.getLevelOrder() + 1);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newClauseVersion, List.of("id", "endedByPersonId", "endedTimestamp"));

  }


  @Test
  public void addClauseBefore() {

    var notIncrementedVersion = new DocumentInstanceSectionClauseVersion();
    notIncrementedVersion.setLevelOrder(1);

    var additionalClauseVersion = new DocumentInstanceSectionClauseVersion();
    additionalClauseVersion.setLevelOrder(clauseVersion.getLevelOrder() + 1);

    clauseRecord.setDocumentTemplateSectionClause(templateClause);

    var updatedVersions = documentClauseService
        .addClauseBefore(PwaDocumentType.INSTANCE, clauseVersion, buildClauseForm(), person, () -> Stream.of(clauseVersion, additionalClauseVersion, notIncrementedVersion));

    var newClauseVersion = updatedVersions.stream()
        .filter(v -> v.getLevelOrder() == clauseVersion.getLevelOrder() - 1)
        .findFirst()
        .orElseThrow();

    var newClause = newClauseVersion.getClause();

    assertThat(newClauseVersion.getClause().getSection()).isEqualTo(section);

    assertThat(newClauseVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newClauseVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newClauseVersion.getClause()).isEqualTo(newClause);
    assertThat(newClauseVersion.getParentClause()).contains(parent);
    assertThat(newClauseVersion.getVersionNo()).isEqualTo(1);
    assertThat(newClauseVersion.getTipFlag()).isTrue();
    assertThat(newClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newClauseVersion.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(newClauseVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    // new clause is in position of clause we were adding before
    assertThat(newClauseVersion.getLevelOrder()).isEqualTo(3);

    // clauses after our clause were incremented
    updatedVersions.stream()
        .filter(v -> !Objects.equals(v, newClauseVersion))
        .forEach(v -> {

          assertThat(v).isNotEqualTo(notIncrementedVersion);

          if (Objects.equals(v, clauseVersion)) {
            assertThat(v.getLevelOrder()).isEqualTo(4);
          } else {
            assertThat(v.getLevelOrder()).isEqualTo(5);
          }

    });

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newClauseVersion, List.of("id", "endedByPersonId", "endedTimestamp"));

  }

  @Test
  public void addSubClauseFor() {

    clauseRecord.setDocumentTemplateSectionClause(templateClause);

    var newClauseVersion = documentClauseService.addSubClause(PwaDocumentType.INSTANCE, clauseVersion, buildClauseForm(), person);

    var newClause = newClauseVersion.getClause();
    assertThat(newClause.getSection()).isEqualTo(section);

    assertThat(newClauseVersion.getName()).isEqualTo(buildClauseForm().getName());
    assertThat(newClauseVersion.getText()).isEqualTo(buildClauseForm().getText());
    assertThat(newClauseVersion.getClause()).isEqualTo(newClause);
    assertThat(newClauseVersion.getLevelOrder()).isEqualTo(1);
    assertThat(newClauseVersion.getVersionNo()).isEqualTo(1);
    assertThat(newClauseVersion.getTipFlag()).isTrue();
    assertThat(newClauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.ACTIVE);
    assertThat(newClauseVersion.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(newClauseVersion.getCreatedTimestamp()).isEqualTo(clock.instant());

    assertThat(newClauseVersion.getParentClause()).contains(clauseRecord);

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

    var updatedVersions = documentClauseService.editClause(
        PwaDocumentType.INSTANCE,
        originalClauseVersion,
        buildClauseForm(),
        new Person(222, null, null, null, null)
    );

    var endedVersion = updatedVersions.stream()
        .filter(v -> v.getId() == 1)
        .findFirst()
        .orElseThrow();

    // initial version updated properly
    assertThat(endedVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.DELETED);
    assertThat(endedVersion.getEndedByPersonId()).isEqualTo(new PersonId(222));
    assertThat(endedVersion.getEndedTimestamp()).isEqualTo(clock.instant());
    assertThat(endedVersion.getTipFlag()).isFalse();

    var newVersion = updatedVersions.stream()
        .filter(v -> v.getId() == null)
        .findFirst()
        .orElseThrow();

    // new version set properly
    assertThat(newVersion.getClause()).isEqualTo(clause);
    assertThat(newVersion.getParentClause()).contains(parent);
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

    var child2DocumentInstanceSectionClause = new DocumentInstanceSectionClause();
    var child2DocumentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    child2DocumentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(child2DocumentInstanceSectionClause);
    child2DocumentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().setDocumentInstance(childDocumentInstance);

    //parent clause
    var documentInstance = new DocumentInstance();
    var documentInstanceSectionClause = new DocumentInstanceSectionClause();
    var documentInstanceSectionClauseVersion = new DocumentInstanceSectionClauseVersion();
    documentInstanceSectionClauseVersion.setDocumentInstanceSectionClause(documentInstanceSectionClause);
    documentInstanceSectionClauseVersion.getDocumentInstanceSectionClause().setDocumentInstance(documentInstance);

    var person = new Person(1, "name", null, null, null);

    documentClauseService.removeClause(
        documentInstanceSectionClauseVersion,
        person,
        clause -> List.of(childDocumentInstanceSectionClauseVersion, child2DocumentInstanceSectionClauseVersion),
        clauses -> List.of(subChildDocumentInstanceSectionClauseVersion)
    );

    assertThat(List.of(
        documentInstanceSectionClauseVersion,
        childDocumentInstanceSectionClauseVersion,
        child2DocumentInstanceSectionClauseVersion,
        subChildDocumentInstanceSectionClauseVersion)).allSatisfy(clauseVersion -> {

      assertThat(clauseVersion.getStatus()).isEqualTo(SectionClauseVersionStatus.DELETED);
      assertThat(clauseVersion.getEndedByPersonId()).isEqualTo(person.getId());
      assertThat(clauseVersion.getEndedTimestamp()).isEqualTo(clock.instant());

    });

  }

  private ClauseForm buildClauseForm() {
    var form = new ClauseForm();
    form.setName("name");
    form.setText("text");
    return form;
  }

}