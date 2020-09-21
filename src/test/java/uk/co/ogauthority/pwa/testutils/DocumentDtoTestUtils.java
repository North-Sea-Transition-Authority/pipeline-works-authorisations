package uk.co.ogauthority.pwa.testutils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.documents.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.service.documents.DocumentDtoFactory;

public class DocumentDtoTestUtils {

  private DocumentDtoTestUtils() {
    throw new AssertionError();
  }

  public static DocumentTemplateDto create(DocumentTemplate documentTemplate,
                                           Clock clock,
                                           Person creatingPerson,
                                           int sectionCount,
                                           int rootClauseCount,
                                           int childClauseCount) {

    Random random = new Random();
    Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToClauseVersionMap = new HashMap<>();

    IntStream.rangeClosed(1, sectionCount).forEach(sectionIdx -> {

      var section = new DocumentTemplateSection();
      section.setId(sectionIdx);
      section.setName("Section " + sectionIdx);
      section.setStatus(DocumentTemplateSectionStatus.ACTIVE);
      section.setDocumentTemplate(documentTemplate);

      var clauseVersions = new ArrayList<DocumentTemplateSectionClauseVersion>();

      IntStream.rangeClosed(1, rootClauseCount).forEach(rootClauseIdx -> {

        var parentClause = new DocumentTemplateSectionClause();
        parentClause.setDocumentTemplateSection(section);
        parentClause.setId(random.nextInt());

        // create version
        var version = new DocumentTemplateSectionClauseVersion();
        version.setId(random.nextInt());
        version.setDocumentTemplateSectionClause(parentClause);
        version.setName("Clause " + rootClauseIdx);
        version.setText(String.format("[%s] [%s] [%s]", rootClauseIdx, rootClauseIdx, rootClauseIdx));
        version.setLevelOrder(rootClauseIdx);
        version.setStatus(SectionClauseVersionStatus.ACTIVE);
        version.setTipFlag(true);
        version.setVersionNo(1);
        version.setCreatedByPersonId(creatingPerson.getId());
        version.setCreatedTimestamp(clock.instant());

        clauseVersions.add(version);

        // create children
        IntStream.rangeClosed(1, childClauseCount).forEach(childClauseIdx -> {

          var childClause = new DocumentTemplateSectionClause();
          childClause.setDocumentTemplateSection(section);
          childClause.setId(random.nextInt());

          // create version
          var childVersion = new DocumentTemplateSectionClauseVersion();
          childVersion.setId(random.nextInt());
          childVersion.setDocumentTemplateSectionClause(childClause);
          String ref = rootClauseIdx + "." + childClauseIdx;
          childVersion.setName("Clause " + ref);
          childVersion.setText(String.format("[%s] [%s] [%s]", ref, ref, ref));
          childVersion.setLevelOrder(childClauseIdx);
          childVersion.setStatus(SectionClauseVersionStatus.ACTIVE);
          childVersion.setTipFlag(true);
          childVersion.setVersionNo(1);
          childVersion.setCreatedByPersonId(creatingPerson.getId());
          childVersion.setCreatedTimestamp(clock.instant());

          childVersion.setParentDocumentTemplateSectionClause(parentClause);

          clauseVersions.add(childVersion);

        });


      });

      sectionToClauseVersionMap.put(section, clauseVersions);

    });

    return new DocumentDtoFactory().create(sectionToClauseVersionMap);

  }

  public static Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> createArgMap(DocumentTemplate template,
                                                                                                      Clock clock) {

    var section1 = new DocumentTemplateSection();
    section1.setId(1);
    section1.setDocumentTemplate(template);
    section1.setName("Section 1");
    section1.setStartTimestamp(Instant.now());

    var section2 = new DocumentTemplateSection();
    section2.setId(2);
    section2.setDocumentTemplate(template);
    section2.setName("Section 2");
    section2.setStartTimestamp(Instant.now());

    var clauseVersion1 = createTemplateClauseVersion(1, clock);
    var clause1 = new DocumentTemplateSectionClause();
    clause1.setDocumentTemplateSection(section1);
    clauseVersion1.setDocumentTemplateSectionClause(clause1);

    var parent1 = createTemplateClauseVersion(11, clock);
    var parent1Clause = new DocumentTemplateSectionClause();
    parent1Clause.setDocumentTemplateSection(section1);
    parent1.setDocumentTemplateSectionClause(parent1Clause);

    clauseVersion1.setParentDocumentTemplateSectionClause(parent1Clause);

    var clauseVersion2 = createTemplateClauseVersion(2, clock);
    var clause2 = new DocumentTemplateSectionClause();
    clause2.setDocumentTemplateSection(section2);
    clauseVersion2.setDocumentTemplateSectionClause(clause2);

    var parent2 = createTemplateClauseVersion(22, clock);
    var parent2Clause = new DocumentTemplateSectionClause();
    parent2Clause.setDocumentTemplateSection(section2);
    parent2.setDocumentTemplateSectionClause(parent2Clause);

    clauseVersion2.setParentDocumentTemplateSectionClause(parent2Clause);

    return Map.of(
        section1, List.of(clauseVersion1),
        section2, List.of(clauseVersion2)
    );

  }

  public static DocumentTemplateSectionClauseVersion createTemplateClauseVersion(int id, Clock clock) {

    var version = new DocumentTemplateSectionClauseVersion();
    version.setId(id);

    setData(version, clock);

    return version;

  }

  private static void setData(DocumentTemplateSectionClauseVersion clauseVersion, Clock clock) {

    clauseVersion.setVersionNo(1);
    clauseVersion.setTipFlag(true);
    clauseVersion.setName("Name");
    clauseVersion.setText("Text");
    clauseVersion.setLevelOrder(1);
    clauseVersion.setCreatedTimestamp(clock.instant());
    clauseVersion.setCreatedByPersonId(new PersonId(1));
    clauseVersion.setStatus(SectionClauseVersionStatus.ACTIVE);

  }

}
