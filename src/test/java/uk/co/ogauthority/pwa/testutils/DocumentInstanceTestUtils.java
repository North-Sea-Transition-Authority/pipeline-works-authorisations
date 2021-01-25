package uk.co.ogauthority.pwa.testutils;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

public class DocumentInstanceTestUtils {

  private DocumentInstanceTestUtils() {
    throw new AssertionError();
  }

  public static List<DocumentInstanceSectionClauseVersionDto> getInstanceSectionClauseVersionDtoList(Integer diId,
                                                                                                     Clock clock,
                                                                                                     Person creatingPerson,
                                                                                                     int sectionCount,
                                                                                                     int rootClauseCount,
                                                                                                     int childClauseCount) {

    Random random = new Random();
    List<DocumentInstanceSectionClauseVersionDto> clauses = new ArrayList<>();

    var sectionList = Arrays.stream(DocumentSection.values())
        .collect(Collectors.toList());

    IntStream.rangeClosed(1, sectionCount).forEach(sectionIdx -> {

      String sectionName = sectionList
          .get(Math.min(sectionIdx, sectionList.size() - 1))
          .name();

      IntStream.rangeClosed(1, rootClauseCount).forEach(rootClauseIdx -> {

        var clauseVersions = new ArrayList<DocumentInstanceSectionClauseVersionDto>();

        var parentClause = new DocumentInstanceSectionClauseVersionDto();
        parentClause.setSectionName(sectionName);
        parentClause.setDiscvId(random.nextInt());
        parentClause.setDiscId(parentClause.getDiscvId()); // use same id for main record as version record
        parentClause.setDiId(diId);

        parentClause.setLevelNumber(1);
        parentClause.setLevelOrder(rootClauseIdx);
        parentClause.setName("Clause " + rootClauseIdx);
        parentClause.setText(String.format("[%s] [%s] [%s]", rootClauseIdx, rootClauseIdx, rootClauseIdx));
        parentClause.setStatus(SectionClauseVersionStatus.ACTIVE.name());
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
          childClause.setDiscId(childClause.getDiscvId()); // use same id for main record as version record
          childClause.setDiId(diId);

          childClause.setLevelNumber(2);
          childClause.setLevelOrder(childClauseIdx);
          String ref = rootClauseIdx + "." + childClauseIdx;
          childClause.setName("Clause " + ref);
          childClause.setText(String.format("[%s] [%s] [%s]", ref, ref, ref));
          childClause.setStatus(SectionClauseVersionStatus.ACTIVE.name());
          childClause.setTipFlag(true);
          childClause.setVersionNo(1);
          childClause.setCreatedByPersonId(creatingPerson.getId());
          childClause.setCreatedTimestamp(clock.instant());

          childClause.setParentDiscId(parentClause.getDiscId());

          clauseVersions.add(childClause);

          IntStream.rangeClosed(1, childClauseCount).forEach(subChildClauseIdx -> {

            var subChildClause = new DocumentInstanceSectionClauseVersionDto();
            subChildClause.setSectionName(sectionName);
            subChildClause.setDiscvId(random.nextInt());
            subChildClause.setDiscId(subChildClause.getDiscvId()); // use same id for main record as version record
            subChildClause.setDiId(diId);

            subChildClause.setLevelNumber(3);
            subChildClause.setLevelOrder(subChildClauseIdx);
            String subRef = ref + "." + subChildClauseIdx;
            subChildClause.setName("Clause " + subRef);
            subChildClause.setText(String.format("[%s] [%s] [%s]", subRef, subRef, subRef));
            subChildClause.setStatus(SectionClauseVersionStatus.ACTIVE.name());
            subChildClause.setTipFlag(true);
            subChildClause.setVersionNo(1);
            subChildClause.setCreatedByPersonId(creatingPerson.getId());
            subChildClause.setCreatedTimestamp(clock.instant());

            subChildClause.setParentDiscId(childClause.getDiscId());

            clauseVersions.add(subChildClause);

          });

        });

        clauses.addAll(clauseVersions);

      });

    });

    return clauses;

  }

}
