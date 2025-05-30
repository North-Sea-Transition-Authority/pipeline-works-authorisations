package uk.co.ogauthority.pwa.service.documents;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;

@Service
public class DocumentClauseService {

  private final SectionClauseCreator sectionClauseCreator;
  private final DocumentClauseFactory documentClauseFactory;
  private final Clock clock;

  @Autowired
  public DocumentClauseService(SectionClauseCreator sectionClauseCreator,
                               DocumentClauseFactory documentClauseFactory,
                               @Qualifier("utcClock") Clock clock) {
    this.sectionClauseCreator = sectionClauseCreator;
    this.documentClauseFactory = documentClauseFactory;
    this.clock = clock;
  }

  /**
   * Create a new clause for a document type.
   * @param documentType we are adding a clause for
   * @param section clause is being added to
   * @param newParent clause that should be set as the parent on the new clause
   * @param form containing clause info to set
   * @param creatingPerson person creating clause
   * @param levelOrder to set for the new clause
   * @return the newly created version
   */
  private SectionClauseVersion addClause(PwaDocumentType documentType,
                                         DocumentTemplateSection section,
                                         @Nullable SectionClause newParent,
                                         Integer levelOrder,
                                         ClauseForm form,
                                         Person creatingPerson) {

    var newClause = documentClauseFactory.createSectionClause(documentType);
    var newClauseVersion = documentClauseFactory.createSectionClauseVersion(documentType);

    newClause.setSection(section);

    newClauseVersion.setParentClause(newParent);

    newClauseVersion.setClause(newClause);

    sectionClauseCreator.setCommonData(
        newClauseVersion,
        form.getName(),
        form.getText(),
        levelOrder,
        SectionClauseVersionStatus.ACTIVE,
        1,
        creatingPerson);

    return newClauseVersion;

  }

  /**
   * Add a new clause on the same level as but one position after the passed-in clause.
   */
  @Transactional
  public SectionClauseVersion addClauseAfter(PwaDocumentType documentType,
                                             SectionClauseVersion versionToAddAfter,
                                             ClauseForm form,
                                             Person creatingPerson) {

    return addClause(
        documentType,
        versionToAddAfter.getClause().getSection(),
        versionToAddAfter.getParentClause().orElse(null),
        versionToAddAfter.getLevelOrder() + 1,
        form,
        creatingPerson);

  }

  /**
   * Add a new clause on the same level as but one position before the passed-in clause.
   * The passed-in clause and any clauses following that (on the same level) are re-ordered.
   */
  @Transactional
  public List<SectionClauseVersion> addClauseBefore(PwaDocumentType documentType,
                                                    SectionClauseVersion versionToAddBefore,
                                                    ClauseForm form,
                                                    Person creatingPerson,
                                                    Supplier<Stream<SectionClauseVersion>> clauseVersionsWithinLevelSupplier) {

    var parentClause = versionToAddBefore.getParentClause().orElse(null);
    var section = versionToAddBefore.getClause().getSection();

    // add a new clause one position higher than the one we are adding before
    var newClauseVersion = addClause(
        documentType,
        section,
        parentClause,
        versionToAddBefore.getLevelOrder() - 1,
        form,
        creatingPerson);

    // increment the position of everything ahead of our new clause within its level, which will leave a gap
    var clauseVersionsToUpdate = clauseVersionsWithinLevelSupplier.get()
        .filter(clauseVersion -> clauseVersion.getLevelOrder() > newClauseVersion.getLevelOrder())
        .collect(Collectors.toList());

    clauseVersionsToUpdate.forEach(clauseVersion -> clauseVersion.setLevelOrder(clauseVersion.getLevelOrder() + 1));

    // fill the gap left by the reordering by bumping the order on our new clause
    newClauseVersion.setLevelOrder(newClauseVersion.getLevelOrder() + 1);
    clauseVersionsToUpdate.add(newClauseVersion);

    return clauseVersionsToUpdate;

  }

  /**
   * Add a child clause for the passed-in clause.
   */
  @Transactional
  public SectionClauseVersion addSubClause(PwaDocumentType documentType,
                                           SectionClauseVersion versionToAddSubFor,
                                           ClauseForm form,
                                           Person creatingPerson) {

    return addClause(
        documentType,
        versionToAddSubFor.getClause().getSection(),
        versionToAddSubFor.getClause(),
        1,
        form,
        creatingPerson);

  }

  @Transactional
  public List<SectionClauseVersion> editClause(PwaDocumentType documentType,
                                               SectionClauseVersion clauseBeingEdited,
                                               ClauseForm form,
                                               Person editingPerson) {

    var newClauseVersion = documentClauseFactory.createSectionClauseVersion(documentType);

    var parentClause = clauseBeingEdited.getParentClause()
        .orElse(null);

    newClauseVersion.setClause(clauseBeingEdited.getClause());
    newClauseVersion.setParentClause(parentClause);

    sectionClauseCreator.setCommonData(
        newClauseVersion,
        form.getName(),
        form.getText(),
        clauseBeingEdited.getLevelOrder(),
        SectionClauseVersionStatus.ACTIVE,
        clauseBeingEdited.getVersionNo() + 1,
        editingPerson);

    clauseBeingEdited.setTipFlag(false);
    clauseBeingEdited.setEndedTimestamp(clock.instant());
    clauseBeingEdited.setEndedByPersonId(editingPerson.getId());
    clauseBeingEdited.setStatus(SectionClauseVersionStatus.DELETED);

    return List.of(clauseBeingEdited, newClauseVersion);

  }

  /**
   * Mark a clause and its child clauses as deleted.
   * @param parentClauseVersion clause being deleted
   * @param removingPerson person doing the deletion
   * @param childClauseFunction a function to return the clauses that are direct descendants of the clause being deleted
   * @param subChildClauseFunction a function to return the clauses that are children of the direct descendants of the clause being deleted
   * @return a list of the clause versions that have been marked as deleted
   */
  @Transactional
  public List<SectionClauseVersion> removeClause(SectionClauseVersion parentClauseVersion,
                                                 Person removingPerson,
                                                 Function<SectionClause, List<SectionClauseVersion>> childClauseFunction,
                                                 Function<Collection<SectionClause>, List<SectionClauseVersion>> subChildClauseFunction) {

    var deletedClauseVersions = new ArrayList<SectionClauseVersion>();

    deletedClauseVersions.add(parentClauseVersion);

    var childClauseVersions = childClauseFunction.apply(parentClauseVersion.getClause());
    deletedClauseVersions.addAll(childClauseVersions);

    var childClauses = childClauseVersions.stream()
        .map(SectionClauseVersion::getClause)
        .collect(Collectors.toList());

    var subChildClauseVersions = subChildClauseFunction.apply(childClauses);
    deletedClauseVersions.addAll(subChildClauseVersions);

    var endTime = clock.instant();
    deletedClauseVersions.forEach(version -> setClauseAsDeleted(version, removingPerson, endTime));

    return deletedClauseVersions;

  }

  private void setClauseAsDeleted(SectionClauseVersion sectionClauseVersion,
                                  Person removingPerson,
                                  Instant endTime) {
    sectionClauseVersion.setStatus(SectionClauseVersionStatus.DELETED);
    sectionClauseVersion.setEndedByPersonId(removingPerson.getId());
    sectionClauseVersion.setEndedTimestamp(endTime);
  }

}