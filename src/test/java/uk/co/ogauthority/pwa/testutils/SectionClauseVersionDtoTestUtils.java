package uk.co.ogauthority.pwa.testutils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

public class SectionClauseVersionDtoTestUtils {

  private SectionClauseVersionDtoTestUtils() {
    throw new AssertionError();
  }

  public static List<DocumentInstanceSectionClauseVersionDto> getInstanceSectionClauseVersionDtoList(Integer diId,
                                                                                                     DocumentSpec documentSpec,
                                                                                                     Clock clock,
                                                                                                     Person creatingPerson,
                                                                                                     int sectionCount,
                                                                                                     int rootClauseCount,
                                                                                                     int childClauseCount) {

    Random random = new Random();
    List<DocumentInstanceSectionClauseVersionDto> clauses = new ArrayList<>();

    var sectionList = new ArrayList<>(documentSpec.getDocumentSectionDisplayOrderMap().keySet());

    IntStream.rangeClosed(1, sectionCount).forEach(sectionIdx -> {

      String sectionName = sectionList
          .get(Math.min(sectionIdx, sectionList.size() - 1))
          .name();

      IntStream.rangeClosed(1, rootClauseCount).forEach(rootClauseIdx -> {

        var clauseVersions = new ArrayList<DocumentInstanceSectionClauseVersionDto>();

        var parentClause = new DocumentInstanceSectionClauseVersionDto();
        parentClause.setSectionName(sectionName);
        parentClause.setDiscvId(random.nextInt());
        parentClause.setClauseId(parentClause.getDiscvId()); // use same id for main record as version record
        parentClause.setDiId(diId);

        parentClause.setLevelNumber(1);
        parentClause.setLevelOrder(rootClauseIdx);
        parentClause.setName("Clause " + rootClauseIdx);
        parentClause.setText(String.format("[%s] [%s] [%s]", rootClauseIdx, rootClauseIdx, rootClauseIdx));
        parentClause.setStatus(SectionClauseVersionStatus.ACTIVE);
        parentClause.setTipFlag(true);
        parentClause.setVersionNo(1);
        parentClause.setCreatedByPersonId(creatingPerson.getId());
        parentClause.setCreatedTimestamp(clock.instant());

        clauseVersions.add(parentClause);

        // create children
        IntStream.rangeClosed(1, childClauseCount).forEach(childClauseIdx -> {

          var childClause = new DocumentInstanceSectionClauseVersionDto();
          childClause.setSectionName(sectionName);
          childClause.setDiscvId(random.nextInt());
          childClause.setClauseId(childClause.getDiscvId()); // use same id for main record as version record
          childClause.setDiId(diId);

          childClause.setLevelNumber(2);
          childClause.setLevelOrder(childClauseIdx);
          String ref = rootClauseIdx + "." + childClauseIdx;
          childClause.setName("Clause " + ref);
          childClause.setText(String.format("[%s] [%s] [%s]", ref, ref, ref));
          childClause.setStatus(SectionClauseVersionStatus.ACTIVE);
          childClause.setTipFlag(true);
          childClause.setVersionNo(1);
          childClause.setCreatedByPersonId(creatingPerson.getId());
          childClause.setCreatedTimestamp(clock.instant());

          childClause.setParentClauseId(parentClause.getClauseId());

          clauseVersions.add(childClause);

          IntStream.rangeClosed(1, childClauseCount).forEach(subChildClauseIdx -> {

            var subChildClause = new DocumentInstanceSectionClauseVersionDto();
            subChildClause.setSectionName(sectionName);
            subChildClause.setDiscvId(random.nextInt());
            subChildClause.setClauseId(subChildClause.getDiscvId()); // use same id for main record as version record
            subChildClause.setDiId(diId);

            subChildClause.setLevelNumber(3);
            subChildClause.setLevelOrder(subChildClauseIdx);
            String subRef = ref + "." + subChildClauseIdx;
            subChildClause.setName("Clause " + subRef);
            subChildClause.setText(String.format("[%s] [%s] [%s]", subRef, subRef, subRef));
            subChildClause.setStatus(SectionClauseVersionStatus.ACTIVE);
            subChildClause.setTipFlag(true);
            subChildClause.setVersionNo(1);
            subChildClause.setCreatedByPersonId(creatingPerson.getId());
            subChildClause.setCreatedTimestamp(clock.instant());

            subChildClause.setParentClauseId(childClause.getClauseId());

            clauseVersions.add(subChildClause);

          });

        });

        clauses.addAll(clauseVersions);

      });

    });

    return clauses;

  }

  public static List<DocumentTemplateSectionClauseVersionDto> getTemplateSectionClauseVersionDtoList(Integer dtId,
                                                                                                     DocumentSpec documentSpec,
                                                                                                     Clock clock,
                                                                                                     Person creatingPerson,
                                                                                                     int sectionCount,
                                                                                                     int rootClauseCount,
                                                                                                     int childClauseCount) {

    Random random = new Random();
    List<DocumentTemplateSectionClauseVersionDto> clauses = new ArrayList<>();

    var sectionList = new ArrayList<>(documentSpec.getDocumentSectionDisplayOrderMap().keySet());

    IntStream.rangeClosed(1, sectionCount).forEach(sectionIdx -> {

      var section = sectionList
          .get(Math.min(sectionIdx, sectionList.size() - 1));

      IntStream.rangeClosed(1, rootClauseCount).forEach(rootClauseIdx -> {

        var clauseVersions = new ArrayList<DocumentTemplateSectionClauseVersionDto>();

        var parentClause = new DocumentTemplateSectionClauseVersionDto();
        parentClause.setSection(section);
        parentClause.setScvId(random.nextInt());
        parentClause.setClauseId(parentClause.getScvId()); // use same id for main record as version record
        parentClause.setDtId(dtId);

        parentClause.setLevelNumber(1);
        parentClause.setLevelOrder(rootClauseIdx);
        parentClause.setName("Clause " + rootClauseIdx);
        parentClause.setText(String.format("[%s] [%s] [%s]", rootClauseIdx, rootClauseIdx, rootClauseIdx));
        parentClause.setStatus(SectionClauseVersionStatus.ACTIVE);
        parentClause.setTipFlag(true);
        parentClause.setVersionNo(1);
        parentClause.setCreatedByPersonId(creatingPerson.getId());
        parentClause.setCreatedTimestamp(clock.instant());

        clauseVersions.add(parentClause);

        // create children
        IntStream.rangeClosed(1, childClauseCount).forEach(childClauseIdx -> {

          var childClause = new DocumentTemplateSectionClauseVersionDto();
          childClause.setSection(section);
          childClause.setScvId(random.nextInt());
          childClause.setClauseId(childClause.getScvId()); // use same id for main record as version record
          childClause.setDtId(dtId);

          childClause.setLevelNumber(2);
          childClause.setLevelOrder(childClauseIdx);
          String ref = rootClauseIdx + "." + childClauseIdx;
          childClause.setName("Clause " + ref);
          childClause.setText(String.format("[%s] [%s] [%s]", ref, ref, ref));
          childClause.setStatus(SectionClauseVersionStatus.ACTIVE);
          childClause.setTipFlag(true);
          childClause.setVersionNo(1);
          childClause.setCreatedByPersonId(creatingPerson.getId());
          childClause.setCreatedTimestamp(clock.instant());

          childClause.setParentClauseId(parentClause.getClauseId());

          clauseVersions.add(childClause);

          IntStream.rangeClosed(1, childClauseCount).forEach(subChildClauseIdx -> {

            var subChildClause = new DocumentTemplateSectionClauseVersionDto();
            subChildClause.setSection(section);
            subChildClause.setScvId(random.nextInt());
            subChildClause.setClauseId(subChildClause.getScvId()); // use same id for main record as version record
            subChildClause.setDtId(dtId);

            subChildClause.setLevelNumber(3);
            subChildClause.setLevelOrder(subChildClauseIdx);
            String subRef = ref + "." + subChildClauseIdx;
            subChildClause.setName("Clause " + subRef);
            subChildClause.setText(String.format("[%s] [%s] [%s]", subRef, subRef, subRef));
            subChildClause.setStatus(SectionClauseVersionStatus.ACTIVE);
            subChildClause.setTipFlag(true);
            subChildClause.setVersionNo(1);
            subChildClause.setCreatedByPersonId(creatingPerson.getId());
            subChildClause.setCreatedTimestamp(clock.instant());

            subChildClause.setParentClauseId(childClause.getClauseId());

            clauseVersions.add(subChildClause);

          });

        });

        clauses.addAll(clauseVersions);

      });

    });

    return clauses;

  }

  public static List<DocumentTemplateSectionClauseVersionDto> getDefaultTemplateSectionClauseVersionDto(DocumentSection section,
                                                                                                        int rootClauseCount) {

    Random random = new Random();
    List<DocumentTemplateSectionClauseVersionDto> clauses = new ArrayList<>();

    IntStream.rangeClosed(1, rootClauseCount).forEach(rootClauseIdx -> {

      var clauseVersions = new ArrayList<DocumentTemplateSectionClauseVersionDto>();

      var parentClause = new DocumentTemplateSectionClauseVersionDto();
      parentClause.setSection(section);
      parentClause.setScvId(random.nextInt());
      parentClause.setClauseId(parentClause.getScvId()); // use same id for main record as version record
      parentClause.setDtId(1);

      parentClause.setLevelNumber(1);
      parentClause.setLevelOrder(rootClauseIdx);
      parentClause.setName("Clause " + rootClauseIdx);
      parentClause.setText(String.format("[%s] [%s] [%s]", rootClauseIdx, rootClauseIdx, rootClauseIdx));
      parentClause.setStatus(SectionClauseVersionStatus.ACTIVE);
      parentClause.setTipFlag(true);
      parentClause.setVersionNo(1);
      parentClause.setCreatedByPersonId(PersonTestUtil.createDefaultPerson().getId());
      parentClause.setCreatedTimestamp(Instant.now());

      clauseVersions.add(parentClause);
      clauses.addAll(clauseVersions);

    });

    return clauses;

  }

}
